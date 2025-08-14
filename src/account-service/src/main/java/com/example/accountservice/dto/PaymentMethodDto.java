package com.example.accountservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class PaymentMethodDto{
        Long id;
        String brand;
        String last4;
        int expMonth;
        int expYear;
        boolean isDefault;
}