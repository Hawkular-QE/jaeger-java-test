package io.jaegertracing.qe.tests;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import io.jaegertracing.qe.TestBase;

import io.jaegertracing.qe.rest.model.Tag;
import io.jaegertracing.qe.rest.model.Trace;
import io.opentracing.Span;

/**
 * Created by Kevin Earls on 14/04/2017.
 *
 */
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
    public void writeASingleSpanTest() {
        String operationName = "writeASingleSpanTest" + operationId.getAndIncrement();
        Span span = tracer().buildSpan(operationName)
                .withTag("simple", true)
                .startManual();
        span.finish();
        waitForFlush();

        List<Trace> traces = jaegerQuery().listTrace(operationName, testStartTime);
        assertEquals(traces.size(), 1, "Expected 1 trace");

        List<io.jaegertracing.qe.rest.model.Span> spans = jaegerQuery().listSpan(traces);
        assertEquals(spans.size(), 1, "Expected 1 span");
        assertEquals(spans.get(0).getOperationName(), operationName);

        assertTag(spans.get(0).getTags(), "simple", true);
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

        waitForFlush();

        List<Trace> traces = jaegerQuery().listTrace(operationName, testStartTime);
        assertEquals(traces.size(), 1, "Expected 1 trace");

        List<io.jaegertracing.qe.rest.model.Span> spans = jaegerQuery().listSpan(traces);
        assertEquals(spans.size(), 3);
        // TODO validate parent child structure, operationNames, etc.
    }

    /**
     * A simple test of the start and end options when fetching traces.
     *
     */
    @Test
    public void testStartEndTest() {
        String operationName = "startEndTest" + operationId.getAndIncrement();
        long endTime = 0;
        long tracesCount = 0;
        for (int i = 0; i < 5; i++) {
            if (i == 3) {
                endTime = System.currentTimeMillis();
                sleep(50);
            }
            Span testSpan = tracer().buildSpan(operationName)
                    .withTag("startEndTestSpan", i)
                    .startManual();
            if (endTime == 0) {
                tracesCount++;
            }
            testSpan.finish();
        }

        waitForFlush();

        List<Trace> traces = jaegerQuery().listTrace(null, testStartTime, endTime);
        assertEquals(traces.size(), tracesCount, "Expected 3 traces");
        // TODO more assertions here ?
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

        waitForFlush();

        List<Trace> traces = jaegerQuery().listTrace(null, testStartTime);
        assertEquals(traces.size(), 2, "Expected 2 traces");

        // TODO more assertions here....
        //dumpAllTraces(traces);

    }

    /**
     * Test span log
     */
    @Test(alwaysRun = false)
    //for now skip this test. TODO: fix this test failure 
    public void spanDotLogIsBrokenTest() {
        String operationName = "spanDotLogIsBrokenTest";
        Span span = tracer().buildSpan(operationName).startManual();
        span.log("event");
        span.finish();

        waitForFlush();

        List<Trace> traces = jaegerQuery().listTrace(operationName, testStartTime);

        assertEquals(traces.size(), 1, "Expected 1 trace");

        //TODO: validate log
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
                .withTag("stringTag", "I am a tag")
                .startManual();
        span.finish();
        waitForFlush();

        List<Trace> traces = jaegerQuery().listTrace(operationName, testStartTime);

        assertEquals(traces.size(), 1, "Expected 1 trace");
        List<io.jaegertracing.qe.rest.model.Span> spans = jaegerQuery().listSpan(traces);
        assertEquals(1, spans.size(), "Expected only 1 span");

        List<Tag> tags = spans.get(0).getTags();

        assertTrue(getTag(tags, "booleanTag").getValue() instanceof Boolean, "booleanTag should be a boolean");
        assertTrue(getTag(tags, "numberTag").getValue() instanceof Number, "numberTag should be a boolean");
        assertTrue(getTag(tags, "stringTag").getValue() instanceof String, "stringTag should be a boolean");

        assertTag(tags, "booleanTag", true);
        assertTag(tags, "numberTag", 42);
        assertTag(tags, "stringTag", "I am a tag");
    }
}
