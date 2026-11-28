package ru.matchflow.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
<<<<<<< HEAD
import jakarta.persistence.PrePersist;
=======
>>>>>>> 93f2e65bd25f1d3fffc2e9e89f3bcc6426d60622
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "teams")
@Getter
@Setter
@NoArgsConstructor
public class Team {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
<<<<<<< HEAD
    @JoinColumn(name = "captain_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private UserAccount captain;
    @Column(name = "name", nullable = false)
=======

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "captain_id", nullable = false)
    private UserAccount captain;

    @Column(nullable = false)
>>>>>>> 93f2e65bd25f1d3fffc2e9e89f3bcc6426d60622
    private String name;
    @Column(name = "tag", length = 10)
    private String tag;
    @Column(name = "creation_date", nullable = false)
    private LocalDateTime creationDate;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "team_members",
            joinColumns = @JoinColumn(name = "team_id", nullable = false),
            inverseJoinColumns = @JoinColumn(name = "user_id", nullable = false)
    )
    private Set<UserAccount> members = new HashSet<>();

    public Team(
            UserAccount captain,
            String name,
            String tag,
            Set<UserAccount> members
    ) {
        this.captain = captain;
        this.name = name;
        this.tag = tag;
        this.members = members;
    }

    @PrePersist
    private void prePersist() {
        creationDate = LocalDateTime.now();
    }
}
