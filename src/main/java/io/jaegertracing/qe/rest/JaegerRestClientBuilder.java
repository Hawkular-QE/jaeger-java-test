package io.jaegertracing.qe.rest;

import io.jaegertracing.qe.rest.ClientInfo;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.google.common.base.Throwables;

/**
 * @author Jeeva Kandasamy (jkandasa)
 */
public class JaegerRestClientBuilder {

    private URI uri;
    private Optional<String> username = Optional.empty();
    private Optional<String> password = Optional.empty();
    private Map<String, Object> headers = new HashMap<>();

    public JaegerRestClientBuilder() {
        try {
            uri = new URI("http://127.0.0.1:16686");
        } catch (URISyntaxException ex) {
            Throwables.propagate(ex);
        }
    }

    public JaegerRestClientBuilder uri(String uri) throws URISyntaxException {
        return uri(new URI(uri));
    }

    public JaegerRestClientBuilder uri(URI uri) {
        this.uri = uri;
        return this;
    }

    public JaegerRestClientBuilder basicAuthentication(String username, String password) {
        this.username = Optional.ofNullable(username);
        this.password = Optional.ofNullable(password);
        return this;
    }

    public JaegerRestClientBuilder basicAuthentication(Optional<String> username, Optional<String> password) {
        this.username = username;
        this.password = password;
        return this;
    }

    public JaegerRestClientBuilder tokenAuthentication(String token) {
        headers.put(JaegerRestClient.KEY_HEADER_AUTHORIZATION, "Bearer " + token);
        return this;
    }

    public JaegerRestClientBuilder addHeader(String key, Object value) {
        headers.put(key, value);
        return this;
    }

    public JaegerRestClient build() {
        ClientInfo clientInfo = new ClientInfo(uri, username, password, headers);
        return new JaegerRestClient(clientInfo);
    }

}
