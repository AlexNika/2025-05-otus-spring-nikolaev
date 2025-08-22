package ru.otus.hw.controllers.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("REST контроллер для работы с книгами ")
@WebMvcTest(BookRestController.class)
class BookRestControllerTest {

    private static final Long MISSING_ID = 42L;
    private static final Long PRESENT_ID = 1L;
    private static final Long PRESENT_ID2 = 2L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
        List<GenreDto> genreDtos = List.of(GenreDto.builder().id(PRESENT_ID).name("Genre_1").build());

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
    void whenGetAllBooks_thenReturnsBooksList() throws Exception {
        //Given
        List<BookDto> books = List.of(bookDto);

        //When
        when(bookService.findAll()).thenReturn(books);

        //Then
        mockMvc.perform(get("/api/v1/books"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(PRESENT_ID))
                .andExpect(jsonPath("$[0].title").value("Test_Book"));

        verify(bookService, times(1)).findAll();
    }

    @DisplayName("должен возвращать книгу по существующему ID")
    @Test
    void whenGetBookById_thenReturnsBook() throws Exception {
        //When
        when(bookService.findById(PRESENT_ID)).thenReturn(java.util.Optional.of(bookDto));

        //Then
        mockMvc.perform(get("/api/v1/books/{id}", PRESENT_ID))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(PRESENT_ID))
                .andExpect(jsonPath("$.title").value("Test_Book"));

        verify(bookService, times(1)).findById(PRESENT_ID);
    }

    @DisplayName("должен возвращать 404 при попытке получить несуществующую книгу")
    @Test
    void whenGetNonExistentBookById_thenReturnsNotFound() throws Exception {
        //When
        when(bookService.findById(MISSING_ID)).thenReturn(java.util.Optional.empty());

        //Then
        mockMvc.perform(get("/api/v1/books/{id}", MISSING_ID))
                .andExpect(status().isNotFound());

        verify(bookService, times(1)).findById(MISSING_ID);
    }

    @DisplayName("должен успешно создать новую книгу с валидными данными")
    @Test
    void whenCreateValidBook_thenReturnsCreatedBook() throws Exception {
        //When
        when(bookService.insert(anyString(), anyLong(), anySet()))
                .thenReturn(bookDto);

        //Then
        mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUpdateBookDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(PRESENT_ID))
                .andExpect(jsonPath("$.title").value("Test_Book"));

        verify(bookService, times(1)).insert(createUpdateBookDto.title(),
                createUpdateBookDto.authorId(), Set.copyOf(createUpdateBookDto.genreIds()));
    }

    @DisplayName("должен возвращать 400 при создании книги с невалидными данными")
    @Test
    void whenCreateInvalidBook_thenReturnsBadRequest() throws Exception {
        //Given
        CreateUpdateBookDto invalidBookDto = CreateUpdateBookDto.builder()
                .title("")
                .authorId(PRESENT_ID)
                .genreIds(List.of(PRESENT_ID))
                .build();

        //Then
        mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidBookDto)))
                .andExpect(status().isBadRequest());

        verify(bookService, never()).insert(anyString(), anyLong(), anySet());
    }

    @DisplayName("должен успешно обновить существующую книгу с валидными данными")
    @Test
    void whenUpdateValidBook_thenReturnsUpdatedBook() throws Exception {
        //When
        when(bookService.update(eq(PRESENT_ID), anyString(), anyLong(), anySet()))
                .thenReturn(bookDto);

        //Then
        mockMvc.perform(put("/api/v1/books/{id}", PRESENT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUpdateBookDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(PRESENT_ID))
                .andExpect(jsonPath("$.title").value("Test_Book"));

        verify(bookService, times(1)).update(eq(PRESENT_ID), eq(createUpdateBookDto.title()),
                eq(createUpdateBookDto.authorId()), anySet());
    }

    @DisplayName("должен возвращать 400 при обновлении книги с невалидными данными")
    @Test
    void whenUpdateInvalidBook_thenReturnsBadRequest() throws Exception {
        //Given
        CreateUpdateBookDto invalidBookDto = CreateUpdateBookDto.builder()
                .title("")
                .authorId(PRESENT_ID)
                .genreIds(List.of(PRESENT_ID))
                .build();

        //Then
        mockMvc.perform(put("/api/v1/books/{id}", PRESENT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidBookDto)))
                .andExpect(status().isBadRequest());

        verify(bookService, never()).update(anyLong(), anyString(), anyLong(), anySet());
    }

    @DisplayName("должен успешно удалить существующую книгу")
    @Test
    void whenDeleteExistingBook_thenReturnsNoContent() throws Exception {
        //When
        doNothing().when(bookService).deleteById(PRESENT_ID);

        //Then
        mockMvc.perform(delete("/api/v1/books/{id}", PRESENT_ID))
                .andExpect(status().isNoContent());

        verify(bookService, times(1)).deleteById(PRESENT_ID);
    }

    @DisplayName("должен возвращать 404 при попытке удаления несуществующей книги")
    @Test
    void whenDeleteNonExistentBook_thenReturnsNotFound() throws Exception {
        //When
        doThrow(new EntityNotFoundException("Book not found")).when(bookService).deleteById(MISSING_ID);

        //Then
        mockMvc.perform(delete("/api/v1/books/{id}", MISSING_ID))
                .andExpect(status().isNotFound());

        verify(bookService, times(1)).deleteById(MISSING_ID);
    }

    @DisplayName("должен возвращать список всех авторов")
    @Test
    void whenGetAllAuthors_thenReturnsAuthorsList() throws Exception {
        //Given
        List<AuthorDto> authors = List.of(AuthorDto.builder().id(PRESENT_ID).fullName("Author_1").build());

        //When
        when(authorService.findAll()).thenReturn(authors);

        //Then
        mockMvc.perform(get("/api/v1/books/authors"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(PRESENT_ID))
                .andExpect(jsonPath("$[0].fullName").value("Author_1"));

        verify(authorService, times(1)).findAll();
    }

    @DisplayName("должен возвращать список всех жанров")
    @Test
    void whenGetAllGenres_thenReturnsGenresList() throws Exception {
        //Given
        List<GenreDto> genres = List.of(GenreDto.builder().id(PRESENT_ID).name("Genre_1").build());

        //When
        when(genreService.findAll()).thenReturn(genres);

        //Then
        mockMvc.perform(get("/api/v1/books/genres"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(PRESENT_ID))
                .andExpect(jsonPath("$[0].name").value("Genre_1"));

        verify(genreService, times(1)).findAll();
    }
}