package io.jaegertracing.qe.rest.clients;

import io.jaegertracing.qe.rest.jaxrs.handlers.JaegerRestHandler;
import io.jaegertracing.qe.rest.model.Criteria;
import io.jaegertracing.qe.rest.model.Result;
import io.jaegertracing.qe.rest.model.Trace;
import io.jaegertracing.qe.rest.BaseClient;
import io.jaegertracing.qe.rest.ClientInfo;
import io.jaegertracing.qe.rest.ClientResponse;
import io.jaegertracing.qe.rest.DefaultClientResponse;
import io.jaegertracing.qe.rest.jaxrs.Empty;
import io.jaegertracing.qe.rest.jaxrs.ResponseCodes;
import io.jaegertracing.qe.rest.jaxrs.RestFactory;

import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.JavaType;

/**
 * @author Jeeva Kandasamy (jkandasa)
 */
public class DefaultTraceClient extends BaseClient<JaegerRestHandler> implements
        TraceClient {

    public DefaultTraceClient(ClientInfo clientInfo) {
        super(clientInfo, new RestFactory<>(JaegerRestHandler.class));
    }

    @Override
    public ClientResponse<Result<Trace>> get(String traceId) {
        Response response = null;
        try {
            response = restApi().getTrace(traceId);
            JavaType javaType = simpleResolver().get(Result.class, Trace.class);
            return new DefaultClientResponse<>(javaType, response, ResponseCodes.GET_SUCCESS_200);
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    @Override
    public ClientResponse<Result<Trace>> list(Criteria criteria) {
        Response response = null;
        try {
            response = restApi().listTrace(criteria.getOperationName(), criteria.getService(), criteria.getTag(),
                    criteria.getStart(), criteria.getEnd(), criteria.getLimit(), criteria.getMinDuration(),
                    criteria.getMaxDuration());
            JavaType javaType = simpleResolver().get(Result.class, Trace.class);
            return new DefaultClientResponse<>(javaType, response, ResponseCodes.GET_SUCCESS_200);
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    @Override
    public ClientResponse<Empty> archive(String traceId) {
        Response response = null;
        try {
            response = restApi().archiveTrace(traceId);
            JavaType javaType = simpleResolver().get(Empty.class);
            return new DefaultClientResponse<>(javaType, response, ResponseCodes.NO_CONTENT_204);
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    @Override
    public ClientResponse<Result<Trace>> getArchived(String traceId) {
        Response response = null;
        try {
            response = restApi().getArchivedTrace(traceId);
            JavaType javaType = simpleResolver().get(Result.class, Trace.class);
            return new DefaultClientResponse<>(javaType, response, ResponseCodes.GET_SUCCESS_200);
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

}
