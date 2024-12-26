package com.asc.library.bookmanagement;

import com.asc.library.bookmanagement.model.Book;
import com.asc.library.bookmanagement.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;


@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class BookManagementServiceApplicationTests {

	@Autowired
	private BookRepository bookRepository;

	@Autowired
	private MockMvc mockMvc;

	@BeforeEach
	public void setUp() {
		// delete all before test
		bookRepository.deleteAll();

		// Prepare test data
		Book book1 = new Book();
		book1.setTitle("Test Book 1");
		book1.setAuthor("Author Test1");
		book1.setPublishedDate(java.time.LocalDate.of(2022, 12, 25));
		bookRepository.save(book1);

		Book book2 = new Book();
		book2.setTitle("Test Book 2");
		book2.setAuthor("Author Test2");
		book2.setPublishedDate(java.time.LocalDate.of(2023, 1, 15));
		bookRepository.save(book2);
	}

	@Test
	void contextLoads() {
		// ตรวจสอบว่า Application Context สามารถโหลดได้
	}

	@Test
	void testSaveBook() throws Exception {
		String bookJson = "{ \"title\": \"Test Book 3\", \"author\": \"Author Test3\", \"publishedDate\": \"2567-01-15\" }";

		// Test for save a book
		mockMvc.perform(post("/books")
						.contentType(MediaType.APPLICATION_JSON)
						.content(bookJson))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.title").value("Test Book 3"))
				.andExpect(jsonPath("$.author").value("Author Test3"))
				.andExpect(jsonPath("$.publishedDate").value("2567-01-15"));
	}

	@Test
	void testGetBooksByAuthor() throws Exception {
		// test get the book by the author
		mockMvc.perform(get("/books?author=Author Test1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].title").value("Test Book 1"))
				.andExpect(jsonPath("$[0].author").value("Author Test1"))
				.andExpect(jsonPath("$[0].publishedDate").value("2566-01-15"));
	}

}
