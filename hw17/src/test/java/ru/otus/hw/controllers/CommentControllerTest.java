package ru.otus.hw.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.services.BookService;
import ru.otus.hw.services.CommentService;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
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

@DisplayName("Контроллер для работы с комментариями")
@WebMvcTest(CommentController.class)
@WithMockUser(username = "admin")
class CommentControllerTest {

    private static final Long MISSING_ID = 42L;
    private static final Long PRESENT_ID = 1L;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CommentService commentService;

    @MockitoBean
    private BookService bookService;

    private BookDto testBookDto;

    @BeforeEach
    void setUp() {
        testBookDto = BookDto.builder().id(PRESENT_ID).title("Book Title").build();
    }

    @DisplayName("должен отображать список всех комментариев для существующей книги")
    @Test
    void whenGetCommentsByExistingBookId_thenReturnsCommentsView() throws Exception {
        //Given - PRESENT_ID
        List<CommentDto> comments = List.of(CommentDto.builder().id(PRESENT_ID).text("Great book!").build());

        //When
        when(bookService.findById(PRESENT_ID)).thenReturn(Optional.of(testBookDto));
        when(commentService.findByBookId(PRESENT_ID)).thenReturn(comments);

        //Then
        mockMvc.perform(get("/books/{bookId}/comments", PRESENT_ID)
                        .header(HttpHeaders.REFERER, "/books"))
                .andExpect(status().isOk())
                .andExpect(view().name("comments"))
                .andExpect(model().attribute("id", PRESENT_ID))
                .andExpect(model().attribute("bookTitle", "Book Title"))
                .andExpect(model().attribute("comments", comments))
                .andExpect(model().attribute("previousUrl", "/books"));

        verify(bookService, times(1)).findById(PRESENT_ID);
        verify(commentService, times(1)).findByBookId(PRESENT_ID);
    }

    @DisplayName("должен возвращать 404 при попытке получить комментарии для несуществующей книги")
    @Test
    void whenGetCommentsByNonExistentBookId_thenReturnsNotFound() throws Exception {
        //Given - book PRESENT_ID

        //When
        when(bookService.findById(PRESENT_ID)).thenReturn(Optional.empty());

        //Then
        mockMvc.perform(get("/books/{bookId}/comments", PRESENT_ID))
                .andExpect(status().isNotFound());

        verify(bookService, times(1)).findById(PRESENT_ID);
        verify(commentService, never()).findByBookId(anyLong());
    }

    @DisplayName("должен возвращать 404 при попытке получить несуществующий комментарий")
    @Test
    void whenGetNonExistentCommentById_thenReturnsNotFound() throws Exception {
        //Given - book PRESENT_ID, comment MISSING_ID

        //When
        when(commentService.findById(MISSING_ID)).thenReturn(Optional.empty());

        //Then
        mockMvc.perform(get("/books/{bookId}/comments/{commentId}/details", PRESENT_ID, MISSING_ID))
                .andExpect(status().isNotFound());

        verify(commentService, times(1)).findById(MISSING_ID);
    }

    @DisplayName("должен отображать форму добавления нового комментария")
    @Test
    void whenShowAddCommentFormForExistingBook_thenReturnsCommentUpsertView() throws Exception {
        //Given - book PRESENT_ID

        //When
        when(bookService.findById(PRESENT_ID)).thenReturn(Optional.of(testBookDto));

        //Then
        mockMvc.perform(get("/books/{bookId}/comments/add", PRESENT_ID)
                        .header(HttpHeaders.REFERER, "/books/1/comments"))
                .andExpect(status().isOk())
                .andExpect(view().name("comment-upsert"))
                .andExpect(model().attributeExists("comment"))
                .andExpect(model().attribute("formAction", "/books/1/comments/add"))
                .andExpect(model().attribute("formTitle", "Создание нового комментария"))
                .andExpect(model().attribute("previousUrl", "/books/1/comments"));

        verify(bookService, times(1)).findById(PRESENT_ID);
    }

    @DisplayName("должен возвращать 404 при попытке добавить комментарий для несуществующей книги")
    @Test
    void whenShowAddCommentFormForNonExistentBook_thenReturnsNotFound() throws Exception {
        //Given - book MISSING_ID

        //When
        when(bookService.findById(MISSING_ID)).thenReturn(Optional.empty());

        //Then
        mockMvc.perform(get("/books/{bookId}/comments/add", MISSING_ID))
                .andExpect(status().isNotFound());

        verify(bookService, times(1)).findById(MISSING_ID);
    }

    @DisplayName("должен отображать форму редактирования существующего комментария")
    @Test
    void whenShowEditCommentFormForExistingComment_thenReturnsCommentUpsertView() throws Exception {
        //Given - book PRESEN_ID, comment PRESENT_ID
        CommentDto commentDto = CommentDto.builder().id(PRESENT_ID).text("Old comment").build();

        //When
        when(bookService.findById(PRESENT_ID)).thenReturn(Optional.of(testBookDto));
        when(commentService.findById(PRESENT_ID)).thenReturn(Optional.of(commentDto));

        //Then
        mockMvc.perform(get("/books/{bookId}/comments/{commentId}/edit", PRESENT_ID, PRESENT_ID)
                        .header(HttpHeaders.REFERER, "/books/1/comments"))
                .andExpect(status().isOk())
                .andExpect(view().name("comment-upsert"))
                .andExpect(model().attribute("comment", commentDto))
                .andExpect(model().attribute("formAction", "/books/1/comments/1/edit"))
                .andExpect(model().attribute("formTitle", "Редактирование комментария"))
                .andExpect(model().attribute("previousUrl", "/books/1/comments"));

        verify(bookService, times(1)).findById(PRESENT_ID);
        verify(commentService, times(1)).findById(PRESENT_ID);
    }

    @DisplayName("должен возвращать 404 при попытке редактировать комментарий несуществующей книги")
    @Test
    void whenShowEditCommentFormForNonExistentBook_thenReturnsNotFound() throws Exception {
        //Given - book MISSING_ID, comment PRESENT_ID

        //When
        when(bookService.findById(MISSING_ID)).thenReturn(Optional.empty());

        //Then
        mockMvc.perform(get("/books/{bookId}/comments/{commentId}/edit", MISSING_ID, PRESENT_ID))
                .andExpect(status().isNotFound());

        verify(bookService, times(1)).findById(MISSING_ID);
    }

    @DisplayName("должен возвращать 404 при попытке редактировать несуществующий комментарий")
    @Test
    void whenShowEditCommentFormForNonExistentComment_thenReturnsNotFound() throws Exception {
        //Given - book PRESENT_ID, comment MISSING_ID
        BookDto bookDto = BookDto.builder().id(PRESENT_ID).title("Book Title").build();

        //When
        when(bookService.findById(PRESENT_ID)).thenReturn(Optional.of(bookDto));
        when(commentService.findById(MISSING_ID)).thenReturn(Optional.empty());

        //Then
        mockMvc.perform(get("/books/{bookId}/comments/{commentId}/edit", PRESENT_ID, MISSING_ID))
                .andExpect(status().isNotFound());

        verify(bookService, times(1)).findById(PRESENT_ID);
        verify(commentService, times(1)).findById(MISSING_ID);
    }

    @DisplayName("должен успешно создать новый комментарий с валидным текстом")
    @Test
    void whenCreateValidComment_thenRedirectsToComments() throws Exception {
        //Given - book PRESENT_ID
        String text = "New comment";

        //Then
        mockMvc.perform(post("/books/{bookId}/comments/add", PRESENT_ID)
                        .param("text", text)
                        .header(HttpHeaders.REFERER, "/books/1/comments")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books/" + PRESENT_ID + "/comments"));

        verify(commentService, times(1)).insert(text, PRESENT_ID);
    }

    @DisplayName("должен отображать форму с ошибками при создании комментария с пустым текстом")
    @Test
    void whenCreateCommentWithEmptyText_thenReturnsCommentUpsertViewWithError() throws Exception {
        //Given - book PRESENT_ID, comment text - invalid

        //Then
        mockMvc.perform(post("/books/{bookId}/comments/add", PRESENT_ID)
                        .param("text", "")
                        .header(HttpHeaders.REFERER, "/books/1/comments")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("comment-upsert"))
                .andExpect(model().attributeHasErrors("comment"))
                .andExpect(model().attribute("formAction", "/books/1/comments/add"))
                .andExpect(model().attribute("formTitle", "Создание нового комментария"));

        verify(commentService, never()).insert(anyString(), anyLong());
    }

    @DisplayName("должен отображать форму с ошибкой при возникновении исключения при создании комментария")
    @Test
    void whenCreateCommentThrowsException_thenReturnsCommentUpsertViewWithError() throws Exception {
        //Given - book PRESENT_ID
        Long bookId = 1L;
        String text = "New comment";
        String errorMessage = "Service error";

        //When
        doThrow(new RuntimeException(errorMessage)).when(commentService).insert(text, PRESENT_ID);

        //Then
        mockMvc.perform(post("/books/{bookId}/comments/add", bookId)
                        .param("text", text)
                        .header(HttpHeaders.REFERER, "/books/1/comments")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("comment-upsert"))
                .andExpect(model().attributeExists("errorMessage"));

        verify(commentService, times(1)).insert(text, bookId);
    }

    @DisplayName("должен успешно обновить существующий комментарий с валидным текстом")
    @Test
    void whenUpdateCommentWithValidText_thenRedirectsToComments() throws Exception {
        //Given - book PRESEN_ID, comment PRESENT_ID
        String text = "Updated comment";

        //Then
        mockMvc.perform(post("/books/{bookId}/comments/{commentId}/edit", PRESENT_ID, PRESENT_ID)
                        .param("text", text)
                        .header(HttpHeaders.REFERER, "/books/1/comments")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books/" + PRESENT_ID + "/comments"));

        verify(commentService, times(1)).update(PRESENT_ID, text);
    }

    @DisplayName("должен отображать форму с ошибками при обновлении комментария с пустым текстом")
    @Test
    void whenUpdateCommentWithEmptyText_thenReturnsCommentUpsertViewWithError() throws Exception {
        //Given - book PRESEN_ID, comment PRESENT_ID, comment text - invalid

        //Then
        mockMvc.perform(post("/books/{bookId}/comments/{commentId}/edit", PRESENT_ID, PRESENT_ID)
                        .param("text", "")
                        .header(HttpHeaders.REFERER, "/books/1/comments")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("comment-upsert"))
                .andExpect(model().attributeHasErrors("comment"))
                .andExpect(model().attribute("formAction", "/books/1/comments/1/edit"))
                .andExpect(model().attribute("formTitle", "Редактирование комментария"));

        verify(commentService, never()).update(anyLong(), anyString());
    }

    @DisplayName("должен отображать форму с ошибкой при возникновении исключения при обновлении комментария")
    @Test
    void whenUpdateCommentThrowsException_thenReturnsCommentUpsertViewWithError() throws Exception {
        //Given - book PRESENT_ID, comment PRESENT_ID
        String text = "Updated comment";
        String errorMessage = "Service error";

        //When
        doThrow(new RuntimeException(errorMessage)).when(commentService).update(PRESENT_ID, text);

        //Then
        mockMvc.perform(post("/books/{bookId}/comments/{commentId}/edit", PRESENT_ID, PRESENT_ID)
                        .param("text", text)
                        .header(HttpHeaders.REFERER, "/books/1/comments")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("comment-upsert"))
                .andExpect(model().attributeExists("errorMessage"));

        verify(commentService, times(1)).update(PRESENT_ID, text);
    }

    @DisplayName("должен успешно удалить существующий комментарий")
    @Test
    void whenDeleteExistingComment_thenRedirectsToReferer() throws Exception {
        //Given - book PRESENT_ID, comment PRESENT_ID
        String referrer = "/books/1/comments";

        //When
        doNothing().when(commentService).deleteById(PRESENT_ID);

        //Then
        mockMvc.perform(delete("/books/{bookId}/comments/{commentId}/delete", PRESENT_ID, PRESENT_ID)
                        .header(HttpHeaders.REFERER, referrer)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(referrer));

        verify(commentService, times(1)).deleteById(PRESENT_ID);
    }
}