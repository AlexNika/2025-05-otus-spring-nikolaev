package ru.otus.hw.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MainPageController.class)
class MainPageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void whenOpenMainPageUsingGetRequest_thenReturnIndexView() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }

    @Test
    void whenOpenMainPageUsingPostRequest_thenReturn500Error() throws Exception {
        mockMvc.perform(post("/"))
                .andExpect(status().is5xxServerError());
    }
}