package com.example.accountservice.dto;

import jakarta.validation.constraints.*;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class PaymentMethodRequest{
        @NotBlank String brand;          // e.g., VISA
        @Pattern(regexp = "\\d{4}") String last4;
        @Min(1) @Max(12) int expMonth;
        @Min(2000) @Max(2100) int expYear;
        @NotBlank String token;          // token from payment gateway
        boolean isDefault;
}