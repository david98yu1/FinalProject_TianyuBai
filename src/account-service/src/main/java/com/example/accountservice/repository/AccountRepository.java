package com.example.accountservice.repository;

import com.example.accountservice.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByAuthUserId(String authUserId);
    boolean existsByEmail(String email);
}

