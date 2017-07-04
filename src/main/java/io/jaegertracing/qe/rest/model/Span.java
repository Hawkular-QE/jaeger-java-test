package io.jaegertracing.qe.rest.model;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author Jeeva Kandasamy (jkandasa)
 */
@Getter
@ToString
@NoArgsConstructor
public class Span {
    private String traceID;
    private String spanID;
    private Integer flags;
    private String operationName;
    private List<Reference> references;
    private Long startTime;
    private Long duration;
    private List<Tag> tags;
    private List<Log> logs;
    private String processID;
}