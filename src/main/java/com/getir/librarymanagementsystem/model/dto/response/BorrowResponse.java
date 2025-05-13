package com.getir.librarymanagementsystem.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BorrowResponse {
    private Long id;
    private String username;
    private String bookTitle;
    private LocalDate borrowDate;
    private LocalDate returnDate;
    private boolean returned;
}
