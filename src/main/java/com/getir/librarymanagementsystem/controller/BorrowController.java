package com.getir.librarymanagementsystem.controller;

import com.getir.librarymanagementsystem.model.dto.request.BorrowRequest;
import com.getir.librarymanagementsystem.model.dto.response.BorrowResponse;
import com.getir.librarymanagementsystem.model.dto.response.OverdueReportResponse;
import com.getir.librarymanagementsystem.service.BorrowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/borrows")
@SecurityRequirement(name = "Bearer Authentication")
@RequiredArgsConstructor
@Tag(name = "Borrow Management", description = "Endpoints for borrowing and returning books")
public class BorrowController {

    private final BorrowService borrowService;

    @PostMapping
    @Operation(summary = "Borrow a book", description = "Allows a user to borrow a book if it is available")
    public ResponseEntity<BorrowResponse> borrow(@RequestBody BorrowRequest request, Authentication authentication) {
        log.info("User '{}' is attempting to borrow a book: {}", authentication.getName(), request);
        BorrowResponse response = borrowService.borrowBook(request, authentication);
        log.info("Book borrowed successfully by user '{}'", authentication.getName());
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get all borrow records", description = "Returns a list of all borrow records")
    public ResponseEntity<List<BorrowResponse>> getAll() {
        log.debug("Fetching all borrow records");
        return ResponseEntity.ok(borrowService.getAllBorrows());
    }

    @PutMapping("/return/{id}")
    @PreAuthorize("hasRole('PATRON')")
    @Operation(summary = "Return a book", description = "Allows a patron to return a borrowed book")
    public ResponseEntity<BorrowResponse> returnBook(@PathVariable Long id, Authentication authentication) {
        log.info("User '{}' is returning book with borrow ID: {}", authentication.getName(), id);
        return ResponseEntity.ok(borrowService.returnBook(id, authentication));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete borrow record", description = "Deletes a borrow record and marks the book as available again")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.warn("Deleting borrow record with ID: {}", id);
        borrowService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/history")
    @PreAuthorize("hasRole('LIBRARIAN')")
    @Operation(summary = "Get borrow history", description = "Returns the complete borrow history (LIBRARIAN access only)")
    public ResponseEntity<List<BorrowResponse>> getAllBorrowHistories() {
        log.debug("Fetching borrow history for all users (LIBRARIAN access)");
        return ResponseEntity.ok(borrowService.getAllBorrows());
    }

    @GetMapping("/overdue")
    @PreAuthorize("hasRole('LIBRARIAN')")
    @Operation(summary = "Get overdue borrows", description = "Returns a list of all overdue borrow records")
    public ResponseEntity<List<BorrowResponse>> getOverdueBorrows() {
        log.debug("Fetching overdue borrow records");
        return ResponseEntity.ok(borrowService.getOverdueBorrows());
    }

    @GetMapping("/overdue-report")
    @PreAuthorize("hasRole('LIBRARIAN')")
    @Operation(summary = "Get overdue report", description = "Generates a report of overdue borrowed books")
    public ResponseEntity<List<OverdueReportResponse>> getOverdueBooks() {
        log.debug("Generating overdue book report");
        return ResponseEntity.ok(borrowService.getOverdueReport());
    }

    @GetMapping("/my-history")
    @PreAuthorize("hasRole('PATRON')")
    @Operation(summary = "Get my borrow history", description = "Returns the borrow history of the currently logged-in user")
    public ResponseEntity<List<BorrowResponse>> getMyBorrowHistory(Authentication authentication) {
        log.debug("Fetching borrow history for user '{}'", authentication.getName());
        return ResponseEntity.ok(borrowService.getBorrowHistoryForUser(authentication));
    }
}
