package io.jaegertracing.qe.rest.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author Jeeva Kandasamy (jkandasa)
 */
@Getter
@ToString
@NoArgsConstructor
public class Dependency {
    private String parent;
    private String child;
    private Long callCount;
}