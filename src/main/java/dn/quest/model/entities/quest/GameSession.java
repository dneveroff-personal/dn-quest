package dn.quest.model.entities.quest;

import dn.quest.model.entities.enums.QuestType;
import dn.quest.model.entities.enums.SessionStatus;
import dn.quest.model.entities.team.Team;
import dn.quest.model.entities.user.User;
import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Data
@Entity
@Table(name="game_sessions",
        indexes = {
                @Index(name="idx_session_quest", columnList="quest_id"),
                @Index(name="idx_session_status", columnList="status")
        })
public class GameSession {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="quest_id", nullable=false)
    private Quest quest;                   // фиксируем квест (версионирование добавим при необходимости)

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="team_id")
    private Team team;                     // если TEAM-режим

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="user_id")
    private User user;                     // если SOLO-режим

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=10)
    private SessionStatus status = SessionStatus.PENDING;

    private Instant startedAt;
    private Instant finishedAt;

    @Column(nullable=false)
    private int bonusTimeSumSec = 0;

    @Column(nullable=false)
    private int penaltyTimeSumSec = 0;
}
