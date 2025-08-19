package dn.quizengine.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AnswerResult {

    private boolean success;
    private String feedback;
    private Long quizId; // Для логирования

    @Override
    public String toString() {
        return quizId.toString() + ": " + (success ? "✅ Верно!" : "❌ Неправильно!") + "\n" + getFeedback();
    }
}
