package ru.otus.hw.controllers.rest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.CreateUpdateBookDto;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.services.AuthorService;
import ru.otus.hw.services.BookService;
import ru.otus.hw.services.GenreService;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("REST контроллер для работы с книгами ")
@WebFluxTest(controllers = {BookRestController.class, AuthorRestController.class, GenreRestController.class})
@Import(RestExceptionHandler.class)
class BookRestControllerTest {

    private static final Long MISSING_ID = 42L;
    private static final Long PRESENT_ID = 1L;
    private static final Long PRESENT_ID2 = 2L;

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private BookService bookService;

    @MockitoBean
    private AuthorService authorService;

    @MockitoBean
    private GenreService genreService;

    private BookDto bookDto;
    private CreateUpdateBookDto createUpdateBookDto;

    @BeforeEach
    void setUp() {
        AuthorDto authorDto = AuthorDto.builder().id(PRESENT_ID).fullName("Author_1").build();
        Set<GenreDto> genreDtos = Set.of(GenreDto.builder().id(PRESENT_ID).name("Genre_1").build());

        bookDto = BookDto.builder()
                .id(PRESENT_ID)
                .title("Test_Book")
                .author(authorDto)
                .genres(genreDtos)
                .build();

        createUpdateBookDto = CreateUpdateBookDto.builder()
                .title("Test_Book")
                .authorId(PRESENT_ID)
                .genreIds(List.of(PRESENT_ID, PRESENT_ID2))
                .build();
    }

    @DisplayName("должен возвращать список всех книг")
    @Test
    void whenGetAllBooks_thenReturnsBooksList() {
        List<BookDto> books = List.of(bookDto);
        when(bookService.findAll()).thenReturn(Flux.fromIterable(books));
        webTestClient.get().uri("/api/v1/books")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(BookDto.class)
                .hasSize(1)
                .contains(bookDto);
        verify(bookService, times(1)).findAll();
    }

    @DisplayName("должен возвращать книгу по существующему ID")
    @Test
    void whenGetBookById_thenReturnsBook() {
        when(bookService.findById(PRESENT_ID)).thenReturn(Mono.just(bookDto));
        webTestClient.get().uri("/api/v1/books/{id}", PRESENT_ID)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(BookDto.class)
                .isEqualTo(bookDto);
        verify(bookService, times(1)).findById(PRESENT_ID);
    }

    @DisplayName("должен возвращать 404 при попытке получить несуществующую книгу")
    @Test
    void whenGetNonExistentBookById_thenReturnsNotFound() {
        when(bookService.findById(MISSING_ID))
                .thenReturn(Mono.error(new EntityNotFoundException("Book not found")));
        webTestClient.get().uri("/api/v1/books/{id}", MISSING_ID)
                .exchange()
                .expectStatus().isNotFound();
        verify(bookService, times(1)).findById(MISSING_ID);
    }

    @DisplayName("должен успешно создать новую книгу с валидными данными")
    @Test
    void whenCreateValidBook_thenReturnsCreatedBook() {
        when(bookService.insert(anyString(), anyLong(), anySet()))
                .thenReturn(Mono.just(bookDto));
        webTestClient.post().uri("/api/v1/books")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createUpdateBookDto)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(BookDto.class)
                .isEqualTo(bookDto);
        verify(bookService, times(1)).insert(createUpdateBookDto.title(),
                createUpdateBookDto.authorId(), Set.copyOf(createUpdateBookDto.genreIds()));
    }

    @DisplayName("должен возвращать 400 при создании книги с невалидными данными")
    @Test
    void whenCreateInvalidBook_thenReturnsBadRequest() {
        CreateUpdateBookDto invalidBookDto = CreateUpdateBookDto.builder()
                .title("")
                .authorId(PRESENT_ID)
                .genreIds(List.of(PRESENT_ID))
                .build();
        webTestClient.post().uri("/api/v1/books")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidBookDto)
                .exchange()
                .expectStatus().isBadRequest();
        verify(bookService, never()).insert(anyString(), anyLong(), anySet());
    }

    @DisplayName("должен успешно обновить существующую книгу с валидными данными")
    @Test
    void whenUpdateValidBook_thenReturnsUpdatedBook() {
        when(bookService.update(eq(PRESENT_ID), anyString(), anyLong(), anySet()))
                .thenReturn(Mono.just(bookDto));
        webTestClient.put().uri("/api/v1/books/{id}", PRESENT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createUpdateBookDto)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(BookDto.class)
                .isEqualTo(bookDto);
        verify(bookService, times(1)).update(eq(PRESENT_ID), eq(createUpdateBookDto.title()),
                eq(createUpdateBookDto.authorId()), anySet());
    }

    @DisplayName("должен возвращать 400 при обновлении книги с невалидными данными")
    @Test
    void whenUpdateInvalidBook_thenReturnsBadRequest() {
        CreateUpdateBookDto invalidBookDto = CreateUpdateBookDto.builder()
                .title("")
                .authorId(PRESENT_ID)
                .genreIds(List.of(PRESENT_ID))
                .build();
        webTestClient.put().uri("/api/v1/books/{id}", PRESENT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidBookDto)
                .exchange()
                .expectStatus().isBadRequest();
        verify(bookService, never()).update(anyLong(), anyString(), anyLong(), anySet());
    }

    @DisplayName("должен успешно удалить существующую книгу")
    @Test
    void whenDeleteExistingBook_thenReturnsNoContent() {
        when(bookService.deleteById(PRESENT_ID)).thenReturn(Mono.empty());
        webTestClient.delete().uri("/api/v1/books/{id}", PRESENT_ID)
                .exchange()
                .expectStatus().isNoContent();
        verify(bookService, times(1)).deleteById(PRESENT_ID);
    }

    @DisplayName("должен возвращать 404 при попытке удаления несуществующей книги")
    @Test
    void whenDeleteNonExistentBook_thenReturnsNotFound() {
        when(bookService.deleteById(MISSING_ID))
                .thenReturn(Mono.error(new EntityNotFoundException("Book not found")));
        webTestClient.delete().uri("/api/v1/books/{id}", MISSING_ID)
                .exchange()
                .expectStatus().isNotFound();
        verify(bookService, times(1)).deleteById(MISSING_ID);
    }

    @DisplayName("должен возвращать список всех авторов")
    @Test
    void whenGetAllAuthors_thenReturnsAuthorsList() {
        List<AuthorDto> authors = List.of(AuthorDto.builder().id(PRESENT_ID).fullName("Author_1").build());
        when(authorService.findAll()).thenReturn(Flux.fromIterable(authors));
        webTestClient.get().uri("/api/v1/authors")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(AuthorDto.class)
                .hasSize(1)
                .contains(authors.get(0));

        verify(authorService, times(1)).findAll();
    }

    @DisplayName("должен возвращать список всех жанров")
    @Test
    void whenGetAllGenres_thenReturnsGenresList() {
        List<GenreDto> genres = List.of(GenreDto.builder().id(PRESENT_ID).name("Genre_1").build());
        when(genreService.findAll()).thenReturn(Flux.fromIterable(genres));
        webTestClient.get().uri("/api/v1/genres")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(GenreDto.class)
                .hasSize(1)
                .contains(genres.get(0));
        verify(genreService, times(1)).findAll();
    }
}