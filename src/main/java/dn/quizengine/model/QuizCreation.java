package dn.quizengine.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.Set;

@Data
public class QuizCreation {
    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Text is required")
    private String text;

    @NotNull(message = "Options are required")
    @Size(min = 2, message = "At least 2 options are required")
    private Set<String> options;

    private Set<Integer> answer; // теперь Set<Integer> вместо Integer
}