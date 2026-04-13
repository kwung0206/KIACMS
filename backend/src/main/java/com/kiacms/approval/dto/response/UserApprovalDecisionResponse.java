package com.kiacms.approval.dto.response;

import com.kiacms.user.entity.User;
import com.kiacms.user.enums.RoleType;
import com.kiacms.user.enums.UserStatus;
import java.time.Instant;
import java.util.UUID;

public record UserApprovalDecisionResponse(
        UUID userId,
        String email,
        String name,
        RoleType roleType,
        UserStatus status,
        String reason,
        Instant reviewedAt
) {
    public static UserApprovalDecisionResponse from(User user) {
        return new UserApprovalDecisionResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRoleType(),
                user.getStatus(),
                user.getAccountStatusReason(),
                user.getReviewedAt()
        );
    }
}
