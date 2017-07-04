package io.jaegertracing.qe.rest.jaxrs;

import java.io.IOException;
import java.util.Map;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

public class RequestHeadersFilter implements ClientRequestFilter {

    private final MultivaluedMap<String, Object> headers;

    public RequestHeadersFilter(Map<String, Object> headers) {
        this.headers = new MultivaluedHashMap<>(headers);
    }

    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        requestContext.getHeaders().putAll(headers);
    }
}
