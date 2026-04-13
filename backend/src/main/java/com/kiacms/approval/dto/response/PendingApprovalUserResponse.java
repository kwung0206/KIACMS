package com.kiacms.approval.dto.response;

import com.kiacms.user.entity.User;
import com.kiacms.user.enums.RoleType;
import com.kiacms.user.enums.UserStatus;
import java.time.Instant;
import java.util.UUID;

public record PendingApprovalUserResponse(
        UUID id,
        String email,
        String name,
        RoleType roleType,
        UserStatus status,
        String phoneNumber,
        Instant createdAt
) {
    public static PendingApprovalUserResponse from(User user) {
        return new PendingApprovalUserResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRoleType(),
                user.getStatus(),
                user.getPhoneNumber(),
                user.getCreatedAt()
        );
    }
}
