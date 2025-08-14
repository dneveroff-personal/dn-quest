package dn.quizengine.controller;

import dn.quizengine.model.dto.*;
import dn.quizengine.repository.CompletedQuizRepository;
import dn.quizengine.repository.QuizRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;

@RestController
@RequestMapping("/api/get")
public class QuizGetController {

    private final QuizRepository quizRepository;
    private final CompletedQuizRepository completedQuizRepository;

    @Autowired
    public QuizGetController(QuizRepository quizRepository, CompletedQuizRepository completedQuizRepository) {
        this.quizRepository = quizRepository;
        this.completedQuizRepository = completedQuizRepository;
    }

    @GetMapping("/all")
    @Operation(
            summary = "Get all quizzes",
            description = "Returns list of all available quizzes"
    )
    public Page<Quiz> getAllQuizzes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize) {

        return quizRepository.findAll(PageRequest.of(page, pageSize));
    }

    @GetMapping("/{id:\\d+}")
    @Operation(summary = "Get quiz by ID", description = "Returns a quiz by its ID")
    public Quiz getQuiz(
            @Parameter(description = "ID of the quiz", example = "1")
            @PathVariable Long id) {

        return quizRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz not found"));
    }

    @GetMapping("/completed")
    public Page<CompletedQuiz> getCompletedQuizzes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            Principal principal) {

        return completedQuizRepository.findByUsernameOrderByCompletedAtDesc(
                principal.getName(),
                PageRequest.of(page, pageSize)
        );
    }

}
