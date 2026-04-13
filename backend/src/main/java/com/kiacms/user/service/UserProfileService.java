package com.kiacms.user.service;

import com.kiacms.global.exception.BusinessException;
import com.kiacms.global.exception.ConflictException;
import com.kiacms.global.exception.ErrorCode;
import com.kiacms.global.exception.ResourceNotFoundException;
import com.kiacms.user.dto.request.ChangePasswordRequest;
import com.kiacms.user.dto.request.UpdateMyProfileRequest;
import com.kiacms.user.dto.response.MyProfileResponse;
import com.kiacms.user.entity.User;
import com.kiacms.user.repository.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public MyProfileResponse getMyProfile(UUID userId) {
        return MyProfileResponse.from(getActiveUser(userId));
    }

    @Transactional
    public MyProfileResponse updateMyProfile(UUID userId, UpdateMyProfileRequest request) {
        User user = getActiveUser(userId);
        String normalizedPhoneNumber = blankToNull(request.phoneNumber());

        if (normalizedPhoneNumber != null
                && userRepository.existsByPhoneNumberAndIdNotAndDeletedAtIsNull(normalizedPhoneNumber, user.getId())) {
            throw new ConflictException("이미 사용 중인 전화번호입니다.");
        }

        user.setName(request.name().trim());
        user.setPhoneNumber(normalizedPhoneNumber);
        user.setBio(blankToNull(request.bio()));

        return MyProfileResponse.from(user);
    }

    @Transactional
    public void changePassword(UUID userId, ChangePasswordRequest request) {
        User user = getActiveUser(userId);

        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS, "현재 비밀번호가 올바르지 않습니다.");
        }

        if (passwordEncoder.matches(request.newPassword(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "새 비밀번호는 현재 비밀번호와 달라야 합니다.");
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
    }

    private User getActiveUser(UUID userId) {
        return userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
