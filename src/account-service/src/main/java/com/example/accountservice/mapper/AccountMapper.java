package com.example.accountservice.mapper;

import com.example.accountservice.dto.AccountResponse;
import com.example.accountservice.dto.AddressDto;
import com.example.accountservice.dto.PaymentMethodDto;
import com.example.accountservice.entity.Account;
import com.example.accountservice.entity.Address;
import com.example.accountservice.entity.PaymentMethod;

public final class AccountMapper {
    private AccountMapper() {
    }

    public static AccountResponse toResponse(Account a) {
        var addrs = a.getAddresses().stream().map(AccountMapper::toDto).toList();
        var pms   = a.getPaymentMethods().stream().map(AccountMapper::toDto).toList();
        return new AccountResponse(a.getId(), a.getAuthUserId(), a.getEmail(), a.getUsername(), addrs, pms);
    }

    public static AddressDto toDto(Address x) {
        return new AddressDto(x.getId(), x.getType(), x.getLine1(), x.getLine2(),
                x.getCity(), x.getState(), x.getZip(), x.getCountry(), x.isDefault());
    }

    public static PaymentMethodDto toDto(PaymentMethod pm) {
        return new PaymentMethodDto(pm.getId(), pm.getBrand(), pm.getLast4(),
                pm.getExpMonth(), pm.getExpYear(), pm.isDefault());
    }
}


