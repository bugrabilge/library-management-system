package com.getir.librarymanagementsystem.controller;

import com.getir.librarymanagementsystem.model.entity.Book;
import com.getir.librarymanagementsystem.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/books")
@SecurityRequirement(name = "Bearer Authentication")
@RequiredArgsConstructor
@Tag(name = "Book Management", description = "Endpoints for managing books in the library")
public class BookController {

    private final BookService bookService;

    @PostMapping
    @Operation(summary = "Add a new book", description = "Create and save a new book to the library")
    public ResponseEntity<Book> save(@RequestBody Book book) {
        log.info("Saving new book: {}", book.getTitle());
        return ResponseEntity.ok(bookService.save(book));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update book", description = "Update an existing book's information by ID")
    public ResponseEntity<Book> update(@PathVariable Long id, @RequestBody Book book) {
        log.info("Updating book with ID {}: {}", id, book.getTitle());
        return ResponseEntity.ok(bookService.update(id, book));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete book", description = "Delete a book by its ID")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("Deleting book with ID: {}", id);
        bookService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get book by ID", description = "Fetch a single book's details using its ID")
    public ResponseEntity<Book> findById(@PathVariable Long id) {
        log.info("Fetching book by ID: {}", id);
        return bookService.findById(id)
                .map(book -> {
                    log.info("Book found: {}", book.getTitle());
                    return ResponseEntity.ok(book);
                })
                .orElseGet(() -> {
                    log.warn("Book not found with ID: {}", id);
                    return ResponseEntity.notFound().build();
                });
    }

    @GetMapping
    @Operation(summary = "Get all books", description = "Retrieve a list of all books in the library")
    public ResponseEntity<List<Book>> findAll() {
        log.info("Fetching all books");
        return ResponseEntity.ok(bookService.findAll());
    }

    @GetMapping("/search")
    @Operation(summary = "Search books", description = "Search books by keyword in title or author")
    public ResponseEntity<List<Book>> search(@RequestParam String keyword) {
        log.info("Searching books with keyword: {}", keyword);
        return ResponseEntity.ok(bookService.search(keyword));
    }
}
