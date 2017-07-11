# Jaeger Java Test
[Jaeger](https://github.com/uber/jaeger) is a distributed tracing system by [Uber Technologies](http://uber.github.io/).
We can run [Jaeger](https://github.com/uber/jaeger) on [Docker](https://www.docker.io), [OpenShift](https://openshift.io/) or in a standalone environment.
When we go with OpenShift environment, we have to access Jaeger from the inside of the environment to run our tests.

In this project test runs with [TestNG](http://testng.org/doc/) testing framework. To run tests on Openshift environment, we have to setup a Jenkins.

## How to run test?
We can run these tests on Openshift or on the standalone environment.

### Run tests on Openshift environment
Download and install [openshift](https://github.com/openshift/origin) and follow set of commands for jenkins job

#### Setup environment for Jenkins
```bash
oc login -u developer
oc new-project jaeger
oc login -u system:admin
oc create -f https://raw.githubusercontent.com/jaegertracing/jaeger-openshift/master/production/daemonset-admin.yml
oc adm policy add-role-to-user daemonset-admin developer -n jaeger # jaeger namespace has been already created and it is accessible by developer user
```

#### Setup Jenkins on OpenShift environment
* [jenkins on Openshift](https://github.com/openshift/origin/tree/master/examples/jenkins)
* Recommended `Memory Limit` for this Jenkins is `1024Mi` or above

##### Install from GUI
* Click on `Add to project` and follow the screens as mentioned here.

![Continuous Integration & Deployment](/doc/images/jenkins-install-1.png "Select Continuous Integration & Deployment")

![jenkins-ephemeral](/doc/images/jenkins-install-2.png "jenkins-ephemeral")

![jenkins-configuration](/doc/images/jenkins-install-3.png "jenkins-configuration")

![jenkins-deployed](/doc/images/jenkins-install-4.png "jenkins-deployed")


##### Install from CLI
```bash
oc login -u developer
oc project jaeger
oc new-app jenkins-ephemeral
```

##### Setup tools `maven` and `jdk`
* Launch Jenkins server: https://jenkins-jaeger.<ip>.xip.io/ Login as `developer` user
* Navigate to https://jenkins-jaeger.<ip>.xip.io/configureTools/
* Add `maven` and `jdk` tool. Which is used in [Jenkins pipeline file](/JenkinsfileOpenShift)

![jdk-tool](/doc/images/jenkins-tools-jdk.png "jdk tool")

![maven-tool](/doc/images/jenkins-tools-maven.png "maven tool")

**Important:** Jenkins server needs permission to access `daemonset` for the project `jaeger`
```bash
oc adm policy add-role-to-user daemonset-admin system:serviceaccount:jaeger:jenkins -n jaeger
```

##### Create Jenkins job with existing pipeline scripts
Jenkins pipeline scripts are located [here](/JenkinsfileOpenShift)

* Login to Jenkins server
* Click on `New Job`
* Enter `Enter an item name` (example: jaeger-openshift-all-in-one-test)
* Select project type as `Pipeline` and click `OK`
* Go to the section `Pipeline`
* Select `Definition` as `Pipeline script from SCM`
* Selct `SCM` as `Git`
* `Repository URL` as https://github.com/Hawkular-QE/jaeger-java-test
* `Script Path` as either `JenkinsfileOpenShift/JenkinsfileAllInOneTest` or `JenkinsfileOpenShift/JenkinsfileProductionTest`
*  Save the project

All set ready :) Now you can execute this job

### Run tests on standalone environment
Sometimes we may need to run only tests on Jaeger server instance locally or with assessable IP (including UDP port). There are few simple steps for this.

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

Test PR
