package com.kiacms.notification.controller;

import com.kiacms.global.exception.ResourceNotFoundException;
import com.kiacms.global.response.ApiResponse;
import com.kiacms.global.security.CustomUserPrincipal;
import com.kiacms.notification.dto.response.NotificationResponse;
import com.kiacms.notification.dto.response.UnreadNotificationCountResponse;
import com.kiacms.notification.service.NotificationService;
import com.kiacms.user.entity.User;
import com.kiacms.user.repository.UserRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @GetMapping
    public ApiResponse<List<NotificationResponse>> getMyNotifications(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "false") boolean unreadOnly
    ) {
        return ApiResponse.ok(notificationService.getMyNotifications(getCurrentUser(principal), unreadOnly));
    }

    @GetMapping("/unread-count")
    public ApiResponse<UnreadNotificationCountResponse> getUnreadCount(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ApiResponse.ok(notificationService.getUnreadCount(getCurrentUser(principal)));
    }

    @PatchMapping("/{notificationId}/read")
    public ApiResponse<NotificationResponse> markAsRead(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable UUID notificationId
    ) {
        return ApiResponse.ok(notificationService.markAsRead(notificationId, getCurrentUser(principal)));
    }

    @PatchMapping("/read-all")
    public ApiResponse<Void> markAllAsRead(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        notificationService.markAllAsRead(getCurrentUser(principal));
        return ApiResponse.ok();
    }

    @DeleteMapping("/{notificationId}")
    public ApiResponse<Void> deleteNotification(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable UUID notificationId
    ) {
        notificationService.deleteNotification(notificationId, getCurrentUser(principal));
        return ApiResponse.ok();
    }

    @DeleteMapping
    public ApiResponse<Void> deleteAllNotifications(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        notificationService.deleteAllNotifications(getCurrentUser(principal));
        return ApiResponse.ok();
    }

    private User getCurrentUser(CustomUserPrincipal principal) {
        return userRepository.findByIdAndDeletedAtIsNull(principal.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found."));
    }
}
