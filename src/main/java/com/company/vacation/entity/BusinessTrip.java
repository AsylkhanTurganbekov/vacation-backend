package com.company.vacation.entity;

import com.company.vacation.entity.enums.BusinessTripStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "business_trips")
public class BusinessTrip extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private User employee;

    @Column(nullable = false, length = 500)
    private String purpose;

    @Column(name = "destination_address", nullable = false, length = 500)
    private String destinationAddress;

    @Column(name = "planned_start_date_time", nullable = false)
    private LocalDateTime plannedStartDateTime;

    @Column(name = "planned_end_date_time", nullable = false)
    private LocalDateTime plannedEndDateTime;

    @Column(name = "actual_start_date_time")
    private LocalDateTime actualStartDateTime;

    @Column(name = "actual_arrival_date_time")
    private LocalDateTime actualArrivalDateTime;

    @Column(name = "actual_return_date_time")
    private LocalDateTime actualReturnDateTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BusinessTripStatus status = BusinessTripStatus.DRAFT;
}
