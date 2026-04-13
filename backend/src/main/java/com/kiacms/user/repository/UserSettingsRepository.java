package com.kiacms.user.repository;

import com.kiacms.user.entity.User;
import com.kiacms.user.entity.UserSettings;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSettingsRepository extends JpaRepository<UserSettings, UUID> {

    Optional<UserSettings> findByUser(User user);
}
