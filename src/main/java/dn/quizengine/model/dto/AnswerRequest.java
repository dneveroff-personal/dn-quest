package dn.quizengine.model.dto;

import lombok.Data;

import java.util.Set;

@Data
public class AnswerRequest {

    private Set<Integer> answer;

}
