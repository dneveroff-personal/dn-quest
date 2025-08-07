package dn.quizengine.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AnswerResult {

    private boolean success;
    private String feedback;
    private Long quizId; // Для логирования
    private String traceId; // Для отслеживания запросов в микросервисах

}
