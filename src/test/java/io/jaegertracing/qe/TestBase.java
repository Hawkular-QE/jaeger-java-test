package io.jaegertracing.qe;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Random;

import com.uber.jaeger.senders.HttpSender;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

import com.uber.jaeger.metrics.Metrics;
import com.uber.jaeger.metrics.NullStatsReporter;
import com.uber.jaeger.metrics.StatsFactoryImpl;
import com.uber.jaeger.reporters.RemoteReporter;
import com.uber.jaeger.reporters.Reporter;
import com.uber.jaeger.samplers.ProbabilisticSampler;
import com.uber.jaeger.samplers.Sampler;
import com.uber.jaeger.senders.Sender;
import com.uber.jaeger.senders.UdpSender;

import io.jaegertracing.qe.api.model.JaegerConfiguration;
import io.jaegertracing.qe.api.model.TestData;
import io.jaegertracing.qe.queryserver.JaegerQueryImpl;
import io.jaegertracing.qe.queryserver.IJaegerQuery;
import io.jaegertracing.qe.rest.JaegerRestClient;
import io.jaegertracing.qe.rest.model.Criteria;
import io.jaegertracing.qe.rest.model.Span;
import io.jaegertracing.qe.rest.model.Tag;
import io.jaegertracing.qe.rest.model.Trace;
import io.opentracing.Tracer;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 1.0.0
 */
@Slf4j
public class TestBase {
    protected static TestData testData = null;
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

    @BeforeSuite
    @Parameters({ "jaegerServerHost", "jaegerQueryProtocol", "jaegerAgentHost", "jaegerCollectorHost",
            "jaegerZipkinThriftPort", "jaegerAgentCompactPort",
            "jaegerAgentBinaryPort", "jaegerZipkinCollectorPort",
            "jaegerQueryPort", "flushInterval", "useCollectorOrAgent", "serviceName" })
    public void updateTestData(ITestContext context,
            @Optional String jaegerServerHost,
            @Optional String jaegerQueryProtocol,
            @Optional String jaegerAgentHost,
            @Optional String jaegerCollectorHost,
            @Optional Integer jaegerZipkinThriftPort,
            @Optional Integer jaegerAgentCompactPort,
            @Optional Integer jaegerAgentBinaryPort,
            @Optional Integer jaegerZipkinCollectorPort,
            @Optional Integer jaegerQueryPort,
            @Optional Integer flushInterval,
            @Optional String useCollectorOrAgent,
            @Optional String serviceName) {

        jaegerServerHost = (jaegerServerHost == null ? getEnv("JAEGER_QUERY_HOST", "localhost") : jaegerServerHost);
        jaegerQueryProtocol = (jaegerQueryProtocol == null ? getEnv("JAEGER_QUERY_PROTOCOL", "http") : jaegerQueryProtocol);
        jaegerAgentHost = (jaegerAgentHost == null ? getEnv("JAEGER_AGENT_HOST", "localhost") : jaegerAgentHost);
        jaegerQueryPort = (jaegerQueryPort == null ? Integer.valueOf(getEnv("JAEGER_PORT_QUERY_HTTP", "16686")) : jaegerQueryPort);
        jaegerCollectorHost = (jaegerCollectorHost == null ? getEnv("JAEGER_COLLECTOR_HOST", "localhost") : jaegerCollectorHost);

        jaegerZipkinThriftPort = (jaegerZipkinThriftPort == null ? Integer.valueOf(getEnv("JAEGER_PORT_AGENT_ZIPKIN_THRIFT", "5775")): jaegerZipkinThriftPort);
        jaegerAgentCompactPort  = (jaegerAgentCompactPort == null ? Integer.valueOf(getEnv("JAEGER_PORT_AGENT_COMPACT", "6831")) : jaegerAgentCompactPort);
        jaegerAgentBinaryPort = (jaegerAgentBinaryPort == null ? Integer.valueOf(getEnv("JAEGER_PORT_AGENT_BINARY", "6832")) : jaegerAgentBinaryPort);
        jaegerZipkinCollectorPort = (jaegerZipkinCollectorPort == null ? Integer.valueOf(getEnv("JAEGER_PORT_ZIPKIN_COLLECTOR", "14268")) : jaegerZipkinCollectorPort);
        useCollectorOrAgent = (useCollectorOrAgent == null ? getEnv("USE_COLLECTOR_OR_AGENT", "collector") : useCollectorOrAgent);

        testData = TestData
                .builder()
                .serviceName(serviceName)
                .config(JaegerConfiguration
                        .builder()
                        .serverHost(jaegerServerHost)
                        .serverQueryProtocol(jaegerQueryProtocol)
                        .agentHost(jaegerAgentHost)
                        .queryPort(jaegerQueryPort)
                        .collectorHost(jaegerCollectorHost)
                        .agentZipkinThriftPort(jaegerZipkinThriftPort)
                        .agentCompactPort(jaegerAgentCompactPort)
                        .agentBinaryPort(jaegerAgentBinaryPort)
                        .zipkinCollectorPort(jaegerZipkinCollectorPort)
                        .useCollectorOrAgent(useCollectorOrAgent)
                        .flushInterval(flushInterval).build()).build();
    }

    private String getEnv(String key, String defaultValue) {
        if (System.getenv(key) != null) {
            return System.getenv(key);
        }
        return defaultValue;
    }

    @BeforeMethod
    public void updateTestStartTime() {
        testStartTime = System.currentTimeMillis() - 1L;
    }

    public JaegerRestClient restClient() {
        if (restClient == null) {
            try {
                restClient = JaegerRestClient
                        .builder()
                        .uri(testData.getConfig().getServerQueryProtocol() + "://"
                                + testData.getConfig().getServerHost() + ":" + testData.getConfig().getQueryPort())
                        .build();
            } catch (URISyntaxException ex) {
                logger.error("Exception,", ex);
            }
        }
        return restClient;
    }

    public IJaegerQuery jaegerQuery() {
        if (jaegerQuery == null) {
            jaegerQuery = new JaegerQueryImpl(restClient(),
                    testData.getServiceName());
        }
        return jaegerQuery;
    }

    public Tracer tracer() {
        Sender sender;
        JaegerConfiguration jaegerConfiguration = testData.getConfig();

        if (tracer == null) {
            if (jaegerConfiguration.useCollector()) {
                String httpEndpoint = "http://" + jaegerConfiguration.getCollectorHost() + ":" + jaegerConfiguration.getZipkinCollectorPort() + "/api/traces";
                logger.info("Using collector endpoint [" + httpEndpoint + "]");

                sender = new HttpSender(httpEndpoint);
                logger.info("Using JAEGER collector on host " + jaegerConfiguration.getCollectorHost() + " port " + jaegerConfiguration.getZipkinCollectorPort());
            } else {
                sender = new UdpSender(jaegerConfiguration.getAgentHost(), jaegerConfiguration.getAgentCompactPort(), 1024);
                logger.info("Using JAEGER agent on host " + jaegerConfiguration.getAgentHost() + " port " + jaegerConfiguration.getAgentCompactPort());
            }

            Metrics metrics = new Metrics(new StatsFactoryImpl(new NullStatsReporter()));
            Reporter reporter = new RemoteReporter(sender, jaegerConfiguration.getFlushInterval(), 100, metrics);
            Sampler sampler = new ProbabilisticSampler(1.0);
            tracer = new com.uber.jaeger.Tracer.Builder(testData.getServiceName(), reporter, sampler).build();
            logger.debug("Tracer details[{}]", testData);
        }
        return tracer;
    }

    public void waitForFlush() {
        sleep(testData.getConfig().getFlushInterval() + 10L);
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

    protected List<Trace> getTraceList(String operationName, Long testStartTime, Long testEndTime, int traceCount) {
        List<Trace> traces = null;
        for (long waitTime = 30 * 1000L; waitTime > 0;) {
            traces = jaegerQuery().listTrace(null, testStartTime, testEndTime);
            if (traces.size() >= traceCount) {
                return traces;
            }
            sleep(1000L);//Sleep 1 second
            waitTime -= 1000L;
        }
        return traces;
    }
}
