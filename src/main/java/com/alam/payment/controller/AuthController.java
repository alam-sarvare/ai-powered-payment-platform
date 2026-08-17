package com.alam.payment.controller;

import com.alam.payment.request.dto.AuthResponse;
import com.alam.payment.request.dto.LoginRequest;
import com.alam.payment.request.dto.RegisterRequest;
import com.alam.payment.entity.User;
import com.alam.payment.service.AuthService;
import com.alam.payment.service.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public String register(
            @Valid @RequestBody RegisterRequest request) {

        authService.register(request);

        return "User registered successfully";
    }

    @PostMapping("/login")
    public AuthResponse login(
            @Valid @RequestBody LoginRequest request) {

        User user =
                authService.authenticate(request);

        String token =
                jwtService.generateToken(user);

        return new AuthResponse(token);
    }
}