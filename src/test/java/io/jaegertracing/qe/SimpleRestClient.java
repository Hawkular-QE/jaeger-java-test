/**
 * Copyright 2017-2018 The Jaeger Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package io.jaegertracing.qe;

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
import java.util.concurrent.TimeUnit;

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
    private static final String JAEGER_QUERY_HOST = evs.getOrDefault("JAEGER_QUERY_HOST", "jaeger-query");
    private static final Integer JAEGER_QUERY_SERVICE_PORT = new Integer(evs.getOrDefault("JAEGER_QUERY_SERVICE_PORT", "80"));
    private static final String SERVICE_NAME = evs.getOrDefault("SERVICE_NAME", "qe");

    // Limit for the number of retries when getting traces
    private static final Integer RETRY_LIMIT = 10;

    private ObjectMapper jsonObjectMapper = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(SimpleRestClient.class);

    /**
     * GET all traces for a service: http://localhost:3001/api/traces?service=something
     * GET a Trace by id: http://localhost:3001/api/traces/23652df68bd54e15
     * GET services http://localhost:3001/api/services
     *
     * GET after a specific time: http://localhost:3001/api/traces?service=something&start=1492098196598
     * NOTE: time is in MICROseconds.
     *
     */
    private List<JsonNode> getTraces(String parameters) {
        List<JsonNode> traces = new ArrayList<>();
        Client client = ClientBuilder.newClient();
        String targetUrl = "http://" + JAEGER_QUERY_HOST + ":" + JAEGER_QUERY_SERVICE_PORT + "/api/traces?service=" + SERVICE_NAME;
        if (parameters != null && !parameters.trim().isEmpty()) {
            targetUrl = targetUrl + "&" + parameters;
        }

        logger.debug("GETTING TRACES: " + targetUrl);
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
     *
     * @param parameters Parameter string to be appended to REST call
     * @param expectedTraceCount expected number of traces
     * @return a List of traces returned from the Jaeger Query API
     */
    public List<JsonNode> getTraces(String parameters, int expectedTraceCount) {
        waitForFlush();
        List<JsonNode> traces = new ArrayList<>();
        int iterations = 0;

        // Retry for up to RETRY_LIMIT seconds to get the expected number of traces
        // TODO make wait time an argument?
        while (iterations < RETRY_LIMIT && traces.size() < expectedTraceCount) {
            iterations++;
            traces = getTraces(parameters);
            if (traces.size() >= expectedTraceCount) {
                return traces;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                logger.warn("Sleep was interrupted", e);
            }
        }

        if (traces.size() < expectedTraceCount) {
            // Get ALL traces and print them out
            logger.error("Didn't find expected number of traces; here are the ones we found");
            dumpAllTraces(traces);

            logger.error("--------------------------------------------------");
            logger.error("AND HERE ARE ALL TRACES ");
            List<JsonNode> allTraces = getTraces("");
            dumpAllTraces(allTraces);
        }

        return traces;
    }


    /**
     * Return all of the traces created since the start time given.  NOTE: The Jaeger Rest API
     * requires a time in microseconds.
     *
     * @param testStartTime time the test started
     * @return A List of Traces created after the time specified.
     */
    public List<JsonNode> getTracesSinceTestStart(Instant testStartTime, int expectedTraceCount) {
        long startTime = TimeUnit.MILLISECONDS.toMicros(testStartTime.toEpochMilli());
        List<JsonNode> traces = getTraces("start=" + startTime, expectedTraceCount);
        return traces;
    }

    /**
     * Return all of the traces created between the start and end times given.  NOTE: The Jaeger Rest API requires times
     * in microseconds.
     *
     * @param testStartTime start time
     * @param testEndTime end time
     * @return A List of traces created between the times specified.
     */
    public List<JsonNode> getTracesBetween(Instant testStartTime, Instant testEndTime, int expectedTraceCount) {
        long startTime = TimeUnit.MILLISECONDS.toMicros(testStartTime.toEpochMilli());
        long endTime = TimeUnit.MILLISECONDS.toMicros(testEndTime.toEpochMilli());
        String parameters = "start=" + startTime + "&end=" + endTime;
        List<JsonNode> traces = getTraces(parameters, expectedTraceCount);
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
            logger.info("------------------ Trace {} ------------------", trace.get("traceID"));
            Iterator<JsonNode> spanIterator = trace.get("spans").iterator();
            while (spanIterator.hasNext()) {
                JsonNode span = spanIterator.next();
                logger.debug(prettyPrintJson(span));  // TODO does this work?
            }
        }
    }
}
