package com.example.authservice.dto;
import jakarta.validation.constraints.*;

public class RegisterRequest {
    @Email @NotBlank private String email;
    @NotBlank private String username;
    @NotBlank @Size(min=6,max=100) private String password;
    // getters/setters...
    public String getEmail(){return email;} public void setEmail(String e){this.email=e;}
    public String getUsername(){return username;} public void setUsername(String u){this.username=u;}
    public String getPassword(){return password;} public void setPassword(String p){this.password=p;}
}
