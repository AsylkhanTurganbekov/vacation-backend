package com.company.vacation.repository;

import com.company.vacation.entity.UserDevice;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserDeviceRepository extends JpaRepository<UserDevice, Long> {

    Optional<UserDevice> findByPushToken(String pushToken);

    Optional<UserDevice> findByUser_IdAndDeviceId(Long userId, String deviceId);

    Optional<UserDevice> findByUser_IdAndPushToken(Long userId, String pushToken);

    List<UserDevice> findByUser_IdInAndActiveTrue(Collection<Long> userIds);
}
