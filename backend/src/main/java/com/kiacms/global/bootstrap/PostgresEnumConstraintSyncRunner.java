package com.kiacms.global.bootstrap;

import com.kiacms.ai.enums.AiFeatureType;
import com.kiacms.notification.enums.NotificationType;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Order(10)
@RequiredArgsConstructor
public class PostgresEnumConstraintSyncRunner implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        syncCheckConstraint(
                "ai_request_logs",
                "ai_request_logs_feature_type_check",
                "feature_type",
                Arrays.stream(AiFeatureType.values()).map(Enum::name).toList()
        );
        syncCheckConstraint(
                "notifications",
                "notifications_type_check",
                "type",
                Arrays.stream(NotificationType.values()).map(Enum::name).toList()
        );
    }

    private void syncCheckConstraint(
            String tableName,
            String constraintName,
            String columnName,
            Iterable<String> enumValues
    ) {
        try {
            String joinedValues = toSqlLiteralList(enumValues);

            jdbcTemplate.execute("ALTER TABLE %s DROP CONSTRAINT IF EXISTS %s"
                    .formatted(tableName, constraintName));
            jdbcTemplate.execute(
                    "ALTER TABLE %s ADD CONSTRAINT %s CHECK (%s IN (%s))"
                            .formatted(tableName, constraintName, columnName, joinedValues)
            );
            log.info("Synchronized enum check constraint {} on {}.{}", constraintName, tableName, columnName);
        } catch (Exception exception) {
            log.warn(
                    "Failed to synchronize enum check constraint {} on {}.{}: {}",
                    constraintName,
                    tableName,
                    columnName,
                    exception.getMessage()
            );
        }
    }

    private String toSqlLiteralList(Iterable<String> enumValues) {
        return StreamSupport.stream(enumValues.spliterator(), false)
                .map(value -> "'" + value.replace("'", "''") + "'")
                .collect(Collectors.joining(", "));
    }
}
