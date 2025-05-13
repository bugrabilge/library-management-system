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
public class OverdueReportResponse {
    private Long borrowId;
    private String bookTitle;
    private String username;
    private LocalDate borrowDate;
    private LocalDate dueDate;
}
