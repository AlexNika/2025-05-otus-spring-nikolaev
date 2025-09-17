package ru.otus.hw.writers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.item.Chunk;
import ru.otus.hw.models.mongo.CommentDocument;
import ru.otus.hw.repositories.mongo.CommentMongoRepository;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentWriterTest {

    @Mock
    private CommentMongoRepository commentRepository;

    @Test
    @DisplayName("Запись комментариев в MongoDB")
    void testCommentWriter() throws Exception {
        // given
        CommentDocument commentDocument = new CommentDocument();
        commentDocument.setText("Test Comment");

        List<CommentDocument> comments = List.of(commentDocument);
        Chunk<CommentDocument> chunk = new Chunk<>(comments);

        // when
        try (CommentWriter commentWriter = new CommentWriter(commentRepository, 4)) {
            commentWriter.write(chunk);

            // then
            verify(commentRepository, times(1)).saveAll(comments);
        }
    }
}
