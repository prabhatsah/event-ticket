package com.eventbooking.api.service;

import com.eventbooking.api.dto.request.CreateEventRequest;
import com.eventbooking.api.dto.response.EventResponse;
import com.eventbooking.api.entity.Event;
import com.eventbooking.api.exception.EventNotFoundException;
import com.eventbooking.api.mapper.EventBookingMapper;
import com.eventbooking.api.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final EventBookingMapper mapper;

    @Transactional
    public EventResponse createEvent(CreateEventRequest request) {

        Event event = mapper.toEvent(request);

        Event savedEvent = eventRepository.save(event);

        return mapper.toEventResponse(savedEvent);
    }

    @Transactional(readOnly = true)
    public EventResponse getEventById(Long eventId) {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));

        return mapper.toEventResponse(event);
    }

    @Transactional(readOnly = true)
    public List<EventResponse> getAllEvents() {

        return eventRepository.findAll()
                .stream()
                .map(mapper::toEventResponse)
                .toList();
    }

    @Transactional
    public void deleteEvent(Long eventId) {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));

        eventRepository.delete(event);
    }
}