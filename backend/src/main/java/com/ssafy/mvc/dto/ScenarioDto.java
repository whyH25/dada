package com.ssafy.mvc.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ScenarioDto {
    private Long   scenarioId;
    private String questionType;
    private String questionText;
    private int    questionOrder;
}
