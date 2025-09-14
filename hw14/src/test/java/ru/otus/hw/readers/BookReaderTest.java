package ru.otus.hw.readers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import ru.otus.hw.models.h2.Author;
import ru.otus.hw.models.h2.Book;
import ru.otus.hw.models.h2.Genre;
import ru.otus.hw.repositories.jpa.BookJpaRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookReaderTest {

    @Mock
    private BookJpaRepository bookRepository;

    @Test
    @DisplayName("Чтение всех книг из репозитория")
    void testReadBooks() throws Exception {
        // given
        Author author = new Author(1L, "Author");
        Genre genre = new Genre(1L, "Genre");

        Book book1 = new Book(1L, "Book 1", author, List.of(genre));
        Book book2 = new Book(2L, "Book 2", author, List.of(genre));
        List<Book> books = List.of(book1, book2);
        Page<Book> bookPage = new PageImpl<>(books);
        Page<Book> emptyPage = new PageImpl<>(List.of());

        when(bookRepository.findAll(any(PageRequest.class)))
                .thenReturn(bookPage)
                .thenReturn(emptyPage);

        BookReader bookReader = new BookReader(bookRepository);

        // when
        Book firstBook = bookReader.read();
        Book secondBook = bookReader.read();
        Book thirdBook = bookReader.read();

        // then
        assertThat(firstBook).isEqualTo(book1);
        assertThat(secondBook).isEqualTo(book2);
        assertThat(thirdBook).isNull();
    }
}