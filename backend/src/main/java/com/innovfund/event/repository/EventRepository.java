package com.innovfund.event.repository;

import com.innovfund.event.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface EventRepository extends JpaRepository<Event, UUID> {
    List<Event> findAllByOrderByEventDateAsc();
    List<Event> findAllByEventDateAfterOrderByEventDateAsc(Instant after);
}
