package com.example.accountservice.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Address{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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