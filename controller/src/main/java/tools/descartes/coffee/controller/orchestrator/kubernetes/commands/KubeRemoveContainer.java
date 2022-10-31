package tools.descartes.coffee.controller.orchestrator.kubernetes.commands;

import tools.descartes.coffee.controller.orchestrator.ClusterClientWrapper;
import tools.descartes.coffee.controller.orchestrator.kubernetes.KubernetesClient;
import tools.descartes.coffee.controller.orchestrator.kubernetes.configuration.KubernetesProperties;
import tools.descartes.coffee.controller.orchestrator.kubernetes.util.KubeUtils;
import tools.descartes.coffee.controller.procedure.ProcedureQueue;
import tools.descartes.coffee.controller.procedure.collection.deployment.RemoveContainer;
import io.kubernetes.client.custom.V1Patch;
import io.kubernetes.client.openapi.ApiException;

public class KubeRemoveContainer extends RemoveContainer {

    private final KubernetesClient client;
    private final KubernetesProperties kubernetesProperties;
    private V1Patch scaleDownPatch;

    public KubeRemoveContainer(ProcedureQueue procedureQueue, ClusterClientWrapper clusterClientWrapper, KubernetesProperties kubernetesProperties, int replicas) {
        super(procedureQueue, replicas);
        this.client = (KubernetesClient) clusterClientWrapper.getClient();
        this.kubernetesProperties = kubernetesProperties;
    }

    @Override
    public int prepare() {
        try {
            int currentScale = KubeUtils.getCurrentScale(client, kubernetesProperties);

            if (currentScale < this.replicas) {
                // TBD: warning vs exception

                throw new IllegalStateException("Trying to delete more pods than currently available(to delete: "
                        + this.replicas + ", available: " + currentScale + ").");
            }

            int replicasToScaleDown = this.replicas == -1 ? 0 : currentScale - this.replicas;
            this.scaleDownPatch = KubeUtils.createScalePatch(replicasToScaleDown);

            this.replicas = this.replicas == -1 ? currentScale : this.replicas;
            this.queueFutureElements = procedureQueue.addToContainerRemoveQueue(this.replicas);
            return this.replicas;

        } catch (ApiException e) {
            throwApiException(e);
            return 0;
        }
    }

    @Override
    public void call() {
        try {
            KubeUtils.scale(client, kubernetesProperties, scaleDownPatch);
        } catch (ApiException e) {
            throwApiException(e);
        }
    }
}
