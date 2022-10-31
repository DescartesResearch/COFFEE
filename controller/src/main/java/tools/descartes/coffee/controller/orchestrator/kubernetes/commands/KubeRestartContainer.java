package tools.descartes.coffee.controller.orchestrator.kubernetes.commands;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import tools.descartes.coffee.controller.orchestrator.ClusterClientWrapper;
import tools.descartes.coffee.controller.orchestrator.kubernetes.KubernetesClient;
import tools.descartes.coffee.controller.orchestrator.kubernetes.configuration.KubernetesProperties;
import tools.descartes.coffee.controller.orchestrator.kubernetes.util.KubeUtils;
import tools.descartes.coffee.controller.procedure.ProcedureQueue;
import tools.descartes.coffee.controller.procedure.collection.Command;
import tools.descartes.coffee.controller.procedure.collection.deployment.RestartContainer;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.models.V1PodList;

/**
 * Current Method:
 * Restart Kubernetes pods by manually deleting pods and let them be recreated
 * 
 * TBD: restart method
 * 
 * - pod: (neuer Pod wird erstellt)
 * -- single pod: delete
 * -- deployment: rollout restart
 * 
 * alternative methods:
 * 
 * scale deployment (2x),
 * set env to restart,
 * replace pod(s)
 */
public class KubeRestartContainer extends RestartContainer {

    private final KubernetesClient client;
    private final KubernetesProperties kubernetesProperties;
    private List<String> currentPodNames;

    public KubeRestartContainer(ProcedureQueue procedureQueue, ClusterClientWrapper clusterClientWrapper, KubernetesProperties kubernetesProperties, int replicas) {
        super(procedureQueue, replicas);
        this.client = (KubernetesClient) clusterClientWrapper.getClient();
        this.kubernetesProperties = kubernetesProperties;
    }

    @Override
    public int prepare() {
        for (int i = 1; i < 3; i++) {
            try {
                int currentScale = KubeUtils.getCurrentScale(client, kubernetesProperties);

                if (currentScale < this.replicas) {
                    throw new IllegalStateException("Trying to restart more pods than currently available(to restart: "
                            + this.replicas + ", available: " + currentScale + ").");
                }

                V1PodList currentPods = client.getAPI().listNamespacedPod(kubernetesProperties.getNaming().getNamespace(), null, null,
                        null, null, null, null, null, null, null, null);

                this.currentPodNames = currentPods.getItems().stream().map(pod -> pod.getMetadata().getName())
                        .collect(Collectors.toList());

                this.replicas = this.replicas == -1 ? currentScale : this.replicas;
                procedureQueue.addToContainerRemoveQueue(this.replicas);
                this.queueFutureElements = procedureQueue.addToContainerStartQueue(Command.RESTART, replicas);

                return this.replicas;
            } catch (ApiException e) {
                if (i < 2) {
                    logger.info("ApiException, trying reconnect");
                    client.reconnect();
                } else {
                    throwApiException(e);
                }
            }
        }

        return 0;
    }

    @Override
    public void call() {
        for (int j = 1; j < 3; j++) {
            try {
                String currentPodName;
                Iterator<String> podNameIterator = this.currentPodNames.iterator();

                for (int i = 0; i < this.replicas; i++) {
                    currentPodName = podNameIterator.next();

                    client.getAPI().deleteNamespacedPod(currentPodName,
                            kubernetesProperties.getNaming().getNamespace(), null, null, null, null, null, null);
                }

            } catch (ApiException e) {
                if (j < 2) {
                    logger.info("Apiexception, reconnect");
                    client.reconnect();
                } else {
                    throwApiException(e);
                }
            }
        }
    }
}
