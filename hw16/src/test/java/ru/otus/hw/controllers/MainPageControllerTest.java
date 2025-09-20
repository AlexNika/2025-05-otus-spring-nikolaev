package ru.otus.hw.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(MainPageController.class)
@WithMockUser(username = "admin")
class MainPageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void whenOpenMainPageUsingGetRequest_thenReturnIndexView() throws Exception {
        mockMvc.perform(get("/")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }

    @Test
    void whenOpenMainPageUsingPostRequest_thenReturn500Error() throws Exception {
        mockMvc.perform(post("/")
                        .with(csrf()))
                .andExpect(status().is5xxServerError());
    }
}