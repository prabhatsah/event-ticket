import http from 'k6/http';
import { config } from '../config.js';

export function createBooking(eventId, userId, quantity = 1) {

    const url = `${config.baseUrl}/api/v1/events/${eventId}/book`;

    const payload = JSON.stringify({ userId, quantity, });

    return http.post(url, payload, {
        headers: {'Content-Type': 'application/json',},
        timeout: config.requestTimeout,
        tags: { endpoint: 'create_booking', },
    });
}

export function getEvents() {
    return http.get( `${config.baseUrl}/api/v1/events`, {
            timeout: config.requestTimeout,
            tags: { endpoint: 'get_events',},
        }
    );
}

export function getEvent(eventId) {
    return http.get(`${config.baseUrl}/api/v1/events/${eventId}`, {
            timeout: config.requestTimeout,
            tags: { endpoint: 'get_event', },
        }
    );
}