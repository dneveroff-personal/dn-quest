package dn.quizengine.service;

import dn.quizengine.controller.QuizDeleteController;
import dn.quizengine.model.dto.AnswerRequest;
import dn.quizengine.model.dto.AnswerResult;
import dn.quizengine.model.dto.CompletedQuiz;
import dn.quizengine.model.dto.Quiz;
import dn.quizengine.repository.CompletedQuizRepository;
import dn.quizengine.repository.QuizRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Set;

@Service
public class QuizService {

    private static final Logger LOGGER = LoggerFactory.getLogger(QuizService.class);
    private final QuizRepository quizRepository;
    private final CompletedQuizRepository completedQuizRepository;

    public QuizService(QuizRepository quizRepository, CompletedQuizRepository completedQuizRepository) {
        this.quizRepository = quizRepository;
        this.completedQuizRepository = completedQuizRepository;
    }

    public String deleteQuiz(Long id, Authentication auth) {
        Quiz quiz = getQuizById(id);

        if (!quiz.getAuthor().equals(auth.getName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        quizRepository.delete(quiz);
        LOGGER.info("QuizId: {} - has been DELETED.", id);

        return quiz.getTitle();
    }

    public Page<Quiz> getAllQuizzes(int page, int pageSize) {
        return quizRepository.findAll(PageRequest.of(page, pageSize));
    }

    public AnswerResult getAnswerResult(Long id, AnswerRequest answerRequest, Principal user) {
        Quiz quiz = getQuizById(id);
        Set<Integer> userAnswers = answerRequest.getAnswer();
        Set<Integer> correctAnswers = quiz.getCorrectAnswers();
        boolean isCorrect = userAnswers != null && userAnswers.equals(correctAnswers);

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

    public Quiz getQuizById(Long id) {
        return quizRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz not found"));
    }

    public Quiz saveQuiz(Quiz quiz) {
        return quizRepository.save(quiz);
    }

}
