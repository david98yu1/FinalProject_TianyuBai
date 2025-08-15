package com.example.commonlib.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    @NotBlank private String login;   // email or username
    @NotBlank private String password;
    public String getLogin(){return login;} public void setLogin(String l){this.login=l;}
    public String getPassword(){return password;} public void setPassword(String p){this.password=p;}
}

