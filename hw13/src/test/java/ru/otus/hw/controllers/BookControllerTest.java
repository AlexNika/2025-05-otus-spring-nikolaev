package ru.otus.hw.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.dto.CreateUpdateBookDto;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.dto.mapper.BookMapper;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.services.AuthorService;
import ru.otus.hw.services.BookService;
import ru.otus.hw.services.CommentService;
import ru.otus.hw.services.GenreService;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@DisplayName("Контроллер для работы с книгами ")
@WebMvcTest(BookController.class)
@WithMockUser(username = "admin")
class BookControllerTest {

    private static final Long MISSING_ID = 42L;
    private static final Long PRESENT_ID = 1L;
    private static final Long PRESENT_ID2 = 2L;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookService bookService;

    @MockitoBean
    private AuthorService authorService;

    @MockitoBean
    private GenreService genreService;

    @MockitoBean
    private CommentService commentService;

    @MockitoBean
    private BookMapper bookMapper;

    private BookDto bookDto;
    private CreateUpdateBookDto createUpdateBookDto;
    private List<AuthorDto> authors;
    private List<GenreDto> genres;
    private List<CommentDto> comments;

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

        authors = List.of(new AuthorDto(PRESENT_ID, "Author_1"));
        genres = List.of(new GenreDto(PRESENT_ID, "Genre_1"));
        comments = List.of(CommentDto.builder().id(PRESENT_ID).text("Test_Comment").build());
    }

    @DisplayName("должен отображать список всех книг")
    @Test
    void whenGetAllBooks_thenReturnsBooksView() throws Exception {
        //Given
        List<BookDto> books = List.of(bookDto);

        //When
        when(bookService.findAll()).thenReturn(books);

        //Then
        mockMvc.perform(get("/books"))
                .andExpect(status().isOk())
                .andExpect(view().name("books"))
                .andExpect(model().attribute("books", books));

        verify(bookService, times(1)).findAll();
    }

    @DisplayName("должен отображать детали книги по существующему ID")
    @Test
    void whenGetBookById_thenReturnsBookView() throws Exception {
        //When
        when(bookService.findById(PRESENT_ID)).thenReturn(Optional.of(bookDto));
        when(commentService.findByBookId(PRESENT_ID)).thenReturn(comments);
        when(authorService.findAll()).thenReturn(authors);
        when(genreService.findAll()).thenReturn(genres);

        //Then
        mockMvc.perform(get("/books/{id}/details", PRESENT_ID))
                .andExpect(status().isOk())
                .andExpect(view().name("book-view"))
                .andExpect(model().attribute("book", bookDto))
                .andExpect(model().attribute("comments", comments))
                .andExpect(model().attribute("authors", authors))
                .andExpect(model().attribute("genres", genres));

        verify(bookService, times(1)).findById(PRESENT_ID);
        verify(commentService, times(1)).findByBookId(PRESENT_ID);
        verify(authorService, times(1)).findAll();
        verify(genreService, times(1)).findAll();
    }

    @DisplayName("должен возвращать 404 при попытке получить несуществующую книгу")
    @Test
    void whenGetNonExistentBookById_thenReturnsNotFound() throws Exception {
        //Given

        //When
        when(bookService.findById(MISSING_ID)).thenReturn(Optional.empty());

        //Then
        mockMvc.perform(get("/books/{id}/details", MISSING_ID))
                .andExpect(status().isNotFound());

        verify(bookService, times(1)).findById(MISSING_ID);
        verify(commentService, never()).findByBookId(anyLong());
    }

    @DisplayName("должен отображать форму добавления новой книги")
    @Test
    void whenShowAddBookForm_thenReturnsBookUpsertView() throws Exception {
        //When
        when(authorService.findAll()).thenReturn(authors);
        when(genreService.findAll()).thenReturn(genres);

        //Then
        mockMvc.perform(get("/books/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("book-upsert"))
                .andExpect(model().attributeExists("book"))
                .andExpect(model().attribute("formTitle", "Создание новой книги"))
                .andExpect(model().attribute("formAction", "/books/add"))
                .andExpect(model().attribute("authors", authors))
                .andExpect(model().attribute("genres", genres));

        verify(authorService, times(1)).findAll();
        verify(genreService, times(1)).findAll();
    }

    @DisplayName("должен отображать форму редактирования существующей книги")
    @Test
    void whenShowEditBookFormWithValidId_thenReturnsBookUpsertView() throws Exception {
        //When
        when(bookService.findById(PRESENT_ID)).thenReturn(Optional.of(bookDto));
        when(bookMapper.toCreateUpdateBookDto(bookDto)).thenReturn(createUpdateBookDto);
        when(authorService.findAll()).thenReturn(authors);
        when(genreService.findAll()).thenReturn(genres);

        //Then
        mockMvc.perform(get("/books/{id}/edit", PRESENT_ID))
                .andExpect(status().isOk())
                .andExpect(view().name("book-upsert"))
                .andExpect(model().attribute("book", createUpdateBookDto))
                .andExpect(model().attribute("formTitle", "Редактирование книги"))
                .andExpect(model().attribute("formAction", "/books/" + PRESENT_ID + "/edit"))
                .andExpect(model().attribute("authors", authors))
                .andExpect(model().attribute("genres", genres));

        verify(bookService, times(1)).findById(PRESENT_ID);
        verify(bookMapper, times(1)).toCreateUpdateBookDto(bookDto);
        verify(authorService, times(1)).findAll();
        verify(genreService, times(1)).findAll();
    }

    @DisplayName("должен возвращать 404 при попытке редактирования несуществующей книги")
    @Test
    void whenShowEditBookFormWithInvalidId_thenReturnsNotFound() throws Exception {
        //When
        when(bookService.findById(MISSING_ID)).thenReturn(Optional.empty());

        //Then
        mockMvc.perform(get("/books/{id}/edit", MISSING_ID))
                .andExpect(status().isNotFound());

        verify(bookService, times(1)).findById(MISSING_ID);
        verify(bookMapper, never()).toCreateUpdateBookDto(any());
    }

    @DisplayName("должен успешно создать новую книгу с валидными данными")
    @Test
    void whenSaveValidBook_thenRedirectsToBooks() throws Exception {
        //When
        when(bookService.insert(anyString(), anyLong(), anySet()))
                .thenReturn(bookDto);

        //Then
        mockMvc.perform(post("/books/add")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("title", createUpdateBookDto.title())
                        .param("authorId", createUpdateBookDto.authorId().toString())
                        .param("genreIds", "1", "2")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books"));

        verify(bookService, times(1)).insert(createUpdateBookDto.title(),
                createUpdateBookDto.authorId(), Set.of(PRESENT_ID, PRESENT_ID2));
    }

    @DisplayName("должен отображать форму с ошибками при создании книги с невалидными данными")
    @Test
    void whenSaveInvalidBook_thenReturnsBookUpsertViewWithError() throws Exception {
        //Then
        mockMvc.perform(post("/books/add")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("title", "")
                        .param("authorId", "1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("book-upsert"))
                .andExpect(model().attributeHasErrors("book"))
                .andExpect(model().attribute("formTitle", "Создание новой книги"));

        verify(bookService, never()).insert(anyString(), anyLong(), anySet());
    }

    @DisplayName("должен успешно обновить существующую книгу с валидными данными")
    @Test
    void whenUpdateValidBook_thenRedirectsToBooks() throws Exception {
        //When
        when(bookService.update(eq(PRESENT_ID), anyString(), anyLong(), anySet())).thenReturn(bookDto);

        //Then
        mockMvc.perform(post("/books/{id}/edit", PRESENT_ID)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("title", createUpdateBookDto.title())
                        .param("authorId", createUpdateBookDto.authorId().toString())
                        .param("genreIds", "1", "2")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books"));

        verify(bookService, times(1)).update(eq(PRESENT_ID), eq(createUpdateBookDto.title()),
                eq(createUpdateBookDto.authorId()), anySet());
    }

    @DisplayName("должен отображать форму с ошибками при обновлении книги с невалидными данными")
    @Test
    void whenUpdateInvalidBook_thenReturnsBookUpsertViewWithError() throws Exception {
        //Then
        mockMvc.perform(post("/books/{id}/edit", PRESENT_ID)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("title", "")
                        .param("authorId", "1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("book-upsert"))
                .andExpect(model().attributeHasErrors("book"))
                .andExpect(model().attribute("formTitle", "Редактирование книги"));

        verify(bookService, never()).update(anyLong(), anyString(), anyLong(), anySet());
    }

    @DisplayName("должен отображать форму с ошибкой при возникновении исключения при сохранении книги")
    @Test
    void whenSaveBookThrowsException_thenReturnsBookUpsertViewWithError() throws Exception {
        //When
        when(bookService.insert(anyString(), anyLong(), anySet()))
                .thenThrow(new RuntimeException("Database error"));

        //Then
        mockMvc.perform(post("/books/add")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("title", createUpdateBookDto.title())
                        .param("authorId", createUpdateBookDto.authorId().toString())
                        .param("genreIds", "1", "2")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("book-upsert"))
                .andExpect(model().attributeExists("errorMessage"));

        verify(bookService, times(1)).insert(createUpdateBookDto.title(),
                createUpdateBookDto.authorId(), Set.of(PRESENT_ID, PRESENT_ID2));
    }

    @DisplayName("должен успешно удалить существующую книгу")
    @Test
    void whenDeleteExistingBook_thenRedirectsToBooks() throws Exception {
        //When
        doNothing().when(bookService).deleteById(PRESENT_ID);

        //Then
        mockMvc.perform(delete("/books/{id}/delete", PRESENT_ID)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books"));

        verify(bookService).deleteById(PRESENT_ID);
    }

    @DisplayName("должен возвращать 404 при попытке удаления несуществующей книги")
    @Test
    void whenDeleteNonExistentBook_thenReturnsNotFound() throws Exception {
        //When
        doThrow(new EntityNotFoundException("Book not found")).when(bookService).deleteById(MISSING_ID);

        //Then
        mockMvc.perform(delete("/books/{id}/delete", MISSING_ID)
                        .with(csrf()))
                .andExpect(status().isNotFound());

        verify(bookService).deleteById(MISSING_ID);
    }
}