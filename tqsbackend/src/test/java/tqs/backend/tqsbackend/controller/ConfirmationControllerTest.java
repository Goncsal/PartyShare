package tqs.backend.tqsbackend.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tqs.backend.tqsbackend.entity.UserRoles;
import tqs.backend.tqsbackend.service.ConfirmationService;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ConfirmationController.class)
class ConfirmationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ConfirmationService confirmationService;

    @Test
    void confirmReturn_AsRenter_Success() throws Exception {
        when(confirmationService.confirmByRenter(1L, 2L)).thenReturn(true);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", 2L);
        session.setAttribute("userRole", UserRoles.RENTER);

        mockMvc.perform(post("/api/bookings/1/confirm-return").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string("Return confirmed successfully"));
    }

    @Test
    void confirmReturn_AsOwner_ReturnsForbidden() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", 1L);
        session.setAttribute("userRole", UserRoles.OWNER);

        mockMvc.perform(post("/api/bookings/1/confirm-return").session(session))
                .andExpect(status().isForbidden());
    }

    @Test
    void confirmReceived_AsOwner_Success() throws Exception {
        when(confirmationService.confirmByOwner(1L, 1L)).thenReturn(true);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", 1L);
        session.setAttribute("userRole", UserRoles.OWNER);

        mockMvc.perform(post("/api/bookings/1/confirm-received").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string("Receipt confirmed successfully"));
    }

    @Test
    void confirmReceived_AsRenter_ReturnsForbidden() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", 2L);
        session.setAttribute("userRole", UserRoles.RENTER);

        mockMvc.perform(post("/api/bookings/1/confirm-received").session(session))
                .andExpect(status().isForbidden());
    }

    @Test
    void getConfirmationStatus_ReturnsStatus() throws Exception {
        when(confirmationService.isFullyConfirmed(1L)).thenReturn(true);

        mockMvc.perform(get("/api/bookings/1/confirmation-status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullyConfirmed").value(true));
    }
}
