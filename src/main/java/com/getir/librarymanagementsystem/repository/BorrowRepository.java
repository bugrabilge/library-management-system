package com.getir.librarymanagementsystem.repository;

import com.getir.librarymanagementsystem.model.entity.Borrow;
import com.getir.librarymanagementsystem.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface BorrowRepository extends JpaRepository<Borrow, Long> {
    List<Borrow> findByUser(User user);
    List<Borrow> findByReturnDateIsNullAndBorrowDateBefore(LocalDate date);
}
