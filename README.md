### WIP Note: This repository and README is still WIP. Further documentation of the source code and all config parameters will be added soon.

# COFFEE: Benchmarking of Container Orchestration Frameworks

COFFEE (short for Container Orchestration Frameworks' Full Experimental Evaluation) is a benchmarking framework for
container orchestration frameworks (e.g. Kubernetes, Nomad). It is able to automate repeatable experiments and covers
many aspects for modern container orchestration frameworks.

Configured Java JDK version: **11**

## General

The project contains four parts - the controller, the test application, a proxy application and a shared module which is used in the other three components.

To build all parts of the application use the *build* script in the base folder.

At the moment, COFFEE supports tests of [Kubernetes](https://kubernetes.io/) and [Nomad](https://www.nomadproject.io/). 

## Configuration

The general configuration files are available in the `controller > resources` folder.

### Kubernetes

To access the cluster api the Java Kubernetes Client is used. Refs:

- Kubernetes Clients   : <https://kubernetes.io/docs/reference/using-api/client-libraries/>
- Official Java Client : <https://github.com/kubernetes-client/java/>

The easiest way to connect COFFEE to your Kubernetes cluster is to use a [kubeconfig file](https://kubernetes.io/docs/concepts/configuration/organize-cluster-access-kubeconfig/). Set your `kubeconfig` file in the `application.properties` file of the controller.
Make sure to install all dependencies that might be necessary for authentication in your cluster (e.g. for GKE: gke-gcloud-auth-plugin). 

To support the cluster certification the TLS version has to be **1.2**.
JDK 11 onwards have support for TLS 1.3 which can cause the error: "extension (5) should not be presented in certificate_request"
To achieve this you can add `-Djdk.tls.client.protocols=TLSv1.2` to the JVM args in the run configuration.

In addition, the local and remote firewalls must allow requests/calls to (API) and from the cluster to pass through.

### Nomad

To access the cluster api the Java Nomad Client is used. Refs:

- Nomad Clients        : <https://www.nomadproject.io/api-docs/libraries-and-sdks>
- Official Java Client : <https://github.com/hashicorp/nomad-java-sdk>

### External Tools

For generating load requests, COFFEE provides an interface for the [HTTP Load Generator](https://github.com/joakimkistowski/HTTP-Load-Generator). Other load generators might be included in the future.

### Docker Images

The test application and proxy might be pushed to a container repository and loaded in the cluster from there. In the `utils` folder, we provide example scripts to build and push the images. Set your repo data there. 

### MySQL Database

To store the results a MySQL database is used. Current version: **5.7.39**. We provide an example `docker compose` file for deployment in the `database` folder. The IP address and port of the database has to be set in the controller properties file.

## Cite us

A paper for this project is currently under review. Citation data will be added as soon as known.

## Any questions?

For questions contact [Martin Straesser](https://se.informatik.uni-wuerzburg.de/software-engineering-group/staff/martin-straesser/).