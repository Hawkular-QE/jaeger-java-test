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

import java.util.List;

import io.jaegertracing.qe.rest.model.Trace;

import io.jaegertracing.qe.rest.model.Criteria;
import io.jaegertracing.qe.rest.model.Span;

/**
 * @author Jeeva Kandasamy (jkandasa)
 */
public interface IJaegerQuery {

    Span getSpan(Criteria criteria);

    List<Trace> listTrace(String operationName, Long startTime, Long endTime);

    List<Trace> listTrace(String operationName, Long startTime);

    List<Trace> listTrace(Criteria criteria);

    List<Span> listSpan(List<Trace> traces);

    List<Span> listSpan(Criteria criteria);

    List<Span> listSpan(String operationName, Long startTime, Long endTime);

    List<Span> listSpan(String operationName, Long startTime);

    Criteria getCriteria(String operationName, Long startTime, Long endTime);

    int traceCount(Criteria criteria);

    int traceCount(String operationName, Long startTime, Long endTime);

    int traceCount(String operationName, Long startTime);

    int spanCount(Criteria criteria);

    int spanCount(String operationName, Long startTime, Long endTime);

    int spanCount(String operationName, Long startTime);
}
