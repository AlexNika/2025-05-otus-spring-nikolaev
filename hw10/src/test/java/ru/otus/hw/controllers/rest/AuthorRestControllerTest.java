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
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.services.AuthorService;
import ru.otus.hw.services.BookService;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
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

@DisplayName("REST контроллер для работы с авторами ")
@WebMvcTest(AuthorRestController.class)
class AuthorRestControllerTest {

    private static final Long MISSING_ID = 42L;
    private static final Long PRESENT_ID = 1L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthorService authorService;

    @MockitoBean
    private BookService bookService;

    private AuthorDto authorDto;

    @BeforeEach
    void setUp() {
        authorDto = AuthorDto.builder().id(PRESENT_ID).fullName("Author_1").build();
    }

    @DisplayName("должен возвращать список всех авторов")
    @Test
    void whenGetAllAuthors_thenReturnsAuthorsList() throws Exception {
        //Given
        List<AuthorDto> authors = List.of(authorDto);

        //When
        when(authorService.findAll()).thenReturn(authors);

        //Then
        mockMvc.perform(get("/api/v1/authors"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(PRESENT_ID))
                .andExpect(jsonPath("$[0].fullName").value("Author_1"));

        verify(authorService, times(1)).findAll();
    }

    @DisplayName("должен возвращать автора по существующему ID")
    @Test
    void whenGetAuthorById_thenReturnsAuthor() throws Exception {
        //When
        when(authorService.findById(PRESENT_ID)).thenReturn(java.util.Optional.of(authorDto));

        //Then
        mockMvc.perform(get("/api/v1/authors/{id}", PRESENT_ID))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(PRESENT_ID))
                .andExpect(jsonPath("$.fullName").value("Author_1"));

        verify(authorService, times(1)).findById(PRESENT_ID);
    }

    @DisplayName("должен возвращать 404 при попытке получить несуществующего автора")
    @Test
    void whenGetNonExistentAuthorById_thenReturnsNotFound() throws Exception {
        //When
        when(authorService.findById(MISSING_ID)).thenReturn(java.util.Optional.empty());

        //Then
        mockMvc.perform(get("/api/v1/authors/{id}", MISSING_ID))
                .andExpect(status().isNotFound());

        verify(authorService, times(1)).findById(MISSING_ID);
    }

    @DisplayName("должен возвращать книги автора по существующему ID автора")
    @Test
    void whenGetAuthorBooksWithValidAuthorId_thenReturnsBooksList() throws Exception {
        //Given
        List<BookDto> books = List.of(BookDto.builder().id(PRESENT_ID).title("Book_1").build());

        //When
        when(authorService.findById(PRESENT_ID)).thenReturn(java.util.Optional.of(authorDto));
        when(bookService.findBooksByAuthorId(PRESENT_ID)).thenReturn(books);

        //Then
        mockMvc.perform(get("/api/v1/authors/{id}/books", PRESENT_ID))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(PRESENT_ID))
                .andExpect(jsonPath("$[0].title").value("Book_1"));

        verify(authorService, times(1)).findById(PRESENT_ID);
        verify(bookService, times(1)).findBooksByAuthorId(PRESENT_ID);
    }

    @DisplayName("должен возвращать 404 при попытке получить книги несуществующего автора")
    @Test
    void whenGetAuthorBooksWithInvalidAuthorId_thenReturnsNotFound() throws Exception {
        //When
        when(authorService.findById(MISSING_ID)).thenReturn(java.util.Optional.empty());

        //Then
        mockMvc.perform(get("/api/v1/authors/{id}/books", MISSING_ID))
                .andExpect(status().isNotFound());

        verify(authorService, times(1)).findById(MISSING_ID);
        verify(bookService, never()).findBooksByAuthorId(anyLong());
    }

    @DisplayName("должен успешно создать нового автора с валидными данными")
    @Test
    void whenCreateValidAuthor_thenReturnsCreatedAuthor() throws Exception {
        //Given
        AuthorDto newAuthorDto = new AuthorDto(null, "New Author");
        AuthorDto createdAuthorDto = new AuthorDto(PRESENT_ID, "New Author");

        //When
        when(authorService.insert(anyString())).thenReturn(createdAuthorDto);

        //Then
        mockMvc.perform(post("/api/v1/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newAuthorDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(PRESENT_ID))
                .andExpect(jsonPath("$.fullName").value("New Author"));

        verify(authorService, times(1)).insert("New Author");
    }

    @DisplayName("должен возвращать 400 при создании автора с невалидными данными")
    @Test
    void whenCreateInvalidAuthor_thenReturnsBadRequest() throws Exception {
        //Given
        AuthorDto invalidAuthorDto = new AuthorDto(null, "");

        //Then
        mockMvc.perform(post("/api/v1/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidAuthorDto)))
                .andExpect(status().isBadRequest());

        verify(authorService, never()).insert(anyString());
    }

    @DisplayName("должен успешно обновить существующего автора с валидными данными")
    @Test
    void whenUpdateValidAuthor_thenReturnsUpdatedAuthor() throws Exception {
        //Given
        AuthorDto updatedAuthorDto = new AuthorDto(PRESENT_ID, "Updated Author");

        //When
        when(authorService.update(anyLong(), anyString())).thenReturn(updatedAuthorDto);

        //Then
        mockMvc.perform(put("/api/v1/authors/{id}", PRESENT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedAuthorDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(PRESENT_ID))
                .andExpect(jsonPath("$.fullName").value("Updated Author"));

        verify(authorService, times(1)).update(PRESENT_ID, "Updated Author");
    }

    @DisplayName("должен возвращать 400 при обновлении автора с невалидными данными")
    @Test
    void whenUpdateInvalidAuthor_thenReturnsBadRequest() throws Exception {
        //Given
        AuthorDto invalidAuthorDto = new AuthorDto(PRESENT_ID, "");

        //Then
        mockMvc.perform(put("/api/v1/authors/{id}", PRESENT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidAuthorDto)))
                .andExpect(status().isBadRequest());

        verify(authorService, never()).update(anyLong(), anyString());
    }

    @DisplayName("должен успешно удалить существующего автора")
    @Test
    void whenDeleteExistingAuthor_thenReturnsNoContent() throws Exception {
        //When
        doNothing().when(authorService).deleteById(PRESENT_ID);

        //Then
        mockMvc.perform(delete("/api/v1/authors/{id}", PRESENT_ID))
                .andExpect(status().isOk());

        verify(authorService).deleteById(PRESENT_ID);
    }

    @DisplayName("должен возвращать 404 при попытке удаления несуществующего автора")
    @Test
    void whenDeleteNonExistentAuthor_thenReturnsNotFound() throws Exception {
        //When
        doThrow(new EntityNotFoundException("Author not found")).when(authorService).deleteById(MISSING_ID);

        //Then
        mockMvc.perform(delete("/api/v1/authors/{id}", MISSING_ID))
                .andExpect(status().isNotFound());

        verify(authorService, times(1)).deleteById(MISSING_ID);
    }
}