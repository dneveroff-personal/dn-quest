package dn.quizengine.controller;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import dn.quizengine.model.AnswerResult;
import dn.quizengine.model.Quiz;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/quiz")
public class QuizController {

    private static final Logger LOGGER = LoggerFactory.getLogger(QuizController.class);
    private final ConcurrentHashMap<UUID, Quiz> quizzes = new ConcurrentHashMap<>();
    private final Cache<UUID, Quiz> quizCache =
            Caffeine.newBuilder().maximumSize(10_000).build(); // Кеш последних тестов

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

    @GetMapping("/{id}")
    @Operation(summary = "Get quiz by ID", description = "Returns a quiz by its UUID") // это для Swagger-а
    public Quiz getQuiz(
            @Parameter(description = "ID of the quiz", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID id
    ) {
        return quizCache.get(id, quizzes::get); // Кешируем!
    }

    @PostMapping("/{id}/solve")
    @Operation(summary = "Answer on quiz by ID", description = "Post UUID of question, and add an answer in param")
    public AnswerResult submitAnswer(
            @Parameter(description = "ID of the quiz", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID id,
            @RequestParam int answer,
            @RequestHeader(name = "X-Trace-Id", required = false) String traceId
    ) {
        /**
         * traceId — это идентификатор для сквозного логирования запросов в распределённых системах.
         * В вашем случае он не обязателен, но полезен для отладки.
         */
        if (traceId == null) {
            traceId = "gen-" + UUID.randomUUID();
        }

        Quiz quiz = quizCache.getIfPresent(id);
        if (quiz == null) {
            quiz = quizzes.get(id);
        }

        boolean isCorrect = quiz != null && answer == quiz.getCorrectOptionIndex();
        LOGGER.info("TraceId: {}, QuizId: {}, Answer: {}", traceId, id, answer);

        return new AnswerResult(
                isCorrect,
                isCorrect ? "Correct!" : "Wrong!",
                id,
                traceId
        );
    }



}
