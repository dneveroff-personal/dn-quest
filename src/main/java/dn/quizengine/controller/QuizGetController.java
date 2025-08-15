package dn.quizengine.controller;

import dn.quizengine.model.dto.*;
import dn.quizengine.repository.CompletedQuizRepository;
import dn.quizengine.service.QuizService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping(Routes.GET)
public class QuizGetController implements Routes {

    private final CompletedQuizRepository completedQuizRepository;
    private final QuizService quizService;

    @Autowired
    public QuizGetController(CompletedQuizRepository completedQuizRepository, QuizService quizService) {
        this.completedQuizRepository = completedQuizRepository;
        this.quizService = quizService;
    }

    @GetMapping(GET_ALL)
    public Page<Quiz> getAllQuizzes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize) {

        return quizService.getAllQuizzes(page, pageSize);
    }

    @GetMapping(ID)
    public Quiz getQuiz(@PathVariable Long id) {
        return quizService.getQuizById(id);
    }

    @GetMapping(GET_COMPLETED)
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
