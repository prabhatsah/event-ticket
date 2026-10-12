package com.eventbooking.api.service;

import com.eventbooking.api.dto.request.CreateBookingRequest;
import com.eventbooking.api.dto.response.BookingResponse;
import com.eventbooking.api.entity.BookingStatus;
import com.eventbooking.api.entity.Event;
import com.eventbooking.api.repository.BookingRepository;
import com.eventbooking.api.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class BookingConcurrencyTest {
    @Autowired
    private BookingService bookingService;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private BookingRepository bookingRepository;

    private Long eventId;

    @BeforeEach
    void setUp() {

        Event event = Event.builder()
                .name("Concurrency Test Event")
                .venue("Test Venue")
                .eventDate(LocalDateTime.now().plusDays(10))
                .totalSeats(10)
                .availableSeats(10)
                .build();

        Event savedEvent = eventRepository.save(event);

        eventId = savedEvent.getId();
    }

    @Test
    void shouldPreventOverBookingWhenMultipleUsersBookConcurrently()
            throws InterruptedException, ExecutionException {

        int totalUsers = 100;
        int seatsAvailable = 10;

        ExecutorService executor =
                Executors.newFixedThreadPool(100);

        CountDownLatch readyLatch =
                new CountDownLatch(totalUsers);

        CountDownLatch startLatch =
                new CountDownLatch(1);

        CountDownLatch completedLatch =
                new CountDownLatch(totalUsers);

        List<Future<BookingResult>> futures = new ArrayList<>();

        for (int i = 0; i < totalUsers; i++) {

            final long userId = i + 1;

            Future<BookingResult> future =
                    executor.submit(() -> {

                        readyLatch.countDown();

                        try {
                            /*
                             * Wait until all users are ready.
                             */
                            startLatch.await();

                            BookingResponse response =
                                    bookingService.createBooking(
                                            eventId,
                                            new CreateBookingRequest(
                                                    userId,
                                                    1
                                            )
                                    );

                            return BookingResult.success(response);
                        } catch (Exception exception) {
                            return BookingResult.failure(exception);
                        } finally {
                            completedLatch.countDown();
                        }
                    });

            futures.add(future);
        }

        /*
         * Wait until all concurrent users are ready.
         */
        assertTrue(
                readyLatch.await(10, TimeUnit.SECONDS),
                "Not all users became ready"
        );

        /*
         * Start all booking attempts.
         */
        startLatch.countDown();

        /*
         * Wait for all booking attempts to finish.
         */
        assertTrue(
                completedLatch.await(30, TimeUnit.SECONDS),
                "Not all booking attempts completed"
        );

        executor.shutdown();

        int successfulBookings = 0;
        int failedBookings = 0;

        for (Future<BookingResult> future : futures) {

            BookingResult result = future.get();

            if (result.success()) {
                successfulBookings++;
            } else {
                failedBookings++;
            }
        }

        /*
         * Only 10 users should successfully book.
         */
        assertEquals(
                seatsAvailable,
                successfulBookings,
                "Only available seats should be bookable"
        );

        /*
         * Remaining users must fail.
         */
        assertEquals(
                totalUsers - seatsAvailable,
                failedBookings,
                "Remaining booking attempts should fail"
        );

        /*
         * Verify database state.
         */
        Event event =
                eventRepository.findById(eventId)
                        .orElseThrow();

        assertEquals(
                0,
                event.getAvailableSeats(),
                "No seats should remain"
        );

        /*
         * Verify booking count.
         */
        long confirmedBookings =
                bookingRepository
                        .findByEventId(eventId)
                        .stream()
                        .filter(booking ->
                                booking.getStatus()
                                        == BookingStatus.CONFIRMED
                        )
                        .count();

        assertEquals(
                seatsAvailable,
                confirmedBookings,
                "Database should contain exactly 10 confirmed bookings"
        );
    }

    private record BookingResult(
            boolean success,
            BookingResponse response,
            Exception exception
    ) {

        static BookingResult success(
                BookingResponse response) {

            return new BookingResult(
                    true,
                    response,
                    null
            );
        }

        static BookingResult failure(
                Exception exception) {

            return new BookingResult(
                    false,
                    null,
                    exception
            );
        }
    }
}
