package com.company.vacation.repository;

import com.company.vacation.entity.UserNotification;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserNotificationRepository extends JpaRepository<UserNotification, Long> {

    Page<UserNotification> findByUser_IdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Optional<UserNotification> findByUser_IdAndEventKey(Long userId, String eventKey);
}
