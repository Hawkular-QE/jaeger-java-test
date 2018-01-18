package io.jaegertracing.qe;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.uber.jaeger.metrics.Metrics;
import com.uber.jaeger.metrics.NullStatsReporter;
import com.uber.jaeger.metrics.StatsFactoryImpl;
import com.uber.jaeger.reporters.RemoteReporter;
import com.uber.jaeger.reporters.Reporter;
import com.uber.jaeger.samplers.ProbabilisticSampler;
import com.uber.jaeger.samplers.Sampler;
import com.uber.jaeger.senders.HttpSender;
import com.uber.jaeger.senders.Sender;
import com.uber.jaeger.senders.UdpSender;

import io.jaegertracing.qe.rest.clients.SimpleRestClient;
import io.jaegertracing.qe.rest.model.QESpan;
import io.opentracing.Tracer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import lombok.extern.slf4j.Slf4j;

import org.testng.annotations.BeforeMethod;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 1.0.0
 */
@Slf4j
public class TestBase {
    public SimpleRestClient simpleRestClient = new SimpleRestClient();
    protected Long testStartTime = null;
    private static Tracer tracer = null;
    public static final Random RANDOM = new Random();

    private static Map<String, String> envs = System.getenv();
    private static final Integer JAEGER_AGENT_COMPACT_PORT = Integer.valueOf(envs.getOrDefault("JAEGER_AGENT_COMPACT_PORT", "6831"));
    private static final String JAEGER_AGENT_HOST = envs.getOrDefault("JAEGER_AGENT_HOST", "localhost");
    private static final String JAEGER_COLLECTOR_HOST  = envs.getOrDefault("JAEGER_COLLECTOR_HOST", "localhost");
    private static Integer JAEGER_FLUSH_INTERVAL = Integer.valueOf(envs.getOrDefault("JAEGER_FLUSH_INTERVAL", "1000"));
    private static final String JAEGER_QUERY_HOST = envs.getOrDefault("JAEGER_QUERY_HOST", "localhost");
    private static final String JAEGER_PORT_AGENT_ZIPKIN_THRIFT = envs.getOrDefault("JAEGER_PORT_AGENT_ZIPKIN_THRIFT", "5775");
    private static final String JAEGER_PORT_QUERY_HTTP  = envs.getOrDefault("JAEGER_PORT_QUERY_HTTP", "16686");
    private static final String JAEGER_PORT_ZIPKIN_COLLECTOR = envs.getOrDefault("JAEGER_PORT_ZIPKIN_COLLECTOR", "14268");
    private static final String SERVICE_NAME  = envs.getOrDefault("SERVICE_NAME", "qe");
    private static final String USE_COLLECTOR_OR_AGENT = envs.getOrDefault("USE_COLLECTOR_OR_AGENT", "collector");

    @BeforeMethod
    public void updateTestStartTime() {
        testStartTime = System.currentTimeMillis() - 1L;
    }

    /**
     *
     * @param tags Map of tags from a span
     * @param key name of the tag we want to check
     * @param expectedValue expected value of that tag
     */
    public void myAssertTag(Map<String, Object> tags, String key, Object expectedValue) {
        assertTrue(tags.containsKey(key), "Could not find key: " + key);
        Object actualValue = tags.get(key);
        assertEquals(expectedValue, actualValue, "Wrong value for key " + key + " expected " + expectedValue.toString());
    }

    public Tracer tracer() {
        Sender sender;

        if (tracer == null) {
            if (USE_COLLECTOR_OR_AGENT.equals("collector")) {
                String httpEndpoint = "http://" + JAEGER_COLLECTOR_HOST + ":" + JAEGER_PORT_ZIPKIN_COLLECTOR + "/api/traces";
                logger.info("Using collector endpoint [" + httpEndpoint + "]");
                sender = new HttpSender(httpEndpoint);
            } else {
                sender = new UdpSender(JAEGER_AGENT_HOST, JAEGER_AGENT_COMPACT_PORT, 1024);
                logger.info("Using JAEGER agent on host " + JAEGER_AGENT_HOST + " port " + JAEGER_AGENT_COMPACT_PORT);
            }

            Metrics metrics = new Metrics(new StatsFactoryImpl(new NullStatsReporter()));
            Reporter reporter = new RemoteReporter(sender, JAEGER_FLUSH_INTERVAL, 100, metrics);
            Sampler sampler = new ProbabilisticSampler(1.0);
            tracer = new com.uber.jaeger.Tracer.Builder(SERVICE_NAME, reporter, sampler).build();
        }
        return tracer;
    }

    public void waitForFlush() {
        sleep(JAEGER_FLUSH_INTERVAL + 10L);
    }

    public void sleep(long milliseconds) {
        try {
            logger.debug("Sleeping {} ms", milliseconds);
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
                case "bool" :
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
                case "string" :
                    String s = jsonTag.get("value").asText();
                    tags.put(key, s);
                    break;
                default:
                    throw new RuntimeException("Unknown tag type [" + tagType + "[");

            }
        }

        Long start = jsonSpan.get("startTime").asLong();
        Long duration = jsonSpan.get("duration").asLong();
        Long end = start + duration;
        String operation = jsonSpan.get("operationName").textValue();
        String id = jsonSpan.get("spanID").textValue();

        QESpan qeSpan = new QESpan(tags, start, end, duration, operation, id, null, null, jsonSpan);
        return qeSpan;
    }
}
