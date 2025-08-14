package com.example.accountservice.dto;

import com.example.accountservice.entity.AddressType;
import jakarta.validation.constraints.*;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AddressRequest{
        @NotNull AddressType type;
        @NotBlank String line1;
        String line2;
        @NotBlank String city;
        @NotBlank String state;
        @NotBlank String zip;
        @NotBlank String country;
        boolean isDefault;
}
