package com.kiacms.auth.dto.response;

import com.kiacms.user.enums.RoleType;
import com.kiacms.user.enums.UserStatus;
import java.util.UUID;

public record SignUpResponse(
        UUID userId,
        String email,
        String name,
        RoleType roleType,
        UserStatus status,
        String message
) {
}
