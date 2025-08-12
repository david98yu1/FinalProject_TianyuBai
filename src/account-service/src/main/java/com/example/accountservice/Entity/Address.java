package com.example.accountservice.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "auth_user_id", nullable = false, length = 100)
    private String authUserId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    private AddressType type;

    @Column(nullable = false, length = 128)
    private String line1;

    @Column(length = 128)
    private String line2;

    @Column(nullable = false, length = 64)
    private String city;

    @Column(nullable = false, length = 64)
    private String state;

    @Column(nullable = false, length = 32)
    private String zip;

    @Column(nullable = false, length = 64)
    private String country;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault;
}