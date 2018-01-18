package io.jaegertracing.qe.rest.model;

import com.fasterxml.jackson.databind.JsonNode;

import io.opentracing.Span;
import io.opentracing.SpanContext;

import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;


/**
 * @author Jeeva Kandasamy (jkandasa)
 */
@Getter
@ToString
@AllArgsConstructor
public class QESpan implements Span {
    private Map<String, Object> tags = new HashMap<String, Object>();
    private Long start;
    private Long end;
    private Long duration;
    private String operation;
    private String id;
    private QESpan parent;
    private Span spanObj;
    private JsonNode json;

    public Span setOperationName(String operation) {
        this.operation = operation;
        if (spanObj != null) {
            spanObj.setOperationName(operation);
        }
        return this;
    }


    public Span setTag(String name, String value) {
        this.tags.put(name, value);
        if (spanObj != null) {
            spanObj.setTag(name, value);
        }
        return this;
    }

    public Span setTag(String name, boolean value) {
        this.tags.put(name, value);
        if (spanObj != null) {
            spanObj.setTag(name, value);
        }
        return this;
    }

    public Span setTag(String name, Number value) {
        this.tags.put(name, value);
        if (spanObj != null) {
            spanObj.setTag(name, value);
        }
        return this;
    }


    @Override
    public Span log(Map<String, ?> fields) {
        spanObj.log(fields);
        return this;
    }


    @Override
    public Span log(long timestampMicroseconds, Map<String, ?> fields) {
        spanObj.log(timestampMicroseconds, fields);
        return this;
    }


    @Override
    public Span log(String event) {
        spanObj.log(event);
        return this;
    }


    @Override
    public Span log(long timestampMicroseconds, String event) {
        spanObj.log(timestampMicroseconds, event);
        return this;
    }


    @Override
    public Span setBaggageItem(String key, String value) {
        spanObj.setBaggageItem(key, value);
        return this;
    }


    @Override
    public String getBaggageItem(String key) {
        return spanObj.getBaggageItem(key);
    }

    public void finish(long end) {
        this.end = end;
        if (spanObj != null) {
            spanObj.finish(end);
        }
    }

    public void finish() {
        finish(System.currentTimeMillis() * 1000L);
    }


    @Override
    public SpanContext context() {
        return spanObj.context();
    }


    public Long getEnd() {
        if (end == null && duration != null) {
            return start + duration;
        }
        return end;
    }

    public Long getDuration() {
        if (start != null && end != null) {
            return end - start;
        }
        return duration;
    }

    public JsonNode getJson() {
        return json;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !QESpan.class.isAssignableFrom(obj.getClass())) {
            return false;
        }
        final QESpan other = (QESpan) obj;
        if (!getOperation().equals(other.getOperation())) {
            return false;
        }
        if (getStart().compareTo(other.getStart()) != 0) {
            return false;
        }
        if (getDuration().compareTo(other.getDuration()) != 0) {
            return false;
        }
        if (!getTags().keySet().equals(other.getTags().keySet())) {
            return false;
        }
        for (String name : getTags().keySet()) {
            if (getTags().get(name) instanceof Number) {
                if (!getTags().get(name).toString().equals(other.getTags().get(name).toString())) {
                    return false;
                }
            } else if (tags.get(name) instanceof Boolean) {
                if (getTags().get(name) != other.getTags().get(name)) {
                    return false;
                }
            } else {
                if (!getTags().get(name).equals(other.getTags().get(name))) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + operation.hashCode();
        return result;
    }
}
