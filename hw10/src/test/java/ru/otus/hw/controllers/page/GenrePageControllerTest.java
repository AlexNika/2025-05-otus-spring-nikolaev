package ru.otus.hw.controllers.page;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.services.GenreService;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@DisplayName("MVC контроллер для работы с жанрами ")
@WebMvcTest(GenrePageController.class)
class GenrePageControllerTest {

    private static final Long MISSING_ID = 42L;
    private static final Long PRESENT_ID = 1L;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GenreService genreService;

    private GenreDto genreDto;

    @BeforeEach
    void setUp() {
        genreDto = GenreDto.builder().id(PRESENT_ID).name("Genre_1").build();
    }

    @DisplayName("должен отображать список всех жанров")
    @Test
    void whenGetAllGenres_thenReturnsGenresView() throws Exception {
        //Then
        mockMvc.perform(get("/genres"))
                .andExpect(status().isOk())
                .andExpect(view().name("genres"));

        verify(genreService, times(0)).findAll();
    }

    @DisplayName("должен отображать детали жанра по существующему ID")
    @Test
    void whenGetGenreById_thenReturnsGenreView() throws Exception {
        //When
        when(genreService.findById(PRESENT_ID)).thenReturn(java.util.Optional.of(genreDto));

        //Then
        mockMvc.perform(get("/genres/{id}/details", PRESENT_ID))
                .andExpect(status().isOk())
                .andExpect(view().name("genre-view"))
                .andExpect(model().attribute("genreId", PRESENT_ID));

        verify(genreService, times(1)).findById(PRESENT_ID);
    }

    @DisplayName("должен возвращать 404 при попытке получить несуществующий жанр")
    @Test
    void whenGetNonExistentGenreById_thenReturnsNotFound() throws Exception {
        //When
        when(genreService.findById(MISSING_ID)).thenReturn(java.util.Optional.empty());

        //Then
        mockMvc.perform(get("/genres/{id}/details", MISSING_ID))
                .andExpect(status().isNotFound());

        verify(genreService, times(1)).findById(MISSING_ID);
    }

    @DisplayName("должен отображать форму добавления нового жанра")
    @Test
    void whenShowAddGenreForm_thenReturnsGenreUpsertView() throws Exception {
        //Then
        mockMvc.perform(get("/genres/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("genre-upsert"))
                .andExpect(model().attributeExists("genre"))
                .andExpect(model().attribute("formTitle", "Создание нового жанра"))
                .andExpect(model().attribute("formAction", "/api/v1/genres"))
                .andExpect(model().attribute("isUpdate", false));
    }

    @DisplayName("должен отображать форму редактирования существующего жанра")
    @Test
    void whenShowEditGenreFormWithValidId_thenReturnsGenreUpsertView() throws Exception {
        //When
        when(genreService.findById(PRESENT_ID)).thenReturn(java.util.Optional.of(genreDto));

        //Then
        mockMvc.perform(get("/genres/{id}/edit", PRESENT_ID))
                .andExpect(status().isOk())
                .andExpect(view().name("genre-upsert"))
                .andExpect(model().attribute("genre", genreDto))
                .andExpect(model().attribute("formTitle", "Редактирование жанра"))
                .andExpect(model().attribute("formAction", "/api/v1/genres/" + PRESENT_ID))
                .andExpect(model().attribute("isUpdate", true))
                .andExpect(model().attribute("genreId", PRESENT_ID));

        verify(genreService, times(1)).findById(PRESENT_ID);
    }

    @DisplayName("должен возвращать 404 при попытке редактирования несуществующего жанра")
    @Test
    void whenShowEditGenreFormWithInvalidId_thenReturnsNotFound() throws Exception {
        //When
        when(genreService.findById(MISSING_ID)).thenReturn(java.util.Optional.empty());

        //Then
        mockMvc.perform(get("/genres/{id}/edit", MISSING_ID))
                .andExpect(status().isNotFound());

        verify(genreService, times(1)).findById(MISSING_ID);
    }
}
