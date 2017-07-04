package io.jaegertracing.qe.rest.jaxrs;

import io.jaegertracing.qe.rest.jaxrs.fasterxml.jackson.ClientObjectMapper;

import java.io.IOException;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

@Provider
public class RequestLoggingFilter implements ClientRequestFilter {

    private static final Logger LOG = LoggerFactory.getLogger(RequestLoggingFilter.class);
    private static final ObjectMapper OBJECT_MAPPER = new ClientObjectMapper();

    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug(">> HTTP: {}", requestContext.getMethod());
            LOG.debug(">> URI: {}", requestContext.getUri());
            LOG.debug(">> Headers: {}", requestContext.getHeaders());
            LOG.debug(">> Data: {}", OBJECT_MAPPER.writerWithDefaultPrettyPrinter()
                .writeValueAsString(requestContext.getEntity()));
        }
    }
}
