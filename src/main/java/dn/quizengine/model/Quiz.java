package dn.quizengine.model;

import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Quiz {

    private UUID id;
    private String title;
    private String text;
    private Set<String> options;

    @JsonIgnore // Исключаем из JSON
    private Set<Integer> correctAnswers;

}
