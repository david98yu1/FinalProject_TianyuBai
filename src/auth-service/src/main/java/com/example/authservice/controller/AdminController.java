package com.example.authservice.controller;

import com.example.authservice.entity.User;
import com.example.authservice.repository.UserRepository;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')") // belt-and-suspenders in addition to SecurityConfig
public class AdminController {

    private final UserRepository users;

    public static class RoleUpdate {
        @NotBlank public String roles; // e.g. "ROLE_ADMIN,ROLE_USER" or "ROLE_ORDER"
    }

    public static class UserView {
        public Long id;
        public String email;
        public String username;
        public String roles;
        public UserView(User u){ this.id=u.getId(); this.email=u.getEmail(); this.username=u.getUsername(); this.roles=u.getRoles(); }
    }

    // Look up by username (handy to get the ID you’ll need for setRoles)
    @GetMapping("/users/by-username/{username}")
    public UserView byUsername(@PathVariable String username) {
        var u = users.findByUsername(username).orElseThrow(NoSuchElementException::new);
        return new UserView(u);
    }

    // Look up by email
    @GetMapping("/users/by-email/{email}")
    public UserView byEmail(@PathVariable String email) {
        var u = users.findByEmail(email).orElseThrow(NoSuchElementException::new);
        return new UserView(u);
    }

    // Set the EXACT roles CSV for a user (overwrites previous roles)
    @PostMapping("/users/{id}/roles")
    public void setRoles(@PathVariable Long id, @RequestBody RoleUpdate req) {
        var u = users.findById(id).orElseThrow(NoSuchElementException::new);
        u.setRoles(req.roles);
        users.save(u);
    }
}
