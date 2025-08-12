package com.example.accountservice.dto;

import jakarta.validation.constraints.*;

public record PaymentMethodRequest(
        @NotBlank String brand,          // e.g., VISA
        @Pattern(regexp = "\\d{4}") String last4,
        @Min(1) @Max(12) int expMonth,
        @Min(2000) @Max(2100) int expYear,
        @NotBlank String token,          // token from payment gateway
        boolean isDefault
) {}