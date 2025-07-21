package dn.quizengine.controller;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import dn.quizengine.model.AnswerResult;
import dn.quizengine.model.Quiz;
import dn.quizengine.model.QuizCreationDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/quizzes")
public class QuizController {

    private static final Logger log = LoggerFactory.getLogger(QuizController.class);
    private final ConcurrentHashMap<UUID, Quiz> quizzes = new ConcurrentHashMap<>();
    private final Cache<UUID, Quiz> quizCache = Caffeine.newBuilder().maximumSize(10_000).build();

    @PostConstruct
    public void init() {
        Quiz javaQuiz = new Quiz(
                UUID.fromString("550e8400-e29b-41d4-a716-446655440000"),
                "The Java Logo",
                "What is depicted on the Java logo?",
                Set.of("Robot", "Tea leaf", "Cup of coffee", "Bug"),
                2
        );
        quizzes.put(javaQuiz.getId(), javaQuiz);
    }

    @PostMapping
    @Operation(
            summary = "Create new quiz",
            description = "Create a new quiz with title, text, options and correct answer index"
    )
    public Quiz createQuiz(@RequestBody QuizCreationDTO quizDTO) {
        Quiz quiz = new Quiz(
                UUID.randomUUID(),
                quizDTO.getTitle(),
                quizDTO.getText(),
                quizDTO.getOptions(),
                quizDTO.getAnswer() != null ? quizDTO.getAnswer() : -1
        );
        quizzes.put(quiz.getId(), quiz);
        return quiz;
    }

    @GetMapping
    @Operation(
            summary = "Get all quizzes",
            description = "Returns list of all available quizzes"
    )
    public Set<Quiz> getAllQuizzes() {
        return Set.copyOf(quizzes.values());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get quiz by ID", description = "Returns a quiz by its UUID")
    public Quiz getQuiz(
            @Parameter(description = "ID of the quiz", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID id
    ) {
        Quiz quiz = quizCache.get(id, quizzes::get);
        if (quiz == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz not found");
        }
        return quiz;
    }

    @PostMapping("/{id}/solve")
    @Operation(summary = "Answer on quiz by ID", description = "Post UUID of question, and add an answer in param")
    public AnswerResult submitAnswer(
            @Parameter(description = "ID of the quiz", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID id,
            @RequestParam int answer,
            @RequestHeader(name = "X-Trace-Id", required = false) String traceId
    ) {
        if (traceId == null) {
            traceId = "gen-" + UUID.randomUUID();
        }

        Quiz quiz = quizCache.getIfPresent(id);
        if (quiz == null) {
            quiz = quizzes.get(id);
        }

        if (quiz == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz not found");
        }

        boolean isCorrect = answer == quiz.getCorrectOptionIndex();
        log.info("TraceId: {}, QuizId: {}, Answer: {}", traceId, id, answer);

        return new AnswerResult(
                isCorrect,
                isCorrect ? "Congratulations, you're right!" : "Wrong answer! Please, try again.",
                id,
                traceId
        );
    }
}