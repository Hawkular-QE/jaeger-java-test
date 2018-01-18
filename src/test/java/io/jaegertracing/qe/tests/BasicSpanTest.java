package io.jaegertracing.qe.tests;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import com.fasterxml.jackson.databind.JsonNode;

import io.jaegertracing.qe.TestBase;
import io.jaegertracing.qe.rest.model.QESpan;
import io.opentracing.Span;

import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.testng.annotations.Test;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 1.0.0
 */
@Slf4j
public class BasicSpanTest extends TestBase {

    /*
     * Test: Create single span, send it to server and do validate it via query
     * api. Steps: 1. Create a span 2. send it to server 3. validate from server
     */
    @Test
    public void singleSpanTest() {
        Span span = tracer().buildSpan("simple-span").start();
        span.setTag("testType", "singleSpanTest");
        //long randomLong = nextLong(100000L) + Integer.MAX_VALUE;
        int random = RANDOM.nextInt(100000);
        span.setTag("random", random);
        span.finish();
        waitForFlush();

        // Validation
        List<JsonNode> traces = simpleRestClient.getTracesSinceTestStart(testStartTime);
        assertEquals(1, traces.size(), "Expected 1 trace");

        List<QESpan> spans = getSpansFromTrace(traces.get(0));
        assertEquals(1, spans.size(), "Expected 1 span");
        QESpan receivedSpan = spans.get(0);
        assertEquals(receivedSpan.getOperation(), "simple-span");
        Map<String, Object> tags = receivedSpan.getTags();
        myAssertTag(tags, "testType", "singleSpanTest");
        myAssertTag(tags, "random", random);
    }

    /*
     * Test: Create parent span and child span Steps: 1. Create parent span and
     * child span 2. validate from server
     */
    @Test
    public void spanWithChildTest() {
        long randomSleep = RANDOM.nextInt(1000 * 2);
        Span parentSpan = tracer().buildSpan("parent-span").start();
        parentSpan.setTag("sentFrom", "automation code");
        Span childSpan = tracer().buildSpan("child-span")
                .asChildOf(parentSpan)
                .start();
        sleep(randomSleep);
        childSpan.finish();
        sleep(50L);
        parentSpan.finish();
        waitForFlush();

        List<JsonNode> traces = simpleRestClient.getTracesSinceTestStart(testStartTime);
        assertEquals(1, traces.size(), "Expected 1 trace");

        List<QESpan> spans = getSpansFromTrace(traces.get(0));
        assertEquals(2, spans.size(), "Expected 2 spans");

        QESpan receivedParentSpan = getSpanByOperationName(spans, "parent-span");
        assertNotNull(receivedParentSpan);
        logger.debug(simpleRestClient.prettyPrintJson(receivedParentSpan.getJson()));
        myAssertTag(receivedParentSpan.getTags(), "sentFrom", "automation code");

        QESpan receivedChildSpan = getSpanByOperationName(spans, "child-span");
        assertNotNull(receivedChildSpan);
    }
}
