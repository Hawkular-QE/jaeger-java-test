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

import com.fasterxml.jackson.databind.JsonNode;

import io.jaegertracing.qe.TestBase;
import io.jaegertracing.qe.rest.model.QESpan;
import io.opentracing.Span;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

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
                .start();
        span.finish();

        List<JsonNode> traces = simpleRestClient.getTracesSinceTestStart(testStartTime);
        assertEquals(1, traces.size(), "Expected 1 trace");
        List<QESpan> spans = getSpansFromTrace(traces.get(0));
        assertEquals(spans.size(), 1, "Expected 1 span");

        myAssertTag(spans.get(0).getTags(), "simple", true);
    }

    /**
     * Write a single span with a sleep before the finish, and make sure the
     * duration is correct.
     *
     */
    @Test
    public void simpleDurationTest()  {
        String operationName = "simpleDurationTest-" + operationId.getAndIncrement();
        Span span = tracer().buildSpan(operationName)
                .withTag("simple", true)
                .start();
        long expectedMinimumDuration = 100;
        sleep(expectedMinimumDuration);
        span.finish();

        List<JsonNode> traces = simpleRestClient.getTracesSinceTestStart(testStartTime);
        assertEquals(1, traces.size(), "Expected 1 trace");
        List<QESpan> spans = getSpansFromTrace(traces.get(0));
        assertEquals(spans.size(), 1, "Expected 1 span");

        long expectedDuration = TimeUnit.MILLISECONDS.toMicros(expectedMinimumDuration);  // Remember duration is in microseconds
        assertTrue(spans.get(0).getDuration() >= expectedDuration, "Expected duration: " + expectedDuration);
    }
}
