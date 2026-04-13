package com.kiacms.global.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.bootstrap.root")
public record RootBootstrapProperties(
        boolean enabled,
        String email,
        String password,
        String name
) {
}
