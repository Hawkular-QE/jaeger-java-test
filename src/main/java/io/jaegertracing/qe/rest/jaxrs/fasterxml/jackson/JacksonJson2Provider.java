package io.jaegertracing.qe.rest.jaxrs.fasterxml.jackson;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.plugins.providers.jackson.ResteasyJackson2Provider;

/**
 * @author jkandasa@redhat.com (Jeeva Kandasamy)
 */
@Provider
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class JacksonJson2Provider extends ResteasyJackson2Provider {

    @Override
    public void writeTo(Object value, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
                        MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException {
        ClientObjectMapper.config(locateMapper(type, mediaType));

        super.writeTo(value, type, genericType, annotations, mediaType, httpHeaders, entityStream);
    }
}
