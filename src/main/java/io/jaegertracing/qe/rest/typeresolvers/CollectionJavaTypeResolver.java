package io.jaegertracing.qe.rest.typeresolvers;

import io.jaegertracing.qe.rest.jaxrs.fasterxml.jackson.ClientObjectMapper;

import java.util.List;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CollectionJavaTypeResolver {

    private final ObjectMapper objectMapper;

    public CollectionJavaTypeResolver() {
        this.objectMapper = new ClientObjectMapper();
    }

    /**
     * List with Generic, i.e.: List<Double>
     */
    public JavaType get(Class<? extends List> collectionClazz, Class<?> clazz) {
        JavaType clazzType = objectMapper.getTypeFactory().constructType(clazz);
        return objectMapper.getTypeFactory().constructCollectionType(collectionClazz, clazzType);
    }

    /**
     * List with Generic, Generic, i.e.: List<Metric<Double>>
     */
    public JavaType get(Class<? extends List> collectionClazz, Class<?> clazz, Class<?> parametrizedClazz) {
        JavaType parametrizedClazzType = objectMapper.getTypeFactory().constructType(parametrizedClazz);
        JavaType clazzType = objectMapper.getTypeFactory().constructParametrizedType(clazz, clazz, parametrizedClazzType);

        return objectMapper.getTypeFactory().constructCollectionType(collectionClazz, clazzType);
    }
}
