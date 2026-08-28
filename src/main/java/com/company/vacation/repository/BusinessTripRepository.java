package com.company.vacation.repository;

import com.company.vacation.entity.BusinessTrip;
import com.company.vacation.entity.enums.BusinessTripStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.LockModeType;

public interface BusinessTripRepository extends JpaRepository<BusinessTrip, Long>, JpaSpecificationExecutor<BusinessTrip> {

    @Override
    @EntityGraph(attributePaths = "employee")
    Page<BusinessTrip> findAll(Specification<BusinessTrip> specification, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = "employee")
    List<BusinessTrip> findAll(Specification<BusinessTrip> specification, Sort sort);

    @Override
    @EntityGraph(attributePaths = "employee")
    Optional<BusinessTrip> findById(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = "employee")
    Optional<BusinessTrip> findWithLockById(Long id);

    @EntityGraph(attributePaths = "employee")
    Optional<BusinessTrip> findByIdAndEmployee_Id(Long id, Long employeeId);

    @EntityGraph(attributePaths = "employee")
    Optional<BusinessTrip> findByExternalTripId(String externalTripId);

    long countByEmployee_Id(Long employeeId);

    long countByEmployee_IdAndStatus(Long employeeId, BusinessTripStatus status);

    @EntityGraph(attributePaths = "employee")
    Optional<BusinessTrip> findTopByEmployee_IdOrderByPlannedStartDateTimeDesc(Long employeeId);
}
