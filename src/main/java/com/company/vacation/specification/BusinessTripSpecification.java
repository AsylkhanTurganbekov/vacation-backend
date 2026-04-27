package com.company.vacation.specification;

import com.company.vacation.entity.BusinessTrip;
import com.company.vacation.entity.enums.BusinessTripStatus;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public final class BusinessTripSpecification {

    private BusinessTripSpecification() {
    }

    public static Specification<BusinessTrip> filter(String queryText, BusinessTripStatus status, Long employeeId,
                                                     String department, LocalDateTime dateFrom, LocalDateTime dateTo) {
        return filter(queryText, status, employeeId != null ? List.of(employeeId) : null, department, dateFrom, dateTo);
    }

    public static Specification<BusinessTrip> filter(String queryText, BusinessTripStatus status,
                                                     Collection<Long> employeeIds, String department,
                                                     LocalDateTime dateFrom, LocalDateTime dateTo) {
        return (root, query, criteriaBuilder) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            if (queryText != null && !queryText.isBlank()) {
                String likeValue = "%" + queryText.trim().toLowerCase() + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("purpose")), likeValue),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("destinationAddress")), likeValue),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("employee").get("fullName")), likeValue)
                ));
            }
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }
            if (employeeIds != null && !employeeIds.isEmpty()) {
                predicates.add(root.get("employee").get("id").in(employeeIds));
            }
            if (department != null && !department.isBlank()) {
                predicates.add(criteriaBuilder.equal(
                        criteriaBuilder.lower(root.get("employee").get("department")),
                        department.trim().toLowerCase()
                ));
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
