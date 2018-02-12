# Jaeger Java Test
[Jaeger](https://github.com/uber/jaeger) is a distributed tracing system by [Uber Technologies](http://uber.github.io/).
We can run [Jaeger](https://github.com/uber/jaeger) on [Docker](https://www.docker.io), [OpenShift](https://openshift.io/) or in a standalone environment.

This repository contains set of functional tests(End-to-End). Validation covers from [jaeger-client-java](https://github.com/uber/jaeger-client-java) to [Jaeger-query](https://github.com/uber/jaeger) server. To know more about test details kindly have a look on [test directory](/src/test/java/io/jaegertracing/qe/tests/). All the test cases commented with flow of execution.

In this project, test runs with [TestNG](http://testng.org/doc/) testing framework.

We can run these tests on Jaeger server instance running locally or with hostname or IP address (**Note:** Jaeger server agent UDP ports and query port should be reachable).


## How to run the test?
#### Environment variables
These tests use the environment variables specified below.  The defaults are for running in an OpenShift environment.  
If you want to run the tests locally you will need to change some of these.

+ `JAEGER_AGENT_HOST` - default "jaeger-agent"
+ `JAEGER_AGENT_PORT` - default 6831
+ `JAEGER_COLLECTOR_HOST` - default "jaeger-collector"
+ `JAEGER_COLLECTOR_PORT` - default 14268
+ `JAEGER_QUERY_HOST` - default "jaeger-query"
+ `JAEGER_QUERY_PORT` - default 80
+ `JAEGER_FLUSH_INTERVAL` - default 1000

#### To run test
```bash
git clone https://github.com/Hawkular-QE/jaeger-java-test
cd jaeger-java-test
mvn test
```