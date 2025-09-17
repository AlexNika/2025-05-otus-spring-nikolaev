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
import ru.otus.hw.repositories.jpa.AuthorJpaRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthorReaderTest {

    @Mock
    private AuthorJpaRepository authorRepository;

    @Test
    @DisplayName("Чтение всех авторов из репозитория")
    void testReadAuthors() throws Exception {
        // given
        Author author1 = new Author(1L, "Author 1");
        Author author2 = new Author(2L, "Author 2");
        List<Author> authors = List.of(author1, author2);
        Page<Author> authorPage = new PageImpl<>(authors);
        Page<Author> emptyPage = new PageImpl<>(List.of());

        when(authorRepository.findAll(any(PageRequest.class)))
                .thenReturn(authorPage)
                .thenReturn(emptyPage);

        AuthorReader authorReader = new AuthorReader(authorRepository);

        // when
        Author firstAuthor = authorReader.read();
        Author secondAuthor = authorReader.read();
        Author thirdAuthor = authorReader.read();

        // then
        assertThat(firstAuthor).isEqualTo(author1);
        assertThat(secondAuthor).isEqualTo(author2);
        assertThat(thirdAuthor).isNull();
    }
}