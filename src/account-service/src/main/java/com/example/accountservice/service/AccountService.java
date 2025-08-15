package com.example.accountservice.service;

import com.example.accountservice.dto.*;
import com.example.accountservice.entity.Account;

import java.util.List;
import java.util.Optional;

public interface AccountService {
    AccountResponse createAccount(CreateAccountRequest req);
    AccountResponse getById(Long id);
    AccountResponse update(Long id, UpdateAccountRequest req);
    List<AddressDto> addAddress(Long accountId, AddressRequest req);
    List<PaymentMethodDto> addPaymentMethod(Long accountId, PaymentMethodRequest req);
    AccountResponse createIfAbsent(String userId, String email, String username);
    Optional<Account> findByAuthUserId(String authUserId);
}