package io.jaegertracing.qe.api.model;

import lombok.ToString;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class TestData {
	private JaegerConfiguration config;
	private String serviceName;

}
