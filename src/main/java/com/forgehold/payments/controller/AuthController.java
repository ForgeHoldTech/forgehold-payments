
package com.forgehold.payments.controller;

import com.forgehold.payments.dto.LoginRequest;
import com.forgehold.payments.dto.LoginResponse;
import com.forgehold.payments.dto.RegisterRequest;
import com.forgehold.payments.entity.AppUser;
import com.forgehold.payments.service.AppUserService;
import com.forgehold.payments.service.AuthService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AppUserService appUserService;
    private final AuthService authService;

    public AuthController(
            AppUserService appUserService,
            AuthService authService) {
        this.appUserService = appUserService;
        this.authService = authService;
    }

    public record RegisterResponse(
            Long id,
            String username,
            String role,
            String message
    ) {}

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(
            @Valid @RequestBody RegisterRequest request) {

        AppUser user = appUserService.registerUser(
                request.getUsername(),
                request.getPassword()
        );

        RegisterResponse response = new RegisterResponse(
                user.getId(),
                user.getUsername(),
                user.getRole(),
                "User registered successfully."
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {

        LoginResponse response = authService.login(
                request.getUsername(),
                request.getPassword()
        );

        return ResponseEntity.ok(response);
    }
}
