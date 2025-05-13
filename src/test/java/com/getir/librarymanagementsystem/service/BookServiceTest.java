package com.getir.librarymanagementsystem.service;

import com.getir.librarymanagementsystem.model.entity.Book;
import com.getir.librarymanagementsystem.repository.BookRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@SpringBootTest
class BookServiceTest {

    @MockBean
    private BookRepository bookRepository;

    @Autowired
    private BookService bookService;

    @Test
    void save_ShouldReturnSavedBook() {
        Book book = new Book();
        book.setTitle("Test Book");
        when(bookRepository.save(book)).thenReturn(book);

        Book savedBook = bookService.save(book);

        assertNotNull(savedBook);
        assertEquals("Test Book", savedBook.getTitle());
        verify(bookRepository).save(book);
    }

    @Test
    void update_WhenBookExists_ShouldUpdateAndReturnBook() {
        Long id = 1L;
        Book existingBook = new Book();
        existingBook.setId(id);
        existingBook.setTitle("Old Title");
        existingBook.setIsbn("1234567890");

        Book updateData = new Book();
        updateData.setTitle("New Title");
        updateData.setAuthor("New Author");
        existingBook.setIsbn("1234567890");

        when(bookRepository.findById(id)).thenReturn(Optional.of(existingBook));
        when(bookRepository.save(existingBook)).thenReturn(existingBook);

        Book updatedBook = bookService.update(id, updateData);

        assertEquals("New Title", updatedBook.getTitle());
        assertEquals("New Author", updatedBook.getAuthor());
        verify(bookRepository).findById(id);
        verify(bookRepository).save(existingBook);
    }

    @Test
    void update_WhenBookNotFound_ShouldThrowException() {
        Long id = 1L;
        when(bookRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                bookService.update(id, new Book()));
        verify(bookRepository).findById(id);
        verify(bookRepository, never()).save(any());
    }

    @Test
    void delete_ShouldCallRepositoryDelete() {
        Long id = 1L;
        bookService.delete(id);
        verify(bookRepository).deleteById(id);
    }

    @Test
    void findById_WhenBookExists_ShouldReturnBook() {
        Long id = 1L;
        Book book = new Book();
        book.setId(id);
        when(bookRepository.findById(id)).thenReturn(Optional.of(book));

        Optional<Book> result = bookService.findById(id);

        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId());
    }

    @Test
    void findById_WhenBookNotExists_ShouldReturnEmpty() {
        Long id = 1L;
        when(bookRepository.findById(id)).thenReturn(Optional.empty());

        Optional<Book> result = bookService.findById(id);

        assertTrue(result.isEmpty());
    }

    @Test
    void findAll_ShouldReturnAllBooks() {
        Book book1 = new Book();
        Book book2 = new Book();
        when(bookRepository.findAll()).thenReturn(Arrays.asList(book1, book2));

        List<Book> books = bookService.findAll();

        assertEquals(2, books.size());
    }

    @Test
    void search_ShouldReturnMatchingBooks() {
        Book book1 = new Book();
        book1.setTitle("Java Programming");
        book1.setAuthor("John Doe");

        Book book2 = new Book();
        book2.setTitle("Spring Boot");
        book2.setAuthor("Jane Doe");

        when(bookRepository.findAll()).thenReturn(Arrays.asList(book1, book2));

        // Test title search
        List<Book> javaResults = bookService.search("java");
        assertEquals(1, javaResults.size());
        assertEquals("Java Programming", javaResults.get(0).getTitle());

        // Test author search
        List<Book> doeResults = bookService.search("doe");
        assertEquals(2, doeResults.size());

        // Test no results
        List<Book> emptyResults = bookService.search("python");
        assertTrue(emptyResults.isEmpty());
    }

    @Test
    void search_ShouldBeCaseInsensitive() {
        Book book = new Book();
        book.setTitle("Spring Framework");
        book.setAuthor("Martin Fowler");

        when(bookRepository.findAll()).thenReturn(List.of(book));

        List<Book> titleResults = bookService.search("spring");
        List<Book> authorResults = bookService.search("fowler");

        assertEquals(1, titleResults.size());
        assertEquals(1, authorResults.size());
    }
}