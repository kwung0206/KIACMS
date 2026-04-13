package com.kiacms.user.entity;

import com.kiacms.global.entity.BaseEntity;
import com.kiacms.user.enums.RoleType;
import com.kiacms.user.enums.UserStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(
        name = "users",
        indexes = {
                @Index(name = "idx_users_status_role", columnList = "status, role_type"),
                @Index(name = "idx_users_reviewer", columnList = "reviewed_by_id")
        }
)
public class User extends BaseEntity {

    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "phone_number", unique = true, length = 20)
    private String phoneNumber;

    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;

    @Column(name = "bio", columnDefinition = "text")
    private String bio;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_type", nullable = false, length = 30)
    private RoleType roleType;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private UserStatus status = UserStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by_id")
    private User reviewedBy;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    @Column(name = "account_status_reason", columnDefinition = "text")
    private String accountStatusReason;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    public void approve(User reviewer) {
        this.status = UserStatus.APPROVED;
        this.reviewedBy = reviewer;
        this.reviewedAt = Instant.now();
        this.accountStatusReason = null;
    }

    public void reject(User reviewer, String reason) {
        this.status = UserStatus.REJECTED;
        this.reviewedBy = reviewer;
        this.reviewedAt = Instant.now();
        this.accountStatusReason = reason;
    }

    public void withdraw(String reason) {
        this.status = UserStatus.WITHDRAWN;
        this.deletedAt = Instant.now();
        this.accountStatusReason = reason;
    }
}
