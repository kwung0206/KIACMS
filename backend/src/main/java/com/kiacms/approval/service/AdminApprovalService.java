package com.kiacms.approval.service;

import com.kiacms.approval.dto.response.PendingApprovalUserResponse;
import com.kiacms.approval.dto.response.UserApprovalDecisionResponse;
import com.kiacms.global.exception.BusinessException;
import com.kiacms.global.exception.ErrorCode;
import com.kiacms.global.exception.ResourceNotFoundException;
import com.kiacms.notification.service.NotificationService;
import com.kiacms.user.entity.User;
import com.kiacms.user.enums.RoleType;
import com.kiacms.user.enums.UserStatus;
import com.kiacms.user.repository.UserRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminApprovalService {

    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public List<PendingApprovalUserResponse> getPendingUsers() {
        return userRepository.findAllByStatusAndDeletedAtIsNullOrderByCreatedAtAsc(UserStatus.PENDING).stream()
                .map(PendingApprovalUserResponse::from)
                .toList();
    }

    @Transactional
    public UserApprovalDecisionResponse approveUser(UUID userId, UUID rootUserId) {
        User targetUser = findUserForApproval(userId);
        User reviewer = findRootReviewer(rootUserId);

        targetUser.approve(reviewer);
        notificationService.createApprovalResultNotification(targetUser);
        return UserApprovalDecisionResponse.from(targetUser);
    }

    @Transactional
    public UserApprovalDecisionResponse rejectUser(UUID userId, UUID rootUserId, String reason) {
        User targetUser = findUserForApproval(userId);
        User reviewer = findRootReviewer(rootUserId);

        targetUser.reject(reviewer, reason);
        notificationService.createApprovalResultNotification(targetUser);
        return UserApprovalDecisionResponse.from(targetUser);
    }

    private User findUserForApproval(UUID userId) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        if (user.getStatus() != UserStatus.PENDING) {
            throw new BusinessException(ErrorCode.DATA_CONFLICT, "Only pending users can be reviewed.");
        }

        return user;
    }

    private User findRootReviewer(UUID rootUserId) {
        User reviewer = userRepository.findByIdAndDeletedAtIsNull(rootUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Reviewer not found."));

        if (reviewer.getRoleType() != RoleType.ROOT) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED, "Only root administrators can review users.");
        }

        return reviewer;
    }
}
