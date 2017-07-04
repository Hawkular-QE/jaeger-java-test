package io.jaegertracing.qe.tests;

import java.net.URISyntaxException;
import java.time.Instant;
import java.util.List;
import java.util.Random;

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
    @Parameters({ "jaegerServerHost", "jaegerQueryProtocol", "jaegerAgentHost", "jaegerZipkinThriftPort",
            "jaegerAgentCompactPort", "jaegerAgentBinaryPort", "jaegerZipkinCollectorPort", "jaegerQueryPort",
            "flushInterval", "serviceName" })
    public void updateTestData(ITestContext context, @Optional String jaegerServerHost,
            @Optional String jaegerQueryProtocol, @Optional String jaegerAgentHost,
            @Optional Integer jaegerZipkinThriftPort, @Optional Integer jaegerAgentCompactPort,
            @Optional Integer jaegerAgentBinaryPort, @Optional Integer jaegerZipkinCollectorPort,
            @Optional Integer jaegerQueryPort, @Optional Integer flushInterval, @Optional String serviceName) {
        testData = TestData
                .builder()
                .serviceName(serviceName)
                .config(JaegerConfiguration
                        .builder()
                        .serverHost(jaegerServerHost == null ?
                                getEnv("JAEGER_QUERY_HOST", "localhost") : jaegerServerHost)
                        .serverQueryProtocol(jaegerQueryProtocol == null ?
                                getEnv("JAEGER_QUERY_PROTOCOL", "http") : jaegerQueryProtocol)
                        .agentHost(jaegerAgentHost == null ?
                                getEnv("JAEGER_AGENT_HOST", "localhost") : jaegerAgentHost)
                        .queryPort(jaegerQueryPort == null ?
                                Integer.valueOf(getEnv("JAEGER_PORT_QUERY_HTTP", "16686")) : jaegerQueryPort)
                        .agentZipkinThriftPort(jaegerZipkinThriftPort == null ?
                                Integer.valueOf(getEnv("JAEGER_PORT_AGENT_ZIPKIN_THRIFT", "5775"))
                                : jaegerZipkinThriftPort)
                        .agentCompactPort(jaegerAgentCompactPort == null ?
                                Integer.valueOf(getEnv("JAEGER_PORT_AGENT_COMPACT", "6831"))
                                : jaegerAgentCompactPort)
                        .agentBinaryPort(jaegerAgentBinaryPort == null ?
                                Integer.valueOf(getEnv("JAEGER_PORT_AGENT_BINARY", "6832"))
                                : jaegerAgentBinaryPort)
                        .zipkinCollectorPort(jaegerZipkinCollectorPort == null ?
                                Integer.valueOf(getEnv("JAEGER_PORT_ZIPKIN_COLLECTOR", "14268"))
                                : jaegerZipkinCollectorPort)
                        .flushInterval(flushInterval).build())
                .build();
    }

    private String getEnv(String key, String defaultValue) {
        if (System.getenv(key) != null) {
            return System.getenv(key);
        }
        return defaultValue;
    }

    @BeforeMethod
    public void updateTestStartTime() {
        testStartTime = Instant.now().toEpochMilli();
    }

    public JaegerRestClient restClient() {
        if (restClient == null) {
            try {
                restClient = JaegerRestClient
                        .builder()
                        .uri(testData.getConfig().getServerQueryProtocol() + "://"
                                + testData.getConfig().getServerHost()
                                + ":" + testData.getConfig().getQueryPort())
                        .build();
            } catch (URISyntaxException ex) {
                _logger.error("Exception,", ex);
            }
        }
        return restClient;
    }

    public IJaegerQuery jaegerQuery() {
        if (jaegerQuery == null) {
            jaegerQuery = new JaegerQueryImpl(restClient(), testData.getServiceName());
        }
        return jaegerQuery;
    }

    public Tracer tracer() {
        if (tracer == null) {
            Sender sender = new UdpSender(testData.getConfig().getAgentHost(), testData.getConfig()
                    .getAgentCompactPort(), 1024);
            Metrics metrics = new Metrics(new StatsFactoryImpl(new NullStatsReporter()));
            Reporter reporter = new RemoteReporter(sender, testData.getConfig().getFlushInterval(), 100, metrics);
            Sampler sampler = new ProbabilisticSampler(1.0);
            tracer = new com.uber.jaeger.Tracer.Builder(testData.getServiceName(), reporter, sampler).build();
            _logger.debug("Tracer details[{}]", testData);
        }
        return tracer;
    }

    public void waitForFlush() {
        sleep(testData.getConfig().getFlushInterval() + 1L);
    }

    public void sleep(long milliseconds) {
        try {
            _logger.debug("Sleeping >> {} ms", milliseconds);
            Thread.sleep(milliseconds);
        } catch (InterruptedException ex) {
            _logger.error("Exception,", ex);
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
                    if (((Boolean) value).booleanValue() == (((Boolean) received)
                            .booleanValue())) {
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
}
