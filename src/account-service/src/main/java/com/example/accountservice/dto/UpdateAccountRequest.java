package com.example.accountservice.dto;

import jakarta.validation.constraints.*;

public record UpdateAccountRequest(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 2, max = 50) String username
) {}