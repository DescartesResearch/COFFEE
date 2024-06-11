# COFFEE Configuration

This document describes the configuration parameters of the COFFEE framework.

## Configuration Files and Locations

Review the following configuration files before build:

| Component    | Type     | Configuration File                                                                                                  |
|--------------|----------|---------------------------------------------------------------------------------------------------------------------|
| Application  | Required | [Link](https://github.com/DescartesResearch/COFFEE/blob/main/application/src/main/resources/application.properties) |
| Controller   | Required | [Link](https://github.com/DescartesResearch/COFFEE/blob/main/controller/src/main/resources/application.properties)  |
| Test Script  | Required | [Link](https://github.com/DescartesResearch/COFFEE/blob/main/test-sequence.script)                                  |
| Build Script | Required | [Link](https://github.com/DescartesResearch/COFFEE/blob/main/utils/buildAndPush.sh)                                 |
| Database     | Optional | [Link](https://github.com/DescartesResearch/COFFEE/blob/main/database/docker-compose.yaml)                          |

## Required Configuration

### Application

In
the [application configuration file](https://github.com/DescartesResearch/COFFEE/blob/main/application/src/main/resources/application.properties)
the IP address and port of the node that hosts the COFFEE controller has to be set.

### Controller

The controller is the coordinator component in the COFFEE framework and therefore has the most configuration parameters.
In the following, we explain the meaning of all entries in the
controller's [configuration file](https://github.com/DescartesResearch/COFFEE/blob/main/controller/src/main/resources/application.properties):

| Config Group        | Setting                  | Description                                                                                                                                                      |
|---------------------|--------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| controller          | exportResults            | `true` if experiment results should be exported to CSV files, `false` otherwise                                                                                  |
|                     | reportDirectory          | Directory used to store experiment result CSV files (see above)                                                                                                  |
|                     | testScript               | Path to the test script (test campaign) to be executed                                                                                                           |
|                     | initialReplicas          | Number of replicas of the test application to be deployed at the start of the experiment                                                                         |
|                     | proxyReplicas            | Number of replicas of the proxy to be deployed at the start of the experiment                                                                                    |
|                     | networkingTimeoutSeconds | HTTP timeout for requests to the cluster and for networking tests in seconds                                                                                     |
| controller.database | address                  | IP address of the controller database                                                                                                                            |
|                     | port                     | Port of the controller database                                                                                                                                  |
| loadgenerator*      | jarfile                  | Path to the load generator's JAR file                                                                                                                            |
|                     | userprofile              | Path to the request definition file for the load generator                                                                                                       |
|                     | intensityFile            | Path to the request intensity file for the load generator (if `NULL`, parameters `requestsPerSec` and `durationSeconds` have to be set                           |
|                     | requestsPerSec           | Constant load intensity to use within the experiment (ignored if `intensityFile` is used)                                                                        |
|                     | durationSeconds          | Maximum operation time of the load generator (ignored if `intensityFile` is used)                                                                                |
|                     | loggingFile              | Path to the logging file of the load generator                                                                                                                   |
|                     | requestLoggingFile       | Path to the request logging file of the load generator                                                                                                           |
| cluster             | orchestrator             | Name of the container orchestration framework (currently supported: `kubernetes`, `nomad`)                                                                       |
|                     | ip                       | IP of the cluster management plane                                                                                                                               |
|                     | port                     | Port of the cluster management plane                                                                                                                             |
|                     | controllerIp             | IP of the COFFEE controller to use from inside the cluster                                                                                                       |
|                     | proxyNodeName            | Name of the node where the COFFEE proxy should be deployed (set `NULL` if unknown or no fixed node)                                                              |
|                     | appContainerPort         | Port where the test application is exposed                                                                                                                       |
|                     | appHealthCheck           | `true` if HTTP health checks should be used for the test application, `false` otherwise                                                                          |
|                     | appImage                 | String to use for pulling the test application container image from the container image registry (set automatically at build time)                               |
|                     | proxyImage               | String to use for pulling the proxy container image from the container image registry (set automatically at build time)                                          |
|                     | updateImage              | String to use for pulling the test application container image used for rolling update tests from the container image registry (set automatically at build time) |

&ast; Supported load generator is the [HTTP Load Generator](https://github.com/joakimkistowski/HTTP-Load-Generator).
Settings only relevant if load is generated in specified test campaign.

Besides this generic setting, further settings are necessary to work with specific orchestrators:

#### Kubernetes

To access the cluster API, the Java Kubernetes Client is used. Refs:

- Kubernetes Clients   : <https://kubernetes.io/docs/reference/using-api/client-libraries/>
- Official Java Client : <https://github.com/kubernetes-client/java/>

The easiest way to connect COFFEE to your Kubernetes cluster is to use
a [kubeconfig file](https://kubernetes.io/docs/concepts/configuration/organize-cluster-access-kubeconfig/).
Make sure to install all dependencies that might be necessary for authentication in your cluster (e.g. for
GKE: `gke-gcloud-auth-plugin`).

To support the cluster certification the TLS version has to be **1.2**. To achieve this you can
add `-Djdk.tls.client.protocols=TLSv1.2` to the JVM args in the run configuration. JDK 11 onwards have support for TLS
1.3 which can cause the error: `extension (5) should not be presented in certificate_request`. In addition, the local
and remote firewalls must allow requests/calls to the Kubernetes API and from the cluster to pass through.

Kubernetes settings to be adjusted in the controller's configuration file:

| Config Group      | Setting                  | Description                                                                                                                                                                                                 |
|-------------------|--------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| kubernetes        | kubeConfigFile           | Path to the kubeconfig file (if not set, `clientCertificateData`, `clientKeyData`, `userName`, `clusterName` and `certificateAuthorityData` have to be set, and advanced auth mechanisms are not supported) |
|                   | clientCertificateData    | Base64 encoded string (see [Kubernetes docs](https://kubernetes.io/docs/tasks/access-application-cluster/configure-access-multiple-clusters/), not needed when `kubeConfigFile` is used)                    |
|                   | clientKeyData            | Base64 encoded string (see [Kubernetes docs](https://kubernetes.io/docs/tasks/access-application-cluster/configure-access-multiple-clusters/), not needed when `kubeConfigFile` is used)                    |
|                   | certificateAuthorityData | Base64 encoded string (see [Kubernetes docs](https://kubernetes.io/docs/tasks/access-application-cluster/configure-access-multiple-clusters/), not needed when `kubeConfigFile` is used)                    |
|                   | userName                 | Kubernetes user account to use for accessing management plane (not needed when `kubeConfigFile` is used)                                                                                                    |
|                   | clusterName              | Kubernetes cluster name (not needed when `kubeConfigFile` is used)                                                                                                                                          |
|                   | ipForLoadAndProxy        | IP address to use for sending load requests and requests to the COFFEE proxy (depends on network layout and used load balancer)                                                                             |
|                   | applicationNodePort      | Port to access test application                                                                                                                                                                             |
|                   | proxyNodePort            | Port to access COFFEE proxy                                                                                                                                                                                 |
| kubernetes.naming | prefix                   | String to use as prefix for all following naming properties                                                                                                                                                 |
|                   | proxyDeployment          | Name of the proxy deployment object                                                                                                                                                                         |
|                   | proxyService             | Name of the proxy service object                                                                                                                                                                            |
|                   | proxyLabel               | Name of the proxy label object                                                                                                                                                                              |
|                   | namespace                | Name of the namespace used for experiments with COFFEE                                                                                                                                                      |
|                   | label                    | Name of the test apps label object                                                                                                                                                                          |
|                   | deployment               | Name of the test apps deployment object                                                                                                                                                                     |
|                   | service                  | Name of the test apps service object                                                                                                                                                                        |
|                   | container                | Name of the test apps container object                                                                                                                                                                      |
|                   | port                     | Name of the test apps port object                                                                                                                                                                           |
| kubernetes.update | strategy                 | Update strategy for the test application deployment (see [Kubernetes docs](https://kubernetes.io/docs/concepts/workloads/controllers/deployment/))                                                          |
|                   | maxSurge                 | Sets relative amount of overprovisioning during update (see [Kubernetes docs](https://kubernetes.io/docs/concepts/workloads/controllers/deployment/))                                                       |
|                   | maxUnavailable           | Sets minimum amount of available replicas during update (see [Kubernetes docs](https://kubernetes.io/docs/concepts/workloads/controllers/deployment/))                                                      |

#### Nomad

To access the cluster API, the Java Nomad Client is used. Refs:

- Nomad Clients        : <https://www.nomadproject.io/api-docs/libraries-and-sdks>
- Official Java Client : <https://github.com/hashicorp/nomad-java-sdk>

Nomad settings to be adjusted in the controller's configuration file:

| Config Group    | Setting                    | Description                                                                                                                                                            |
|-----------------|----------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| nomad           | datacenter                 | Name of the data center                                                                                                                                                |
|                 | driver                     | Driver to use for the COFFEE components (only `docker` tested so far)                                                                                                  |
| nomad.haproxy*  | jobId                      | Job ID for the HAProxy load balancer                                                                                                                                   |
|                 | name                       | Name of the HAProxy job                                                                                                                                                |
|                 | port                       | Port to use for the HAProxy job                                                                                                                                        |
|                 | uiPort                     | Port to use for the HAProxy UI                                                                                                                                         |
|                 | checkIntervalSeconds       | Interval in seconds for the health check for HAProxy                                                                                                                   |
|                 | checkTimeoutSeconds        | Timeout for the HAProxy health check                                                                                                                                   |
|                 | version                    | Version of HAProxy to use                                                                                                                                              |
|                 | cpu                        | CPU limit for HAProxy in millicores                                                                                                                                    |
|                 | memory                     | Memory limit for HAProxy in MB                                                                                                                                         |
| nomad.naming    | prefix                     | String to use as prefix for all following naming properties                                                                                                            |
|                 | namespace                  | Name of the namespace used for experiments with COFFEE                                                                                                                 |
|                 | job                        | Name of the test apps job object                                                                                                                                       |
|                 | taskGroup                  | Name of the test apps task group object                                                                                                                                |
|                 | task                       | Name of the test apps task object                                                                                                                                      |
|                 | portLabel                  | Name of the test apps port label object                                                                                                                                |
|                 | service                    | Name of the test apps service object                                                                                                                                   |
|                 | proxy                      | Prefix to use for all proxy related objects                                                                                                                            |
| nomad.update    | healthStatus               | Mechanism to use for determining health status (see [Nomad docs](https://developer.hashicorp.com/nomad/docs/job-specification/update))                                 |
|                 | staggerMilliSeconds        | Nomad update setting (see [Nomad docs](https://developer.hashicorp.com/nomad/docs/job-specification/update))                                                           |
|                 | minHealthyTimeMilliSeconds | Time in ms for a updated instance to be healthy to continue updating (see [Nomad docs](https://developer.hashicorp.com/nomad/docs/job-specification/update))           |
|                 | maxParallel                | Determines how many instances should be updated in parallel (see [Nomad docs](https://developer.hashicorp.com/nomad/docs/job-specification/update))                    |
| nomad.restart   | delaySeconds               | Nomad restart setting (see [Nomad docs](https://developer.hashicorp.com/nomad/docs/job-specification/restart))                                                         |
|                 | intervalSeconds            | Nomad restart setting (see [Nomad docs](https://developer.hashicorp.com/nomad/docs/job-specification/restart))                                                         |
|                 | limit                      | Nomad restart setting (see [Nomad docs](https://developer.hashicorp.com/nomad/docs/job-specification/restart))                                                         |
|                 | graceSeconds               | Nomad restart setting (see [Nomad docs](https://developer.hashicorp.com/nomad/docs/job-specification/restart))                                                         |
|                 | ignoreWarnings             | Nomad restart setting (see [Nomad docs](https://developer.hashicorp.com/nomad/docs/job-specification/restart))                                                         |
| nomad.health    | intervalSeconds            | Interval in seconds for the health checks                                                                                                                              |
|                 | timeoutSeconds             | Timeout for the health checks                                                                                                                                          |
| nomad.storage** | volumeType                 | Type of the volume to use for the test container (see [Nomad docs](https://developer.hashicorp.com/nomad/tutorials/stateful-workloads/stateful-workloads-host-volumes) |
|                 | volumeSource               | Source of the provisioned volume (see [Nomad docs](https://developer.hashicorp.com/nomad/tutorials/stateful-workloads/stateful-workloads-host-volumes)                 |                                                                                                      |

&ast; Nomad requires setup of
a [load balancer](https://developer.hashicorp.com/nomad/tutorials/load-balancing/load-balancing). We
use [HAProxy](https://developer.hashicorp.com/nomad/tutorials/load-balancing/load-balancing-haproxy). Settings are only
relevant if HAProxy is used for load balancing.
&ast;&ast; Only needed if the STORAGE command is used in the test script

### Test Script

The test script/campaign has to be reviewed before starting COFFEE. In
the [example](https://github.com/DescartesResearch/COFFEE/blob/main/test-sequence.script) provided, we simply start one
container, wait few seconds and then remove this container again. The test campaigns used in our paper can be found in
the [examples folder](https://github.com/DescartesResearch/COFFEE/tree/main/examples) of this repository.

### Build Script

Both Kubernetes and Nomad clusters pull container images from a registry. In the build process of COFFEE, Docker images
of all components are created and pushed to a repository. In
the [build script](https://github.com/DescartesResearch/COFFEE/blob/main/utils/buildAndPush.sh) you have to set your
repository where the COFFEE images should be stored. Make sure that you have push access on the build machine and pull
access on all machines inside your test cluster.

## Optional Configuration

### Database

COFFEE stores the benchmark results in a MySQL database. We start this database along with a PHPMyAdmin container using
a [docker compose file](https://github.com/DescartesResearch/COFFEE/blob/main/database/docker-compose.yaml). In this
file, you might change authentication data for the database.