package com.example.accountservice.dto;

import com.example.accountservice.entity.AddressType;
import jakarta.validation.constraints.*;

public record AddressRequest(
        @NotNull AddressType type,
        @NotBlank String line1,
        String line2,
        @NotBlank String city,
        @NotBlank String state,
        @NotBlank String zip,
        @NotBlank String country,
        boolean isDefault
) {}
