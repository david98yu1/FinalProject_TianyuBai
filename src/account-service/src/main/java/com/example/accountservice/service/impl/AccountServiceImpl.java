package com.example.accountservice.service.impl;

import com.example.accountservice.dto.*;
import com.example.accountservice.entity.*;
import com.example.accountservice.repository.*;
import com.example.accountservice.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
@Transactional
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
    ModelMapper modelMapper = new ModelMapper();

    private final AccountRepository accountRepo;
    private final AddressRepository addressRepo;
    private final PaymentMethodRepository pmRepo;

    public AccountResponse createAccount(CreateAccountRequest req) {
        if (accountRepo.existsByEmail(req.getEmail())) throw new IllegalArgumentException("email already exists");
        var acc = Account.builder().authUserId(req.getAuthUserId()).email(req.getEmail()).username(req.getUsername()).build();
        Account account = accountRepo.save(acc);
        return modelMapper.map(account, AccountResponse.class);
    }

    @Transactional(readOnly = true)
    public AccountResponse getById(Long id) {
        var acc = accountRepo.findById(id).orElseThrow(() -> new NoSuchElementException("account not found"));
        acc.getAddresses().size(); acc.getPaymentMethods().size(); // init lazies if needed
        return modelMapper.map(acc, AccountResponse.class);
    }

    public AccountResponse update(Long id, UpdateAccountRequest req) {
        var acc = accountRepo.findById(id).orElseThrow(() -> new NoSuchElementException("account not found"));
        acc.setEmail(req.getEmail()); acc.setUsername(req.getUsername());
        return modelMapper.map(acc, AccountResponse.class);
    }

    public java.util.List<AddressDto> addAddress(Long accountId, AddressRequest req) {
        var acc = accountRepo.findById(accountId).orElseThrow(() -> new NoSuchElementException("account not found"));
        var address = Address.builder()
                .account(acc).type(req.getType()).line1(req.getLine1()).line2(req.getLine2())
                .city(req.getCity()).state(req.getState()).zip(req.getZip()).country(req.getCountry())
                .isDefault(req.isDefault()).build();
        addressRepo.save(address);
        return addressRepo.findByAccountId(accountId).stream()
                .map(a -> modelMapper.map(a, AddressDto.class))
                .toList();
    }

    public java.util.List<PaymentMethodDto> addPaymentMethod(Long accountId, PaymentMethodRequest req) {
        var acc = accountRepo.findById(accountId).orElseThrow(() -> new NoSuchElementException("account not found"));
        var pm = PaymentMethod.builder()
                .account(acc).brand(req.getBrand()).last4(req.getLast4())
                .expMonth(req.getExpMonth()).expYear(req.getExpYear())
                .token(req.getToken()).isDefault(req.isDefault()).build();
        pmRepo.save(pm);
        return pmRepo.findByAccountId(accountId).stream()
                .map(x -> modelMapper.map(x, PaymentMethodDto.class))
                .toList();
    }
}
