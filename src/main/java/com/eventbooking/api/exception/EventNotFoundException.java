package com.eventbooking.api.exception;

public class EventNotFoundException extends RuntimeException {

    public EventNotFoundException(Long eventId) {
        super("Event not found with id: " + eventId);
    }
}