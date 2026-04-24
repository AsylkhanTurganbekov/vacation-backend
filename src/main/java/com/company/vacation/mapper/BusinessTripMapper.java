package com.company.vacation.mapper;

import com.company.vacation.dto.trip.TripResponse;
import com.company.vacation.entity.BusinessTrip;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BusinessTripMapper {

    @Mapping(target = "employeeId", source = "employee.id")
    @Mapping(target = "employeeName", source = "employee.fullName")
    TripResponse toResponse(BusinessTrip trip);
}
