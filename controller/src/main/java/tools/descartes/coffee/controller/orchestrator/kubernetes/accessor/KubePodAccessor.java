package tools.descartes.coffee.controller.orchestrator.kubernetes.accessor;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import tools.descartes.coffee.controller.config.ClusterProperties;
import tools.descartes.coffee.controller.orchestrator.BaseClusterClient;
import tools.descartes.coffee.controller.orchestrator.ClusterClientWrapper;
import tools.descartes.coffee.controller.orchestrator.kubernetes.configuration.KubernetesProperties;
import tools.descartes.coffee.controller.orchestrator.kubernetes.util.KubeUtils;
import org.springframework.data.util.Pair;

import tools.descartes.coffee.controller.orchestrator.kubernetes.KubernetesClient;
import tools.descartes.coffee.controller.orchestrator.ContainerAccessor;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodList;

public class KubePodAccessor implements ContainerAccessor {
        private static final Logger logger = Logger.getLogger(KubePodAccessor.class.getName());
        private final KubernetesClient client;
        private final KubernetesProperties kubernetesProperties;
        private final ClusterProperties clusterProperties;

        public KubePodAccessor(ClusterClientWrapper clientWrapper, KubernetesProperties kubernetesProperties, ClusterProperties clusterProperties) {
                BaseClusterClient baseClient = clientWrapper.getClient();
                if (baseClient instanceof KubernetesClient) {
                        client = (KubernetesClient) baseClient;
                } else {
                        throw new IllegalArgumentException("No KubernetesClient available");
                }
                this.kubernetesProperties = kubernetesProperties;
                this.clusterProperties = clusterProperties;
        }

        public String getRandomPodIp() throws ApiException {

                V1PodList currentPods = client.getAPI().listNamespacedPod(kubernetesProperties.getNaming().getNamespace(), null,
                                null,
                                null, null, null, null, null, null, null, null);

                List<V1Pod> runningPods = currentPods.getItems().stream()
                                .filter(pod -> pod.getStatus().getPhase().equals("Running"))
                                .collect(Collectors.toList());

                if (runningPods.isEmpty()) {
                        throw new IllegalStateException(
                                        "Error occurred while getting a pod ip: No pods currently deployed.");
                }

                return runningPods.iterator().next().getStatus().getPodIP();
        }

        public List<String> getRandomContainerAddresses(int numberOfAddresses) throws ApiException {
                for (int i = 1; i < 3; i++) {
                        try {
                                V1PodList currentPods = client.getAPI().listNamespacedPod(kubernetesProperties.getNaming().getNamespace(), null,
                                        null,
                                        null, null, null, null, null, null, null, null);

                                List<V1Pod> runningPods = currentPods.getItems().stream()
                                        .filter(pod -> pod.getStatus().getPhase().equals("Running"))
                                        .collect(Collectors.toList());

                                if (runningPods.size() < numberOfAddresses) {
                                        throw new IllegalStateException("Error occurred while getting " + numberOfAddresses
                                                + " container addresses: Less containers currently deployed: "
                                                + runningPods.size());
                                }

                                return runningPods.subList(0, numberOfAddresses).stream()
                                        .map(pod -> pod.getStatus().getPodIP() + ":" + clusterProperties.getAppContainerPort())
                                        .collect(Collectors.toList());
                        } catch (ApiException apie) {
                                if (i == 1) {
                                        logger.info("ApiException, trying reconnect");
                                        client.reconnect();
                                } else {
                                        throw apie;
                                }
                        }
                }
                throw new IllegalStateException();
        }

        @Override
        public List<Pair<String, String>> getNetworkingContainerAddresses() throws ApiException {
                for (int i = 1; i < 3; i++) {
                        try {
                                V1PodList pods = client.getAPI().listNamespacedPod(kubernetesProperties.getNaming().getNamespace(), null, null,
                                        null,
                                        null, null, null, null, null, null, null);

                                logger.info("proxyNodeName: " + (clusterProperties.getProxyNodeName() != null ? clusterProperties.getProxyNodeName() : "NOT PREDEFINED"));
                                for (V1Pod pod : pods.getItems()) {
                                        logger.info("Pod " + pod.getMetadata().getName() +
                                                " on node " + pod.getSpec().getNodeName() +
                                                " with ip " + pod.getStatus().getPodIP());
                                }

                                // get pod ips for distinct nodes
                                if (clusterProperties.getProxyNodeName() != null) {
                                        return pods.getItems().stream()
                                                .filter(pod -> !pod.getSpec().getNodeName().equals(clusterProperties.getProxyNodeName()))
                                                .filter(distinctByNodeName(pod -> pod.getSpec().getNodeName()))
                                                .map(pod -> Pair.of(
                                                        pod.getSpec().getNodeName(),
                                                        pod.getStatus().getPodIP() + ":"
                                                                + clusterProperties.getAppContainerPort()))
                                                .collect(Collectors.toList());
                                } else {
                                        return pods.getItems().stream()
                                                .filter(distinctByNodeName(pod -> pod.getSpec().getNodeName()))
                                                .map(pod -> Pair.of(
                                                        pod.getSpec().getNodeName(),
                                                        pod.getStatus().getPodIP() + ":"
                                                                + clusterProperties.getAppContainerPort()))
                                                .collect(Collectors.toList());
                                }
                        } catch (ApiException apie) {
                                if (i < 2) {
                                        logger.info("ApiException, reconnect");
                                        client.reconnect();
                                } else {
                                        throw apie;
                                }
                        }
                }
                throw new IllegalStateException();
        }

        private static <T> Predicate<T> distinctByNodeName(
                        Function<? super T, ?> nodeNameExtractor) {

                Map<Object, Boolean> seen = new ConcurrentHashMap<>();
                return pair -> seen.putIfAbsent(nodeNameExtractor.apply(pair), Boolean.TRUE) == null;
        }

        @Override
        public int getCurrentScale() throws Exception {
                return KubeUtils.getCurrentScale(client, kubernetesProperties);
        }

        @Override
        public String getProxyAddress() {
                if (kubernetesProperties.getIpForLoadAndProxy() == null) {
                        return clusterProperties.getIp() + ":" + kubernetesProperties.getProxyNodePort();
                } else {
                        return kubernetesProperties.getIpForLoadAndProxy() + ":" + kubernetesProperties.getProxyNodePort();
                }
        }

        @Override
        public String getLoadBalancerAddress() {
                if (kubernetesProperties.getIpForLoadAndProxy() == null) {
                        return clusterProperties.getIp() + ":" + kubernetesProperties.getApplicationNodePort();
                } else {
                        return kubernetesProperties.getIpForLoadAndProxy() + ":" + kubernetesProperties.getApplicationNodePort();
                }
        }

}