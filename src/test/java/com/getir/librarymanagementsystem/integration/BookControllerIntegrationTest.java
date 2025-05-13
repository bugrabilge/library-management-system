package com.getir.librarymanagementsystem.integration;

import com.getir.librarymanagementsystem.model.entity.Book;
import com.getir.librarymanagementsystem.repository.BookRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class BookControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Book book1;
    private Book book2;

    @BeforeEach
    void setup() {
        // Clear database before each test
        bookRepository.deleteAll();

        // Setup test data
        book1 = Book.builder()
                .title("Clean Code")
                .author("Robert C. Martin")
                .isbn("9780132350884")
                .publicationDate(LocalDate.of(2008, 8, 1))
                .genre("Programming")
                .available(true)
                .build();

        book2 = Book.builder()
                .title("Design Patterns")
                .author("Erich Gamma")
                .isbn("9780201633610")
                .publicationDate(LocalDate.of(1994, 10, 31))
                .genre("Computer Science")
                .available(true)
                .build();

        book1 = bookRepository.save(book1);
        book2 = bookRepository.save(book2);
    }

    @AfterEach
    void tearDown() {
        bookRepository.deleteAll();
    }

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    void whenSaveBook_thenReturnSavedBook() throws Exception {
        Book newBook = Book.builder()
                .title("Test Driven Development")
                .author("Kent Beck")
                .isbn("9780321146533")
                .publicationDate(LocalDate.of(2002, 11, 18))
                .genre("Software Development")
                .available(true)
                .build();

        ResultActions response = mockMvc.perform(post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newBook)));

        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is(newBook.getTitle())))
                .andExpect(jsonPath("$.author", is(newBook.getAuthor())))
                .andExpect(jsonPath("$.isbn", is(newBook.getIsbn())))
                .andExpect(jsonPath("$.available", is(true)));

        List<Book> books = bookRepository.findAll();
        assertEquals(3, books.size());
        assertTrue(books.stream().anyMatch(b -> b.getIsbn().equals(newBook.getIsbn())));
    }

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    void whenUpdateBook_thenReturnUpdatedBook() throws Exception {
        Book updatedBook = Book.builder()
                .title("Clean Code Updated")
                .author("Robert C. Martin")
                .isbn("9780132350884")
                .publicationDate(LocalDate.of(2020, 1, 1))
                .genre("Updated Programming")
                .available(false)
                .build();

        ResultActions response = mockMvc.perform(put("/api/books/{id}", book1.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedBook)));

        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is(updatedBook.getTitle())))
                .andExpect(jsonPath("$.genre", is(updatedBook.getGenre())))
                .andExpect(jsonPath("$.available", is(false)));

        Book dbBook = bookRepository.findById(book1.getId()).orElseThrow();
        assertEquals(updatedBook.getTitle(), dbBook.getTitle());
        assertEquals(updatedBook.isAvailable(), dbBook.isAvailable());
    }

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    void whenDeleteBook_thenReturnNoContent() throws Exception {
        ResultActions response = mockMvc.perform(delete("/api/books/{id}", book1.getId()));

        response.andDo(print())
                .andExpect(status().isNoContent());

        List<Book> books = bookRepository.findAll();
        assertEquals(1, books.size());
        assertTrue(books.stream().noneMatch(b -> b.getId().equals(book1.getId())));
    }

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    void whenFindById_thenReturnBook() throws Exception {
        ResultActions response = mockMvc.perform(get("/api/books/{id}", book1.getId()));

        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(book1.getId().intValue())))
                .andExpect(jsonPath("$.title", is(book1.getTitle())))
                .andExpect(jsonPath("$.genre", is(book1.getGenre())))
                .andExpect(jsonPath("$.available", is(book1.isAvailable())));
    }

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    void whenFindAll_thenReturnAllBooks() throws Exception {
        ResultActions response = mockMvc.perform(get("/api/books"));

        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title", is(book1.getTitle())))
                .andExpect(jsonPath("$[1].title", is(book2.getTitle())));
    }

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    void whenSearchByTitle_thenReturnMatchingBooks() throws Exception {
        ResultActions response = mockMvc.perform(get("/api/books/search")
                .param("keyword", "Clean"));

        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is(book1.getTitle())));
    }

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    void whenSearchByAuthor_thenReturnMatchingBooks() throws Exception {
        ResultActions response = mockMvc.perform(get("/api/books/search")
                .param("keyword", "Gamma"));

        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].author", is(book2.getAuthor())));
    }

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    void whenSearchByNonExistingKeyword_thenReturnEmptyList() throws Exception {
        ResultActions response = mockMvc.perform(get("/api/books/search")
                .param("keyword", "NonExisting"));

        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}