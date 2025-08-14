package dn.quizengine.repository;

import dn.quizengine.model.dto.CompletedQuiz;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CompletedQuizRepository extends JpaRepository<CompletedQuiz, Long> {

    @Query("SELECT c FROM CompletedQuiz c WHERE c.username = :username ORDER BY c.completedAt DESC")
    Page<CompletedQuiz> findByUsernameOrderByCompletedAtDesc(@Param("username") String username, Pageable pageable);
}