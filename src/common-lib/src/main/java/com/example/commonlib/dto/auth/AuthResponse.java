package com.example.commonlib.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private Instant expiresAt;
    public String getToken(){return token;} public void setToken(String t){this.token=t;}
    public Instant getExpiresAt(){return expiresAt;} public void setExpiresAt(Instant e){this.expiresAt=e;}
}
