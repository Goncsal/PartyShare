package tqs.backend.tqsbackend.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HomeController.class)
class HomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void whenGetHome_thenReturnIndexView() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("profile"))
                .andExpect(model().attributeExists("currentTime"))
                .andExpect(model().attributeExists("version"))
                .andExpect(model().attributeExists("applicationName"));
    }

    @Test
    void whenGetHome_thenVersionIsCorrect() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("version", "1.0.0-SNAPSHOT"));
    }

    @Test
    void whenGetHealth_thenReturnHealthView() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(view().name("health"))
                .andExpect(model().attributeExists("status"))
                .andExpect(model().attributeExists("profile"))
                .andExpect(model().attributeExists("timestamp"));
    }

    @Test
    void whenGetHealth_thenStatusIsUp() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("status", "UP"));
    }
}
