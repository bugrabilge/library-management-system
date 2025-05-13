package com.getir.librarymanagementsystem.service;

import com.getir.librarymanagementsystem.model.entity.Book;
import com.getir.librarymanagementsystem.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BookService {

    private final BookRepository bookRepository;

    public Book save(Book book) {
        log.debug("Saving book: {}", book);
        return bookRepository.save(book);
    }

    public Book update(Long id, Book book) {
        Book existing = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        if (!existing.getIsbn().equals(book.getIsbn())) {
            Optional<Book> isbnOwner = bookRepository.findByIsbn(book.getIsbn());
            if (isbnOwner.isPresent() && !isbnOwner.get().getId().equals(id)) {
                throw new IllegalArgumentException("ISBN already exists for another book.");
            }
        }

        existing.setTitle(book.getTitle());
        existing.setAuthor(book.getAuthor());
        existing.setIsbn(book.getIsbn());
        existing.setPublicationDate(book.getPublicationDate());
        existing.setGenre(book.getGenre());
        existing.setAvailable(book.isAvailable());

        return bookRepository.save(existing);
    }


    public void delete(Long id) {
        log.debug("Deleting book with ID: {}", id);
        bookRepository.deleteById(id);
    }

    public Optional<Book> findById(Long id) {
        log.debug("Searching for book by ID: {}", id);
        return bookRepository.findById(id);
    }

    public List<Book> findAll() {
        log.debug("Fetching all books");
        return bookRepository.findAll();
    }

    public List<Book> search(String keyword) {
        log.debug("Searching books with keyword: {}", keyword);
        return bookRepository.findAll().stream()
                .filter(b -> b.getTitle().toLowerCase().contains(keyword.toLowerCase())
                        || b.getAuthor().toLowerCase().contains(keyword.toLowerCase()))
                .toList();
    }
}
