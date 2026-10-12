package com.eventbooking.api.service;

import com.eventbooking.api.dto.request.CreateBookingRequest;
import com.eventbooking.api.dto.response.BookingResponse;
import com.eventbooking.api.entity.Booking;
import com.eventbooking.api.entity.BookingStatus;
import com.eventbooking.api.entity.Event;
import com.eventbooking.api.exception.BookingNotFoundException;
import com.eventbooking.api.exception.EventNotFoundException;
import com.eventbooking.api.exception.InsufficientSeatsException;
import com.eventbooking.api.mapper.EventBookingMapper;
import com.eventbooking.api.repository.BookingRepository;
import com.eventbooking.api.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingTransactionService {

    private final EventRepository eventRepository;
    private final BookingRepository bookingRepository;
    private final EventBookingMapper mapper;

    @Transactional
    public BookingResponse createBooking(Long eventId, CreateBookingRequest request) {


        // PostgreSQL row-level lock.
        Event event = eventRepository.findByIdForUpdate(eventId).orElseThrow(() -> new EventNotFoundException(eventId));

        /*
         * Validate seat availability.
         */
        if (event.getAvailableSeats() < request.quantity()) {
            throw new InsufficientSeatsException(request.quantity(),
                    event.getAvailableSeats());
        }

        /*
         * Decrease available seats.
         */
        event.setAvailableSeats(event.getAvailableSeats() - request.quantity()
        );

        /*
         * Create booking.
         */
        Booking booking = mapper.toBooking(request, event);

        /*
         * Persist booking.
         */
        Booking savedBooking = bookingRepository.save(booking);

        return mapper.toBookingResponse(savedBooking);
    }

    @Transactional(readOnly = true)
    public BookingResponse getBookingById(Long bookingId) {

        Booking booking =
                bookingRepository.findById(bookingId).orElseThrow(() -> new BookingNotFoundException(bookingId));

        return mapper.toBookingResponse(booking);
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getBookingsByUserId(Long userId) {

        return bookingRepository.findByUserId(userId)
                .stream()
                .map(mapper::toBookingResponse)
                .toList();
    }

    @Transactional
    public BookingResponse cancelBooking(Long bookingId) {

        Booking booking = bookingRepository.findByIdForUpdate(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));

        /*
         * Cancellation must be idempotent. If the booking was already cancelled, do
         * not restore the seats again.If the booking
         *
         *  was already cancelled, do not restore the seats again.
         */
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            return mapper.toBookingResponse(booking);
        }

        Event event = booking.getEvent();

        /*
         * The booking row is locked above. The event row is also locked before
         * modifying its seat count.
         */
        Event lockedEvent = eventRepository.findByIdForUpdate(event.getId())
                .orElseThrow(() ->
                        new EventNotFoundException(
                                event.getId()
                        )
                );

        lockedEvent.setAvailableSeats(lockedEvent.getAvailableSeats() + booking.getQuantity());

        booking.setStatus(BookingStatus.CANCELLED);

        return mapper.toBookingResponse(booking);
    }
}