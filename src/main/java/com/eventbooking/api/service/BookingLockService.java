package com.eventbooking.api.service;

import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class BookingLockService {

    private final RedissonClient redissonClient;

    public RLock getEventLock(Long eventId) {
        return redissonClient.getLock(
                "booking:event:" + eventId
        );
    }

    public boolean tryLock(
            Long eventId,
            long waitTime,
            TimeUnit timeUnit) {

        RLock lock = getEventLock(eventId);

        try {
            return lock.tryLock(
                    waitTime,
                    timeUnit
            );
        } catch (InterruptedException exception) {

            Thread.currentThread().interrupt();

            throw new IllegalStateException(
                    "Interrupted while acquiring booking lock",
                    exception
            );
        }
    }

    public void unlock(Long eventId) {

        RLock lock = getEventLock(eventId);

        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }
}