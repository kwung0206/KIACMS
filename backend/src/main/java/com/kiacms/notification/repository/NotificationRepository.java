package com.kiacms.notification.repository;

import com.kiacms.notification.entity.Notification;
import com.kiacms.user.entity.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    List<Notification> findAllByRecipientOrderByCreatedAtDesc(User recipient);

    List<Notification> findAllByRecipientAndIsReadFalseOrderByCreatedAtDesc(User recipient);

    Optional<Notification> findByIdAndRecipient(UUID id, User recipient);

    long countByRecipientAndIsReadFalse(User recipient);

    void deleteAllByRecipient(User recipient);
}
