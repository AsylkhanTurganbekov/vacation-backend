package com.company.vacation.controller;

import com.company.vacation.service.TripEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/trip-events")
@RequiredArgsConstructor
public class TripEventImageController {

    private final TripEventService tripEventService;

    @GetMapping("/{eventId}/image")
    public ResponseEntity<Resource> getEventImage(@PathVariable Long eventId) {
        Resource image = tripEventService.getTripEventImage(eventId);
        MediaType mediaType = MediaTypeFactory.getMediaType(image)
                .orElse(MediaType.APPLICATION_OCTET_STREAM);
        return ResponseEntity.ok()
                .contentType(mediaType)
                .body(image);
    }
}
