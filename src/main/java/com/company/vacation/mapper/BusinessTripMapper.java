package com.company.vacation.mapper;

import com.company.vacation.dto.trip.TripResponse;
import com.company.vacation.entity.BusinessTrip;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BusinessTripMapper {

    @Mapping(target = "employeeId", source = "employee.id")
    @Mapping(target = "employeeName", source = "employee.fullName")
    @Mapping(target = "employeeAvatarUrl",
            expression = "java(trip.getEmployee().getAvatarFileName() != null ? \"/api/v1/users/\" + trip.getEmployee().getId() + \"/avatar\" : null)")
    TripResponse toResponse(BusinessTrip trip);
}
