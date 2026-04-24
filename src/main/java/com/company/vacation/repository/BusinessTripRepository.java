package com.company.vacation.repository;

import com.company.vacation.entity.BusinessTrip;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface BusinessTripRepository extends JpaRepository<BusinessTrip, Long>, JpaSpecificationExecutor<BusinessTrip> {

    Optional<BusinessTrip> findByIdAndEmployee_Id(Long id, Long employeeId);
}
