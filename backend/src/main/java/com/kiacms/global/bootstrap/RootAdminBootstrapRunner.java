package com.kiacms.global.bootstrap;

import com.kiacms.global.config.properties.RootBootstrapProperties;
import com.kiacms.user.entity.User;
import com.kiacms.user.entity.UserSettings;
import com.kiacms.user.enums.RoleType;
import com.kiacms.user.enums.UserStatus;
import com.kiacms.user.repository.UserRepository;
import com.kiacms.user.repository.UserSettingsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class RootAdminBootstrapRunner implements CommandLineRunner {

    private final RootBootstrapProperties properties;
    private final UserRepository userRepository;
    private final UserSettingsRepository userSettingsRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (!properties.enabled()) {
            return;
        }

        if (properties.email() == null || properties.email().isBlank()
                || properties.password() == null || properties.password().isBlank()) {
            log.warn("Root bootstrap is enabled but email/password are missing. Skipping root bootstrap.");
            return;
        }

        if (userRepository.findByEmail(properties.email()).isPresent()) {
            return;
        }

        User rootUser = userRepository.save(User.builder()
                .email(properties.email())
                .passwordHash(passwordEncoder.encode(properties.password()))
                .name(properties.name())
                .roleType(RoleType.ROOT)
                .status(UserStatus.APPROVED)
                .build());

        userSettingsRepository.save(UserSettings.builder().user(rootUser).build());
        log.info("Local root user bootstrap completed for {}", properties.email());
    }
}
