package com.kiacms.auth.service;

import com.kiacms.auth.dto.request.LoginRequest;
import com.kiacms.auth.dto.request.SignUpRequest;
import com.kiacms.auth.dto.response.AuthUserResponse;
import com.kiacms.auth.dto.response.LoginResponse;
import com.kiacms.auth.dto.response.SignUpResponse;
import com.kiacms.global.exception.BusinessException;
import com.kiacms.global.exception.ConflictException;
import com.kiacms.global.exception.ErrorCode;
import com.kiacms.global.security.CustomUserPrincipal;
import com.kiacms.global.security.JwtTokenProvider;
import com.kiacms.user.entity.User;
import com.kiacms.user.entity.UserSettings;
import com.kiacms.user.enums.RoleType;
import com.kiacms.user.enums.UserStatus;
import com.kiacms.user.repository.UserRepository;
import com.kiacms.user.repository.UserSettingsRepository;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserSettingsRepository userSettingsRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public SignUpResponse signUp(SignUpRequest request) {
        validateSignupRole(request.roleType());

        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email is already registered.");
        }

        User user = User.builder()
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .name(request.name())
                .phoneNumber(blankToNull(request.phoneNumber()))
                .roleType(request.roleType())
                .status(UserStatus.PENDING)
                .build();

        User savedUser = userRepository.save(user);
        userSettingsRepository.save(UserSettings.builder().user(savedUser).build());

        return new SignUpResponse(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getName(),
                savedUser.getRoleType(),
                savedUser.getStatus(),
                "가입 신청이 완료되었습니다. Root 관리자 승인 전까지는 주요 기능을 사용할 수 없습니다."
        );
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        if (user.getDeletedAt() != null || user.getStatus() == UserStatus.WITHDRAWN) {
            throw new BusinessException(ErrorCode.ACCOUNT_WITHDRAWN);
        }

        if (user.getStatus() == UserStatus.PENDING) {
            throw new BusinessException(ErrorCode.ACCOUNT_PENDING_APPROVAL);
        }

        if (user.getStatus() == UserStatus.REJECTED) {
            String message = user.getAccountStatusReason() == null || user.getAccountStatusReason().isBlank()
                    ? ErrorCode.ACCOUNT_REJECTED.getMessage()
                    : user.getAccountStatusReason();
            throw new BusinessException(ErrorCode.ACCOUNT_REJECTED, message);
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );

            CustomUserPrincipal principal = (CustomUserPrincipal) authentication.getPrincipal();
            user.setLastLoginAt(Instant.now());

            String accessToken = jwtTokenProvider.createAccessToken(principal);
            return new LoginResponse(
                    "Bearer",
                    accessToken,
                    jwtTokenProvider.getAccessTokenValiditySeconds(),
                    AuthUserResponse.from(user)
            );
        } catch (BadCredentialsException ex) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }
    }

    private void validateSignupRole(RoleType roleType) {
        if (roleType == RoleType.ROOT) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "ROOT role cannot be selected during signup.");
        }
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
