package io.jaegertracing.qe.rest.clients;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleRestClient {
    private static Map<String, String> evs = System.getenv();
    private static final Integer JAEGER_FLUSH_INTERVAL = new Integer(evs.getOrDefault("JAEGER_FLUSH_INTERVAL", "1000"));
    private static final Integer JAEGER_API_PORT = new Integer(evs.getOrDefault("JAEGER_API_PORT", "16686"));
    private static final String JAEGER_SERVER_HOST = evs.getOrDefault("JAEGER_SERVER_HOST", "localhost");
    private static final String SERVICE_NAME = evs.getOrDefault("SERVICE_NAME", "qe");
    private ObjectMapper jsonObjectMapper = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(SimpleRestClient.class);

    /**
     * TODO: Figure out the Jaeger REST Api.  Key code can be found
     *
     *  https://github.com/uber/jaeger/blob/master/cmd/query/app/handler.go#L120-L130 with parameter info
     *  https://github.com/uber/jaeger/blob/master/cmd/query/app/query_parser.go#L68-L81
     *
     * GET all traces for a service: http://localhost:3001/api/traces?service=something
     * GET a Trace by id: http://localhost:3001/api/traces/23652df68bd54e15
     * GET services http://localhost:3001/api/services
     *
     * GET after a specific time: http://localhost:3001/api/traces?service=something&start=1492098196598
     * NOTE: time is in MICROseconds.
     *
     * FIXME - add retry ability?
     *
     */
    public List<JsonNode> getTraces(String parameters) {
        waitForFlush(); // TODO make sure this is necessary
        List<JsonNode> traces = new ArrayList<>();
        Client client = ClientBuilder.newClient();
        String targetUrl = "http://" + JAEGER_SERVER_HOST + ":" + JAEGER_API_PORT + "/api/traces?service=" + SERVICE_NAME;
        if (parameters != null && !parameters.trim().isEmpty()) {
            targetUrl = targetUrl + "&" + parameters;
        }

        try  {
            WebTarget target = client.target(targetUrl);
            Invocation.Builder builder = target.request();
            builder.accept(MediaType.APPLICATION_JSON);
            String result = builder.get(String.class);

            JsonNode jsonPayload = jsonObjectMapper.readTree(result);
            JsonNode data = jsonPayload.get("data");
            Iterator<JsonNode> traceIterator = data.iterator();
            while (traceIterator.hasNext()) {
                traces.add(traceIterator.next());
            }
        } catch (IOException e) {
            // TODO what is the best thing to do here
            throw new RuntimeException(e);
        } finally {
            client.close();
        }

        return traces;
    }

    /**
     * Get all traces from the server
     */
    public List<JsonNode> getTraces()  {
        return getTraces("");
    }

    /**
     * Return all of the traces created since the start time given.  NOTE: The Jaeger Rest API
     * requires a time in microseconds.
     *
     * @param testStartTime time the test started
     * @return A List of Traces created after the time specified.
     */
    public List<JsonNode> getTracesSinceTestStart(Instant testStartTime) {
        List<JsonNode> traces = getTraces("start=" + (testStartTime.toEpochMilli() * 1000));
        return traces;
    }

    /**
     * Return all of the traces created between the start and end times given.  NOTE: The Jaeger Rest API requires times
     * in microseconds.
     *
     * @param start start time
     * @param end end time
     * @return A List of traces created between the times specified.
     */
    public List<JsonNode> getTracesBetween(Instant start, Instant end) {
        String parameters = "start=" + (start.toEpochMilli() * 1000) + "&end=" + (end.toEpochMilli() * 1000);
        List<JsonNode> traces = getTraces(parameters);
        return traces;

    }


    /**
     * Make sure spans are flushed before trying to retrieve them
     */
    public void waitForFlush() {
        try {
            Thread.sleep(JAEGER_FLUSH_INTERVAL);
        } catch (InterruptedException e) {
            logger.warn("Sleep interrupted", e);
        }
    }


    /**
     * Return a formatted JSON String
     * @param json Some json that you want to format
     * @return pretty formatted json
     */
    public String prettyPrintJson(JsonNode json) {
        String pretty = "";
        try {
            ObjectWriter writer = jsonObjectMapper.writerWithDefaultPrettyPrinter();
            pretty = writer.writeValueAsString(json);
        } catch (JsonProcessingException jpe) {
            logger.error("prettyPrintJson Failed", jpe);
        }

        return pretty;
    }

    /**
     * Debugging method
     *
     * @param traces A list of traces to print
     */
    protected void dumpAllTraces(List<JsonNode> traces)  {
        logger.info("Got " + traces.size() + " traces");

        for (JsonNode trace : traces) {
            logger.info("------------------ Trace {} ------------------", trace.get("traceId"));
            Iterator<JsonNode> spanIterator = trace.get("spans").iterator();
            while (spanIterator.hasNext()) {
                JsonNode span = spanIterator.next();
                prettyPrintJson(span);  // TODO does this work?
            }
        }
    }
}
