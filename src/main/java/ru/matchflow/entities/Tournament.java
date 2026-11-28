package ru.matchflow.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "tournaments")
@Getter
@Setter
@NoArgsConstructor
public class Tournament {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @JoinColumn(name = "admin_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private UserAccount admin;

    @JoinColumn(name = "sport_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Sport sport;

    @Column(name = "title")
    private String title;

    @Column(name = "regulations", columnDefinition = "text")
    private String regulations;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "participation_type", columnDefinition = "enum_participation_type", nullable = false)
    private ParticipationType participationType;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(columnDefinition = "enum_format", nullable = false)
    private Format format;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(columnDefinition = "enum_status_tournaments", nullable = false)
    private Status status;

    @Column(name = "reg_start_date", nullable = false)
    private LocalDateTime regStartDate;

    @Column(name = "reg_end_date", nullable = false)
    private LocalDateTime regEndDate;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Column(name = "max_participants", nullable = false)
    private int maxParticipants;

    public Tournament(
            UserAccount admin,
            Sport sport,
            String title,
            String regulations,
            ParticipationType participationType,
            Format format,
            LocalDateTime regStartDate,
            LocalDateTime regEndDate,
            LocalDateTime startDate,
            LocalDateTime endDate,
            int maxParticipants
    ) {
        this.admin = admin;
        this.sport = sport;
        this.title = title;
        this.regulations = regulations;
        this.participationType = participationType;
        this.format = format;
        this.regStartDate = regStartDate;
        this.regEndDate = regEndDate;
        this.startDate = startDate;
        this.endDate = endDate;
        this.maxParticipants = maxParticipants;
    }

    @PrePersist
    private void prePersist() {
        status = Status.DRAFT;
    }

    public enum ParticipationType {
        SOLO, TEAM
    }

    public enum Format {
        SINGLE_ELIMINATION, DOUBLE_ELIMINATION, ROUND_ROBIN, SWISS
    }

    public enum Status {
        DRAFT, REGISTRATION, ONGOING, FINISHED, CANCELLED
    }
}
