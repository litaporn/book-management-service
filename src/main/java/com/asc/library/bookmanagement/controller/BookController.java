package com.asc.library.bookmanagement.controller;

import com.asc.library.bookmanagement.model.Book;
import com.asc.library.bookmanagement.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/books")
public class BookController {

    private final BookRepository bookRepository;

    @Autowired
    public BookController(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    // GET /books?author={authorName}
    @GetMapping
    public ResponseEntity<List<Book>> getBooksByAuthor(@RequestParam("author") String authorName) {
        if (authorName == null || authorName.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        List<Book> books = bookRepository.findByAuthor(authorName);

        if (books.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        // covert publishedDate from C.E. เป็น B.E. before sent response to client
        for (Book book : books) {
            int buddhistYear = book.getPublishedDate().getYear() + 543;
            book.setPublishedDate(LocalDate.of(buddhistYear, book.getPublishedDate().getMonthValue(), book.getPublishedDate().getDayOfMonth()));
        }

        return ResponseEntity.ok(books);
    }

    // POST /books
    @PostMapping
    public ResponseEntity<Book> saveBook(@RequestBody Book book) {
        // Validate
        if (book.getPublishedDate() == null) {
            throw new IllegalArgumentException("Published date must not be null");
        } else if (book.getTitle() == null || book.getTitle().isEmpty()) {
            throw new IllegalArgumentException("Title must not be null");
        } else if (book.getAuthor() == null || book.getAuthor().isEmpty()) {
            throw new IllegalArgumentException("Author must not be null");
        }

        // Convert Buddhist calendar year (B.E.) to Gregorian calendar year (C.E.)
        LocalDate gregorianDate = LocalDate.of(
                book.getPublishedDate().getYear() - 543,
                book.getPublishedDate().getMonthValue(),
                book.getPublishedDate().getDayOfMonth()
        );

        if (gregorianDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Published date must not be in the future");
        }

        if (gregorianDate.getYear() < 1000) {
            throw new IllegalArgumentException("Published date must be greater than or equal to the year 1000");
        }

        book.setPublishedDate(gregorianDate);

        // Save book to the database
        Book savedBook = bookRepository.save(book);

        // Convert publishedDate to Buddhist calendar year (B.E.) and send response to client
        int buddhistYear = savedBook.getPublishedDate().getYear() + 543;

        // create object for sent to client
        Book responseBook = new Book();
        responseBook.setId(savedBook.getId());
        responseBook.setTitle(savedBook.getTitle());
        responseBook.setAuthor(savedBook.getAuthor());

        responseBook.setPublishedDate(LocalDate.of(buddhistYear, savedBook.getPublishedDate().getMonthValue(), savedBook.getPublishedDate().getDayOfMonth()));

        // HttpStatus.CREATED 201
        return ResponseEntity.status(HttpStatus.CREATED).body(responseBook);
    }
}
