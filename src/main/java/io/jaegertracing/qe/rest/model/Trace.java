package io.jaegertracing.qe.rest.model;

import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author Jeeva Kandasamy (jkandasa)
 */
@Getter
@ToString
@NoArgsConstructor
public class Trace {
    private String traceID;
    private List<Span> spans;
    private Map<String, Process> processes;
}