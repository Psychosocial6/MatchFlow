package ru.matchflow.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "tournaments")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Tournament {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "admin_id", nullable = false)
    private int adminId;

    @Column(name = "sport_id", nullable = false)
    private int sportId;

    private String title;

    @Column(columnDefinition = "text")
    private String regulations;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "participation_type", columnDefinition = "enum_participation_type", nullable = false)
    private ParticipationType participationType;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(columnDefinition = "enum_format", nullable = false)
    private Format format;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(columnDefinition = "enum_status_tournaments", nullable = false)
    private Status status = Status.DRAFT;

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
