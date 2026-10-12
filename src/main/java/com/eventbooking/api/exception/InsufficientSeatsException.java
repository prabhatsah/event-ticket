package com.eventbooking.api.exception;

public class InsufficientSeatsException extends RuntimeException {

    public InsufficientSeatsException(
            Integer requested,
            Integer available) {

        super(
                "Insufficient seats. Requested: "
                        + requested
                        + ", Available: "
                        + available
        );
    }
}