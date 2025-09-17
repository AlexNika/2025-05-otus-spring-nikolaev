package ru.otus.hw.writers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.item.Chunk;
import ru.otus.hw.models.mongo.BookDocument;
import ru.otus.hw.repositories.mongo.BookMongoRepository;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookWriterTest {

    @Mock
    private BookMongoRepository bookRepository;

    @Test
    @DisplayName("Запись книг в MongoDB")
    void testBookWriter() throws Exception {
        // given
        BookDocument bookDocument = new BookDocument();
        bookDocument.setTitle("Test Book");

        List<BookDocument> books = List.of(bookDocument);
        Chunk<BookDocument> chunk = new Chunk<>(books);

        // when
        try (BookWriter bookWriter = new BookWriter(bookRepository, 4)) {
            bookWriter.write(chunk);

            // then
            verify(bookRepository, times(1)).saveAll(books);
        }
    }
}
