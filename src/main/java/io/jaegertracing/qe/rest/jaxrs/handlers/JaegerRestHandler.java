package io.jaegertracing.qe.rest.jaxrs.handlers;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author Jeeva Kandasamy (jkandasa)
 */

@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface JaegerRestHandler {
    @GET
    @Path("/traces/{id}")
    Response getTrace(@PathParam("id") String traceId);

    @GET
    @Path("/traces")
    Response listTrace(
            @QueryParam("operation") String operation,
            @QueryParam("service") String service,
            @QueryParam("tag") String tag,
            @QueryParam("start") Long start,
            @QueryParam("end") Long end,
            @QueryParam("limit") Long limit,
            @QueryParam("minDuration") String minDuration,
            @QueryParam("maxDuration") String maxDuration);

    @GET
    @Path("/archive/{id}")
    Response getArchivedTrace(@PathParam("id") String traceId);

    @POST
    @Path("/archive/{id}")
    Response archiveTrace(@PathParam("id") String traceId);

    @GET
    @Path("/services")
    Response getServices();

    @GET
    @Path("/operations")
    Response getOperations(@QueryParam("service") String service);

    // This will be removed later.
    // https://github.com/uber/jaeger/blob/master/cmd/query/app/handler.go#L128
    @GET
    @Path("/services/{service}/operations")
    Response getOperationsLegacy(@PathParam("service") String service);

    @GET
    @Path("/dependencies")
    Response getDependencies(long endTs, long lookback);
}
