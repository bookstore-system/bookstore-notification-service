package com.notfound.bookstorenotificationservice.repository;

import com.notfound.bookstorenotificationservice.model.entity.NotificationPreference;
import com.notfound.bookstorenotificationservice.model.enums.NotificationChannel;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface NotificationPreferenceRepository
        extends JpaRepository<NotificationPreference, UUID>, JpaSpecificationExecutor<NotificationPreference> {

    Optional<NotificationPreference> findByUserIdAndChannel(UUID userId, NotificationChannel channel);
}

