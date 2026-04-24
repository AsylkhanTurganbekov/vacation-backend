package com.company.vacation.repository;

import com.company.vacation.entity.TripEvent;
import com.company.vacation.entity.enums.TripEventType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TripEventRepository extends JpaRepository<TripEvent, Long> {

    List<TripEvent> findByTrip_IdOrderByEventTimeAsc(Long tripId);

    boolean existsByTrip_IdAndType(Long tripId, TripEventType type);

    Optional<TripEvent> findByTrip_IdAndType(Long tripId, TripEventType type);

    long countByVerificationStatus(com.company.vacation.entity.enums.VerificationStatus verificationStatus);
}
