package com.company.vacation.mapper;

import com.company.vacation.dto.trip.TripEventResponse;
import com.company.vacation.entity.TripEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TripEventMapper {

    @Mapping(target = "tripId", source = "trip.id")
    @Mapping(target = "imageUrl",
            expression = "java(tripEvent.getImageFileName() != null ? \"/api/v1/trip-events/\" + tripEvent.getId() + \"/image\" : null)")
    TripEventResponse toResponse(TripEvent tripEvent);
}
