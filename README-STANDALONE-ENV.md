
### Run tests on standalone environment
Sometimes we may need to run only tests on the Jaeger server instance running locally or with hostname or IP(**Note:** UDP port should be reachable).

#### Update Jaeger server details
We can update Jaeger server details as bash environment. We can set all variables or only a few.

Supported types:

* `JAEGER_QUERY_HOST` - default `localhost`
* `JAEGER_QUERY_PROTOCOL` - can be `http` or `https`. default `http`
* `JAEGER_AGENT_HOST` - default `localhost`
* `JAEGER_PORT_QUERY_HTTP` - default `16686`
* `JAEGER_PORT_AGENT_ZIPKIN_THRIFT` - default `5775`
* `JAEGER_PORT_AGENT_COMPACT` - default `6831`
* `JAEGER_PORT_AGENT_BINARY` - default `6832`
* `JAEGER_PORT_ZIPKIN_COLLECTOR` - default `14268`

#### To run tests
```bash
git clone https://github.com/Hawkular-QE/jaeger-java-test
cd jaeger-java-test
mvn test
```