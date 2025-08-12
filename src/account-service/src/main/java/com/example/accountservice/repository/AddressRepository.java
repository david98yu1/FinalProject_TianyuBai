package com.example.accountservice.repository;

import com.example.accountservice.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByAccountId(Long accountId);
}