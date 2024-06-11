# COFFEE Overview

This document gives an overview about the COFFEE framework, its components and their roles. The general idea of COFFEE is
to measure several performance metrics of container clusters, such as deployment times, update times or networking metrics.
It therefore deploys several instances of a test application in the cluster. These instances register at a central component,
the COFFEE controller, which acts as a supervisor for the experiments. The user designs an experiment using a test script
(we also like to say test campaign). At the moment, COFFEE supports tests of [Kubernetes](https://kubernetes.io/) and [Nomad](https://www.nomadproject.io/).
The following picture shows all components of COFFEE and in the following sections,
we go a bit more into details about each of them.

<object data="https://github.com/DescartesResearch/COFFEE/blob/main/docs/overview.pdf" type="application/pdf" width="700px" height="700px">
    <embed src="https://github.com/DescartesResearch/COFFEE/blob/main/docs/overview.pdf">
        <p>This browser does not support PDFs. Please download the PDF to view it: <a href="https://github.com/DescartesResearch/COFFEE/blob/main/docs/overview.pdf">Download PDF</a>.</p>
    </embed>
</object>

## Test script

The test script is the specification of the experiment/benchmark to execute. The user utilizes a simple scripting language to specify the experiment.
In the root directory of this repository, we provide an exemplary test script like this:

```
seq
start 1
delay 10
remove 1
endseq
```

This script starts with a `seq` statement that indicates that every statement until `endseq` should be interpreted as a sequence meaning that every command is executed one after each other.
Without using `seq` every command would be executed in independent threads meaning that all commands would run in parallel. In our case, we prefer sequential execution.
This sequence consists of three steps. First, we start one instance of the test application using the `start` command. After the start succeeded, we wait for 10 seconds (using the `delay` command).
Finally, we remove our deployed instance again using the `remove` command. The COFFEE framework will execute this test script and collects associated metrics to each command, such as the container
readiness time for the start command. More advanced scripts can be found in our [examples folder](https://github.com/DescartesResearch/COFFEE/tree/main/examples). The following table gives an overview of supported commands:

| Command            | Description                                                        |
|--------------------|--------------------------------------------------------------------|
| START `n`          | Starts `n` test app instances                                      |
| RESTART `n`        | Restarts `n` test app instances                                    |
| HEALTH `n`         | Sets unhealthy flag in `n` test app instances                      |
| CRASH `n`          | Causes crash of `n` test app instances                             |
| UPDATE `n`         | Updates `n` test app instances (changes image)                     |
| NETWORK            | Measures round-trip time between running instances                 |
| STORAGE            | Measures IO metrics for file read and write at one random instance |
| REMOVE `n`         | Shuts down `n` test app instances                                  |
| LOAD / ENDLOAD     | Starts/ends load generation                                        |
| SEQ / ENDSEQ       | Starts/ends a sequence                                             |
| LOOP `n` / ENDLOOP | Starts/ends a loop with `n` iterations                             |
| OFFSET `t`         | Invokes next command after `t` seconds                             |
| DELAY `t`          | Pauses a sequence/loop for `t` seconds                             |

## COFFEE controller

The COFFEE controller is the heart of the COFFEE framework. It parses the test campaign specified by the user and sends
the commands to the cluster under test. It might therefore contact the cluster control plane directly or will use the
COFFEE proxy. The COFFEE proxy is always used for communication with the COFFEE test app instances (e.g. for the `HEALTH` command).
COFFEE test app instances report metrics to the COFFEE controller which stores them into a connected database and might output average
values after the experiment on demand. For experiments with user workload, the COFFEE controller offers an interface to 
instantiate a load generator. As best practice, the COFFEE controller should be deployed outside the test cluster to 
avoid interferences.

## COFFEE proxy

The COFFEE proxy is a slim utility that acts as a bridge between the COFFEE controller and the COFFEE test app instances.
The COFFEE controller can usually not directly interact with specific COFFEE test app instances as a network barrier is 
in place to protect the container cluster. The COFFEE proxy meanwhile runs in the test cluster and can therefore directly
address COFFEE test app instances.

## COFFEE test app

The COFFEE test app instances are so to say the agents in the test cluster. When a test app instance starts, it reports
its start time to the COFFEE controller. Afterwards it waits for commands to be executed. Depending on the test campaign,
a COFFEE test app instance might answer requests from the load generator, contact other COFFEE test app instances,
simulate a crash, or pretends to be in an unhealthy state. For each command, the COFFEE test app instances report metrics
to the COFFEE controller. When a test app instance is requested to shut down, it reports the shut down time, its total uptime, and 
the number of processes requests from the load generator.

## External tooling

### Load generator

For generating load requests, COFFEE provides an interface for the [HTTP Load Generator](https://github.com/joakimkistowski/HTTP-Load-Generator). Other load generators might be included in the future.

### Docker images and registry

The test application and proxy might be pushed to a container image repository (e.g., Docker Hub) and loaded in the cluster from there. In the `utils` folder, we provide example scripts to build and push the images. Set your repo data there.

### MySQL database

To store the results of an experiment, the COFFEE controller uses a MySQL database is used. Current version: **5.7.39**. We provide an example `docker compose` file for deployment in the `database` folder. The IP address and port of the database has to be set in the controller's properties file.
