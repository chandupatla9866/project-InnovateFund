package com.innovfund.meeting.service;

import com.innovfund.meeting.entity.Meeting;
import com.innovfund.meeting.entity.MeetingStatus;
import com.innovfund.meeting.repository.MeetingRepository;
import com.innovfund.notification.entity.NotificationType;
import com.innovfund.notification.service.NotificationService;
import com.innovfund.user.service.UserDisplayNameService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Every 15 minutes, notifies both parties of an accepted meeting starting within the next hour —
 * once per meeting (tracked via {@code reminderSent}), so it never double-notifies.
 */
@Component
@RequiredArgsConstructor
public class MeetingReminderJob {

    private final MeetingRepository meetingRepository;
    private final NotificationService notificationService;
    private final UserDisplayNameService userDisplayNameService;

    @Scheduled(fixedRate = 15, timeUnit = java.util.concurrent.TimeUnit.MINUTES)
    @Transactional
    public void sendDueReminders() {
        Instant now = Instant.now();
        List<Meeting> due = meetingRepository.findDueForReminder(MeetingStatus.ACCEPTED, now, now.plus(Duration.ofHours(1)));
        for (Meeting meeting : due) {
            String when = "in " + Duration.between(now, meeting.getScheduledAt()).toMinutes() + " min";
            notificationService.notify(meeting.getRequester(), NotificationType.MEETING_ACCEPTED,
                    "Reminder: your meeting with " + userDisplayNameService.resolveFullName(meeting.getRecipient()) + " starts " + when,
                    "/meetings");
            notificationService.notify(meeting.getRecipient(), NotificationType.MEETING_ACCEPTED,
                    "Reminder: your meeting with " + userDisplayNameService.resolveFullName(meeting.getRequester()) + " starts " + when,
                    "/meetings");
            meeting.setReminderSent(true);
            meetingRepository.save(meeting);
        }
    }
}
