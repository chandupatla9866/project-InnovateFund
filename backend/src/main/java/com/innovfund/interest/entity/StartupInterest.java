package com.innovfund.interest.entity;

import com.innovfund.startup.entity.Startup;
import com.innovfund.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * "Express Interest" — an investor explicitly signaling "I want to explore investing" on a
 * startup. Deliberately a separate, heavier-weight signal from {@code Follow} (which is just
 * "keep me updated"): expressing interest notifies the founder and appears in the founder's
 * review queue for that startup.
 */
@Entity
@Table(name = "startup_interests", uniqueConstraints =
        @UniqueConstraint(name = "uq_interest_investor_startup", columnNames = {"investor_id", "startup_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StartupInterest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "investor_id", nullable = false)
    private User investor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "startup_id", nullable = false)
    private Startup startup;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(20)", nullable = false)
    private InterestStatus status = InterestStatus.PENDING;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
