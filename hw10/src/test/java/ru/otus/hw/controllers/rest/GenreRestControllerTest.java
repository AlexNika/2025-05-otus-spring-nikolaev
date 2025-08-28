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
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.services.GenreService;

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

@DisplayName("REST контроллер для работы с жанрами ")
@WebMvcTest(GenreRestController.class)
class GenreRestControllerTest {

    private static final Long MISSING_ID = 42L;
    private static final Long PRESENT_ID = 1L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private GenreService genreService;

    private GenreDto genreDto;

    @BeforeEach
    void setUp() {
        genreDto = GenreDto.builder().id(PRESENT_ID).name("Genre_1").build();
    }

    @DisplayName("должен возвращать список всех жанров")
    @Test
    void whenGetAllGenres_thenReturnsGenresList() throws Exception {
        //Given
        List<GenreDto> genres = List.of(genreDto);

        //When
        when(genreService.findAll()).thenReturn(genres);

        //Then
        mockMvc.perform(get("/api/v1/genres"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(PRESENT_ID))
                .andExpect(jsonPath("$[0].name").value("Genre_1"));

        verify(genreService, times(1)).findAll();
    }

    @DisplayName("должен возвращать жанр по существующему ID")
    @Test
    void whenGetGenreById_thenReturnsGenre() throws Exception {
        //When
        when(genreService.findById(PRESENT_ID)).thenReturn(java.util.Optional.of(genreDto));

        //Then
        mockMvc.perform(get("/api/v1/genres/{id}", PRESENT_ID))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(PRESENT_ID))
                .andExpect(jsonPath("$.name").value("Genre_1"));

        verify(genreService, times(1)).findById(PRESENT_ID);
    }

    @DisplayName("должен возвращать 404 при попытке получить несуществующий жанр")
    @Test
    void whenGetNonExistentGenreById_thenReturnsNotFound() throws Exception {
        //When
        when(genreService.findById(MISSING_ID)).thenReturn(java.util.Optional.empty());

        //Then
        mockMvc.perform(get("/api/v1/genres/{id}", MISSING_ID))
                .andExpect(status().isNotFound());

        verify(genreService, times(1)).findById(MISSING_ID);
    }

    @DisplayName("должен успешно создать новый жанр с валидными данными")
    @Test
    void whenCreateValidGenre_thenReturnsCreatedGenre() throws Exception {
        //Given
        GenreDto newGenreDto = new GenreDto(null, "New Genre");
        GenreDto createdGenreDto = new GenreDto(PRESENT_ID, "New Genre");

        //When
        when(genreService.insert(anyString())).thenReturn(createdGenreDto);

        //Then
        mockMvc.perform(post("/api/v1/genres")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newGenreDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(PRESENT_ID))
                .andExpect(jsonPath("$.name").value("New Genre"));

        verify(genreService, times(1)).insert("New Genre");
    }

    @DisplayName("должен возвращать 400 при создании жанра с невалидными данными")
    @Test
    void whenCreateInvalidGenre_thenReturnsBadRequest() throws Exception {
        //Given
        GenreDto invalidGenreDto = new GenreDto(null, "");

        //Then
        mockMvc.perform(post("/api/v1/genres")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidGenreDto)))
                .andExpect(status().isBadRequest());

        verify(genreService, never()).insert(anyString());
    }

    @DisplayName("должен успешно обновить существующий жанр с валидными данными")
    @Test
    void whenUpdateValidGenre_thenReturnsUpdatedGenre() throws Exception {
        //Given
        GenreDto updatedGenreDto = new GenreDto(PRESENT_ID, "Updated Genre");

        //When
        when(genreService.update(anyLong(), anyString())).thenReturn(updatedGenreDto);

        //Then
        mockMvc.perform(put("/api/v1/genres/{id}", PRESENT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedGenreDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(PRESENT_ID))
                .andExpect(jsonPath("$.name").value("Updated Genre"));

        verify(genreService, times(1)).update(PRESENT_ID, "Updated Genre");
    }

    @DisplayName("должен возвращать 400 при обновлении жанра с невалидными данными")
    @Test
    void whenUpdateInvalidGenre_thenReturnsBadRequest() throws Exception {
        //Given
        GenreDto invalidGenreDto = new GenreDto(PRESENT_ID, "");

        //Then
        mockMvc.perform(put("/api/v1/genres/{id}", PRESENT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidGenreDto)))
                .andExpect(status().isBadRequest());

        verify(genreService, never()).update(anyLong(), anyString());
    }

    @DisplayName("должен успешно удалить существующий жанр")
    @Test
    void whenDeleteExistingGenre_thenReturnsOk() throws Exception {
        //When
        doNothing().when(genreService).deleteById(PRESENT_ID);

        //Then
        mockMvc.perform(delete("/api/v1/genres/{id}", PRESENT_ID))
                .andExpect(status().isOk());

        verify(genreService, times(1)).deleteById(PRESENT_ID);
    }

    @DisplayName("должен возвращать 404 при попытке удаления несуществующего жанра")
    @Test
    void whenDeleteNonExistentGenre_thenReturnsNotFound() throws Exception {
        //When
        doThrow(new EntityNotFoundException("Genre not found")).when(genreService).deleteById(MISSING_ID);

        //Then
        mockMvc.perform(delete("/api/v1/genres/{id}", MISSING_ID))
                .andExpect(status().isNotFound());

        verify(genreService, times(1)).deleteById(MISSING_ID);
    }
}