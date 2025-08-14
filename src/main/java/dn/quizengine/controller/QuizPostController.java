package dn.quizengine.controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import dn.quizengine.model.dto.*;
import dn.quizengine.repository.CompletedQuizRepository;
import dn.quizengine.repository.QuizRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/post")
public class QuizPostController {

    private static final Logger LOGGER = LoggerFactory.getLogger(QuizPostController.class);
    private final QuizRepository quizRepository;
    private final CompletedQuizRepository completedQuizRepository;

    @Autowired
    public QuizPostController(QuizRepository quizRepository, CompletedQuizRepository completedQuizRepository) {
        this.quizRepository = quizRepository;
        this.completedQuizRepository = completedQuizRepository;
    }

    @PostMapping("/new")
    @Operation(
            summary = "Create new quiz",
            description = "Create a new quiz with title, text, options and correct answer index"
    )
    public Quiz createQuiz(@Valid @RequestBody QuizCreation quizDTO, @AuthenticationPrincipal UserDetails details) {
        Quiz quiz = new Quiz();
        quiz.setTitle(quizDTO.getTitle());
        quiz.setText(quizDTO.getText());
        quiz.setOptions(quizDTO.getOptions());
        quiz.setCorrectAnswers(quizDTO.getAnswer());
        quiz.setAuthor(Optional.ofNullable(details.getUsername()).orElse("TEST2"));
        return quizRepository.save(quiz);
    }

    @PostMapping("/{id}/solve")
    @Operation(summary = "Answer on quiz by ID", description = "Post ID of question, and add an answer in param")
    public AnswerResult submitAnswer(
            @Parameter(description = "ID of the quiz", example = "1")
            @PathVariable Long id,
            @RequestBody AnswerRequest answerRequest,
            Principal user) {

        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz not found"));

        Set<Integer> userAnswers = answerRequest.getAnswer();
        Set<Integer> correctAnswers = quiz.getCorrectAnswers();

        boolean isCorrect = userAnswers != null &&
                userAnswers.equals(correctAnswers);

        LOGGER.info("QuizId: {}, Answers: {}", id, userAnswers);

        if (isCorrect) {
            completedQuizRepository.save(
                    new CompletedQuiz(quiz.getId(), user.getName(), LocalDateTime.now())
            );
        }

        return new AnswerResult(
                isCorrect,
                isCorrect ? "Congratulations, you're right!" : "Wrong answer! Please, try again.",
                id
        );
    }

}
