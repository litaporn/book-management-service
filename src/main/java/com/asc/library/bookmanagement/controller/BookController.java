package com.asc.library.bookmanagement.controller;

import com.asc.library.bookmanagement.model.Book;
import com.asc.library.bookmanagement.model.ErrorResponse;
import com.asc.library.bookmanagement.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

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
    public ResponseEntity<?> getBooksByAuthor(@RequestParam("author") String authorName) {
        if (authorName == null || authorName.isEmpty()) {
            return new ResponseEntity<>(new ErrorResponse("Author must not be null."), HttpStatus.BAD_REQUEST);
        }
        List<Book> books = bookRepository.findByAuthor(authorName);

        if (books.isEmpty()) {
            return new ResponseEntity<>(new ErrorResponse("No books found for the specified author."), HttpStatus.NOT_FOUND);
        }
        // covert publishedDate from C.E. to B.E. before sent response to client
        for (Book book : books) {
            int buddhistYear = book.getPublishedDate().getYear() + 543;
            book.setPublishedDate(LocalDate.of(buddhistYear, book.getPublishedDate().getMonthValue(), book.getPublishedDate().getDayOfMonth()));
        }

        return ResponseEntity.ok(books); //200
    }

    // POST /books
    @PostMapping
    public ResponseEntity<?> saveBook(@RequestBody Book book, Locale locale) {
        // Validate
        if (book.getPublishedDate() == null) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Published date must not be null."));
        } else if (book.getTitle() == null || book.getTitle().isEmpty()) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Title must not be null."));
        } else if (book.getAuthor() == null || book.getAuthor().isEmpty()) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Author must not be null."));
        }

        // Convert Buddhist calendar year (B.E.) to Gregorian calendar year (C.E.)
        LocalDate publishedDate = LocalDate.of(
                book.getPublishedDate().getYear() - 543,
                book.getPublishedDate().getMonthValue(),
                book.getPublishedDate().getDayOfMonth()
        );

        if (publishedDate.isAfter(LocalDate.now())) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Published date must be less than or equal to the current year."));
        }

        if (publishedDate.isBefore(LocalDate.of(1000,1,1))) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Published date must be greater than or equal to the year 1000."));
        }

        book.setPublishedDate(publishedDate);

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
