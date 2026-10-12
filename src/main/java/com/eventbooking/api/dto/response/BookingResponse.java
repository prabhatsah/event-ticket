package com.eventbooking.api.dto.response;

import com.eventbooking.api.entity.BookingStatus;

import java.time.LocalDateTime;

public record BookingResponse(
        Long id,
        Long userId,
        Long eventId,
        Integer quantity,
        BookingStatus status,
        LocalDateTime createdAt
) {
}