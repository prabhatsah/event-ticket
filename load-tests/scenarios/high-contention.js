import { check } from 'k6';

import { Counter } from 'k6/metrics';

import { createBooking } from '../helpers/booking.js';
import { config } from '../config.js';

const successfulBookings = new Counter('successful_bookings');

const insufficientSeats = new Counter('insufficient_seats');

const lockTimeouts = new Counter('lock_timeouts');

const unexpectedResponses = new Counter('unexpected_responses');

export const options = {

    scenarios: {
        concurrent_booking: {
            executor: 'per-vu-iterations',
            vus: 1000,
            iterations: 1,
            maxDuration: '2m',
        },
    },

    thresholds: {
        unexpected_responses: ['count==0'],
    },
};

export default function () {

    const userId = (__VU * 1000000) + __ITER + 1;

    const response = createBooking(
        config.eventId,
        userId,
        1
    );

    if (response.status === 201) {
        successfulBookings.add(1);
    } else if (response.status === 409) {
        let errorCode = '';

        try {
            errorCode = response.json('error');
        } catch (error) {
            errorCode = '';
        }

        if (errorCode === 'INSUFFICIENT_SEATS') {
            insufficientSeats.add(1);
        } else if (errorCode === 'BOOKING_LOCK_UNAVAILABLE') {
            lockTimeouts.add(1);
        } else {
            unexpectedResponses.add(1);
        }
    } else {
        unexpectedResponses.add(1);
    }

    check(response, {
        'expected booking response': (r) =>
            r.status === 201 ||
            (
                r.status === 409 &&
                (
                    r.json('error') === 'INSUFFICIENT_SEATS' ||
                    r.json('error') === 'BOOKING_LOCK_UNAVAILABLE'
                )
            ),
    });
}