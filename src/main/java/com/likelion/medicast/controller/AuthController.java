package com.likelion.medicast.controller;

import com.likelion.medicast.dto.AuthResponse;
import com.likelion.medicast.dto.LoginRequest;
import com.likelion.medicast.dto.SignupRequest;
import com.likelion.medicast.dto.UserResponse;
import com.likelion.medicast.service.AuthService;
import com.likelion.medicast.security.AuthenticatedUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@Valid @RequestBody SignupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authService.signup(request));
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/my")
    public UserResponse my(@AuthenticationPrincipal AuthenticatedUser user) {
        return UserResponse.from(user);
    }
}
