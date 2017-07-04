package io.jaegertracing.qe.rest;

import static com.google.common.base.Preconditions.checkNotNull;
import io.jaegertracing.qe.rest.jaxrs.RestFactory;
import io.jaegertracing.qe.rest.typeresolvers.CollectionJavaTypeResolver;
import io.jaegertracing.qe.rest.typeresolvers.MapJavaTypeResolver;
import io.jaegertracing.qe.rest.typeresolvers.SimpleJavaTypeResolver;

public abstract class BaseClient<T> {

    private T restAPI;
    private SimpleJavaTypeResolver simpleJavaTypeResolver = new SimpleJavaTypeResolver();
    private CollectionJavaTypeResolver collectionJavaTypeResolver = new CollectionJavaTypeResolver();
    private MapJavaTypeResolver mapJavaTypeResolver = new MapJavaTypeResolver();

    public BaseClient(ClientInfo clientInfo, RestFactory<T> restFactory) {
        checkNotNull(clientInfo);
        restAPI = restFactory.createAPI(clientInfo);
    }

    public T restApi() {
        return this.restAPI;
    }

    public SimpleJavaTypeResolver simpleResolver() {
        return simpleJavaTypeResolver;
    }

    public CollectionJavaTypeResolver collectionResolver() {
        return collectionJavaTypeResolver;
    }

    public MapJavaTypeResolver mapResolver() {
        return mapJavaTypeResolver;
    }
}
