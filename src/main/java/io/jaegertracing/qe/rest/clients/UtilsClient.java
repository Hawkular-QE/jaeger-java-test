package io.jaegertracing.qe.rest.clients;

import io.jaegertracing.qe.rest.model.Dependency;
import io.jaegertracing.qe.rest.model.Result;
import io.jaegertracing.qe.rest.ClientResponse;

/**
 * @author Jeeva Kandasamy (jkandasa)
 */
public interface UtilsClient {
	ClientResponse<Result<String>> getServices();

	ClientResponse<Result<String>> getOperations(String service);

	ClientResponse<Result<Dependency>> getDependencies(long endTs, long lookback);
}
