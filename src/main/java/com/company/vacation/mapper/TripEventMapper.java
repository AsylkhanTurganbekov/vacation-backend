package com.company.vacation.mapper;

import com.company.vacation.dto.trip.TripEventResponse;
import com.company.vacation.entity.TripEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TripEventMapper {

    @Mapping(target = "tripId", source = "trip.id")
    TripEventResponse toResponse(TripEvent tripEvent);
}
