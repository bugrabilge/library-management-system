package com.getir.librarymanagementsystem.service;

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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@SpringBootTest
class BorrowServiceTest {

    @MockBean
    private BorrowRepository borrowRepository;

    @MockBean
    private BookRepository bookRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private BorrowMapper borrowMapper;

    @Autowired
    private BorrowService borrowService;

    @Test
    void borrowBook_ShouldSuccessfullyBorrowWhenBookAvailable() {
        // Arrange
        String username = "testUser";
        Long bookId = 1L;
        LocalDate borrowDate = LocalDate.now();
        LocalDate returnDate = borrowDate.plusDays(14);

        BorrowRequest request = new BorrowRequest();
        request.setBookId(bookId);
        request.setBorrowDate(borrowDate);
        request.setReturnDate(returnDate);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(username);

        User user = new User();
        user.setUsername(username);

        Book book = new Book();
        book.setId(bookId);
        book.setTitle("Test Book");
        book.setAvailable(true);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));

        Borrow savedBorrow = new Borrow();
        savedBorrow.setId(1L);
        savedBorrow.setUser(user);
        savedBorrow.setBook(book);
        savedBorrow.setBorrowDate(borrowDate);
        savedBorrow.setReturnDate(returnDate);

        when(borrowRepository.save(any(Borrow.class))).thenReturn(savedBorrow);

        // Act
        BorrowResponse response = borrowService.borrowBook(request, authentication);

        // Assert
        assertNotNull(response);
        assertEquals(username, response.getUsername());
        assertEquals("Test Book", response.getBookTitle());
        assertEquals(borrowDate, response.getBorrowDate());
        assertEquals(returnDate, response.getReturnDate());

        verify(bookRepository).save(book);
        assertFalse(book.isAvailable());
        verify(borrowRepository).save(any(Borrow.class));
    }

    @Test
    void returnBook_ShouldSuccessfullyReturnBook() {
        // Arrange
        Long borrowId = 1L;
        String username = "testUser";
        Long userId = 1L;

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(username);

        User user = new User();
        user.setId(userId);
        user.setUsername(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        Book book = new Book();
        book.setAvailable(false);

        Borrow borrow = new Borrow();
        borrow.setId(borrowId);
        borrow.setUser(user);
        borrow.setBook(book);
        borrow.setReturned(false);

        when(borrowRepository.findById(borrowId)).thenReturn(Optional.of(borrow));
        when(borrowRepository.save(borrow)).thenReturn(borrow);
        when(bookRepository.save(book)).thenReturn(book);

        BorrowResponse expectedResponse = new BorrowResponse();
        when(borrowMapper.toResponse(borrow)).thenReturn(expectedResponse);

        // Act
        BorrowResponse response = borrowService.returnBook(borrowId, authentication);

        // Assert
        assertNotNull(response);
        assertEquals(expectedResponse, response);
        assertTrue(borrow.isReturned());
        assertTrue(book.isAvailable());
        assertEquals(LocalDate.now(), borrow.getReturnDate());
        verify(borrowRepository).save(borrow);
        verify(bookRepository).save(book);
    }

    @Test
    void getBorrowHistoryForUser_ShouldReturnUserBorrows() {
        // Arrange
        String username = "testUser";
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(username);

        User user = new User();
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        Borrow borrow1 = new Borrow();
        Borrow borrow2 = new Borrow();
        when(borrowRepository.findByUser(user)).thenReturn(List.of(borrow1, borrow2));

        BorrowResponse response1 = new BorrowResponse();
        BorrowResponse response2 = new BorrowResponse();
        when(borrowMapper.toResponse(borrow1)).thenReturn(response1);
        when(borrowMapper.toResponse(borrow2)).thenReturn(response2);

        // Act
        List<BorrowResponse> result = borrowService.getBorrowHistoryForUser(authentication);

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.contains(response1));
        assertTrue(result.contains(response2));
        verify(borrowRepository).findByUser(user);
    }

    @Test
    void getOverdueBorrows_ShouldReturnOverdueBorrows() {
        // Arrange
        Borrow overdueBorrow = new Borrow();
        when(borrowRepository.findByReturnDateIsNullAndBorrowDateBefore(any(LocalDate.class)))
                .thenReturn(List.of(overdueBorrow));

        BorrowResponse expectedResponse = new BorrowResponse();
        when(borrowMapper.toResponse(overdueBorrow)).thenReturn(expectedResponse);

        // Act
        List<BorrowResponse> result = borrowService.getOverdueBorrows();

        // Assert
        assertEquals(1, result.size());
        assertEquals(expectedResponse, result.get(0));
        verify(borrowRepository).findByReturnDateIsNullAndBorrowDateBefore(any(LocalDate.class));
    }

    @Test
    void getOverdueReport_ShouldReturnOverdueReport() {
        // Arrange
        Borrow overdueBorrow = new Borrow();
        when(borrowRepository.findByReturnDateIsNullAndBorrowDateBefore(any(LocalDate.class)))
                .thenReturn(List.of(overdueBorrow));

        OverdueReportResponse expectedResponse = new OverdueReportResponse();
        when(borrowMapper.toOverdueResponse(overdueBorrow)).thenReturn(expectedResponse);

        // Act
        List<OverdueReportResponse> result = borrowService.getOverdueReport();

        // Assert
        assertEquals(1, result.size());
        assertEquals(expectedResponse, result.get(0));
        verify(borrowRepository).findByReturnDateIsNullAndBorrowDateBefore(any(LocalDate.class));
    }

    @Test
    void delete_ShouldDeleteBorrowAndMakeBookAvailable() {
        // Arrange
        Long borrowId = 1L;
        Book book = new Book();
        book.setAvailable(false);

        Borrow borrow = new Borrow();
        borrow.setBook(book);
        when(borrowRepository.findById(borrowId)).thenReturn(Optional.of(borrow));
        when(bookRepository.save(book)).thenReturn(book);

        // Act
        borrowService.delete(borrowId);

        // Assert
        assertTrue(book.isAvailable());
        verify(bookRepository).save(book);
        verify(borrowRepository).deleteById(borrowId);
    }
}