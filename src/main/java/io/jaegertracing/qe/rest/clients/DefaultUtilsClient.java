package io.jaegertracing.qe.rest.clients;

import io.jaegertracing.qe.rest.jaxrs.handlers.JaegerRestHandler;
import io.jaegertracing.qe.rest.model.Dependency;
import io.jaegertracing.qe.rest.model.Result;
import io.jaegertracing.qe.rest.BaseClient;
import io.jaegertracing.qe.rest.ClientInfo;
import io.jaegertracing.qe.rest.ClientResponse;
import io.jaegertracing.qe.rest.DefaultClientResponse;
import io.jaegertracing.qe.rest.jaxrs.ResponseCodes;
import io.jaegertracing.qe.rest.jaxrs.RestFactory;

import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.JavaType;

/**
 * @author Jeeva Kandasamy (jkandasa)
 */
public class DefaultUtilsClient extends BaseClient<JaegerRestHandler> implements
        UtilsClient {

    public DefaultUtilsClient(ClientInfo clientInfo) {
        super(clientInfo, new RestFactory<>(JaegerRestHandler.class));
    }

    @Override
    public ClientResponse<Result<String>> getServices() {
        Response response = null;
        try {
            response = restApi().getServices();
            JavaType javaType = simpleResolver().get(Result.class, String.class);
            return new DefaultClientResponse<>(javaType, response, ResponseCodes.GET_SUCCESS_200);
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    @Override
    public ClientResponse<Result<String>> getOperations(String service) {
        Response response = null;
        try {
            response = restApi().getOperations(service);
            JavaType javaType = simpleResolver().get(Result.class, String.class);
            return new DefaultClientResponse<>(javaType, response, ResponseCodes.GET_SUCCESS_200);
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    @Override
    public ClientResponse<Result<Dependency>> getDependencies(long endTs, long lookback) {
        Response response = null;
        try {
            response = restApi().getDependencies(endTs, lookback);
            JavaType javaType = simpleResolver().get(Result.class, Dependency.class);
            return new DefaultClientResponse<>(javaType, response, ResponseCodes.GET_SUCCESS_200);
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }
}
