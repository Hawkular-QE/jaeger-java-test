package io.jaegertracing.qe.api.model;

import lombok.ToString;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class JaegerConfiguration {
    private String serverHost;
    private String serverQueryProtocol;
    private String agentHost;
    private Integer queryPort;
    private Integer agentZipkinThriftPort;
    private Integer agentCompactPort;
    private Integer agentBinaryPort;
    private Integer zipkinCollectorPort;
    private String collectorHost;
    private Integer flushInterval;
    private String useCollectorOrAgent;

    public Integer getAgentZipkinThriftPort() {
        return agentZipkinThriftPort != null ? agentZipkinThriftPort : 5775;
    }

    public Integer getAgentCompactPort() {
        return agentCompactPort != null ? agentCompactPort : 6831;
    }

    public Integer getAgentBinaryPort() {
        return agentBinaryPort != null ? agentBinaryPort : 6832;
    }

    public Integer getZipkinCollectorPort() {
        return zipkinCollectorPort != null ? zipkinCollectorPort : 14268;
    }

    public Integer getQueryPort() {
        return queryPort != null ? queryPort : 16686;
    }

    public String getServerQueryProtocol() {
        return serverQueryProtocol != null ? serverQueryProtocol : "http";
    }

    public String getServerHost() {
        return serverHost != null ? serverHost : "localhost";
    }

    public String getAgentHost() {
        return agentHost != null ? agentHost : "localhost";
    }

    public Integer getFlushInterval() {
        return flushInterval != null ? flushInterval : 100;
    }

    public boolean useCollector() {
        return useCollectorOrAgent.equalsIgnoreCase("collector");
    }
}