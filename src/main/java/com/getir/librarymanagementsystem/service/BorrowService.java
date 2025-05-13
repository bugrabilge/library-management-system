package com.getir.librarymanagementsystem.service;

import com.getir.librarymanagementsystem.model.dto.event.BookAvailabilityEvent;
import com.getir.librarymanagementsystem.model.dto.request.BorrowRequest;
import com.getir.librarymanagementsystem.model.dto.response.BorrowResponse;
import com.getir.librarymanagementsystem.model.dto.response.OverdueReportResponse;
import com.getir.librarymanagementsystem.model.entity.Book;
import com.getir.librarymanagementsystem.model.entity.Borrow;
import com.getir.librarymanagementsystem.model.entity.User;
import com.getir.librarymanagementsystem.model.mapper.BorrowMapper;
import com.getir.librarymanagementsystem.repository.BookRepository;
import com.getir.librarymanagementsystem.repository.BorrowRepository;
import com.getir.librarymanagementsystem.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BorrowService {

    private final BorrowRepository borrowRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final BorrowMapper borrowMapper;
    private static final int BORROW_PERIOD_DAYS = 14;
    private final BookAvailabilityPublisher bookAvailabilityPublisher;

    public BorrowResponse borrowBook(BorrowRequest request, Authentication authentication) {
        String username = authentication.getName();
        log.info("User '{}' attempting to borrow book with ID {}", username, request.getBookId());

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.error("User '{}' not found while borrowing", username);
                    return new RuntimeException("User not found");
                });

        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> {
                    log.error("Book with ID {} not found", request.getBookId());
                    return new RuntimeException("Book not found");
                });

        if (!book.isAvailable()) {
            log.warn("Book with ID {} is not available for borrowing", book.getId());
            throw new RuntimeException("Book is not available");
        }

        book.setAvailable(false);
        bookRepository.save(book);

        bookAvailabilityPublisher.publish(
                new BookAvailabilityEvent(book.getId(), false)
        );

        Borrow borrow = Borrow.builder()
                .user(user)
                .book(book)
                .borrowDate(request.getBorrowDate())
                .returnDate(request.getReturnDate())
                .build();

        Borrow saved = borrowRepository.save(borrow);
        log.info("User '{}' successfully borrowed book '{}'", username, book.getTitle());

        return BorrowResponse.builder()
                .id(saved.getId())
                .username(user.getUsername())
                .bookTitle(book.getTitle())
                .borrowDate(saved.getBorrowDate())
                .returnDate(saved.getReturnDate())
                .build();
    }

    public List<BorrowResponse> getAllBorrows() {
        log.debug("Fetching all borrow records");
        return borrowRepository.findAll().stream().map(borrow ->
                BorrowResponse.builder()
                        .id(borrow.getId())
                        .username(borrow.getUser().getUsername())
                        .bookTitle(borrow.getBook().getTitle())
                        .borrowDate(borrow.getBorrowDate())
                        .returnDate(borrow.getReturnDate())
                        .build()
        ).collect(Collectors.toList());
    }

    public BorrowResponse returnBook(Long borrowId, Authentication authentication) {
        String username = authentication.getName();
        log.info("User '{}' attempting to return borrow ID {}", username, borrowId);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.error("User '{}' not found while returning", username);
                    return new UsernameNotFoundException("User not found");
                });

        Borrow borrow = borrowRepository.findById(borrowId)
                .orElseThrow(() -> {
                    log.error("Borrow record with ID {} not found", borrowId);
                    return new EntityNotFoundException("Borrow not found");
                });

        if (borrow.isReturned()) {
            log.warn("Borrow record {} is already returned", borrowId);
            throw new AccessDeniedException("Book is already returned");
        }

        if (!borrow.getUser().getId().equals(user.getId())) {
            log.warn("User '{}' is not authorized to return borrow ID {}", username, borrowId);
            throw new AccessDeniedException("You are not authorized to return this book.");
        }

        borrow.setReturnDate(LocalDate.now());
        borrow.setReturned(true);
        Book book = borrow.getBook();
        book.setAvailable(true);
        bookRepository.save(book);

        bookAvailabilityPublisher.publish(
                new BookAvailabilityEvent(book.getId(), true)
        );

        Borrow updated = borrowRepository.save(borrow);

        log.info("User '{}' successfully returned book '{}'", username, book.getTitle());

        return borrowMapper.toResponse(updated);
    }

    public List<BorrowResponse> getBorrowHistoryForUser(Authentication authentication) {
        String username = authentication.getName();
        log.debug("Fetching borrow history for user '{}'", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.error("User '{}' not found for history fetch", username);
                    return new UsernameNotFoundException("User not found");
                });

        List<Borrow> borrows = borrowRepository.findByUser(user);
        return borrows.stream().map(borrowMapper::toResponse).toList();
    }

    public List<BorrowResponse> getOverdueBorrows() {
        LocalDate threshold = LocalDate.now().minusDays(BORROW_PERIOD_DAYS);
        log.info("Fetching overdue borrows before {}", threshold);

        List<Borrow> overdues = borrowRepository.findByReturnDateIsNullAndBorrowDateBefore(threshold);
        return overdues.stream().map(borrowMapper::toResponse).toList();
    }

    public List<OverdueReportResponse> getOverdueReport() {
        LocalDate dueDateThreshold = LocalDate.now().minusDays(BORROW_PERIOD_DAYS);
        log.info("Generating overdue report for borrows before {}", dueDateThreshold);

        List<Borrow> overdueBorrows = borrowRepository.findByReturnDateIsNullAndBorrowDateBefore(dueDateThreshold);
        return overdueBorrows.stream()
                .map(borrowMapper::toOverdueResponse)
                .collect(Collectors.toList());
    }

    public void delete(Long id) {
        log.warn("Deleting borrow record with ID {}", id);
        Borrow borrow = borrowRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Borrow record {} not found for deletion", id);
                    return new RuntimeException("Borrow not found");
                });

        borrow.getBook().setAvailable(true);
        bookRepository.save(borrow.getBook());
        borrowRepository.deleteById(id);

        log.info("Borrow record {} deleted successfully", id);
    }
}
