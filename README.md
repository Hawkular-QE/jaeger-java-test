# Jaeger Java Test
[Jaeger](https://github.com/uber/jaeger) is a distributed tracing system by [Uber Technologies](http://uber.github.io/).
We can run [Jaeger](https://github.com/uber/jaeger) on [Docker](https://www.docker.io), [OpenShift](https://openshift.io/) or in a standalone environment.

This repository contains set of functional tests(End-to-End). Validation covers from [jaeger-client-java](https://github.com/uber/jaeger-client-java) to [Jaeger-query](https://github.com/uber/jaeger) server. To know more about test details kindly have a look on [test directory](/src/test/java/io/jaegertracing/qe/tests/). All the test cases commented with flow of execution.

In this project, test runs with [TestNG](http://testng.org/doc/) testing framework.

We can run these tests on Jaeger server instance running locally or with hostname or IP address (**Note:** Jaeger server agent UDP ports and query port should be reachable).


## How to run the test?
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

#### To run test
```bash
git clone https://github.com/Hawkular-QE/jaeger-java-test
cd jaeger-java-test
mvn test
```