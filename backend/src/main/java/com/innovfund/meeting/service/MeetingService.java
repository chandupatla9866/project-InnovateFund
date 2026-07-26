package com.innovfund.meeting.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.innovfund.common.AccessDeniedCustomException;
import com.innovfund.common.BadRequestException;
import com.innovfund.common.ResourceNotFoundException;
import com.innovfund.meeting.dto.CreateMeetingRequest;
import com.innovfund.meeting.dto.MeetingDto;
import com.innovfund.meeting.dto.MeetingSummaryResult;
import com.innovfund.meeting.entity.Meeting;
import com.innovfund.meeting.entity.MeetingStatus;
import com.innovfund.meeting.repository.MeetingRepository;
import com.innovfund.notification.entity.NotificationType;
import com.innovfund.notification.service.NotificationService;
import com.innovfund.startup.entity.Startup;
import com.innovfund.startup.service.StartupService;
import com.innovfund.user.entity.User;
import com.innovfund.user.repository.UserRepository;
import com.innovfund.user.service.UserDisplayNameService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MeetingService {

    private final MeetingRepository meetingRepository;
    private final UserRepository userRepository;
    private final StartupService startupService;
    private final NotificationService notificationService;
    private final UserDisplayNameService userDisplayNameService;
    private final MeetingSummaryService meetingSummaryService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public MeetingDto request(User requester, CreateMeetingRequest request) {
        User recipient = userRepository.findById(request.recipientId())
                .orElseThrow(() -> new ResourceNotFoundException("Recipient not found"));
        Startup startup = request.startupId() != null ? startupService.findOrThrow(request.startupId()) : null;

        Meeting meeting = Meeting.builder()
                .requester(requester)
                .recipient(recipient)
                .startup(startup)
                .scheduledAt(request.scheduledAt())
                .durationMinutes(request.durationMinutes() != null ? request.durationMinutes() : 30)
                .notes(request.notes())
                .build();
        meeting = meetingRepository.save(meeting);

        notificationService.notify(recipient, NotificationType.MEETING_REQUESTED,
                userDisplayNameService.resolveFullName(requester) + " requested a meeting", "/meetings");

        return toDto(meeting);
    }

    @Transactional(readOnly = true)
    public List<MeetingDto> listMine(User user) {
        return meetingRepository.findAllForUser(user.getId()).stream().map(this::toDto).toList();
    }

    @Transactional
    public MeetingDto accept(User user, UUID meetingId) {
        Meeting meeting = findOrThrow(meetingId);
        assertRecipient(user, meeting);
        meeting.setStatus(MeetingStatus.ACCEPTED);
        meeting.setMeetingLink("https://meet.jit.si/InnovateFund-" + meeting.getId());
        meetingRepository.save(meeting);
        notificationService.notify(meeting.getRequester(), NotificationType.MEETING_ACCEPTED,
                userDisplayNameService.resolveFullName(user) + " accepted your meeting request", "/meetings");
        return toDto(meeting);
    }

    @Transactional
    public MeetingDto reject(User user, UUID meetingId) {
        Meeting meeting = findOrThrow(meetingId);
        assertRecipient(user, meeting);
        meeting.setStatus(MeetingStatus.REJECTED);
        meetingRepository.save(meeting);
        notificationService.notify(meeting.getRequester(), NotificationType.MEETING_REJECTED,
                userDisplayNameService.resolveFullName(user) + " declined your meeting request", "/meetings");
        return toDto(meeting);
    }

    @Transactional
    public MeetingDto cancel(User user, UUID meetingId) {
        Meeting meeting = findOrThrow(meetingId);
        if (!meeting.getRequester().getId().equals(user.getId()) && !meeting.getRecipient().getId().equals(user.getId())) {
            throw new AccessDeniedCustomException("Not your meeting");
        }
        if (meeting.getStatus() == MeetingStatus.CANCELLED) {
            throw new BadRequestException("Meeting already cancelled");
        }
        meeting.setStatus(MeetingStatus.CANCELLED);
        meetingRepository.save(meeting);
        return toDto(meeting);
    }

    @Transactional
    public MeetingSummaryResult summarize(User user, UUID meetingId, String transcript) {
        Meeting meeting = findOrThrow(meetingId);
        if (!meeting.getRequester().getId().equals(user.getId()) && !meeting.getRecipient().getId().equals(user.getId())) {
            throw new AccessDeniedCustomException("Not your meeting");
        }
        MeetingSummaryResult result = meetingSummaryService.summarize(transcript);
        meeting.setTranscript(transcript);
        meeting.setSummaryJson(writeJson(result));
        meetingRepository.save(meeting);
        return result;
    }

    @Transactional(readOnly = true)
    public MeetingSummaryResult getSummary(User user, UUID meetingId) {
        Meeting meeting = findOrThrow(meetingId);
        if (!meeting.getRequester().getId().equals(user.getId()) && !meeting.getRecipient().getId().equals(user.getId())) {
            throw new AccessDeniedCustomException("Not your meeting");
        }
        if (meeting.getSummaryJson() == null) {
            throw new ResourceNotFoundException("No summary yet for this meeting");
        }
        return readJson(meeting.getSummaryJson());
    }

    @SneakyThrows
    private String writeJson(MeetingSummaryResult result) {
        return objectMapper.writeValueAsString(result);
    }

    @SneakyThrows
    private MeetingSummaryResult readJson(String json) {
        return objectMapper.readValue(json, MeetingSummaryResult.class);
    }

    private void assertRecipient(User user, Meeting meeting) {
        if (!meeting.getRecipient().getId().equals(user.getId())) {
            throw new AccessDeniedCustomException("Only the meeting recipient can respond");
        }
    }

    private Meeting findOrThrow(UUID id) {
        return meetingRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Meeting not found"));
    }

    private MeetingDto toDto(Meeting m) {
        return new MeetingDto(
                m.getId(),
                m.getRequester().getId(), userDisplayNameService.resolveFullName(m.getRequester()),
                m.getRecipient().getId(), userDisplayNameService.resolveFullName(m.getRecipient()),
                m.getStartup() != null ? m.getStartup().getId() : null,
                m.getStartup() != null ? m.getStartup().getName() : null,
                m.getScheduledAt(), m.getDurationMinutes(), m.getStatus(), m.getMeetingLink(), m.getNotes(), m.getCreatedAt()
        );
    }
}
