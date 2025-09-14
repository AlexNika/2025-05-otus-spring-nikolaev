package ru.otus.hw.readers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import ru.otus.hw.models.h2.Comment;
import ru.otus.hw.repositories.jpa.CommentJpaRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentReaderTest {

    @Mock
    private CommentJpaRepository commentRepository;

    @Test
    @DisplayName("Чтение всех комментариев из репозитория")
    void testReadComments() throws Exception {
        // given
        Comment comment1 = new Comment(1L, "Comment 1", null);
        Comment comment2 = new Comment(2L, "Comment 2", null);
        List<Comment> comments = List.of(comment1, comment2);
        Page<Comment> commentPage = new PageImpl<>(comments);
        Page<Comment> emptyPage = new PageImpl<>(List.of());

        when(commentRepository.findAll(any(PageRequest.class)))
                .thenReturn(commentPage)
                .thenReturn(emptyPage);

        CommentReader commentReader = new CommentReader(commentRepository);

        // when
        Comment firstComment = commentReader.read();
        Comment secondComment = commentReader.read();
        Comment thirdComment = commentReader.read();

        // then
        assertThat(firstComment).isEqualTo(comment1);
        assertThat(secondComment).isEqualTo(comment2);
        assertThat(thirdComment).isNull();
    }
}