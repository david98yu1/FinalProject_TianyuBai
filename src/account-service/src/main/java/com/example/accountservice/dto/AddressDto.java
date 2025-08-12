package com.example.accountservice.dto;

import com.example.accountservice.entity.AddressType;

public record AddressDto(
        Long id,
        AddressType type,
        String line1,
        String line2,
        String city,
        String state,
        String zip,
        String country,
        boolean isDefault
) {}