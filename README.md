# Jaeger Java Test
[Jaeger](https://github.com/uber/jaeger) is a distributed tracing system by [Uber Technologies](http://uber.github.io/).
We can run [Jaeger](https://github.com/uber/jaeger) on [Docker](https://www.docker.io), [OpenShift](https://openshift.io/) or in a standalone environment.
When we go with OpenShift environment, we have to access Jaeger from the inside of the environment to run our tests.

In this project, test runs with [TestNG](http://testng.org/doc/) testing framework. To run tests on Openshift environment, we have to setup a Jenkins.

## How to run the test?
We can run these tests on Openshift or on the standalone environment.

**Note:** for standalone environment [have a look here](/README-STANDALONE-ENV.md)

### Prerequirements
* Setup [openshift](https://github.com/openshift/origin) with the user `developer`

#### Setup environment for Jenkins
Jenkins need set of permissions to deploy/un-deploy Jaeger on Openshift. The following commands satisfy those requirements.

Login as a `root` user on the OpenShift cluster machine and execute the following commands.

```bash
oc login -u developer
oc new-project jaeger
oc login -u system:admin
oc create -f https://raw.githubusercontent.com/jaegertracing/jaeger-openshift/master/production/daemonset-admin.yml
oc adm policy add-role-to-user daemonset-admin developer -n jaeger
```

#### Setup Jenkins instance on OpenShift
We have two flavors of Jenkins for Openshift. 1. `jenkins-persistent`, 2. `jenkins-ephemeral`. In this document, We are going with the second option. To know more about Jenkins on Openshift read [this page](https://github.com/openshift/origin/tree/master/examples/jenkins)

We tried Jenkins with default `512Mi`. Works too slow. Hence we would recommend `Memory Limit` for this Jenkins about `1024Mi` or above.

##### Installation steps
* Open OpenShift console [Example: https://localhost:8443]
* Login as `developer` user
* Select `jaeger` namespace(project).
* Click on `Add to project` and follow the screens below.

![Continuous Integration & Deployment](/doc/images/jenkins-install-1.png "Select Continuous Integration & Deployment")

![jenkins-ephemeral](/doc/images/jenkins-install-2.png "jenkins-ephemeral")

![jenkins-configuration](/doc/images/jenkins-install-3.png "jenkins-configuration")

![jenkins-deployed](/doc/images/jenkins-install-4.png "jenkins-deployed")


**Important:** Jenkins server needs permission to access `daemonset` for the project `jaeger`. Login as a `root` user on the OpenShift cluster machine and execute the following commands.
```bash
oc login -u system:admin
oc adm policy add-role-to-user daemonset-admin system:serviceaccount:jaeger:jenkins -n jaeger
```

##### Setup tools [`maven-3.5.0` and `jdk8`]
* Launch Jenkins server: https://jenkins-jaeger.<ip>.xip.io/ Login as `developer` user
* Go to `Manage Jenkins` >> `Global Tool Configuration`(https://jenkins-jaeger.<ip>.xip.io/configureTools/)
* Add `maven` and `jdk` tool. Which is used in [Jenkins pipeline file](/JenkinsfileOpenShift)
* JDK 8 should be used with the name of `jdk8`
* Maven 3.5.0 should be used in the name of `maven-3.5.0`

![jdk-tool](/doc/images/jenkins-tools-jdk8.png "jdk tool")

![maven-tool](/doc/images/jenkins-tools-maven-3_5_0.png "maven tool")


##### Create Jenkins job with existing pipeline scripts
Jenkins pipeline scripts are located [here](/JenkinsfileOpenShift)

* Login to Jenkins server [Example: https://jenkins-jaeger.<ip>.xip.io/] with the Openshift user `developer`
* Click on `New Item`
* Enter `Enter an item name` (example: jaeger-openshift-all-in-one-test)
* Select project type as `Pipeline` and click `OK`
* Go to the section `Pipeline`
* Select `Definition` as `Pipeline script from SCM`
* Selct `SCM` as `Git`
* `Repository URL` as https://github.com/Hawkular-QE/jaeger-java-test
* `Script Path` as either `JenkinsfileOpenShift/JenkinsfileAllInOneTest` or `JenkinsfileOpenShift/JenkinsfileProductionTest`
*  Save the project

All set ready :) Now you can execute this job
