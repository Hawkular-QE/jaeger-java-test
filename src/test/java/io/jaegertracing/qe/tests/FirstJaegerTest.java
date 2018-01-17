package io.jaegertracing.qe.tests;

import com.fasterxml.jackson.databind.JsonNode;
import io.jaegertracing.qe.TestBase;
import io.jaegertracing.qe.rest.clients.SimpleRestClient;
import io.jaegertracing.qe.rest.model.QESpan;
import io.opentracing.Span;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Created by Kevin Earls on 14/04/2017.
 *
 */
@Slf4j
public class FirstJaegerTest extends TestBase {
    AtomicInteger operationId = new AtomicInteger(0);

    @BeforeMethod
    public void beforeMethod() {
        operationId.incrementAndGet();
    }

    /**
     * A simple test that just creates one span, and verifies that it was created correctly.
     *
     */
    @Test
    public void writeASingleSpanTest() throws Exception {
        String operationName = "writeASingleSpanTest" + operationId.getAndIncrement();
        Span span = tracer().buildSpan(operationName)
                .withTag("simple", true)
                .startManual();
        span.finish();
        waitForFlush();

        List<JsonNode> traces = simpleRestClient.getTracesSinceTestStart(testStartTime);
        assertEquals(1, traces.size(), "Expected 1 trace");

        List<QESpan> spans = getSpansFromTrace(traces.get(0));
        assertEquals(spans.size(), 1, "Expected 1 span");
        QESpan receivedSpan = spans.get(0);
        assertEquals(receivedSpan.getOperation(), operationName);
        logger.debug(simpleRestClient.prettyPrintJson(receivedSpan.getJson()));

        assertTrue(receivedSpan.getTags().size() >= 3);
        myAssertTag(receivedSpan.getTags(), "simple", true);
    }


    /**
     * Simple test of creating a span with children
     *
     */
    @Test
    public void spanWithChildrenTest() {
        String operationName = "spanWithChildrenTest" + operationId.getAndIncrement();
        Span parentSpan = tracer().buildSpan(operationName)
                .withTag("simple", true)
                .startManual();

        Span childSpan1 = tracer().buildSpan(operationName + "-child1")
                .asChildOf(parentSpan)
                .withTag("child", 1)
                .startManual();
        sleep(100);

        Span childSpan2 = tracer().buildSpan(operationName + "-child2")
                .asChildOf(parentSpan)
                .withTag("child", 2)
                .startManual();
        sleep(50);

        childSpan1.finish();
        childSpan2.finish();

        parentSpan.finish();

        List<JsonNode> traces = simpleRestClient.getTracesSinceTestStart(testStartTime);
        assertEquals(traces.size(), 1, "Expected 1 trace");
        List<QESpan> spans = getSpansFromTrace(traces.get(0));
        assertEquals(spans.size(), 3);

        // TODO validate parent child structure, operationNames, etc.
    }

    /**
     * A simple test of the start and end options when fetching traces.
     *
     * For @Ignore see https://github.com/Hawkular-QE/jaeger-java-test/issues/14
     */
    @Test(enabled=false)
    public void testStartEndTest() {
        String operationName = "startEndTest" + operationId.getAndIncrement();
        long testEndTime = 0;
        int expectedTraceCount = 3;
        for (int i = 0; i < 5; i++) {
            if (i == expectedTraceCount) {
                testEndTime = System.currentTimeMillis();
                sleep(50);
            }
            Span testSpan = tracer().buildSpan(operationName)
                    .withTag("startEndTestSpan", i)
                    .startManual();
            testSpan.finish();
        }

        List<JsonNode> traces = simpleRestClient.getTracesBetween(testStartTime, testEndTime);
        assertEquals(traces.size(), expectedTraceCount, "Expected " + expectedTraceCount + " traces");
        // TODO add more assertions
    }

    /**
     * This should create 2 traces as Jaeger closes a trace when finish() is called
     * on the top-level span
     *
     */
    @Test
    public void successiveSpansTest() {
        String operationName = "successiveSpansTest" + operationId.getAndIncrement();
        Span firstSpan = tracer().buildSpan(operationName)
                .withTag("firstSpan", true)
                .startManual();
        sleep(50);
        firstSpan.finish();

        operationName = "successiveSpansTest" + operationId.getAndIncrement();
        Span secondSpan = tracer().buildSpan(operationName)
                .withTag("secondSpan", true)
                .startManual();
        sleep(75);
        secondSpan.finish();

        List<JsonNode> traces = simpleRestClient.getTracesSinceTestStart(testStartTime);
        assertEquals(traces.size(), 2, "Expected 2 traces");
        List<QESpan> spans = getSpansFromTrace(traces.get(0));
    }

    /**
     * Test span log
     */
    @Test
    public void spanDotLogIsBrokenTest() {
        String operationName = "spanDotLogIsBrokenTest";
        Span span = tracer().buildSpan(operationName).startManual();
        Map<String, String> logFields = new HashMap<>();
        logFields.put("something", "happened");
        logFields.put("event", "occured");
        span.log(logFields);
        //span.log("event");
        span.finish();

        List<JsonNode> traces = simpleRestClient.getTracesSinceTestStart(testStartTime);
        assertEquals(traces.size(), 1, "Expected 1 trace");

        //TODO: validate log; need to update QESpan to add log fields, or get them directly from Json
        List<QESpan> spans = getSpansFromTrace(traces.get(0));
        QESpan receivedSpan = spans.get(0);
        assertEquals(receivedSpan.getOperation(), operationName);
        logger.debug(simpleRestClient.prettyPrintJson(receivedSpan.getJson()));
    }

    /**
     * According to the OpenTracing spec tags can be String, Number, or Boolean
     *
     */
    @Test
    public void tagsShouldBeTypedTest() {
        String operationName = "tagsShouldBeTypedTest";
        Span span = tracer().buildSpan(operationName)
                .withTag("booleanTag", true)
                .withTag("numberTag", 42)
                .withTag("floatTag", Math.PI)
                .withTag("stringTag", "I am a tag")
                .startManual();
        span.finish();
        waitForFlush();

        List<JsonNode> traces = simpleRestClient.getTracesSinceTestStart(testStartTime);
        assertEquals(1, traces.size(), "Expected 1 trace");
        List<QESpan> spans = getSpansFromTrace(traces.get(0));
        assertEquals(spans.size(), 1, "Expected 1 span");
        Map<String, Object> tags = spans.get(0).getTags();

        // TODO do we need to validate the tag type in the Json?
        myAssertTag(tags, "booleanTag", true);
        myAssertTag(tags, "numberTag", 42);
        myAssertTag(tags, "floatTag", Math.PI);
        myAssertTag(tags, "stringTag", "I am a tag");
    }
}
