package com.kiacms.user.dto.response;

import com.kiacms.user.entity.User;
import com.kiacms.user.enums.RoleType;
import com.kiacms.user.enums.UserStatus;
import java.time.Instant;
import java.util.UUID;

public record MyProfileResponse(
        UUID id,
        String email,
        String name,
        String phoneNumber,
        String profileImageUrl,
        String bio,
        RoleType roleType,
        UserStatus status,
        Instant lastLoginAt
) {
    public static MyProfileResponse from(User user) {
        return new MyProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getPhoneNumber(),
                user.getProfileImageUrl(),
                user.getBio(),
                user.getRoleType(),
                user.getStatus(),
                user.getLastLoginAt()
        );
    }
}
