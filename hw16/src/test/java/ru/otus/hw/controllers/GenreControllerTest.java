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
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.services.GenreService;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Контроллер для работы с жанрами")
@WebMvcTest(GenreController.class)
@WithMockUser(username = "admin")
class GenreControllerTest {

    private static final Long MISSING_ID = 42L;
    private static final Long PRESENT_ID = 1L;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GenreService genreService;

    private GenreDto testGenreDto;

    @BeforeEach
    void setUp() {
        testGenreDto = new GenreDto(PRESENT_ID, "Fantasy");
    }

    @DisplayName("должен отображать список всех жанров")
    @Test
    void whenGetAllGenres_thenReturnsGenresView() throws Exception {
        //Given
        List<GenreDto> genres = List.of(testGenreDto, new GenreDto(2L, "Science Fiction"));

        //When
        when(genreService.findAll()).thenReturn(genres);

        //Then
        mockMvc.perform(get("/genres")
                        .header("Referer", "/books"))
                .andExpect(status().isOk())
                .andExpect(view().name("genres"))
                .andExpect(model().attribute("genres", genres))
                .andExpect(model().attribute("previousUrl", "/books"));

        verify(genreService, times(1)).findAll();
    }

    @DisplayName("должен отображать детали существующего жанра")
    @Test
    void whenGetExistingGenreById_thenReturnsGenreView() throws Exception {
        //Given - PRESENT_ID

        //When
        when(genreService.findById(PRESENT_ID)).thenReturn(Optional.of(testGenreDto));

        //Then
        mockMvc.perform(get("/genres/{id}/details", PRESENT_ID)
                        .header("Referer", "/genres"))
                .andExpect(status().isOk())
                .andExpect(view().name("genre-view"))
                .andExpect(model().attribute("genre", testGenreDto))
                .andExpect(model().attribute("previousUrl", "/genres"));

        verify(genreService, times(1)).findById(PRESENT_ID);
    }

    @DisplayName("должен возвращать 404 при попытке получить несуществующий жанр")
    @Test
    void whenGetNonExistentGenreById_thenReturnsNotFound() throws Exception {
        //Given - MISSING_ID

        //When
        when(genreService.findById(MISSING_ID)).thenReturn(Optional.empty());

        //Then
        mockMvc.perform(get("/genres/{id}/details", MISSING_ID))
                .andExpect(status().isNotFound());

        verify(genreService, times(1)).findById(MISSING_ID);
    }

    @DisplayName("должен отображать форму добавления нового жанра")
    @Test
    void whenShowAddGenreForm_thenReturnsGenreUpsertView() throws Exception {

        //Then
        mockMvc.perform(get("/genres/add")
                        .header("Referer", "/genres"))
                .andExpect(status().isOk())
                .andExpect(view().name("genre-upsert"))
                .andExpect(model().attribute("formAction", "/genres/add"))
                .andExpect(model().attribute("formTitle", "Создание нового жанра"))
                .andExpect(model().attributeExists("genre"));

        verify(genreService, never()).findById(anyLong());
    }

    @DisplayName("должен отображать форму редактирования существующего жанра")
    @Test
    void whenShowEditGenreFormForExistingGenre_thenReturnsGenreUpsertView() throws Exception {
        //Given - PRESENT_ID

        //When
        when(genreService.findById(PRESENT_ID)).thenReturn(Optional.of(testGenreDto));

        //Then
        mockMvc.perform(get("/genres/{id}/edit", PRESENT_ID)
                        .header("Referer", "/genres"))
                .andExpect(status().isOk())
                .andExpect(view().name("genre-upsert"))
                .andExpect(model().attribute("formAction", "/genres/1/edit"))
                .andExpect(model().attribute("formTitle", "Редактирование жанра"))
                .andExpect(model().attribute("genre", testGenreDto));

        verify(genreService, times(1)).findById(PRESENT_ID);
    }

    @DisplayName("должен возвращать 404 при попытке редактировать несуществующий жанр")
    @Test
    void whenShowEditGenreFormForNonExistentGenre_thenReturnsNotFound() throws Exception {
        //Given - MISSING_ID

        //When
        when(genreService.findById(MISSING_ID)).thenReturn(Optional.empty());

        //Then
        mockMvc.perform(get("/genres/{id}/edit", MISSING_ID))
                .andExpect(status().isNotFound());

        verify(genreService, times(1)).findById(MISSING_ID);
    }

    @DisplayName("должен успешно создать новый жанр с валидным названием")
    @Test
    void whenCreateValidGenre_thenRedirectsToGenres() throws Exception {
        mockMvc.perform(post("/genres/add")
                        .param("name", "New Genre")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .header("Referer", "/genres")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/genres"));

        verify(genreService, times(1)).insert("New Genre");
    }

    @DisplayName("должен отображать форму с ошибками при создании жанра с пустым названием")
    @Test
    void whenCreateGenreWithEmptyName_thenReturnsGenreUpsertViewWithError() throws Exception {
        //Given - genre name - invalid

        //Then
        mockMvc.perform(post("/genres/add")
                        .param("name", "") // Пустое имя - невалидно
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .header("Referer", "/genres")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("genre-upsert"))
                .andExpect(model().attributeHasFieldErrors("genre", "name"));

        verify(genreService, never()).insert(anyString());
    }

    @DisplayName("должен успешно обновить существующий жанр с валидным названием")
    @Test
    void whenUpdateGenreWithValidName_thenRedirectsToGenres() throws Exception {
        //Given - PRESENT_ID, genre name - valid

        //When
        when(genreService.findById(PRESENT_ID)).thenReturn(Optional.of(testGenreDto));

        //Then
        mockMvc.perform(post("/genres/{id}/edit", PRESENT_ID)
                        .param("id", "1")
                        .param("name", "Updated Genre")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .header("Referer", "/genres")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/genres"));

        verify(genreService, times(1)).update(PRESENT_ID, "Updated Genre");
    }

    @DisplayName("должен отображать форму с ошибками при обновлении жанра с пустым названием")
    @Test
    void whenUpdateGenreWithEmptyName_thenReturnsGenreUpsertViewWithError() throws Exception {
        //Given - PRESENT_ID, genre name - empty

        //When
        when(genreService.findById(PRESENT_ID)).thenReturn(Optional.of(testGenreDto));

        //Then
        mockMvc.perform(post("/genres/{id}/edit", PRESENT_ID)
                        .param("id", "1")
                        .param("name", "") // Пустое имя - невалидно
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .header("Referer", "/genres")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("genre-upsert"))
                .andExpect(model().attributeHasFieldErrors("genre", "name"));

        verify(genreService, never()).findById(PRESENT_ID);
        verify(genreService, never()).update(anyLong(), anyString());
    }

    @DisplayName("должен отображать форму с ошибкой при возникновении исключения при создании жанра")
    @Test
    void whenCreateGenreThrowsException_thenReturnsGenreUpsertViewWithError() throws Exception {
        //When
        when(genreService.insert(anyString())).thenThrow(new RuntimeException("Service error"));

        //Then
        mockMvc.perform(post("/genres/add")
                        .param("name", "New Genre")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .header("Referer", "/genres")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("genre-upsert"));

        verify(genreService, times(1)).insert("New Genre");
    }

    @DisplayName("должен успешно удалить существующий жанр")
    @Test
    void whenDeleteExistingGenre_thenRedirectsToGenres() throws Exception {
        //Given - PRESENT_ID

        //Then
        mockMvc.perform(delete("/genres/{id}/delete", PRESENT_ID)
                        .header("Referer", "/genres")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/genres"));

        verify(genreService, times(1)).deleteById(PRESENT_ID);
    }
}