package ru.otus.hw.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.dto.mappers.AuthorMapper;
import ru.otus.hw.dto.mappers.AuthorMapperImpl;
import ru.otus.hw.dto.mappers.BookMapperImpl;
import ru.otus.hw.dto.mappers.CommentMapperImpl;
import ru.otus.hw.dto.mappers.GenreMapperImpl;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Author;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static ru.otus.hw.utils.Lists.getFirst;
import static ru.otus.hw.utils.Lists.getLast;
import static ru.otus.hw.utils.ValidationMessages.ENTITY_NOT_FOUND_MESSAGE;
import static ru.otus.hw.utils.ValidationMessages.ILLEGAL_ARGUMENT_MESSAGE;

@DisplayName("Сервис для работы с авторами")
@Import({
        AuthorServiceImpl.class,
        BookServiceImpl.class,
        AuthorMapperImpl.class,
        BookMapperImpl.class,
        GenreMapperImpl.class,
        CommentMapperImpl.class
})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthorServiceImplTest extends AbstractServiceTest {

    private static final String MISSING_ID = "689f4e8dd54028370a051194";

    @Autowired
    private AuthorService authorService;

    @Autowired
    private AuthorMapper authorMapper;

    @DisplayName("должен загружать список всех авторов")
    @Order(1)
    @Test
    void whenFindAllAuthors_thenReturnAllAuthors() {
        //Given
        int expectedAuthorSize = 3;
        String authorFullName = "New_Author_1";
        Author newAuthor = this.insertAuthor(authorFullName);

        //When
        List<AuthorDto> authors = authorService.findAll();

        //Then
        assertAll(
                () -> assertThat(authors)
                        .isNotNull()
                        .isNotEmpty()
                        .hasSize(expectedAuthorSize + 1),
                () -> assertThat(authors)
                        .contains(authorMapper.toAuthorDto(newAuthor))
                        .extracting(AuthorDto::fullName)
                        .containsExactlyInAnyOrder("Author_1", "Author_2", "Author_3", authorFullName));
    }

    @DisplayName("должен загружать автора по id")
    @Order(2)
    @Test
    void whenFindAuthorById_thenReturnAuthor() {
        //Given
        String authorFullName = "New_Author_2";
        Author newAuthor = this.insertAuthor(authorFullName);

        //When
        Optional<AuthorDto> foundAuthor = authorService.findById(newAuthor.getId());

        //Then
        assertThat(foundAuthor).isPresent();
        assertThat(foundAuthor.get().fullName()).isNotBlank();
        assertThat(foundAuthor.get().id()).isEqualTo(newAuthor.getId());
        assertThat(foundAuthor.get().fullName()).isEqualTo(newAuthor.getFullName());
    }

    @DisplayName("должен возвращать Optional.empty() если автор не найден по id")
    @Order(3)
    @Test
    void whenFindAuthorByNonExistentId_thenReturnOptionalEmpty() {
        //Given - author MISSING_ID

        //When
        Optional<AuthorDto> optionalAuthor = authorService.findById(MISSING_ID);

        //Then
        assertThat(optionalAuthor).isNotNull().isEmpty();
    }

    @DisplayName("должен загружать список авторов по ids")
    @Order(4)
    @Test
    void whenFindAuthorsByIds_thenReturnAuthors() {
        //Given
        Set<String> ids = authorService.findAll()
                .stream()
                .map(AuthorDto::id)
                .limit(2L)
                .collect(Collectors.toSet());

        //When
        List<AuthorDto> authors = authorService.findByIds(ids);

        //Then
        assertThat(authors).isNotNull().isNotEmpty().hasSize(ids.size());
        assertThat(getFirst(authors).fullName()).isEqualTo("Author_1");
        assertThat(getLast(authors).fullName()).isEqualTo("Author_2");
    }

    @DisplayName("должен выбрасывать исключение при поиске авторов по пустому списку ids")
    @Order(5)
    @Test
    void whenFindAuthorsByEmptyIds_thenThrowIllegalArgumentException() {
        //Given
        Set<String> ids = Set.of();

        //Then
        assertThatThrownBy(() -> authorService.findByIds(ids))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(ILLEGAL_ARGUMENT_MESSAGE.getMessage(Author.class.getSimpleName()));
    }

    @DisplayName("должен возвращать пустой список если авторов нет по ids")
    @Order(6)
    @Test
    void whenFindAuthorsByNonExistentIds_thenReturnEmptyList() {
        //Given
        Set<String> missingIds = Set.of(MISSING_ID, "3911g78g9il49r0be33425b5");

        //When
        List<AuthorDto> authors = authorService.findByIds(missingIds);

        //Then
        assertThat(authors).isNotNull().isEmpty();
    }

    @DisplayName("должен сохранять нового автора")
    @Order(7)
    @Test
    void whenInsertNewAuthor_thenReturnSavedAuthor() {
        //Given
        String authorFullName = "New_Author_3";

        //When
        AuthorDto author = authorService.insert(authorFullName);
        Optional<AuthorDto> savedAuthor = authorService.findById(author.id());

        //Then
        assertThat(author.id()).isNotEqualTo(MISSING_ID).isNotEmpty();
        assertThat(author.fullName()).isEqualTo(authorFullName);

        assertThat(savedAuthor).isPresent();
        assertThat(savedAuthor.get().fullName()).isEqualTo(authorFullName);
    }

    @DisplayName("должен обновлять существующего автора")
    @Order(8)
    @Test
    void whenUpdateAuthor_thenReturnUpdatedAuthor() {
        //Given
        String authorFullName = "New_Author_4";
        Author newAuthor = this.insertAuthor(authorFullName);
        assertThat(authorService.findById(newAuthor.getId())).isPresent();
        String updatedFullName = "Updated_Author_4";

        //When
        AuthorDto author = authorService.update(newAuthor.getId(), updatedFullName);
        Optional<AuthorDto> updatedAuthor = authorService.findById(newAuthor.getId());

        //Then
        assertThat(author.id()).isNotNull().isEqualTo(newAuthor.getId());
        assertThat(author.fullName()).isNotEqualTo(authorFullName);
        assertThat(author.fullName()).isEqualTo(updatedFullName);

        assertThat(updatedAuthor).isPresent();
        assertThat(updatedAuthor.get().fullName()).isEqualTo(updatedFullName);
    }

    @DisplayName("должен выбрасывать исключение при обновления авторов по несуществующему id")
    @Order(9)
    @Test
    void whenFindAuthorByNonExistentId_thenThrowEntityNotFoundException() {
        //Given - author MISSING_ID

        //Then
        assertThatThrownBy(() -> authorService.update(MISSING_ID, ""))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(ENTITY_NOT_FOUND_MESSAGE.getMessage(Author.class.getSimpleName(), MISSING_ID));
    }

    @DisplayName("должен удалять автора")
    @Order(10)
    @Test
    void whenDeleteAuthorById_thenAuthorIsDeleted() {
        //Given - PRESENT_ID
        String authorFullName = "New_Author_5";
        Author newAuthor = this.insertAuthor(authorFullName);
        assertThat(authorService.findById(newAuthor.getId())).isPresent();

        //When
        authorService.deleteById(newAuthor.getId());

        //Then
        assertThat(authorService.findById(newAuthor.getId())).isEmpty();
    }
}