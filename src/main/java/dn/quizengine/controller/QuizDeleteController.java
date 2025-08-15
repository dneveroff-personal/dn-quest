package dn.quizengine.controller;

import dn.quizengine.service.QuizService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(Routes.DELETE)
public class QuizDeleteController implements Routes {

    private final QuizService quizService;

    @Autowired
    public QuizDeleteController(QuizService quizService) {
        this.quizService = quizService;
    }

    @DeleteMapping(ID)
    public ResponseEntity<String> deleteQuiz(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok("Quiz " + quizService.deleteQuiz(id, auth) + " has been deleted.");
    }

}
