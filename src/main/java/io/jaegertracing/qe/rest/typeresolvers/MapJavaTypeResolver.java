package io.jaegertracing.qe.rest.typeresolvers;

import io.jaegertracing.qe.rest.jaxrs.fasterxml.jackson.ClientObjectMapper;

import java.util.Map;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MapJavaTypeResolver {

    private final ObjectMapper objectMapper;

    public MapJavaTypeResolver() {
        this.objectMapper = new ClientObjectMapper();
    }

    /**
     * Map with Generic Key and Value, i.e.: Map<String, TaggedBucketPoint>
     */
    public JavaType get(Class<? extends Map> mapClazz, Class<?> mapClazzKey, Class<?> mapClazzValue) {
        JavaType mapClazzKeyType = objectMapper.getTypeFactory().constructType(mapClazzKey);
        JavaType mapClazzValueType = objectMapper.getTypeFactory().constructType(mapClazzValue);

        return objectMapper.getTypeFactory().constructMapType(mapClazz, mapClazzKeyType, mapClazzValueType);
    }

    /**
     * Map with Generic Key and Genric Value, i.e.: Map<String, List<TaggedBucketPoint>>
     */
    public JavaType get(
        Class<? extends Map> mapClazz, Class<?> mapClazzKey, Class<?> mapClazzValue, Class<?> mapClazzParametrizedValue) {
        JavaType mapClazzKeyType = objectMapper.getTypeFactory().constructType(mapClazzKey);
        JavaType parametrizedClazzType = objectMapper.getTypeFactory().constructParametrizedType(mapClazzValue, mapClazzValue, mapClazzParametrizedValue);

        return objectMapper.getTypeFactory().constructMapType(mapClazz, mapClazzKeyType, parametrizedClazzType);
    }

    /**
     * Map of a Map with Generic Key and Value, i.e.: Map<ElementType, Map<CanonicalPath, Integer>>
     */
    public JavaType get(
        Class<? extends Map> mapClazz, Class<?> mapClazzKey, Class<? extends Map> mapClazzValue, Class<?> mapClazzParametrizedKey, Class<?> mapClazzParametrizedValue) {
        JavaType mapClazzKeyType = objectMapper.getTypeFactory().constructType(mapClazzKey);

        JavaType mapClazzParametrizedKeyType = objectMapper.getTypeFactory().constructType(mapClazzParametrizedKey);
        JavaType mapClazzParametrizedValueType = objectMapper.getTypeFactory().constructType(mapClazzParametrizedValue);
        JavaType innerMap = objectMapper.getTypeFactory().constructMapType(mapClazzValue, mapClazzParametrizedKeyType, mapClazzParametrizedValueType);

        return objectMapper.getTypeFactory().constructMapType(mapClazz, mapClazzKeyType, innerMap);
    }
}
