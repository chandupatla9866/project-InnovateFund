package com.innovfund.event.service;

import com.innovfund.common.ResourceNotFoundException;
import com.innovfund.event.dto.CreateEventRequest;
import com.innovfund.event.dto.EventDto;
import com.innovfund.event.entity.Event;
import com.innovfund.event.repository.EventRepository;
import com.innovfund.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;

    @Transactional
    public EventDto create(User admin, CreateEventRequest request) {
        Event event = Event.builder()
                .type(request.type())
                .title(request.title())
                .description(request.description())
                .eventDate(request.eventDate())
                .location(request.location())
                .link(request.link())
                .createdBy(admin)
                .build();
        return toDto(eventRepository.save(event));
    }

    @Transactional
    public EventDto update(UUID id, CreateEventRequest request) {
        Event event = findOrThrow(id);
        event.setType(request.type());
        event.setTitle(request.title());
        event.setDescription(request.description());
        event.setEventDate(request.eventDate());
        event.setLocation(request.location());
        event.setLink(request.link());
        return toDto(eventRepository.save(event));
    }

    @Transactional
    public void delete(UUID id) {
        eventRepository.delete(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<EventDto> listUpcoming() {
        return eventRepository.findAllByEventDateAfterOrderByEventDateAsc(Instant.now()).stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<EventDto> listAll() {
        return eventRepository.findAllByOrderByEventDateAsc().stream().map(this::toDto).toList();
    }

    private Event findOrThrow(UUID id) {
        return eventRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Event not found"));
    }

    private EventDto toDto(Event e) {
        return new EventDto(e.getId(), e.getType(), e.getTitle(), e.getDescription(), e.getEventDate(),
                e.getLocation(), e.getLink(), e.getCreatedAt());
    }
}
