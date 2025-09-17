package ru.otus.hw.processors;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.models.h2.Author;
import ru.otus.hw.models.mongo.AuthorDocument;
import ru.otus.hw.service.CacheService;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AuthorProcessor implements ItemProcessor<Author, AuthorDocument> {

    private final CacheService cacheService;

    @Override
    public AuthorDocument process(Author author) {
        AuthorDto authorDto;
        Optional<AuthorDto> cachedAuthor = cacheService.getAuthorFromCache(author.getId());
        if (cachedAuthor.isPresent()) {
            authorDto = cachedAuthor.get();
        } else {
            authorDto = new AuthorDto(
                    String.valueOf(author.getId()),
                    author.getFullName(),
                    author.getCreated(),
                    author.getUpdated()
            );
            cacheService.cacheAuthor(author.getId(), authorDto);
        }
        AuthorDocument authorDocument = new AuthorDocument();
        authorDocument.setFullName(author.getFullName());
        return authorDocument;
    }
}