package dn.quizengine.controller;

import java.security.Principal;
import java.util.Optional;

import dn.quizengine.model.dto.*;
import dn.quizengine.service.QuizService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(Routes.POST)
public class QuizPostController implements Routes {

    private final QuizService quizService;
    private final String ADMIN_USER = "admin";

    @Autowired
    public QuizPostController( QuizService quizService) {
        this.quizService = quizService;
    }

    @PostMapping(POST_NEW)
    public Quiz createQuiz(@Valid @RequestBody QuizCreation quizDTO, @AuthenticationPrincipal UserDetails details) {
        Quiz quiz = new Quiz();
        quiz.setTitle(quizDTO.getTitle());
        quiz.setText(quizDTO.getText());
        quiz.setOptions(quizDTO.getOptions());
        quiz.setCorrectAnswers(quizDTO.getAnswer());
        quiz.setAuthor(Optional.ofNullable(details.getUsername()).orElse(ADMIN_USER));
        return quizService.saveQuiz(quiz);
    }

    @PostMapping(POST_SOLVE)
    public AnswerResult submitAnswer(
            @PathVariable Long id,
            @RequestBody AnswerRequest answerRequest,
            Principal user) {

        return quizService.getAnswerResult(id, answerRequest, user.getName());
    }

}
