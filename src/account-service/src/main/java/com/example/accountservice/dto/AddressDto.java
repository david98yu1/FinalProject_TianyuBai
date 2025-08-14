package com.example.accountservice.dto;

import com.example.accountservice.entity.AddressType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AddressDto {
    Long id;
    AddressType type;
    String line1;
    String line2;
    String city;
    String state;
    String zip;
    String country;
    boolean isDefault;
}