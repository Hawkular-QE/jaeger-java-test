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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;

import io.jaegertracing.Configuration.SenderConfiguration;
import io.jaegertracing.internal.JaegerTracer;
import io.jaegertracing.internal.reporters.RemoteReporter;
import io.jaegertracing.internal.samplers.ProbabilisticSampler;
import io.jaegertracing.spi.Sampler;
import io.opentracing.Tracer;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 1.0.0
 */
public class TestBase {
    public SimpleRestClient simpleRestClient = new SimpleRestClient();
    protected Instant testStartTime = null;
    private static Tracer tracer = null;
    public static final ThreadLocalRandom RANDOM = ThreadLocalRandom.current();

    private static Map<String, String> envs = System.getenv();
    private static final String JAEGER_AGENT_HOST = envs.getOrDefault("JAEGER_AGENT_HOST", "jaeger-agent");
    private static final Integer JAEGER_AGENT_PORT = Integer.valueOf(envs.getOrDefault("JAEGER_AGENT_PORT", "6831"));
    private static final String JAEGER_COLLECTOR_HOST = envs.getOrDefault("JAEGER_COLLECTOR_HOST", "jaeger-collector");
    private static final String JAEGER_COLLECTOR_PORT = envs.getOrDefault("JAEGER_PORT_ZIPKIN_COLLECTOR", "14268");
    private static Integer JAEGER_FLUSH_INTERVAL = Integer.valueOf(envs.getOrDefault("JAEGER_FLUSH_INTERVAL", "1000"));

    private static final String TEST_SERVICE_NAME = envs.getOrDefault("TEST_SERVICE_NAME", "qe");
    private static final String USE_COLLECTOR_OR_AGENT = envs.getOrDefault("USE_COLLECTOR_OR_AGENT", "collector");

    private static final Logger logger = LoggerFactory.getLogger(TestBase.class);

    @Rule
    public TestRule watcher = new TestWatcher() {
        protected void starting(Description description) {
            logger.debug("Starting test: " + description.getMethodName());
        }
    };

    @Before
    public void updateTestStartTime() {
        testStartTime = Instant.now();
        sleep(10);
    }

    /**
     *
     * @param tags Map of tags from a span
     * @param key name of the tag we want to check
     * @param expectedValue expected value of that tag
     */
    public void myAssertTag(Map<String, Object> tags, String key, Object expectedValue) {
        assertTrue("Could not find key: " + key, tags.containsKey(key));
        Object actualValue = tags.get(key);
        assertEquals("Wrong value for key " + key + " expected " + expectedValue.toString(), expectedValue,
                actualValue);
    }

    public Tracer tracer() {
        if (tracer == null) {
            SenderConfiguration conf = null;

            if (USE_COLLECTOR_OR_AGENT.equals("collector")) {
                String httpEndpoint = "http://" + JAEGER_COLLECTOR_HOST + ":" + JAEGER_COLLECTOR_PORT + "/api/traces";
                logger.info("Using collector endpoint [" + httpEndpoint + "]");
                conf = new SenderConfiguration()
                        .withEndpoint(httpEndpoint);
            } else {
                logger.info("Using JAEGER agent on host " + JAEGER_AGENT_HOST + " port " + JAEGER_AGENT_PORT);
                conf = new SenderConfiguration()
                        .withAgentHost(JAEGER_AGENT_HOST)
                        .withAgentPort(JAEGER_AGENT_PORT);
            }
            RemoteReporter remoteReporter = new RemoteReporter.Builder()
                    .withSender(conf.getSender())
                    .withFlushInterval(JAEGER_FLUSH_INTERVAL)
                    .build();

            Sampler sampler = new ProbabilisticSampler(1.0);
            tracer = new JaegerTracer.Builder(TEST_SERVICE_NAME)
                    .withReporter(remoteReporter)
                    .withSampler(sampler)
                    .build();
        }
        return tracer;
    }

    public void waitForFlush() {
        sleep(JAEGER_FLUSH_INTERVAL + 10L);
    }

    public void sleep(long milliseconds) {
        try {
            //logger.debug("Sleeping {} ms", milliseconds);
            Thread.sleep(milliseconds);
        } catch (InterruptedException ex) {
            logger.error("Exception,", ex);
        }
    }

    /**
     *
     * @param spans List of spans from a given trace
     * @param targetOperationName the operation name of the span we're looking for
     * @return A Span with the specified operation name
     */
    public QESpan getSpanByOperationName(List<QESpan> spans, String targetOperationName) {
        for (QESpan s : spans) {
            if (s.getOperation().equals(targetOperationName)) {
                return s;
            }
        }
        return null;
    }

    /**
     * Convert all JSON Spans in the trace returned by the Jaeeger Rest API to QESpans
     **
     * @param trace A single trace
     * @return A list of all spans contained in this trace
     */
    public List<QESpan> getSpansFromTrace(JsonNode trace) {
        List<QESpan> spans = new ArrayList<>();
        Iterator<JsonNode> spanIterator = trace.get("spans").iterator();

        while (spanIterator.hasNext()) {
            JsonNode jsonSpan = spanIterator.next();
            QESpan span = createSpanFromJsonNode(jsonSpan);
            spans.add(span);
        }
        return spans;
    }

    /**
     * Convert a Span in JSON returned from the Jaeger REST API to a QESpan
     * @param jsonSpan JSON representation of a span from a trace
     * @return A QESpan build from the given json
     */
    public QESpan createSpanFromJsonNode(JsonNode jsonSpan) {
        Map<String, Object> tags = new HashMap<>();

        JsonNode jsonTags = jsonSpan.get("tags");
        Iterator<JsonNode> jsonTagsIterator = jsonTags.iterator();
        while (jsonTagsIterator.hasNext()) {
            JsonNode jsonTag = jsonTagsIterator.next();
            String key = jsonTag.get("key").asText();
            String tagType = jsonTag.get("type").asText();
            switch (tagType) {
                case "bool":
                    boolean b = jsonTag.get("value").asBoolean();
                    tags.put(key, b);
                    break;
                case "float64":
                    Number n = jsonTag.get("value").asDouble();
                    tags.put(key, n);
                    break;
                case "int64":
                    Integer i = jsonTag.get("value").asInt();
                    tags.put(key, i);
                    break;
                case "string":
                    String s = jsonTag.get("value").asText();
                    tags.put(key, s);
                    break;
                default:
                    throw new RuntimeException("Unknown tag type [" + tagType + "[");
            }
        }

        Long start = jsonSpan.get("startTime").asLong();
        Long duration = jsonSpan.get("duration").asLong();
        String operation = jsonSpan.get("operationName").textValue();
        String id = jsonSpan.get("spanID").textValue();

        QESpan qeSpan = new QESpan.Builder(tags, start, duration, operation, id)
                .json(jsonSpan)
                .build();
        return qeSpan;
    }
}
