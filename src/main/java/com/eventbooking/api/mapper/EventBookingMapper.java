package com.eventbooking.api.mapper;

import com.eventbooking.api.dto.request.CreateBookingRequest;
import com.eventbooking.api.dto.request.CreateEventRequest;
import com.eventbooking.api.dto.response.BookingResponse;
import com.eventbooking.api.dto.response.EventResponse;
import com.eventbooking.api.entity.Booking;
import com.eventbooking.api.entity.BookingStatus;
import com.eventbooking.api.entity.Event;
import org.springframework.stereotype.Component;

@Component
public class EventBookingMapper {

    public Event toEvent(CreateEventRequest request) {
        return Event.builder()
                .name(request.name())
                .venue(request.venue())
                .eventDate(request.eventDate())
                .totalSeats(request.totalSeats())
                .availableSeats(request.totalSeats())
                .build();
    }

    public EventResponse toEventResponse(Event event) {
        return new EventResponse(
                event.getId(),
                event.getName(),
                event.getVenue(),
                event.getEventDate(),
                event.getTotalSeats(),
                event.getAvailableSeats(),
                event.getCreatedAt()
        );
    }

    public Booking toBooking(
            CreateBookingRequest request,
            Event event) {

        return Booking.builder()
                .userId(request.userId())
                .event(event)
                .quantity(request.quantity())
                .status(BookingStatus.CONFIRMED)
                .build();
    }

    public BookingResponse toBookingResponse(Booking booking) {
        return new BookingResponse(
                booking.getId(),
                booking.getUserId(),
                booking.getEvent().getId(),
                booking.getQuantity(),
                booking.getStatus(),
                booking.getCreatedAt()
        );
    }
}