package io.jaegertracing.qe.rest.jaxrs;

import java.io.IOException;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class ResponseLoggingFilter implements ClientResponseFilter {

    private static final Logger LOG = LoggerFactory.getLogger(ResponseLoggingFilter.class);

    @Override
    public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("<< Response headers:{}", responseContext.getHeaders());
            LOG.debug("<< Status -> code:{}, message:{}",
                          responseContext.getStatusInfo().getStatusCode(),
                          responseContext.getStatusInfo().getReasonPhrase());
        }
    }
}
