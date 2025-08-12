package com.example.accountservice.dto;

import java.util.List;

public record AccountResponse(
        Long id,
        String authUserId,
        String email,
        String username,
        List<AddressDto> addresses,
        List<PaymentMethodDto> paymentMethods
) {}