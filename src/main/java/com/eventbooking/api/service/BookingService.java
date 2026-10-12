package com.eventbooking.api.service;

import com.eventbooking.api.dto.request.CreateBookingRequest;
import com.eventbooking.api.dto.response.BookingResponse;
import com.eventbooking.api.exception.BookingLockException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingLockService bookingLockService;
    private final BookingTransactionService bookingTransactionService;

    public BookingResponse createBooking(
            Long eventId,
            CreateBookingRequest request) {

        boolean lockAcquired = bookingLockService.tryLock(eventId, 5, TimeUnit.SECONDS);

        if (!lockAcquired) {
            throw new BookingLockException(eventId);
        }

        try {
            return bookingTransactionService
                    .createBooking(
                            eventId,
                            request
                    );
        } finally {
            // Always release the Redis lock.
            bookingLockService.unlock(eventId);
        }
    }

    public BookingResponse getBookingById(Long bookingId) {
        return bookingTransactionService.getBookingById(bookingId);
    }

    public List<BookingResponse> getBookingsByUserId(Long userId) {
        return bookingTransactionService.getBookingsByUserId(userId);
    }

    public BookingResponse cancelBooking(Long bookingId) {

        // first need the booking to know which event needs to be locked.
        BookingResponse booking = bookingTransactionService.getBookingById(bookingId);

        Long eventId = booking.eventId();

        boolean lockAcquired = bookingLockService.tryLock(eventId, 5, TimeUnit.SECONDS);

        if (!lockAcquired) {
            throw new BookingLockException(eventId);
        }

        try {
            return bookingTransactionService.cancelBooking(bookingId);

        } finally {
            bookingLockService.unlock(eventId);
        }
    }
}