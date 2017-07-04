package io.jaegertracing.qe.rest;

import static com.google.common.base.Preconditions.checkNotNull;

import java.net.URI;
import java.util.Map;
import java.util.Optional;

import com.google.common.collect.ImmutableMap;

public final class ClientInfo {

    private final URI endpointUri;
    private final Optional<String> username;
    private final Optional<String> password;
    private final Map<String, Object> headers;

    public ClientInfo(URI endpointUri, Optional<String> username, Optional<String> password, Map<String, Object>
            headers) {
        this.endpointUri = checkNotNull(endpointUri);
        this.username = checkNotNull(username);
        this.password = checkNotNull(password);
        this.headers = ImmutableMap.copyOf(checkNotNull(headers));
    }

    public URI getEndpointUri() {
        return endpointUri;
    }

    public Optional<String> getUsername() {
        return username;
    }

    public Optional<String> getPassword() {
        return password;
    }

    public Map<String, Object> getHeaders() {
        return headers;
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClientInfo that = (ClientInfo) o;

        if (endpointUri != null ? !endpointUri.equals(that.endpointUri) : that.endpointUri != null) return false;
        if (username != null ? !username.equals(that.username) : that.username != null) return false;
        if (password != null ? !password.equals(that.password) : that.password != null) return false;
        return headers != null ? headers.equals(that.headers) : that.headers == null;

    }

    @Override public int hashCode() {
        int result = endpointUri != null ? endpointUri.hashCode() : 0;
        result = 31 * result + (username != null ? username.hashCode() : 0);
        result = 31 * result + (password != null ? password.hashCode() : 0);
        result = 31 * result + (headers != null ? headers.hashCode() : 0);
        return result;
    }

    @Override public String toString() {
        return "ClientInfo{" +
                "endpointUri=" + endpointUri +
                ", username=" + username +
                ", password=" + password +
                ", headers=" + headers +
                '}';
    }
}
