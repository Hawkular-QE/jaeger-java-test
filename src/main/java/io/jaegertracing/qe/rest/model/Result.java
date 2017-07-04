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
public class Result<T> {
    private List<T> data;
    private Long total;
    private Long limit;
    private Long offset;
    private String errors;
}