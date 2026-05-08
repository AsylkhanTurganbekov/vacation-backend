package com.company.vacation.specification;

import static org.assertj.core.api.Assertions.assertThat;

import com.company.vacation.entity.BusinessTrip;
import com.company.vacation.entity.User;
import com.company.vacation.entity.enums.BusinessTripStatus;
import com.company.vacation.entity.enums.Role;
import com.company.vacation.repository.BusinessTripRepository;
import com.company.vacation.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Sort;

@DataJpaTest(properties = {
        "spring.liquibase.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class BusinessTripSpecificationDateRangeTest {

    @Autowired
    private BusinessTripRepository businessTripRepository;

    @Autowired
    private UserRepository userRepository;

    private User employee;

    @BeforeEach
    void setUp() {
        businessTripRepository.deleteAll();
        userRepository.deleteAll();
        employee = new User();
        employee.setFullName("Calendar Employee");
        employee.setEmail("calendar.employee@vacation.local");
        employee.setPasswordHash("hash");
        employee.setRole(Role.EMPLOYEE);
        employee.setDepartment("Field Service");
        employee.setPosition("Field Engineer");
        employee.setActive(true);
        employee = userRepository.save(employee);
    }

    @Test
    void shouldIncludeTripFullyInsideRequestedMonth() {
        BusinessTrip trip = saveTrip(
                "Inside month",
                LocalDateTime.of(2026, 5, 1, 9, 0),
                LocalDateTime.of(2026, 5, 3, 18, 0)
        );

        List<BusinessTrip> result = findForRange(
                LocalDateTime.of(2026, 5, 1, 0, 0),
                LocalDateTime.of(2026, 5, 31, 23, 59)
        );

        assertThat(result).extracting(BusinessTrip::getId).containsExactly(trip.getId());
    }

    @Test
    void shouldIncludeTripStartedInPreviousMonthAndContinuesIntoRequestedMonth() {
        BusinessTrip trip = saveTrip(
                "Crosses from previous month",
                LocalDateTime.of(2026, 4, 29, 9, 0),
                LocalDateTime.of(2026, 5, 2, 18, 0)
        );

        List<BusinessTrip> result = findForRange(
                LocalDateTime.of(2026, 5, 1, 0, 0),
                LocalDateTime.of(2026, 5, 31, 23, 59)
        );

        assertThat(result).extracting(BusinessTrip::getId).containsExactly(trip.getId());
    }

    @Test
    void shouldIncludeTripEndingInNextMonthWhenItStartsInRequestedMonth() {
        BusinessTrip trip = saveTrip(
                "Crosses into next month",
                LocalDateTime.of(2026, 5, 30, 9, 0),
                LocalDateTime.of(2026, 6, 2, 18, 0)
        );

        List<BusinessTrip> result = findForRange(
                LocalDateTime.of(2026, 5, 1, 0, 0),
                LocalDateTime.of(2026, 5, 31, 23, 59)
        );

        assertThat(result).extracting(BusinessTrip::getId).containsExactly(trip.getId());
    }

    @Test
    void shouldExcludeTripFullyBeforeRequestedMonth() {
        saveTrip(
                "Before month",
                LocalDateTime.of(2026, 4, 1, 9, 0),
                LocalDateTime.of(2026, 4, 5, 18, 0)
        );

        List<BusinessTrip> result = findForRange(
                LocalDateTime.of(2026, 5, 1, 0, 0),
                LocalDateTime.of(2026, 5, 31, 23, 59)
        );

        assertThat(result).isEmpty();
    }

    @Test
    void shouldExcludeTripFullyAfterRequestedMonth() {
        saveTrip(
                "After month",
                LocalDateTime.of(2026, 6, 1, 9, 0),
                LocalDateTime.of(2026, 6, 5, 18, 0)
        );

        List<BusinessTrip> result = findForRange(
                LocalDateTime.of(2026, 5, 1, 0, 0),
                LocalDateTime.of(2026, 5, 31, 23, 59)
        );

        assertThat(result).isEmpty();
    }

    private BusinessTrip saveTrip(String purpose, LocalDateTime start, LocalDateTime end) {
        BusinessTrip trip = new BusinessTrip();
        trip.setEmployee(employee);
        trip.setPurpose(purpose);
        trip.setDestinationAddress("Astana");
        trip.setPlannedStartDateTime(start);
        trip.setPlannedEndDateTime(end);
        trip.setStatus(BusinessTripStatus.APPROVED);
        return businessTripRepository.save(trip);
    }

    private List<BusinessTrip> findForRange(LocalDateTime dateFrom, LocalDateTime dateTo) {
        return businessTripRepository.findAll(
                BusinessTripSpecification.filter(null, null, employee.getId(), null, dateFrom, dateTo),
                Sort.by("plannedStartDateTime")
        );
    }
}
