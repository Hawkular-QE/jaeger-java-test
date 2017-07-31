package io.jaegertracing.qe.tests;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.jaegertracing.qe.TestBase;
import io.jaegertracing.qe.rest.model.Trace;
import io.opentracing.Span;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 1.0.0
 */
public class BasicSpanTest extends TestBase {

    /*
     * Test: Create single span, send it to server and do validate it via query
     * api. Steps: 1. Create a span 2. send it to server 3. validate from server
     */
    @Test
    public void singleSpanTest() {
        Span span = tracer().buildSpan("simple-span").startManual();
        span.setTag("testType", "singleSpanTest");
        long randomLong = nextLong(100000L);
        span.setTag("randomLong", randomLong);
        span.finish();
        waitForFlush();

        // Validation
        List<Trace> traces = getTraceList(null, testStartTime, 1);
        Assert.assertEquals(traces.size(), 1);
        List<io.jaegertracing.qe.rest.model.Span> spans = jaegerQuery()
                .listSpan(traces);
        Assert.assertEquals(spans.size(), 1);
        Assert.assertEquals(spans.get(0).getOperationName(), "simple-span");
        assertTag(spans.get(0).getTags(), "testType", "singleSpanTest");
        assertTag(spans.get(0).getTags(), "randomLong", randomLong);
    }

    /*
     * Test: Create parent span and child span Steps: 1. Create parent span and
     * child span 2. validate from server
     */
    @Test
    public void spanWithChildTest() {
        long randomSleep = nextLong(1000 * 2L);
        Span parentSpan = tracer().buildSpan("parent-span").startManual();
        parentSpan.setTag("sentFrom", "automation code");
        Span childSpan = tracer().buildSpan("child-span").asChildOf(parentSpan)
                .startManual();
        sleep(randomSleep);
        childSpan.finish();
        sleep(50L);
        parentSpan.finish();
        waitForFlush();

        // Validation
        List<Trace> traces = getTraceList(null, testStartTime, 1);
        Assert.assertEquals(traces.size(), 1);
        List<io.jaegertracing.qe.rest.model.Span> spans = jaegerQuery().listSpan(traces);
        Assert.assertEquals(spans.size(), 2);
        io.jaegertracing.qe.rest.model.Span span1 = getSpan(spans,
                "parent-span");
        Assert.assertNotNull(span1);
        assertTag(span1.getTags(), "sentFrom", "automation code");
        io.jaegertracing.qe.rest.model.Span span2 = getSpan(spans, "child-span");
        Assert.assertNotNull(span2);
    }
}
