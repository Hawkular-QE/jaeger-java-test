/*
 * Copyright 2015-2017 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.jaegertracing.qe.tests;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import io.jaegertracing.qe.rest.model.Trace;
import io.opentracing.Span;

/**
 * Created by Kevin Earls on 04 April 2017.
 */
public class TagAndDurationTests extends TestBase {
    AtomicLong operationId = new AtomicLong(Instant.now().getEpochSecond());

    @BeforeMethod
    public void beforeMethod() {
        operationId.incrementAndGet();
    }

    /**
     * Write a single span with one tag, and verify that the correct tag is returned
     */
    @Test
    public void simpleTagTest() throws Exception {
        String operationName = "simpleTagTest-" + operationId.getAndIncrement();
        Span span = tracer().buildSpan(operationName)
                .withTag("simple", true)
                .startManual();
        span.finish();

        waitForFlush();

        List<Trace> traces = jaegerQuery().listTrace(operationName, testStartTime);

        assertEquals(traces.size(), 1, "Expected 1 trace");

        List<io.jaegertracing.qe.rest.model.Span> spans = jaegerQuery().listSpan(traces);
        assertEquals(spans.size(), 1);

        assertTag(spans.get(0).getTags(), "simple", true);
    }

    /**
     * Write a single span with a sleep before the finish, and make sure the
     * duration is correct.
     *
     * @throws InterruptedException
     */
    @Test
    public void simpleDurationTest() throws Exception {
        String operationName = "simpleDurationTest-" + operationId.getAndIncrement();
        Span span = tracer().buildSpan(operationName)
                .withTag("simple", true)
                .startManual();
        long expectedMinimumDuration = 100;
        sleep(expectedMinimumDuration);
        span.finish();
        waitForFlush();

        List<Trace> traces = jaegerQuery().listTrace(operationName, testStartTime);

        assertEquals(traces.size(), 1, "Expected 1 trace");

        List<io.jaegertracing.qe.rest.model.Span> spans = jaegerQuery().listSpan(traces);

        assertEquals(spans.size(), 1, "Expected 1 span");

        long expectedDuration = expectedMinimumDuration * 1000L;

        assertTrue(spans.get(0).getDuration() >= expectedDuration, "Expected duration: " + expectedDuration);
    }
}
