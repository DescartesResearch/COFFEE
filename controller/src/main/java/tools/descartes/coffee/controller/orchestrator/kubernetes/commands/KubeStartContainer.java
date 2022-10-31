package tools.descartes.coffee.controller.orchestrator.kubernetes.commands;

import tools.descartes.coffee.controller.orchestrator.ClusterClientWrapper;
import tools.descartes.coffee.controller.orchestrator.kubernetes.KubernetesClient;
import tools.descartes.coffee.controller.orchestrator.kubernetes.configuration.KubernetesProperties;
import tools.descartes.coffee.controller.orchestrator.kubernetes.util.KubeUtils;
import tools.descartes.coffee.controller.procedure.ProcedureQueue;
import tools.descartes.coffee.controller.procedure.collection.Command;
import tools.descartes.coffee.controller.procedure.collection.deployment.StartContainer;

import io.kubernetes.client.custom.V1Patch;
import io.kubernetes.client.openapi.ApiException;

public class KubeStartContainer extends StartContainer {

    private final KubernetesClient client;
    private final KubernetesProperties kubernetesProperties;
    private V1Patch scaleUpPatch;

    public KubeStartContainer(ProcedureQueue procedureQueue, ClusterClientWrapper clusterClientWrapper, KubernetesProperties kubernetesProperties, int replicas) {
        super(procedureQueue, replicas);
        this.client = (KubernetesClient) clusterClientWrapper.getClient();
        this.kubernetesProperties = kubernetesProperties;
    }

    @Override
    public int prepare() {
        try {
            int currentScale = KubeUtils.getCurrentScale(client, kubernetesProperties);

            int replicasToScaleUp = currentScale + this.replicas;
            this.scaleUpPatch = KubeUtils.createScalePatch(replicasToScaleUp);

            this.queueFutureElements = procedureQueue.addToContainerStartQueue(Command.START, replicas);
            return this.replicas;
        } catch (ApiException e) {
            throwApiException(e);
        }

        return 0;
    }

    @Override
    public void call() {
        try {
            KubeUtils.scale(client, kubernetesProperties, this.scaleUpPatch);
        } catch (ApiException e) {
            throwApiException(e);
        }
    }
}
