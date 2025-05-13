package com.getir.librarymanagementsystem.model.dto.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class BorrowRequest {
    private Long bookId;
    private LocalDate borrowDate;
    private LocalDate returnDate;
}
