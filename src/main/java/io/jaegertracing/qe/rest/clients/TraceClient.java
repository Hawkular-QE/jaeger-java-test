package io.jaegertracing.qe.rest.clients;

import io.jaegertracing.qe.rest.model.Criteria;
import io.jaegertracing.qe.rest.model.Result;
import io.jaegertracing.qe.rest.model.Trace;
import io.jaegertracing.qe.rest.ClientResponse;
import io.jaegertracing.qe.rest.jaxrs.Empty;

/**
 * @author Jeeva Kandasamy (jkandasa)
 */
public interface TraceClient {
	ClientResponse<Empty> archive(String traceId);

	ClientResponse<Result<Trace>> getArchived(String traceId);

	ClientResponse<Result<Trace>> get(String traceId);

	ClientResponse<Result<Trace>> list(Criteria criteria);
}