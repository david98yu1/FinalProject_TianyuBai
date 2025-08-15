package com.example.authservice.controller;

import com.example.authservice.dto.*;
import com.example.authservice.entity.User;
import com.example.authservice.repository.UserRepository;
import com.example.authservice.security.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.NoSuchElementException;
import com.example.commonlib.dto.auth.AuthResponse;
import com.example.commonlib.dto.auth.LoginRequest;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest r) {
        users.findByEmail(r.getEmail()).ifPresent(u -> { throw new IllegalArgumentException("email taken"); });
        users.findByUsername(r.getUsername()).ifPresent(u -> { throw new IllegalArgumentException("username taken"); });

        User u = users.save(User.builder()
                .email(r.getEmail())
                .username(r.getUsername())
                .passwordHash(encoder.encode(r.getPassword()))
                .roles("ROLE_USER")
                .build());

        String token = jwt.generate(u.getId(), u.getUsername(), u.getRoles());
        return new AuthResponse(token, Instant.now().plusMillis(3600000));
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest r) {
        User u = users.findByEmail(r.getLogin())
                .or(() -> users.findByUsername(r.getLogin()))
                .orElseThrow(() -> new NoSuchElementException("user not found"));

        if (!encoder.matches(r.getPassword(), u.getPasswordHash()))
            throw new IllegalArgumentException("invalid credentials");

        String token = jwt.generate(u.getId(), u.getUsername(), u.getRoles());
        return new AuthResponse(token, Instant.now().plusMillis(3600000));
    }
}
