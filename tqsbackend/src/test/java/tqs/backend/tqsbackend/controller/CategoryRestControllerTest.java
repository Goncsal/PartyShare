package tqs.backend.tqsbackend.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tqs.backend.tqsbackend.entity.Category;
import tqs.backend.tqsbackend.service.CategoryService;

import java.util.Arrays;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoryRestController.class)
class CategoryRestControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private CategoryService categoryService;

    @Test
    void getAllCategories_ReturnsList() throws Exception {
        Category c1 = new Category("Party");
        c1.setId(1L);
        Category c2 = new Category("Tools");
        c2.setId(2L);

        given(categoryService.getAllCategories()).willReturn(Arrays.asList(c1, c2));

        mvc.perform(get("/api/categories")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("Party")))
                .andExpect(jsonPath("$[1].name", is("Tools")));
    }

    @Test
    void createCategory_ReturnsCreated() throws Exception {
        Category c1 = new Category("New Category");
        c1.setId(10L);

        given(categoryService.createCategory("New Category")).willReturn(c1);

        mvc.perform(post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"New Category\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("New Category")))
                .andExpect(jsonPath("$.id", is(10)));
    }
}
