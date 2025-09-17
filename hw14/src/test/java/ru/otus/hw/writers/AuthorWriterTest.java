package ru.otus.hw.writers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.item.Chunk;
import ru.otus.hw.models.mongo.AuthorDocument;
import ru.otus.hw.repositories.mongo.AuthorMongoRepository;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthorWriterTest {

    @Mock
    private AuthorMongoRepository authorRepository;

    @Test
    @DisplayName("Запись авторов в MongoDB")
    void testAuthorWriter() throws Exception {
        // given
        AuthorDocument authorDocument = new AuthorDocument();
        authorDocument.setFullName("Test Author");

        List<AuthorDocument> authors = List.of(authorDocument);
        Chunk<AuthorDocument> chunk = new Chunk<>(authors);

        // when
        try (AuthorWriter authorWriter = new AuthorWriter(authorRepository, 2)) {
            authorWriter.write(chunk);

            // then
            verify(authorRepository, times(1)).saveAll(authors);
        }
    }
}