package com.eventbooking.api.exception;

public class BookingLockException extends RuntimeException {

    public BookingLockException(Long eventId) {
        super(
                "Could not acquire booking lock for event: "
                        + eventId
        );
    }
}