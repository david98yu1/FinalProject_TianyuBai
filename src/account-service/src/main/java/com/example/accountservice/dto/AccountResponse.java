package com.example.accountservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponse {
        Long id;
        String authUserId;
        String email;
        String username;
        List<AddressDto> addresses;
        List<PaymentMethodDto> paymentMethods;
}