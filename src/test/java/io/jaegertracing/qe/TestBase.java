package io.jaegertracing.qe;

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
import io.jaegertracing.qe.queryserver.IJaegerQuery;
import io.jaegertracing.qe.queryserver.JaegerQueryImpl;
import io.jaegertracing.qe.rest.JaegerRestClient;
import io.jaegertracing.qe.rest.model.Criteria;
import io.jaegertracing.qe.rest.model.Span;
import io.jaegertracing.qe.rest.model.Tag;
import io.jaegertracing.qe.rest.model.Trace;
import io.opentracing.Tracer;
import lombok.extern.slf4j.Slf4j;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 1.0.0
 */
@Slf4j
public class TestBase {
    protected Long testStartTime = null;
    private static Tracer tracer = null;
    private static JaegerRestClient restClient = null;
    private static IJaegerQuery jaegerQuery = null;
    static final Random RANDOM = new Random();
    public static Long nextLong(long start, long end) {
        return start + ((long) (RANDOM.nextDouble() * (end - start)));
    }
    public static Long nextLong(long end) {
        return nextLong(0L, end);
    }

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

    public JaegerRestClient restClient() {
        if (restClient == null) {
            try {
                restClient = JaegerRestClient
                        .builder()
                        .uri("http://" + JAEGER_QUERY_HOST + ":" + JAEGER_PORT_QUERY_HTTP)
                        .build();
            } catch (URISyntaxException ex) {
                logger.error("Exception,", ex);
            }
        }
        return restClient;
    }

    public IJaegerQuery jaegerQuery() {
        if (jaegerQuery == null) {
            jaegerQuery = new JaegerQueryImpl(restClient(), SERVICE_NAME);
        }
        return jaegerQuery;
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

    public Criteria getCriteria(String service, Long start) {
        return Criteria.builder().service(service).start(start * 1000L).build();
    }

    public void assertTag(List<Tag> tags, String key, Object value) {
        boolean tagFound = false;
        for (Tag tag : tags) {
            Object received = tag.getValue();
            if (tag.getKey().equals(key)) {
                if (value instanceof Number) {
                    if (((Number) value).doubleValue() == (((Number) received).doubleValue())) {
                        tagFound = true;
                    }
                } else if (value instanceof Boolean) {
                    if (((Boolean) value).booleanValue() == (((Boolean) received).booleanValue())) {
                        tagFound = true;
                    }
                } else if (((String) value).equals(received)) {
                    tagFound = true;
                }
            }
            if (tagFound) {
                break;
            }
        }
        Assert.assertTrue(tagFound);
    }

    public Tag getTag(List<Tag> tags, String key) {
        for (Tag tag : tags) {
            if (tag.getKey().equals(key)) {
                return tag;
            }
        }
        return null;
    }

    public Span getSpan(List<Span> spans, String name) {
        for (Span span : spans) {
            if (span.getOperationName().equals(name)) {
                return span;
            }
        }
        return null;
    }

    @AfterSuite
    protected void afterClass() {
        waitForFlush();
    }

    protected List<Trace> getTraceList(String operationName, Long testStartTime, int traceCount) {
        return getTraceList(operationName, testStartTime, null, traceCount);
    }

    protected List<Trace> getTraceList(String operationName, Long testStartTime, Long testEndTime, int expectedTraceCount) {
        List<Trace> traces = null;
        for (long waitTime = 30 * 1000L; waitTime > 0;) {
            traces = jaegerQuery().listTrace(operationName, testStartTime, testEndTime);
            if (traces.size() >= expectedTraceCount) {
                return traces;
            }
            sleep(1000L);//Sleep 1 second
            waitTime -= 1000L;
        }
        return traces;
    }
}
