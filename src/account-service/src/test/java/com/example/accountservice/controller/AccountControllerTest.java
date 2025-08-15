package com.example.accountservice.controller;

import com.example.accountservice.dto.*;
import com.example.accountservice.security.JwtService;
import com.example.accountservice.service.AccountService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AccountController.class)
@AutoConfigureMockMvc(addFilters = false) // disable security filters for simple controller tests
class AccountControllerTest {

    @Autowired MockMvc mvc;

    @MockitoBean AccountService service;
    @MockitoBean JwtService jwtService;

    @Test
    @DisplayName("POST /accounts -> 201 Created with body")
    void create_account() throws Exception {
        AccountResponse resp = new AccountResponse(1L, "u-1", "a@b.com", "alice", List.of(), List.of());
        when(service.createAccount(any(CreateAccountRequest.class))).thenReturn(resp);

        mvc.perform(post("/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"a@b.com\",\"username\":\"alice\",\"authUserId\":\"u-1\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.authUserId").value("u-1"))
            .andExpect(jsonPath("$.email").value("a@b.com"))
            .andExpect(jsonPath("$.username").value("alice"));
    }

    @Test
    @DisplayName("GET /accounts/{id} -> 200 OK")
    void get_account() throws Exception {
        AccountResponse resp = new AccountResponse(2L, "u-2", "b@c.com", "bob", List.of(), List.of());
        when(service.getById(2L)).thenReturn(resp);

        mvc.perform(get("/accounts/2"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.id").value(2))
           .andExpect(jsonPath("$.authUserId").value("u-2"));
    }

    @Test
    @DisplayName("PUT /accounts/{id} -> 200 OK with updated fields")
    void update_account() throws Exception {
        AccountResponse resp = new AccountResponse(3L, "u-3", "new@x.com", "newname", List.of(), List.of());
        when(service.update(eq(3L), any(UpdateAccountRequest.class))).thenReturn(resp);

        mvc.perform(put("/accounts/3")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"new@x.com\",\"username\":\"newname\"}"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.email").value("new@x.com"))
           .andExpect(jsonPath("$.username").value("newname"));
    }

    @Test
    @DisplayName("POST /accounts/{id}/addresses -> 200 OK returns list")
    void add_address() throws Exception {
        when(service.addAddress(eq(5L), any(AddressRequest.class)))
                .thenReturn(List.of(new AddressDto(100L, null, "l1", null, "LA","CA","90001","US", true)));

        mvc.perform(post("/accounts/5/addresses")
                .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"SHIPPING\",\"line1\":\"l1\",\"city\":\"LA\",\"state\":\"CA\",\"zip\":\"90001\",\"country\":\"US\",\"isDefault\":true}"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$[0].id").value(100));
    }

    @Test
    @DisplayName("POST /accounts/{id}/payment-methods -> 200 OK returns list")
    void add_payment() throws Exception {
        when(service.addPaymentMethod(eq(6L), any(PaymentMethodRequest.class)))
                .thenReturn(List.of(new PaymentMethodDto(200L, "VISA", "4242", 12, 2030, true)));

        mvc.perform(post("/accounts/6/payment-methods")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"brand\":\"VISA\",\"last4\":\"4242\",\"expMonth\":12,\"expYear\":2030,\"token\":\"tok_abc\",\"isDefault\":true}"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$[0].id").value(200))
           .andExpect(jsonPath("$[0].brand").value("VISA"));
    }
}