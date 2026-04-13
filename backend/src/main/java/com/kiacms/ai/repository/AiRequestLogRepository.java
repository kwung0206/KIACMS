package com.kiacms.ai.repository;

import com.kiacms.ai.entity.AiRequestLog;
import com.kiacms.ai.enums.AiFeatureType;
import com.kiacms.user.entity.User;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiRequestLogRepository extends JpaRepository<AiRequestLog, UUID> {

    List<AiRequestLog> findAllByRequesterOrderByCreatedAtDesc(User requester);

    List<AiRequestLog> findAllByFeatureTypeOrderByCreatedAtDesc(AiFeatureType featureType);
}
