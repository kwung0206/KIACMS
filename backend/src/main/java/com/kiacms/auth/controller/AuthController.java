package com.kiacms.auth.controller;

import com.kiacms.auth.dto.request.LoginRequest;
import com.kiacms.auth.dto.request.SignUpRequest;
import com.kiacms.auth.dto.response.LoginResponse;
import com.kiacms.auth.dto.response.SignUpResponse;
import com.kiacms.auth.service.AuthService;
import com.kiacms.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ApiResponse<SignUpResponse> signUp(@Valid @RequestBody SignUpRequest request) {
        return ApiResponse.ok(authService.signUp(request));
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(authService.login(request));
    }
}
