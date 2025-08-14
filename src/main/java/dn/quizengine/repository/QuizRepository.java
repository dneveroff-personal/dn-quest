package dn.quizengine.repository;

import dn.quizengine.model.dto.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
}