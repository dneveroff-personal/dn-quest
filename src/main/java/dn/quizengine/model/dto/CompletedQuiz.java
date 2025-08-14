package dn.quizengine.model.dto;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "completed_quizzes")
public class CompletedQuiz {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long quizId;

    private LocalDateTime completedAt;

    private String username;

    public CompletedQuiz(Long quizId, String username, LocalDateTime completedAt) {
        this.quizId = quizId;
        this.username = username;
        this.completedAt = completedAt;
    }

    public CompletedQuiz() {

    }
}
