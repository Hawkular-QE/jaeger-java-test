package io.jaegertracing.qe.rest.model;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

/**
 * @author Jeeva Kandasamy (jkandasa)
 */
@ToString
@Builder
@Data
public class Criteria {
    private String operationName;
    private String service;
    private String tag;
    private Long start;
    private Long end;
    private Long limit;
    private String minDuration;
    private String maxDuration;
}
