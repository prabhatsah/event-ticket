package com.eventbooking.api.dto.response;

import java.time.LocalDateTime;

public record EventResponse(
        Long id,
        String name,
        String venue,
        LocalDateTime eventDate,
        Integer totalSeats,
        Integer availableSeats,
        LocalDateTime createdAt
) {
}