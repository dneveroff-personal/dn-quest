package dn.quizengine.controller;

import dn.quizengine.model.dto.Quiz;
import dn.quizengine.repository.QuizRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/delete")
public class QuizDeleteController {

    private static final Logger LOGGER = LoggerFactory.getLogger(QuizDeleteController.class);
    private final QuizRepository quizRepository;

    @Autowired
    public QuizDeleteController(QuizRepository quizRepository) {
        this.quizRepository = quizRepository;
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "DELETE header handler", description = "Remove quiz by ID")
    public ResponseEntity<String> deleteQuiz(
            @Parameter(description = "ID of the quiz", example = "1")
            @PathVariable Long id, Authentication auth) {

        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (!quiz.getAuthor().equals(auth.getName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        quizRepository.delete(quiz);
        LOGGER.info("QuizId: {} - has been DELETED.", id);

        return ResponseEntity.ok("Quiz " + quiz.getTitle() + " has been deleted.");
    }

}
