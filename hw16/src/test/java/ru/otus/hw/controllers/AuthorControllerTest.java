package ru.otus.hw.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.services.AuthorService;
import ru.otus.hw.services.BookService;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.HashMap;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
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

@DisplayName("Контроллер для работы с авторами")
@WebMvcTest(AuthorController.class)
@WithMockUser(username = "admin")
class AuthorControllerTest {

    private static final Long MISSING_ID = 42L;
    private static final Long PRESENT_ID = 1L;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthorService authorService;

    @MockitoBean
    private BookService bookService;

    private AuthorDto testAuthorDto;
    private BookDto testBookDto;

    @BeforeEach
    void setUp() {
        testAuthorDto = new AuthorDto(PRESENT_ID, "Test Author");
        testBookDto = new BookDto(PRESENT_ID, "Test Book", testAuthorDto, List.of());
    }

    @DisplayName("должен отображать список всех авторов")
    @Test
    void whenGetAllAuthors_thenReturnsAuthorsView() throws Exception {
        //Given
        List<AuthorDto> authors = List.of(testAuthorDto);
        List<BookDto> books = List.of(testBookDto);

        Map<AuthorDto, Map<Long, String>> authorsAndTheirBooks = new HashMap<>();
        Map<Long, String> bookMap = new HashMap<>();
        bookMap.put(testBookDto.id(), testBookDto.title());
        authorsAndTheirBooks.put(testAuthorDto, bookMap);

        //When
        when(authorService.findAll()).thenReturn(authors);
        when(bookService.findBooksByAuthorId(PRESENT_ID)).thenReturn(books);

        //Then
        mockMvc.perform(get("/authors")
                        .header("Referer", "/books"))
                .andExpect(status().isOk())
                .andExpect(view().name("authors"))
                .andExpect(model().attribute("authorsAndTheirBooks", authorsAndTheirBooks))
                .andExpect(model().attribute("previousUrl", "/books"));

        verify(authorService, times(1)).findAll();
        verify(bookService, times(1)).findBooksByAuthorId(PRESENT_ID);
    }

    @DisplayName("должен отображать детали существующего автора")
    @Test
    void whenGetExistingAuthorById_thenReturnsAuthorView() throws Exception {
        //Given
        List<BookDto> books = List.of(testBookDto);

        //When
        when(authorService.findById(1L)).thenReturn(Optional.of(testAuthorDto));
        when(bookService.findBooksByAuthorId(1L)).thenReturn(books);

        //Then
        mockMvc.perform(get("/authors/{id}/details", PRESENT_ID)
                        .header("Referer", "/authors"))
                .andExpect(status().isOk())
                .andExpect(view().name("author-view"))
                .andExpect(model().attribute("author", testAuthorDto))
                .andExpect(model().attribute("books", books))
                .andExpect(model().attribute("previousUrl", "/authors"));

        verify(authorService, times(1)).findById(PRESENT_ID);
        verify(bookService, times(1)).findBooksByAuthorId(PRESENT_ID);
    }

    @DisplayName("должен возвращать 404 при попытке получить несуществующего автора")
    @Test
    void whenGetNonExistentAuthorById_thenReturnsNotFound() throws Exception {
        //Given - MISSING_ID

        //When
        when(authorService.findById(MISSING_ID)).thenReturn(Optional.empty());

        //Then
        mockMvc.perform(get("/authors/{id}/details", MISSING_ID))
                .andExpect(status().isNotFound());

        verify(authorService, times(1)).findById(MISSING_ID);
        verify(bookService, never()).findBooksByAuthorId(anyLong());
    }

    @DisplayName("должен отображать форму добавления нового автора")
    @Test
    void whenShowAddAuthorForm_thenReturnsAuthorUpsertView() throws Exception {

        //Then
        mockMvc.perform(get("/authors/add")
                        .header("Referer", "/authors"))
                .andExpect(status().isOk())
                .andExpect(view().name("author-upsert"))
                .andExpect(model().attribute("formAction", "/authors/add"))
                .andExpect(model().attribute("formTitle", "Создание нового автора"))
                .andExpect(model().attributeExists("author"));

        verify(authorService, never()).findById(anyLong());
    }

    @DisplayName("должен отображать форму редактирования существующего автора")
    @Test
    void whenShowEditAuthorFormForExistingAuthor_thenReturnsAuthorUpsertView() throws Exception {
        //Given - PRESENT_ID

        //When
        when(authorService.findById(PRESENT_ID)).thenReturn(Optional.of(testAuthorDto));

        //Then
        mockMvc.perform(get("/authors/{id}/edit", PRESENT_ID)
                        .header("Referer", "/authors"))
                .andExpect(status().isOk())
                .andExpect(view().name("author-upsert"))
                .andExpect(model().attribute("formAction", "/authors/1/edit"))
                .andExpect(model().attribute("formTitle", "Редактирование автора"))
                .andExpect(model().attribute("author", testAuthorDto));

        verify(authorService, times(1)).findById(PRESENT_ID);
    }

    @DisplayName("должен возвращать 404 при попытке редактировать несуществующего автора")
    @Test
    void whenShowEditAuthorFormForNonExistentAuthor_thenReturnsNotFound() throws Exception {
        //Given - MISSING_IS

        //When
        when(authorService.findById(MISSING_ID)).thenReturn(Optional.empty());

        //Then
        mockMvc.perform(get("/authors/{id}/edit", MISSING_ID))
                .andExpect(status().isNotFound());

        verify(authorService, times(1)).findById(MISSING_ID);
    }

    @DisplayName("должен успешно создать нового автора с валидным именем")
    @Test
    void whenCreateValidAuthor_thenRedirectsToAuthors() throws Exception {

        //Then
        mockMvc.perform(post("/authors/add")
                        .param("fullName", "New Author")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .header("Referer", "/authors")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/authors"));

        verify(authorService, times(1)).insert("New Author");
    }

    @DisplayName("должен отображать форму с ошибками при создании автора с пустым именем")
    @Test
    void whenCreateAuthorWithEmptyName_thenReturnsAuthorUpsertViewWithError() throws Exception {
        //Given - author full name - invalid

        //Then
        mockMvc.perform(post("/authors/add")
                        .param("fullName", "") // Пустое имя - невалидно
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .header("Referer", "/authors")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("author-upsert"))
                .andExpect(model().attributeHasFieldErrors("author", "fullName"));

        verify(authorService, never()).insert(anyString());
    }

    @DisplayName("должен успешно обновить существующего автора с валидным именем")
    @Test
    void whenUpdateAuthorWithValidName_thenRedirectsToAuthors() throws Exception {
        //Given - PRESENT_ID

        //When
        when(authorService.findById(PRESENT_ID)).thenReturn(Optional.of(testAuthorDto));

        //Then
        mockMvc.perform(post("/authors/{id}/edit", PRESENT_ID)
                        .param("id", "1")
                        .param("fullName", "Updated Author")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .header("Referer", "/authors")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/authors"));

        verify(authorService, times(1)).update(PRESENT_ID, "Updated Author");
    }

    @DisplayName("должен отображать форму с ошибками при обновлении автора с пустым именем")
    @Test
    void whenUpdateAuthorWithEmptyName_thenReturnsAuthorUpsertViewWithError() throws Exception {
        //Given - PRESENT_ID, author full name - invalid

        //When
        when(authorService.findById(PRESENT_ID)).thenReturn(Optional.of(testAuthorDto));

        mockMvc.perform(post("/authors/{id}/edit", PRESENT_ID)
                        .param("id", "1")
                        .param("fullName", "") // Пустое имя - невалидно
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .header("Referer", "/authors")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("author-upsert"))
                .andExpect(model().attributeHasFieldErrors("author", "fullName"));

        verify(authorService, never()).update(anyLong(), anyString());
    }

    @DisplayName("должен отображать форму с ошибкой при возникновении исключения при создании автора")
    @Test
    void whenCreateAuthorThrowsException_thenReturnsAuthorUpsertViewWithError() throws Exception {
        //Given - PRESENT_ID

        //When
        when(authorService.insert(anyString())).thenThrow(new RuntimeException("Service error"));

        //Then
        mockMvc.perform(post("/authors/add")
                        .param("fullName", "New Author")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .header("Referer", "/authors")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("author-upsert"))
                .andExpect(model().attributeExists("errorMessage"));

        verify(authorService, times(1)).insert("New Author");
    }

    @DisplayName("должен отображать форму с ошибкой при возникновении исключения при обновлении автора")
    @Test
    void whenUpdateAuthorThrowsException_thenReturnsAuthorUpsertViewWithError() throws Exception {
        //Given - PRESENT_ID

        //When
        when(authorService.findById(PRESENT_ID)).thenReturn(Optional.of(testAuthorDto));
        when(authorService.update(anyLong(), anyString())).thenThrow(new RuntimeException("Service error"));

        //Then
        mockMvc.perform(post("/authors/{id}/edit", PRESENT_ID)
                        .param("id", "1")
                        .param("fullName", "Updated Author")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .header("Referer", "/authors")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("author-upsert"))
                .andExpect(model().attributeExists("errorMessage"));

        verify(authorService, times(1)).update(PRESENT_ID, "Updated Author");
    }

    @DisplayName("должен успешно удалить существующего автора")
    @Test
    void whenDeleteExistingAuthor_thenRedirectsToAuthors() throws Exception {
        //Given - PRESENT_ID

        //Then
        mockMvc.perform(delete("/authors/{id}/delete", PRESENT_ID)
                        .header("Referer", "/authors")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/authors"));

        verify(authorService, times(1)).deleteById(PRESENT_ID);
    }
}