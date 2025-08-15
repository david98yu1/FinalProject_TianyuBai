package com.example.accountservice.controller;

import com.example.accountservice.security.JwtService;
import com.example.accountservice.service.AccountService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = InternalAccountController.class)
@AutoConfigureMockMvc(addFilters = false) // keep simple: disable filters
class InternalAccountControllerTest {

    @Autowired MockMvc mvc;
    @MockitoBean AccountService service;
    @MockitoBean
    JwtService jwtService;

    @Test
    @DisplayName("POST /internal/accounts/on-register -> 201 Created and calls service.createIfAbsent")
    void on_register() throws Exception {
        String body = "{\"authUserId\":\"42\",\"email\":\"x@y.com\",\"username\":\"x\"}";
        mvc.perform(post("/internal/accounts/on-register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        verify(service).createIfAbsent(anyString(), anyString(), anyString());
    }
}