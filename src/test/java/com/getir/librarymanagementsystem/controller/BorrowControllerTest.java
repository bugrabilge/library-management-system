package com.getir.librarymanagementsystem.controller;

import com.getir.librarymanagementsystem.model.dto.request.BorrowRequest;
import com.getir.librarymanagementsystem.model.dto.response.BorrowResponse;
import com.getir.librarymanagementsystem.model.dto.response.OverdueReportResponse;
import com.getir.librarymanagementsystem.service.BorrowService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class BorrowControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BorrowService borrowService;

    @Autowired
    private ObjectMapper objectMapper;

    private final LocalDate today = LocalDate.now();
    private final LocalDate nextWeek = LocalDate.now().plusDays(7);

    @Test
    @WithMockUser(roles = {"PATRON", "LIBRARIAN"})
    void borrow_shouldReturnBorrowResponse() throws Exception {
        BorrowRequest request = new BorrowRequest();
        request.setBookId(1L);
        request.setBorrowDate(today);
        request.setReturnDate(nextWeek);

        BorrowResponse response = BorrowResponse.builder()
                .id(1L)
                .username("testuser")
                .bookTitle("Test Book")
                .borrowDate(today)
                .returnDate(nextWeek)
                .returned(false)
                .build();

        when(borrowService.borrowBook(any(BorrowRequest.class), any(Authentication.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/borrows")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.bookTitle").value("Test Book"))
                .andExpect(jsonPath("$.borrowDate").value(today.toString()))
                .andExpect(jsonPath("$.returnDate").value(nextWeek.toString()))
                .andExpect(jsonPath("$.returned").value(false));
    }

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    void getAll_shouldReturnAllBorrows() throws Exception {
        BorrowResponse response = BorrowResponse.builder()
                .id(1L)
                .bookTitle("Test Book")
                .borrowDate(today)
                .build();

        when(borrowService.getAllBorrows()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/borrows"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].bookTitle").value("Test Book"))
                .andExpect(jsonPath("$[0].borrowDate").value(today.toString()));
    }

    @Test
    @WithMockUser(roles = "PATRON")
    void returnBook_shouldReturnUpdatedBorrow() throws Exception {
        BorrowResponse response = BorrowResponse.builder()
                .id(1L)
                .bookTitle("Test Book")
                .returned(true)
                .returnDate(today)
                .build();

        when(borrowService.returnBook(anyLong(), any(Authentication.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/borrows/return/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.bookTitle").value("Test Book"))
                .andExpect(jsonPath("$.returned").value(true))
                .andExpect(jsonPath("$.returnDate").value(today.toString()));
    }

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    void getOverdueReport_shouldReturnReport() throws Exception {
        OverdueReportResponse response = new OverdueReportResponse();
        response.setBorrowId(1L);
        response.setBookTitle("Overdue Book");
        response.setUsername("user1");
        response.setBorrowDate(today.minusDays(15));
        response.setDueDate(today.minusDays(1));

        when(borrowService.getOverdueReport()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/borrows/overdue-report"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].borrowId").value(1L))
                .andExpect(jsonPath("$[0].bookTitle").value("Overdue Book"))
                .andExpect(jsonPath("$[0].borrowDate").value(today.minusDays(15).toString()))
                .andExpect(jsonPath("$[0].dueDate").value(today.minusDays(1).toString()));
    }

    @Test
    @WithMockUser(roles = "PATRON")
    void getMyBorrowHistory_shouldReturnUserHistory() throws Exception {
        BorrowResponse response = BorrowResponse.builder()
                .id(1L)
                .bookTitle("My Book")
                .borrowDate(today.minusDays(5))
                .returnDate(today.plusDays(2))
                .build();

        when(borrowService.getBorrowHistoryForUser(any(Authentication.class)))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/borrows/my-history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].bookTitle").value("My Book"))
                .andExpect(jsonPath("$[0].borrowDate").value(today.minusDays(5).toString()))
                .andExpect(jsonPath("$[0].returnDate").value(today.plusDays(2).toString()));
    }

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    void getOverdueBorrows_shouldReturnCorrectStructure() throws Exception {
        BorrowResponse response = BorrowResponse.builder()
                .id(1L)
                .bookTitle("Overdue Book")
                .borrowDate(today.minusDays(20))
                .returnDate(null)
                .returned(false)
                .build();

        when(borrowService.getOverdueBorrows()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/borrows/overdue"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].bookTitle").value("Overdue Book"))
                .andExpect(jsonPath("$[0].borrowDate").value(today.minusDays(20).toString()))
                .andExpect(jsonPath("$[0].returned").value(false));
    }
}