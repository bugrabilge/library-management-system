package com.getir.librarymanagementsystem.model.mapper;

import com.getir.librarymanagementsystem.model.dto.request.BorrowRequest;
import com.getir.librarymanagementsystem.model.dto.response.BorrowResponse;
import com.getir.librarymanagementsystem.model.dto.response.OverdueReportResponse;
import com.getir.librarymanagementsystem.model.entity.Book;
import com.getir.librarymanagementsystem.model.entity.Borrow;
import com.getir.librarymanagementsystem.model.entity.User;
import org.springframework.stereotype.Component;

@Component
public class BorrowMapper {

    public Borrow toEntity(BorrowRequest request, User user, Book book) {
        return Borrow.builder()
                .user(user)
                .book(book)
                .borrowDate(request.getBorrowDate())
                .returnDate(request.getReturnDate())
                .returned(false)
                .build();
    }

    public BorrowResponse toResponse(Borrow borrow) {
        return BorrowResponse.builder()
                .id(borrow.getId())
                .username(borrow.getUser().getUsername())
                .bookTitle(borrow.getBook().getTitle())
                .borrowDate(borrow.getBorrowDate())
                .returnDate(borrow.getReturnDate())
                .returned(borrow.isReturned())
                .build();
    }

    public OverdueReportResponse toOverdueResponse(Borrow borrow) {
        return OverdueReportResponse.builder()
                .borrowId(borrow.getId())
                .bookTitle(borrow.getBook().getTitle())
                .username(borrow.getUser().getUsername())
                .borrowDate(borrow.getBorrowDate())
                .dueDate(borrow.getBorrowDate().plusDays(14))
                .build();
    }


}
