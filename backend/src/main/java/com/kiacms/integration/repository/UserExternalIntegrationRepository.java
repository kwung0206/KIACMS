package com.kiacms.integration.repository;

import com.kiacms.integration.entity.UserExternalIntegration;
import com.kiacms.integration.enums.ExternalIntegrationProvider;
import com.kiacms.user.entity.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserExternalIntegrationRepository extends JpaRepository<UserExternalIntegration, UUID> {

    Optional<UserExternalIntegration> findByUserAndProvider(User user, ExternalIntegrationProvider provider);
}
