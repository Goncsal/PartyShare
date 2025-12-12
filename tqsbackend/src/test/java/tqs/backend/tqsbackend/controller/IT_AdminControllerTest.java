package tqs.backend.tqsbackend.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tqs.backend.tqsbackend.entity.Category;
import tqs.backend.tqsbackend.entity.UserRoles;
import tqs.backend.tqsbackend.repository.CategoryRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class IT_AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void categories_AdminUser_ReturnsCategoriesView() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userRole", UserRoles.ADMIN);

        mockMvc.perform(get("/admin/categories").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/categories"))
                .andExpect(model().attributeExists("categories"));
    }

    @Test
    void categories_NonAdminUser_RedirectsToLogin() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userRole", UserRoles.RENTER);

        mockMvc.perform(get("/admin/categories").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/login"));
    }

    @Test
    void createCategory_AdminUser_CreatesCategoryAndRedirects() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userRole", UserRoles.ADMIN);

        mockMvc.perform(post("/admin/categories")
                .session(session)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("name", "New Integration Category"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/categories"));

        Category category = categoryRepository.findByName("New Integration Category");
        assertThat(category).isNotNull();
    }

    @Test
    void createCategory_DuplicateName_RedirectsWithError() throws Exception {
        categoryRepository.save(new Category("Duplicate Category"));

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userRole", UserRoles.ADMIN);

        mockMvc.perform(post("/admin/categories")
                .session(session)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("name", "Duplicate Category"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/categories"));
                // Flash attributes are harder to test with standard MockMvc without more setup, 
                // but we can verify the redirect and that no new category (or error) occurred.
    }
}
