package ru.otus.hw.controllers.page;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.services.BookService;
import ru.otus.hw.services.CommentService;

import java.util.List;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@DisplayName("MVC контроллер для работы с комментариями ")
@WebMvcTest(CommentPageController.class)
class CommentPageControllerTest {

    private static final Long MISSING_ID = 42L;
    private static final Long PRESENT_ID = 1L;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CommentService commentService;

    @MockitoBean
    private BookService bookService;

    private BookDto bookDto;
    private CommentDto commentDto;

    @BeforeEach
    void setUp() {
        bookDto = BookDto.builder().id(PRESENT_ID).title("Test_Book").build();
        commentDto = CommentDto.builder().id(PRESENT_ID).text("Test_Comment").build();
    }

    @DisplayName("должен отображать список всех комментариев по книге")
    @Test
    void whenGetAllCommentsByBookId_thenReturnsCommentsView() throws Exception {
        //Given
        List<CommentDto> comments = List.of(commentDto);

        //When
        when(bookService.findById(PRESENT_ID)).thenReturn(java.util.Optional.of(bookDto));
        when(commentService.findByBookId(PRESENT_ID)).thenReturn(comments);

        //Then
        mockMvc.perform(get("/books/{bookId}/comments", PRESENT_ID))
                .andExpect(status().isOk())
                .andExpect(view().name("comments"))
                .andExpect(model().attribute("id", PRESENT_ID))
                .andExpect(model().attribute("bookTitle", "Test_Book"))
                .andExpect(model().attribute("comments", comments));

        verify(bookService, times(1)).findById(PRESENT_ID);
        verify(commentService, times(1)).findByBookId(PRESENT_ID);
    }

    @DisplayName("должен возвращать 404 при попытке получить комментарии для несуществующей книги")
    @Test
    void whenGetAllCommentsForNonExistentBook_thenReturnsNotFound() throws Exception {
        //When
        when(bookService.findById(MISSING_ID)).thenReturn(java.util.Optional.empty());

        //Then
        mockMvc.perform(get("/books/{bookId}/comments", MISSING_ID))
                .andExpect(status().isNotFound());

        verify(bookService, times(1)).findById(MISSING_ID);
        verify(commentService, times(0)).findByBookId(MISSING_ID);
    }

    @DisplayName("должен отображать детали комментария по существующему ID")
    @Test
    void whenGetCommentById_thenReturnsCommentView() throws Exception {
        //When
        when(commentService.findById(PRESENT_ID)).thenReturn(java.util.Optional.of(commentDto));

        //Then
        mockMvc.perform(get("/books/{bookId}/comments/{commentId}/details", PRESENT_ID, PRESENT_ID))
                .andExpect(status().isOk())
                .andExpect(view().name("comment-view"))
                .andExpect(model().attribute("comment", commentDto))
                .andExpect(model().attribute("bookId", PRESENT_ID))
                .andExpect(model().attribute("commentId", PRESENT_ID));

        verify(commentService, times(1)).findById(PRESENT_ID);
    }

    @DisplayName("должен возвращать 404 при попытке получить несуществующий комментарий")
    @Test
    void whenGetNonExistentCommentById_thenReturnsNotFound() throws Exception {
        //When
        when(commentService.findById(MISSING_ID)).thenReturn(java.util.Optional.empty());

        //Then
        mockMvc.perform(get("/books/{bookId}/comments/{commentId}/details", PRESENT_ID, MISSING_ID))
                .andExpect(status().isNotFound());

        verify(commentService, times(1)).findById(MISSING_ID);
    }

    @DisplayName("должен отображать форму добавления нового комментария")
    @Test
    void whenShowAddCommentForm_thenReturnsCommentUpsertView() throws Exception {
        //When
        when(bookService.findById(PRESENT_ID)).thenReturn(java.util.Optional.of(bookDto));

        //Then
        mockMvc.perform(get("/books/{bookId}/comments/add", PRESENT_ID))
                .andExpect(status().isOk())
                .andExpect(view().name("comment-upsert"))
                .andExpect(model().attributeExists("comment"))
                .andExpect(model().attribute("formTitle", "Создание нового комментария"))
                .andExpect(model().attribute("formAction", "/books/" + PRESENT_ID + "/comments/add"));

        verify(bookService, times(1)).findById(PRESENT_ID);
    }

    @DisplayName("должен возвращать 404 при попытке добавить комментарий к несуществующей книге")
    @Test
    void whenShowAddCommentFormForNonExistentBook_thenReturnsNotFound() throws Exception {
        //When
        when(bookService.findById(MISSING_ID)).thenReturn(java.util.Optional.empty());

        //Then
        mockMvc.perform(get("/books/{bookId}/comments/add", MISSING_ID))
                .andExpect(status().isNotFound());

        verify(bookService, times(1)).findById(MISSING_ID);
    }

    @DisplayName("должен отображать форму редактирования существующего комментария")
    @Test
    void whenShowEditCommentFormWithValidId_thenReturnsCommentUpsertView() throws Exception {
        //When
        when(bookService.findById(PRESENT_ID)).thenReturn(java.util.Optional.of(bookDto));
        when(commentService.findById(PRESENT_ID)).thenReturn(java.util.Optional.of(commentDto));

        //Then
        mockMvc.perform(get("/books/{bookId}/comments/{commentId}/edit", PRESENT_ID, PRESENT_ID))
                .andExpect(status().isOk())
                .andExpect(view().name("comment-upsert"))
                .andExpect(model().attribute("comment", commentDto))
                .andExpect(model().attribute("formTitle", "Редактирование комментария"))
                .andExpect(model().attribute("formAction", "/books/" + PRESENT_ID + "/comments/" + PRESENT_ID + "/edit"));

        verify(bookService, times(1)).findById(PRESENT_ID);
        verify(commentService, times(1)).findById(PRESENT_ID);
    }

    @DisplayName("должен возвращать 404 при попытке редактировать комментарий несуществующей книги")
    @Test
    void whenShowEditCommentFormForNonExistentBook_thenReturnsNotFound() throws Exception {
        //When
        when(bookService.findById(MISSING_ID)).thenReturn(java.util.Optional.empty());

        //Then
        mockMvc.perform(get("/books/{bookId}/comments/{commentId}/edit", MISSING_ID, PRESENT_ID))
                .andExpect(status().isNotFound());

        verify(bookService, times(1)).findById(MISSING_ID);
        verify(commentService, times(0)).findById(PRESENT_ID);
    }

    @DisplayName("должен возвращать 404 при попытке редактировать несуществующий комментарий")
    @Test
    void whenShowEditCommentFormWithInvalidId_thenReturnsNotFound() throws Exception {
        //When
        when(bookService.findById(PRESENT_ID)).thenReturn(java.util.Optional.of(bookDto));
        when(commentService.findById(MISSING_ID)).thenReturn(java.util.Optional.empty());

        //Then
        mockMvc.perform(get("/books/{bookId}/comments/{commentId}/edit", PRESENT_ID, MISSING_ID))
                .andExpect(status().isNotFound());

        verify(bookService, times(1)).findById(PRESENT_ID);
        verify(commentService, times(1)).findById(MISSING_ID);
    }

    @DisplayName("должен успешно удалить существующий комментарий")
    @Test
    void whenDeleteExistingComment_thenRedirectsToReferrer() throws Exception {
        //Given
        String referrer = "/books/" + PRESENT_ID + "/comments";

        //When
        doNothing().when(commentService).deleteById(PRESENT_ID);

        //Then
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .delete("/books/{bookId}/comments/{commentId}/delete", PRESENT_ID, PRESENT_ID)
                        .header("Referer", referrer))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(referrer));

        verify(commentService, times(1)).deleteById(PRESENT_ID);
    }
}