package com.kiacms.approval.repository;

import com.kiacms.approval.entity.RoleUpgradeRequest;
import com.kiacms.approval.enums.RoleUpgradeRequestStatus;
import com.kiacms.user.entity.User;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleUpgradeRequestRepository extends JpaRepository<RoleUpgradeRequest, UUID> {

    List<RoleUpgradeRequest> findAllByRequesterAndStatusOrderByCreatedAtDesc(User requester, RoleUpgradeRequestStatus status);
}
