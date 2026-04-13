package com.kiacms.approval.entity;

import com.kiacms.approval.enums.RoleUpgradeRequestStatus;
import com.kiacms.global.entity.BaseEntity;
import com.kiacms.user.entity.User;
import com.kiacms.user.enums.RoleType;
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
        name = "role_upgrade_requests",
        indexes = {
                @Index(name = "idx_rur_requester_status", columnList = "requester_id, status"),
                @Index(name = "idx_rur_requested_role", columnList = "requested_role, status")
        }
)
public class RoleUpgradeRequest extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_role", nullable = false, length = 30)
    private RoleType currentRole;

    @Enumerated(EnumType.STRING)
    @Column(name = "requested_role", nullable = false, length = 30)
    private RoleType requestedRole;

    @Column(name = "request_reason", nullable = false, columnDefinition = "text")
    private String requestReason;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private RoleUpgradeRequestStatus status = RoleUpgradeRequestStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by_id")
    private User reviewedBy;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    @Column(name = "rejection_reason", columnDefinition = "text")
    private String rejectionReason;

    public void approve(User reviewer) {
        this.status = RoleUpgradeRequestStatus.APPROVED;
        this.reviewedBy = reviewer;
        this.reviewedAt = Instant.now();
        this.rejectionReason = null;
    }

    public void reject(User reviewer, String reason) {
        this.status = RoleUpgradeRequestStatus.REJECTED;
        this.reviewedBy = reviewer;
        this.reviewedAt = Instant.now();
        this.rejectionReason = reason;
    }

    public void cancel() {
        this.status = RoleUpgradeRequestStatus.CANCELLED;
    }
}
