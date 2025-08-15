package com.example.authservice.controller;

import com.example.authservice.entity.User;
import com.example.authservice.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@WebMvcTest(controllers = AdminController.class)
@AutoConfigureMockMvc(addFilters = false) // keep filters off; we'll use @WithMockUser for role
class AdminControllerTest {

    @Autowired MockMvc mvc;
    @MockitoBean UserRepository users;
    @MockitoBean com.example.authservice.security.JwtService jwtService;

    @Test
    @WithMockUser(roles="ADMIN")
    @DisplayName("GET /admin/users/by-email/{email} -> 200 and returns user view")
    void get_by_email() throws Exception {
        User u = User.builder().id(77L).email("boss@x.com").username("boss").roles("ROLE_ADMIN").passwordHash("h").build();
        when(users.findByEmail("boss@x.com")).thenReturn(Optional.of(u));

        mvc.perform(MockMvcRequestBuilders.get("/admin/users/by-email/{email}", "boss@x.com"))
           .andExpect(MockMvcResultMatchers.status().isOk())
           .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(77))
           .andExpect(MockMvcResultMatchers.jsonPath("$.email").value("boss@x.com"))
           .andExpect(MockMvcResultMatchers.jsonPath("$.username").value("boss"));
    }

    @Test
    @WithMockUser(roles="ADMIN")
    @DisplayName("POST /admin/users/{id}/roles -> 200 and saves with new roles")
    void set_roles() throws Exception {
        User u = User.builder().id(5L).email("u@x.com").username("u").passwordHash("h").roles("ROLE_USER").build();
        when(users.findById(5L)).thenReturn(Optional.of(u));
        when(users.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        mvc.perform(MockMvcRequestBuilders.post("/admin/users/{id}/roles", 5L)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"roles\":\"ROLE_ADMIN,ROLE_USER\"}"))
           .andExpect(MockMvcResultMatchers.status().isOk());

        verify(users).save(argThat(saved -> "ROLE_ADMIN,ROLE_USER".equals(saved.getRoles())));
    }
}