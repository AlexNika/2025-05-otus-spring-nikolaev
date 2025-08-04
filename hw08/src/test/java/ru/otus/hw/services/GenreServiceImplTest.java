package ru.otus.hw.services;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.dto.mappers.*;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Genre;

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

@DisplayName("Сервис для работы с жанрами")
@Import({GenreServiceImpl.class,
        BookServiceImpl.class,
        AuthorMapperImpl.class,
        BookMapperImpl.class,
        GenreMapperImpl.class,
        CommentMapperImpl.class
})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class GenreServiceImplTest extends AbstractServiceTest {

    private static final String MISSING_ID = "689f4e8dd54028370a051194";

    @Autowired
    private GenreService genreService;

    @Autowired
    private GenreMapper genreMapper;

    @DisplayName("должен загружать список жанров")
    @Order(1)
    @Test
    void whenFindAllGenres_thenReturnAllGenres() {
        //Given
        int expectedGenreSize = 6;
        String genreName = "New_Genre_1";
        Genre newGenre = this.insertGenre(genreName);

        //When
        List<GenreDto> genres = genreService.findAll();

        //Then
        assertAll(
                () -> assertThat(genres)
                        .isNotNull()
                        .isNotEmpty()
                        .hasSize(expectedGenreSize + 1),
                () -> assertThat(genres)
                        .contains(genreMapper.toGenreDto(newGenre))
                        .extracting(GenreDto::name)
                        .containsExactlyInAnyOrder("Genre_1", "Genre_2", "Genre_3", "Genre_4", "Genre_5",
                                "Genre_6", genreName));
    }

    @DisplayName("должен загружать жанр по id")
    @Order(2)
    @Test
    void whenFindAuthorById_thenReturnAuthor() {
        //Given
        String genreName = "New_Genre_2";
        Genre newGenre = this.insertGenre(genreName);

        //When
        Optional<GenreDto> foundGenre = genreService.findById(newGenre.getId());

        //Then
        assertThat(foundGenre).isPresent();
        assertThat(foundGenre.get().name()).isNotBlank();
        assertThat(foundGenre.get().id()).isEqualTo(newGenre.getId());
        assertThat(foundGenre.get().name()).isEqualTo(newGenre.getName());
    }

    @DisplayName("должен возвращать Optional.empty() если жанр не найден по id")
    @Order(3)
    @Test
    void whenFindAuthorByNonExistentId_thenReturnOptionalEmpty() {
        //Given - genre MISSING_ID

        //When
        Optional<GenreDto> optionalGenre = genreService.findById(MISSING_ID);

        //Then
        assertThat(optionalGenre).isNotNull().isEmpty();
    }

    @DisplayName("должен загружать список жанров по ids")
    @Order(4)
    @Test
    void whenFindGenresByIds_thenReturnGenres() {
        //Given
        Set<String> ids = genreService.findAll()
                .stream()
                .map(GenreDto::id)
                .limit(3L)
                .collect(Collectors.toSet());

        //When
        List<GenreDto> genres = genreService.findByIds(ids);

        //Then
        assertThat(genres).isNotNull().isNotEmpty().hasSize(ids.size());
        assertThat(getFirst(genres).name()).isEqualTo("Genre_1");
        assertThat(getLast(genres).name()).isEqualTo("Genre_3");
    }

    @DisplayName("должен выбрасывать исключение при поиске жанров по пустому списку ids")
    @Order(5)
    @Test
    void whenFindGenresByEmptyIds_thenThrowIllegalArgumentException() {
        //Given
        Set<String> ids = Set.of();

        //Then
        assertThatThrownBy(() -> genreService.findByIds(ids))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(ILLEGAL_ARGUMENT_MESSAGE.getMessage(Genre.class.getSimpleName()));
    }

    @DisplayName("должен возвращать пустой список если жанров нет по ids")
    @Order(6)
    @Test
    void whenFindGenresByNonExistentIds_thenReturnEmptyList() {
        //Given
        Set<String> missingIds = Set.of(MISSING_ID, "3911f78d9il49r0be33425b5");

        //When
        List<GenreDto> genres = genreService.findByIds(missingIds);

        //Then
        assertThat(genres).isNotNull().isEmpty();
    }

    @DisplayName("должен сохранять новый жанр")
    @Order(7)
    @Test
    void whenInsertNewGenre_thenReturnSavedGenre() {
        //Given
        String genreName = "New_Genre_3";

        //When
        GenreDto genre = genreService.insert(genreName);
        Optional<GenreDto> savedGenre = genreService.findById(genre.id());

        //Then
        assertThat(genre.id()).isNotEqualTo(MISSING_ID).isNotEmpty();
        assertThat(genre.name()).isEqualTo(genreName);

        assertThat(savedGenre).isPresent();
        assertThat(savedGenre.get().name()).isEqualTo(genreName);
    }

    @DisplayName("должен обновлять существующий жанр")
    @Order(8)
    @Test
    void whenUpdateGenre_thenReturnUpdatedGenre() {
        //Given
        String genreName = "New_Genre_4";
        Genre newGenre = this.insertGenre(genreName);
        assertThat(genreService.findById(newGenre.getId())).isPresent();
        String updatedName = "Updated_Genre_4";

        //When
        GenreDto genre = genreService.update(newGenre.getId(), updatedName);
        Optional<GenreDto> updatedAuthor = genreService.findById(newGenre.getId());

        //Then
        assertThat(genre.id()).isNotNull().isEqualTo(newGenre.getId());
        assertThat(genre.name()).isNotEqualTo(genreName);
        assertThat(genre.name()).isEqualTo(updatedName);

        assertThat(updatedAuthor).isPresent();
        assertThat(updatedAuthor.get().name()).isEqualTo(updatedName);
    }

    @DisplayName("должен выбрасывать исключение при обновления жанра по несуществующему id")
    @Order(9)
    @Test
    void whenFindGenreByNonExistentId_thenThrowEntityNotFoundException() {
        //Given - genre MISSING_ID

        //Then
        assertThatThrownBy(() -> genreService.update(MISSING_ID, ""))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(ENTITY_NOT_FOUND_MESSAGE.getMessage(Genre.class.getSimpleName(), MISSING_ID));
    }

    @DisplayName("должен удалять жанр")
    @Order(10)
    @Test
    void whenDeleteGenreById_thenGenreIsDeleted() {
        //Given
        String genreName = "New_Genre_5";
        Genre newGenre = this.insertGenre(genreName);
        assertThat(genreService.findById(newGenre.getId())).isPresent();

        //When
        genreService.deleteById(newGenre.getId());

        //Then
        assertThat(genreService.findById(newGenre.getId())).isEmpty();
    }
}
