package com.kiacms.auth.dto.response;

import com.kiacms.user.entity.User;
import com.kiacms.user.enums.RoleType;
import com.kiacms.user.enums.UserStatus;
import java.util.UUID;

public record AuthUserResponse(
        UUID id,
        String email,
        String name,
        RoleType roleType,
        UserStatus status
) {
    public static AuthUserResponse from(User user) {
        return new AuthUserResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRoleType(),
                user.getStatus()
        );
    }
}
