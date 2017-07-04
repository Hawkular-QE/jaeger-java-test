package io.jaegertracing.qe.rest.typeresolvers;

import io.jaegertracing.qe.rest.jaxrs.fasterxml.jackson.ClientObjectMapper;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SimpleJavaTypeResolver {

    private final ObjectMapper objectMapper;

    public SimpleJavaTypeResolver() {
        this.objectMapper = new ClientObjectMapper();
    }

    /**
     * Simple Class, i.e.: Double
     */
    public JavaType get(Class<?> clazz) {
        return objectMapper.getTypeFactory().constructType(clazz);
    }

    /**
     * Simple Class with Generic, i.e.: Metric<Double>
     */
    public JavaType get(Class<?> clazz, Class<?> parametrizedClazz) {
        JavaType parametrizedClazzType = objectMapper.getTypeFactory().constructType(parametrizedClazz);

        return objectMapper.getTypeFactory().constructParametrizedType(clazz, clazz, parametrizedClazzType);
    }
}
