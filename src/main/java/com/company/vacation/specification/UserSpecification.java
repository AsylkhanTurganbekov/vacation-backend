package com.company.vacation.specification;

import com.company.vacation.entity.User;
import com.company.vacation.entity.enums.Role;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public final class UserSpecification {

    private UserSpecification() {
    }

    public static Specification<User> filter(String queryText, Role role, String department, Boolean active) {
        return (root, query, criteriaBuilder) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            if (queryText != null && !queryText.isBlank()) {
                String likeValue = "%" + queryText.trim().toLowerCase() + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("fullName")), likeValue),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), likeValue),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("department")), likeValue),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("position")), likeValue)
                ));
            }
            if (role != null) {
                predicates.add(criteriaBuilder.equal(root.get("role"), role));
            }
            if (department != null && !department.isBlank()) {
                predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(root.get("department")), department.trim().toLowerCase()));
            }
            if (active != null) {
                predicates.add(criteriaBuilder.equal(root.get("active"), active));
            }
            return criteriaBuilder.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
    }
}
