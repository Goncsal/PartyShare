package tqs.backend.tqsbackend.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tqs.backend.tqsbackend.entity.*;
import tqs.backend.tqsbackend.service.WalletService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WalletController.class)
class WalletControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WalletService walletService;

    @Test
    void getWallet_AsOwner_ReturnsWallet() throws Exception {
        User owner = new User("Owner", "owner@test.com", "pass", UserRoles.OWNER);
        owner.setId(1L);

        Wallet wallet = new Wallet(owner);
        wallet.setId(1L);
        wallet.setBalance(new BigDecimal("500.00"));
        wallet.setPendingBalance(new BigDecimal("100.00"));
        wallet.setCreatedAt(LocalDateTime.now());

        when(walletService.getWalletByOwnerId(1L)).thenReturn(Optional.of(wallet));

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", 1L);
        session.setAttribute("userRole", UserRoles.OWNER);

        mockMvc.perform(get("/api/wallet").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.balance").value(500.00))
                .andExpect(jsonPath("$.pendingBalance").value(100.00));
    }

    @Test
    void getWallet_NotOwner_ReturnsForbidden() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", 1L);
        session.setAttribute("userRole", UserRoles.RENTER);

        mockMvc.perform(get("/api/wallet").session(session))
                .andExpect(status().isForbidden());
    }

    @Test
    void getWallet_NoSession_ReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/wallet"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getTransactions_AsOwner_ReturnsList() throws Exception {
        User owner = new User("Owner", "owner@test.com", "pass", UserRoles.OWNER);
        owner.setId(1L);

        Wallet wallet = new Wallet(owner);
        wallet.setId(1L);

        Item item = new Item();
        item.setId(1L);
        item.setOwnerId(1L);

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setItem(item);

        WalletTransaction tx = new WalletTransaction(wallet, booking, new BigDecimal("100.00"));
        tx.setId(1L);
        tx.setCreatedAt(LocalDateTime.now());

        when(walletService.getTransactionsByOwnerId(1L)).thenReturn(List.of(tx));

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", 1L);
        session.setAttribute("userRole", UserRoles.OWNER);

        mockMvc.perform(get("/api/wallet/transactions").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].amount").value(100.00))
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    void withdraw_AsOwner_Success() throws Exception {
        when(walletService.withdrawAll(1L)).thenReturn(new BigDecimal("500.00"));

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", 1L);
        session.setAttribute("userRole", UserRoles.OWNER);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/api/wallet/withdraw").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.withdrawnAmount").value(500.00));
    }

    @Test
    void withdraw_NotOwner_ReturnsForbidden() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", 1L);
        session.setAttribute("userRole", UserRoles.RENTER);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/api/wallet/withdraw").session(session))
                .andExpect(status().isForbidden());
    }

    @Test
    void withdraw_NoBalance_ReturnsBadRequest() throws Exception {
        when(walletService.withdrawAll(1L)).thenThrow(new IllegalArgumentException("No available balance"));

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", 1L);
        session.setAttribute("userRole", UserRoles.OWNER);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/api/wallet/withdraw").session(session))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}
