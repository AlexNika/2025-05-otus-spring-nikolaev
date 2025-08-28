package ru.otus.hw.controllers.page;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.services.AuthorService;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@DisplayName("MVC контроллер для работы с авторами ")
@WebMvcTest(AuthorPageController.class)
class AuthorPageControllerTest {

    private static final Long MISSING_ID = 42L;
    private static final Long PRESENT_ID = 1L;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthorService authorService;

    private AuthorDto authorDto;

    @BeforeEach
    void setUp() {
        authorDto = AuthorDto.builder().id(PRESENT_ID).fullName("Author_1").build();
    }

    @DisplayName("должен отображать список всех авторов")
    @Test
    void whenGetAllAuthors_thenReturnsAuthorsView() throws Exception {
        //Then
        mockMvc.perform(get("/authors"))
                .andExpect(status().isOk())
                .andExpect(view().name("authors"));

        verify(authorService, times(0)).findAll();
    }

    @DisplayName("должен отображать детали автора по существующему ID")
    @Test
    void whenGetAuthorById_thenReturnsAuthorView() throws Exception {
        //When
        when(authorService.findById(PRESENT_ID)).thenReturn(java.util.Optional.of(authorDto));

        //Then
        mockMvc.perform(get("/authors/{id}/details", PRESENT_ID))
                .andExpect(status().isOk())
                .andExpect(view().name("author-view"))
                .andExpect(model().attribute("authorId", PRESENT_ID));

        verify(authorService, times(1)).findById(PRESENT_ID);
    }

    @DisplayName("должен возвращать 404 при попытке получить несуществующего автора")
    @Test
    void whenGetNonExistentAuthorById_thenReturnsNotFound() throws Exception {
        //When
        when(authorService.findById(MISSING_ID)).thenReturn(java.util.Optional.empty());

        //Then
        mockMvc.perform(get("/authors/{id}/details", MISSING_ID))
                .andExpect(status().isNotFound());

        verify(authorService, times(1)).findById(MISSING_ID);
    }

    @DisplayName("должен отображать форму добавления нового автора")
    @Test
    void whenShowAddAuthorForm_thenReturnsAuthorUpsertView() throws Exception {
        //Then
        mockMvc.perform(get("/authors/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("author-upsert"))
                .andExpect(model().attributeExists("author"))
                .andExpect(model().attribute("formTitle", "Создание нового автора"))
                .andExpect(model().attribute("formAction", "/api/v1/authors"))
                .andExpect(model().attribute("isUpdate", false));
    }

    @DisplayName("должен отображать форму редактирования существующего автора")
    @Test
    void whenShowEditAuthorFormWithValidId_thenReturnsAuthorUpsertView() throws Exception {
        //When
        when(authorService.findById(PRESENT_ID)).thenReturn(java.util.Optional.of(authorDto));

        //Then
        mockMvc.perform(get("/authors/{id}/edit", PRESENT_ID))
                .andExpect(status().isOk())
                .andExpect(view().name("author-upsert"))
                .andExpect(model().attribute("author", authorDto))
                .andExpect(model().attribute("formTitle", "Редактирование автора"))
                .andExpect(model().attribute("formAction", "/api/v1/authors/" + PRESENT_ID))
                .andExpect(model().attribute("isUpdate", true))
                .andExpect(model().attribute("authorId", PRESENT_ID));

        verify(authorService, times(1)).findById(PRESENT_ID);
    }

    @DisplayName("должен возвращать 404 при попытке редактирования несуществующего автора")
    @Test
    void whenShowEditAuthorFormWithInvalidId_thenReturnsNotFound() throws Exception {
        //When
        when(authorService.findById(MISSING_ID)).thenReturn(java.util.Optional.empty());

        //Then
        mockMvc.perform(get("/authors/{id}/edit", MISSING_ID))
                .andExpect(status().isNotFound());

        verify(authorService, times(1)).findById(MISSING_ID);
    }
}