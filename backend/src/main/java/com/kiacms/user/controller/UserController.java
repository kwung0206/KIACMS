package com.kiacms.user.controller;

import com.kiacms.global.response.ApiResponse;
import com.kiacms.global.security.CustomUserPrincipal;
import com.kiacms.user.dto.request.ChangePasswordRequest;
import com.kiacms.user.dto.request.UpdateMyProfileRequest;
import com.kiacms.user.dto.response.MyProfileResponse;
import com.kiacms.user.service.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserProfileService userProfileService;

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('STUDENT','INSTRUCTOR','MENTOR','ROOT')")
    public ApiResponse<MyProfileResponse> getMyProfile(@AuthenticationPrincipal CustomUserPrincipal principal) {
        return ApiResponse.ok(userProfileService.getMyProfile(principal.getUserId()));
    }

    @PutMapping("/me")
    @PreAuthorize("hasAnyRole('STUDENT','INSTRUCTOR','MENTOR','ROOT')")
    public ApiResponse<MyProfileResponse> updateMyProfile(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody UpdateMyProfileRequest request
    ) {
        return ApiResponse.ok(userProfileService.updateMyProfile(principal.getUserId(), request));
    }

    @PutMapping("/me/password")
    @PreAuthorize("hasAnyRole('STUDENT','INSTRUCTOR','MENTOR','ROOT')")
    public ApiResponse<Void> changeMyPassword(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        userProfileService.changePassword(principal.getUserId(), request);
        return ApiResponse.ok(null);
    }
}
