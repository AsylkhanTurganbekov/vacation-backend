package com.company.vacation.repository;

import com.company.vacation.entity.BusinessTrip;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.domain.Specification;

public interface BusinessTripRepository extends JpaRepository<BusinessTrip, Long>, JpaSpecificationExecutor<BusinessTrip> {

    @Override
    @EntityGraph(attributePaths = "employee")
    Page<BusinessTrip> findAll(Specification<BusinessTrip> specification, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = "employee")
    Optional<BusinessTrip> findById(Long id);

    @EntityGraph(attributePaths = "employee")
    Optional<BusinessTrip> findByIdAndEmployee_Id(Long id, Long employeeId);
}
