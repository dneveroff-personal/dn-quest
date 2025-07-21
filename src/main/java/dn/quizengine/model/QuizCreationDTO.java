package dn.quizengine.model;

import lombok.Data;
import java.util.Set;

@Data
public class QuizCreationDTO {
    private String title;
    private String text;
    private Set<String> options;
    private Integer answer; // индекс правильного ответа
}