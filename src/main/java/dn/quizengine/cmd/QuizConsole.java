package dn.quizengine.cmd;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class QuizConsole implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Wellcome to the DN Quiz Console!");
    }
}
