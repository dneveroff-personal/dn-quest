package dn.quizengine.controller;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import dn.quizengine.model.dto.*;
import dn.quizengine.model.repository.AppUserRepository;
import dn.quizengine.model.repository.QuizRepository;
import dn.quizengine.model.user.AppUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/quizzes")
public class QuizController {

    private static final Logger LOGGER = LoggerFactory.getLogger(QuizController.class);
    //private final ConcurrentHashMap<UUID, Quiz> quizzes = new ConcurrentHashMap<>();
    private final Cache<UUID, Quiz> quizCache = Caffeine.newBuilder().maximumSize(10_000).build();
    private final QuizRepository quizRepository;
    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public QuizController(QuizRepository quizRepository, AppUserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.quizRepository = quizRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void init() {
        if (quizRepository.count() == 0) {
            Quiz quiz = new Quiz();
            quiz.setTitle("The Java Logo");
            quiz.setText("What is depicted on the Java logo?");
            quiz.setOptions(Set.of("Robot", "Tea leaf", "Cup of coffee", "Bug"));
            quiz.setCorrectAnswers(Set.of(2));
            //Quiz savedQuiz = quizRepository.save(quiz);
            //quizzes.put(savedQuiz.getId(), savedQuiz);
        }
    }

    @PostMapping
    @Operation(
            summary = "Create new quiz",
            description = "Create a new quiz with title, text, options and correct answer index"
    )
    public Quiz createQuiz(@Valid @RequestBody QuizCreation quizDTO) {
        Quiz quiz = new Quiz();
        quiz.setTitle(quizDTO.getTitle());
        quiz.setText(quizDTO.getText());
        quiz.setOptions(quizDTO.getOptions());
        quiz.setCorrectAnswers(quizDTO.getAnswer());

        //Quiz savedQuiz = quizRepository.save(quiz);
        //quizzes.put(savedQuiz.getId(), savedQuiz);
        return quizRepository.save(quiz);
    }

    @GetMapping(path = "/test")
    public String test() {
        return "Access to '/test' granted";
    }

    @GetMapping(path = "/ping")
    public String ping() {
        return "YOU SEND PING - HERE THE PONG!";
    }

    @PostMapping(path = "/register")
    public String register(@RequestBody RegistrationRequest request) {
        String userName = request.getUsername();

        return userRepository.findAppUserByUsername(userName)
                .map(user -> {
                    user.setPassword(passwordEncoder.encode(request.getPassword()));
                    user.setAuthority(request.getAuthority());
                    userRepository.save(user);
                    return "User " + userName + " has been updated!";
                })
                .orElseGet(() -> {
                    var user = new AppUser();
                    user.setUsername(userName);
                    user.setPassword(passwordEncoder.encode(request.getPassword()));
                    user.setAuthority(request.getAuthority());
                    userRepository.save(user);
                    return "New user " + userName +" successfully registered!";
                });
    }

    @GetMapping
    @Operation(
            summary = "Get all quizzes",
            description = "Returns list of all available quizzes"
    )
    public List<Quiz> getAllQuizzes() {
        return quizRepository.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get quiz by ID", description = "Returns a quiz by its ID")
    public Quiz getQuiz(
            @Parameter(description = "ID of the quiz", example = "1")
            @PathVariable Long id
    ) {
        return quizRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz not found"));
    }

    @PostMapping("/{id}/solve")
    @Operation(summary = "Answer on quiz by ID", description = "Post ID of question, and add an answer in param")
    public AnswerResult submitAnswer(
            @Parameter(description = "ID of the quiz", example = "1")
            @PathVariable Long id,
            @RequestBody AnswerRequest answerRequest,
            @RequestHeader(name = "X-Trace-Id", required = false) String traceId
    ) {
        if (traceId == null) {
            traceId = "gen-" + System.currentTimeMillis();
        }

        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz not found"));

        Set<Integer> userAnswers = answerRequest.getAnswer();
        Set<Integer> correctAnswers = quiz.getCorrectAnswers();

        boolean isCorrect = userAnswers != null &&
                correctAnswers != null &&
                userAnswers.equals(correctAnswers);

        LOGGER.info("TraceId: {}, QuizId: {}, Answers: {}", traceId, id, userAnswers);

        return new AnswerResult(
                isCorrect,
                isCorrect ? "Congratulations, you're right!" : "Wrong answer! Please, try again.",
                id,
                traceId
        );
    }

}
