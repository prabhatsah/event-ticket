package com.eventbooking.api.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record CreateEventRequest(

        @NotBlank(message = "Event name is required")
        @Size(max = 200, message = "Event name must not exceed 200 characters")
        String name,

        @NotBlank(message = "Venue is required")
        @Size(max = 200, message = "Venue must not exceed 200 characters")
        String venue,

        @NotNull(message = "Event date is required")
        @Future(message = "Event date must be in the future")
        LocalDateTime eventDate,

        @NotNull(message = "Total seats are required")
        @Min(value = 1, message = "Total seats must be at least 1")
        Integer totalSeats
) {
}