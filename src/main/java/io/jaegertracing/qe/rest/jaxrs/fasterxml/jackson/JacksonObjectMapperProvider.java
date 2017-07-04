package io.jaegertracing.qe.rest.jaxrs.fasterxml.jackson;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.databind.ObjectMapper;

@Provider
public class JacksonObjectMapperProvider implements ContextResolver<ObjectMapper> {
    private final ObjectMapper objectMapper;

    public JacksonObjectMapperProvider() throws Exception {
        objectMapper = new ClientObjectMapper();
    }

    @Override
    public ObjectMapper getContext(Class<?> type) {
        return objectMapper;
    }

}
