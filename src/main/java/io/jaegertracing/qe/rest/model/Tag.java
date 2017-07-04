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
public class Tag {
    private String key;
    private String type;
    private Object value;
}
