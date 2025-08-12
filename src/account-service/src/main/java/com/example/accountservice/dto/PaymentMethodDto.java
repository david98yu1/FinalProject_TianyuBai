package com.example.accountservice.dto;

public record PaymentMethodDto(
        Long id,
        String brand,
        String last4,
        int expMonth,
        int expYear,
        boolean isDefault
) {}