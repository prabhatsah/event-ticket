import { sleep, check } from 'k6';

import {getEvents, getEvent,} from '../helpers/booking.js';

import { config } from '../config.js';

export const options = {
    stages: [
        { duration: '30s', target: 20 },
        { duration: '1m', target: 50 },
        { duration: '1m', target: 100 },
        { duration: '30s', target: 0 },
    ],

    thresholds: {
        http_req_failed: ['rate<0.01'],
        http_req_duration: ['p(95)<1000'],
    },
};

export default function () {

    const eventsResponse = getEvents();

    check(eventsResponse, {
        'get events returns 200': (response) =>
            response.status === 200,
    });

    const eventResponse = getEvent(config.eventId);

    check(eventResponse, {
        'get event returns 200': (response) =>
            response.status === 200,
    });

    sleep(1);
}