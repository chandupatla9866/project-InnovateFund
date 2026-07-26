package com.innovfund.meeting.repository;

import com.innovfund.meeting.entity.Meeting;
import com.innovfund.meeting.entity.MeetingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface MeetingRepository extends JpaRepository<Meeting, UUID> {

    @Query("""
            select m from Meeting m
            where m.requester.id = :userId or m.recipient.id = :userId
            order by m.scheduledAt desc
            """)
    List<Meeting> findAllForUser(@Param("userId") UUID userId);

    @Query("""
            select m from Meeting m
            where m.status = :status and m.reminderSent = false
            and m.scheduledAt between :from and :to
            """)
    List<Meeting> findDueForReminder(@Param("status") MeetingStatus status,
                                      @Param("from") Instant from, @Param("to") Instant to);
}
