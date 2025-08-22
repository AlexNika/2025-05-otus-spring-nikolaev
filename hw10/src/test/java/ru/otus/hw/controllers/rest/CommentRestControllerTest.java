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
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.dto.CommentMinDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.services.CommentService;

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

@DisplayName("REST контроллер для работы с комментариями ")
@WebMvcTest(CommentRestController.class)
class CommentRestControllerTest {

    private static final Long MISSING_ID = 42L;
    private static final Long PRESENT_ID = 1L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CommentService commentService;

    private CommentDto commentDto;
    private Book book;

    @BeforeEach
    void setUp() {
        Author author = new Author(PRESENT_ID, "Author_1");
        book = new Book(PRESENT_ID, "Test_Book", author, List.of());
        commentDto = CommentDto.builder().id(PRESENT_ID).text("Test_Comment").book(book).build();
    }

    @DisplayName("должен возвращать список всех комментариев по книге")
    @Test
    void whenGetAllCommentsByBookId_thenReturnsCommentsList() throws Exception {
        //Given
        List<CommentDto> comments = List.of(commentDto);

        //When
        when(commentService.findByBookId(PRESENT_ID)).thenReturn(comments);

        //Then
        mockMvc.perform(get("/api/v1/books/{bookId}/comments", PRESENT_ID))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(PRESENT_ID))
                .andExpect(jsonPath("$[0].text").value("Test_Comment"));

        verify(commentService, times(1)).findByBookId(PRESENT_ID);
    }

    @DisplayName("должен возвращать комментарий по существующему ID")
    @Test
    void whenGetCommentById_thenReturnsComment() throws Exception {
        //When
        when(commentService.findById(PRESENT_ID)).thenReturn(java.util.Optional.of(commentDto));

        //Then
        mockMvc.perform(get("/api/v1/books/{bookId}/comments/{commentId}", PRESENT_ID, PRESENT_ID))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(PRESENT_ID))
                .andExpect(jsonPath("$.text").value("Test_Comment"))
                .andExpect(jsonPath("$.bookId").value(PRESENT_ID))
                .andExpect(jsonPath("$.bookTitle").value("Test_Book"));

        verify(commentService, times(1)).findById(PRESENT_ID);
    }

    @DisplayName("должен возвращать 404 при попытке получить несуществующий комментарий")
    @Test
    void whenGetNonExistentCommentById_thenReturnsNotFound() throws Exception {
        //When
        when(commentService.findById(MISSING_ID)).thenReturn(java.util.Optional.empty());

        //Then
        mockMvc.perform(get("/api/v1/books/{bookId}/comments/{commentId}", PRESENT_ID, MISSING_ID))
                .andExpect(status().isNotFound());

        verify(commentService, times(1)).findById(MISSING_ID);
    }

    @DisplayName("должен успешно создать новый комментарий с валидными данными")
    @Test
    void whenCreateValidComment_thenReturnsCreatedComment() throws Exception {
        //Given
        CommentMinDto newCommentDto = new CommentMinDto(null, "New Comment");
        CommentDto createdCommentDto = CommentDto.builder().id(PRESENT_ID).text("New Comment").book(book).build();

        //When
        when(commentService.insert(anyString(), anyLong())).thenReturn(createdCommentDto);

        //Then
        mockMvc.perform(post("/api/v1/books/{bookId}/comments", PRESENT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCommentDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(PRESENT_ID))
                .andExpect(jsonPath("$.text").value("New Comment"));

        verify(commentService, times(1)).insert("New Comment", PRESENT_ID);
    }

    @DisplayName("должен возвращать 400 при создании комментария с невалидными данными")
    @Test
    void whenCreateInvalidComment_thenReturnsBadRequest() throws Exception {
        //Given
        CommentMinDto invalidCommentDto = new CommentMinDto(null, "");

        //Then
        mockMvc.perform(post("/api/v1/books/{bookId}/comments", PRESENT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidCommentDto)))
                .andExpect(status().isBadRequest());

        verify(commentService, never()).insert(anyString(), anyLong());
    }

    @DisplayName("должен успешно обновить существующий комментарий с валидными данными")
    @Test
    void whenUpdateValidComment_thenReturnsUpdatedComment() throws Exception {
        //Given
        CommentMinDto updatedCommentDto = new CommentMinDto(PRESENT_ID, "Updated Comment");
        CommentDto updatedCommentResult = CommentDto.builder().id(PRESENT_ID).text("Updated Comment").book(book).build();

        //When
        when(commentService.update(anyLong(), anyString())).thenReturn(updatedCommentResult);

        //Then
        mockMvc.perform(put("/api/v1/books/{bookId}/comments/{commentId}", PRESENT_ID, PRESENT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedCommentDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(PRESENT_ID))
                .andExpect(jsonPath("$.text").value("Updated Comment"));

        verify(commentService, times(1)).update(PRESENT_ID, "Updated Comment");
    }

    @DisplayName("должен возвращать 400 при обновлении комментария с невалидными данными")
    @Test
    void whenUpdateInvalidComment_thenReturnsBadRequest() throws Exception {
        //Given
        CommentMinDto invalidCommentDto = new CommentMinDto(PRESENT_ID, "");

        //Then
        mockMvc.perform(put("/api/v1/books/{bookId}/comments/{commentId}", PRESENT_ID, PRESENT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidCommentDto)))
                .andExpect(status().isBadRequest());

        verify(commentService, never()).update(anyLong(), anyString());
    }

    @DisplayName("должен успешно удалить существующий комментарий")
    @Test
    void whenDeleteExistingComment_thenReturnsNoContent() throws Exception {
        //When
        doNothing().when(commentService).deleteById(PRESENT_ID);

        //Then
        mockMvc.perform(delete("/api/v1/books/{bookId}/comments/{commentId}", PRESENT_ID, PRESENT_ID))
                .andExpect(status().isNoContent());

        verify(commentService, times(1)).deleteById(PRESENT_ID);
    }

    @DisplayName("должен возвращать 404 при попытке удаления несуществующего комментария")
    @Test
    void whenDeleteNonExistentComment_thenReturnsNotFound() throws Exception {
        //When
        doThrow(new EntityNotFoundException("Comment not found")).when(commentService).deleteById(MISSING_ID);

        //Then
        mockMvc.perform(delete("/api/v1/books/{bookId}/comments/{commentId}", PRESENT_ID, MISSING_ID))
                .andExpect(status().isNotFound());

        verify(commentService, times(1)).deleteById(MISSING_ID);
    }
}