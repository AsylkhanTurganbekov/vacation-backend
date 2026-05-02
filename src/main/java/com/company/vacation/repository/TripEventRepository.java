package com.company.vacation.repository;

import com.company.vacation.entity.TripEvent;
import com.company.vacation.entity.enums.TripEventType;
import com.company.vacation.entity.enums.VerificationStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

public interface TripEventRepository extends JpaRepository<TripEvent, Long> {

    @EntityGraph(attributePaths = "trip.employee")
    List<TripEvent> findByTrip_IdOrderByEventTimeAsc(Long tripId);

    boolean existsByTrip_IdAndType(Long tripId, TripEventType type);

    Optional<TripEvent> findByTrip_IdAndType(Long tripId, TripEventType type);

    @EntityGraph(attributePaths = "trip.employee")
    Optional<TripEvent> findWithTripAndEmployeeById(Long id);

    @EntityGraph(attributePaths = "trip.employee")
    Optional<TripEvent> findTopByTrip_IdOrderByEventTimeDescCreatedAtDesc(Long tripId);

    long countByVerificationStatus(com.company.vacation.entity.enums.VerificationStatus verificationStatus);

    long countByTrip_Employee_Id(Long employeeId);

    long countByTrip_Employee_IdAndVerificationStatus(Long employeeId, VerificationStatus verificationStatus);

    @EntityGraph(attributePaths = "trip.employee")
    Optional<TripEvent> findTopByTrip_Employee_IdOrderByEventTimeDescCreatedAtDesc(Long employeeId);
}
