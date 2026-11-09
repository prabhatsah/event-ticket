import { sleep, check } from 'k6';

import {
    getEvents,
    getEvent,
} from '../helpers/booking.js';

import { config } from '../config.js';

export const options = {

    scenarios: {

        scaling_test: {

            executor: 'ramping-vus',

            startVUs: 0,

            stages: [
                { duration: '1m', target: 100 },
                { duration: '2m', target: 500 },
                { duration: '2m', target: 1000 },
                { duration: '2m', target: 3000 },
                { duration: '2m', target: 5000 },
                { duration: '2m', target: 10000 },
                { duration: '2m', target: 10000 },
                { duration: '2m', target: 0 },
            ],

//            stages: [
//                { duration: '40s', target: 10000 },
//                { duration: '40s', target: 20000 },
//                { duration: '40s', target: 30000 },
//                { duration: '40s', target: 40000 },
//                { duration: '40s', target: 50000 },
//                { duration: '20s', target: 0 },
//            ],
            gracefulRampDown: '30s',
        },
    },

    thresholds: {

        http_req_duration: [
            'p(95)<1500',
            'p(99)<3000',
        ],

        http_req_failed: [
            'rate<0.02',
        ],
    },
};

export default function () {

    const response = getEvents();

    check(response, {
        'events endpoint returns 200': (r) =>
            r.status === 200,
    });

    const eventResponse = getEvent(config.eventId);

    check(eventResponse, {
        'event endpoint returns 200': (r) =>
            r.status === 200,
    });

    sleep(Math.random() * 3 + 1);
}