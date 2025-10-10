package dn.quest.cmd;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
public class QuestConsole implements CommandLineRunner {

    private final Consumer<String> cmdPrint;

    public QuestConsole(Consumer<String> cmdPrint) {
        this.cmdPrint = cmdPrint;
    }

    @Override
    public void run(String... args) throws Exception {
        cmdPrint.accept("\nWellcome to the DN Quest Console!\nStart from http://localhost:8080/\n" +
                "To work with database via PgAdmin http://localhost:5050\n");
    }
}
