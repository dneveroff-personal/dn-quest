package dn.quizengine.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class AnswerResult {

    private boolean success;
    private String feedback;
    private UUID quizId; // Для логирования
    private String traceId; // Для отслеживания запросов в микросервисах

}
