package com.example.authservice.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name="users", indexes = {
        @Index(name="idx_user_email", columnList="email", unique=true),
        @Index(name="idx_user_username", columnList="username", unique=true)
})
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, length=254) private String email;
    @Column(nullable=false, length=50)  private String username;
    @Column(nullable=false, length=100) private String passwordHash;

    // comma-separated roles, e.g. "ROLE_USER" or "ROLE_ADMIN,ROLE_USER"
    @Column(nullable=false, length=120) private String roles;
}
