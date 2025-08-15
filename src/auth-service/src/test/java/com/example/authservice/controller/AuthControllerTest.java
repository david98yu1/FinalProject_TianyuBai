package com.example.authservice.controller;

import com.example.authservice.entity.User;
import com.example.authservice.repository.UserRepository;
import com.example.authservice.security.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Instant;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false) // disable security filters for slice tests
class AuthControllerTest {

    @Autowired MockMvc mvc;

    @MockitoBean UserRepository users;
    @MockitoBean PasswordEncoder encoder;
    @MockitoBean JwtService jwt;

    @Test
    @DisplayName("POST /auth/login -> 200 OK with token when credentials valid")
    void login_success() throws Exception {
        // Arrange a user and mocks
        User u = User.builder()
                .id(1L)
                .email("a@b.com")
                .username("alice")
                .passwordHash("$2a$10$hash") // not used directly
                .roles("ROLE_ADMIN,ROLE_USER")
                .build();
        when(users.findByEmail("alice")).thenReturn(Optional.empty());
        when(users.findByUsername("alice")).thenReturn(Optional.of(u));
        when(encoder.matches("secret", "$2a$10$hash")).thenReturn(true);
        when(jwt.generate(1L, "alice", "ROLE_ADMIN,ROLE_USER")).thenReturn("mock.jwt.token");

        // Act
        ResultActions res = mvc.perform(MockMvcRequestBuilders.post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"login\":\"alice\",\"password\":\"secret\"}"));

        // Assert: basic shape
        res.andExpect(MockMvcResultMatchers.status().isOk())
           .andExpect(MockMvcResultMatchers.jsonPath("$.token").value("mock.jwt.token"))
           .andExpect(MockMvcResultMatchers.jsonPath("$.expiresAt").exists());
    }

    @Test
    @DisplayName("POST /auth/login -> 400 when password mismatch")
    void login_badPassword() throws Exception {
        User u = User.builder()
                .id(2L).email("x@y.com").username("x").passwordHash("hash").roles("ROLE_USER").build();
        when(users.findByEmail("x")).thenReturn(Optional.empty());
        when(users.findByUsername("x")).thenReturn(Optional.of(u));
        when(encoder.matches("wrong", "hash")).thenReturn(false);

        mvc.perform(MockMvcRequestBuilders.post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"login\":\"x\",\"password\":\"wrong\"}"))
           .andExpect(MockMvcResultMatchers.status().is4xxClientError());
    }

    @Test
    @DisplayName("POST /auth/login -> 404 when user not found")
    void login_userNotFound() throws Exception {
        when(users.findByEmail("ghost")).thenReturn(Optional.empty());
        when(users.findByUsername("ghost")).thenReturn(Optional.empty());

        mvc.perform(MockMvcRequestBuilders.post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"login\":\"ghost\",\"password\":\"irrelevant\"}"))
           .andExpect(MockMvcResultMatchers.status().is4xxClientError());
    }
}