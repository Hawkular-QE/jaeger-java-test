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
package io.jaegertracing.qe.queryserver;

import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;

import io.jaegertracing.qe.rest.model.Trace;
import io.jaegertracing.qe.rest.model.Result;
import io.jaegertracing.qe.rest.ClientResponse;
import io.jaegertracing.qe.rest.model.Span;
import io.jaegertracing.qe.rest.JaegerRestClient;
import io.jaegertracing.qe.rest.model.Criteria;

/**
 * @author Jeeva Kandasamy (jkandasa)
 */
public class JaegerQueryImpl implements IJaegerQuery {
    private JaegerRestClient client = null;
    private String service = null;

    public JaegerQueryImpl(JaegerRestClient client, String service) {
        this.client = client;
        this.service = service;
    }

    public Criteria getCriteria(String operationName, Long startTime, Long endTime) {
        return Criteria.builder()
                .service(service)
                .operationName(operationName)
                .start(startTime)
                .end(endTime)
                .build();
    }

    private void updateCriteria(Criteria criteria) {
        if (criteria.getService() == null) {
            criteria.setService(service);
        }
        if (criteria.getStart() != null) {
            criteria.setStart(criteria.getStart() * 1000L);
        }
        if (criteria.getEnd() != null) {
            criteria.setEnd(criteria.getEnd() * 1000L);
        }
    }

    private void assertResponse(ClientResponse<?> response) {
        Assert.assertTrue(response.isSuccess());
    }

    @Override
    public List<Trace> listTrace(String operationName, Long startTime, Long endTime) {
        return listTrace(getCriteria(operationName, startTime, endTime));
    }

    @Override
    public List<Trace> listTrace(String operationName, Long startTime) {
        return listTrace(operationName, startTime, null);
    }

    @Override
    public List<Trace> listTrace(Criteria criteria) {
        updateCriteria(criteria);
        List<Trace> traces = new ArrayList<Trace>();
        ClientResponse<Result<Trace>> traceResponse = client.trace().list(criteria);
        assertResponse(traceResponse);
        if (traceResponse.getEntity().getData() == null || traceResponse.getEntity().getData().isEmpty()) {
            return traces;
        }
        return traceResponse.getEntity().getData();
    }

    public int traceCount(Criteria criteria) {
        return listTrace(criteria).size();
    }

    @Override
    public Span getSpan(Criteria criteria) {
        List<Span> spans = listSpan(criteria);
        if (!spans.isEmpty()) {
            return spans.get(0);
        }
        return null;
    }

    @Override
    public List<Span> listSpan(List<Trace> traces) {
        List<Span> spans = new ArrayList<Span>();
        for (Trace trace : traces) {
            spans.addAll(trace.getSpans());
        }
        return spans;
    }

    @Override
    public List<Span> listSpan(Criteria criteria) {
        return listSpan(listTrace(criteria));
    }

    @Override
    public List<Span> listSpan(String operationName, Long startTime, Long endTime) {
        return listSpan(getCriteria(operationName, startTime, endTime));
    }

    @Override
    public List<Span> listSpan(String operationName, Long startTime) {
        return listSpan(operationName, startTime, null);
    }

    @Override
    public int spanCount(Criteria criteria) {
        return listSpan(criteria).size();
    }

    @Override
    public int traceCount(String operationName, Long startTime, Long endTime) {
        return traceCount(getCriteria(operationName, startTime, endTime));
    }

    @Override
    public int traceCount(String operationName, Long startTime) {
        return traceCount(operationName, startTime);
    }

    @Override
    public int spanCount(String operationName, Long startTime, Long endTime) {
        return spanCount(getCriteria(operationName, startTime, endTime));
    }

    @Override
    public int spanCount(String operationName, Long startTime) {
        return spanCount(operationName, startTime, null);
    }
}
