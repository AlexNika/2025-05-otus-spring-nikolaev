package ru.otus.hw.processors;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.models.h2.Comment;
import ru.otus.hw.models.mongo.CommentDocument;
import ru.otus.hw.service.CacheService;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CommentProcessor implements ItemProcessor<Comment, CommentDocument> {

    private final CacheService cacheService;

    @Override
    public CommentDocument process(Comment comment) {
        String cacheKey = "comment_" + comment.getId();
        Optional<CommentDto> cachedComment = cacheService.getFromCache(cacheKey, CommentDto.class);
        CommentDto commentDto;

        if (cachedComment.isPresent()) {
            commentDto = cachedComment.get();
        } else {
            commentDto = new CommentDto(
                    String.valueOf(comment.getId()),
                    comment.getText(),
                    String.valueOf(comment.getBook().getId()),
                    comment.getCreated(),
                    comment.getUpdated()
            );
            cacheService.putInCache(cacheKey, commentDto);
        }
        CommentDocument commentDocument = new CommentDocument();
        commentDocument.setText(comment.getText());

        return commentDocument;
    }
}