# Troubleshooting COFFEE

As COFFEE is a complex framework with many parameters and works with different clusters and infrastructures, there are many sources of failures. In this document, we collect some common errors that occured in the past.

## Troubleshooting Kubernetes communication

A good option to start with when experiencing `APIException` while working with COFFEE and a Kubernetes cluster is to turn on the debugging option of the Kubernetes Java client, therefore navigate to [KubernetesClient.java](https://github.com/DescartesResearch/COFFEE/blob/main/controller/src/main/java/tools/descartes/coffee/controller/orchestrator/kubernetes/KubernetesClient.java#L55), set `client.setDebugging(true)` and then recompile COFFEE.

## Troubleshooting hanging scripts

When using sequences or loops in COFFEE scripts, COFFEE controller waits for the commands to complete. For this, e.g. for container starts/restarts/updates, the COFFEE test applications send messages to the controller. Consequently, we have to ensure that the controller is reachable for the test application instances. Make sure to set the controller IP/DNS and port in the [application properties](https://github.com/DescartesResearch/COFFEE/blob/main/application/src/main/resources/application.properties) correctly, then build and push the test application to a registry.
