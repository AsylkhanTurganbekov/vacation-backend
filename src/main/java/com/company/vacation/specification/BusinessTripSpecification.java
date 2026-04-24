package com.company.vacation.specification;

import com.company.vacation.entity.BusinessTrip;
import com.company.vacation.entity.enums.BusinessTripStatus;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public final class BusinessTripSpecification {

    private BusinessTripSpecification() {
    }

    public static Specification<BusinessTrip> filter(BusinessTripStatus status, Long employeeId,
                                                     LocalDateTime dateFrom, LocalDateTime dateTo) {
        return (root, query, criteriaBuilder) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }
            if (employeeId != null) {
                predicates.add(criteriaBuilder.equal(root.get("employee").get("id"), employeeId));
            }
            if (dateFrom != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("plannedStartDateTime"), dateFrom));
            }
            if (dateTo != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("plannedEndDateTime"), dateTo));
            }
            return criteriaBuilder.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
    }
}
