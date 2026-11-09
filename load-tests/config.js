export const config = {
    baseUrl: __ENV.BASE_URL || 'http://localhost:30080',

    eventId: Number(__ENV.EVENT_ID || 1),

    requestTimeout: '30s',
};