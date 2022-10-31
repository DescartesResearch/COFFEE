package tools.descartes.coffee.controller.orchestrator.kubernetes.commands;

import tools.descartes.coffee.controller.config.ClusterProperties;
import tools.descartes.coffee.controller.orchestrator.ClusterClientWrapper;
import tools.descartes.coffee.controller.orchestrator.kubernetes.KubernetesClient;
import tools.descartes.coffee.controller.orchestrator.kubernetes.configuration.KubernetesProperties;
import tools.descartes.coffee.controller.orchestrator.kubernetes.util.KubeUtils;
import tools.descartes.coffee.controller.monitoring.controller.ContainerController;
import tools.descartes.coffee.controller.procedure.ProcedureQueue;
import tools.descartes.coffee.controller.procedure.collection.deployment.UpdateContainer;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.models.V1Deployment;
import tools.descartes.coffee.controller.procedure.BaseProcedure;
import tools.descartes.coffee.shared.AppVersion;

/**
 * to execute: kubectl set image | kubectl edit | kubectl rollout update
 * 
 * @see <a href="https://kubernetes.io/docs/concepts/workloads/controllers/deployment/#updating-a-deployment">...</a>
 * 
 */
public class KubeUpdateContainer extends UpdateContainer {

    private final KubernetesClient client;
    private final KubernetesProperties kubernetesProperties;
    private V1Deployment imageUpdateReplacement;

    public KubeUpdateContainer(ProcedureQueue procedureQueue, ClusterClientWrapper clusterClientWrapper, ClusterProperties clusterProperties, KubernetesProperties kubernetesProperties, int replicas) {
        super(procedureQueue, clusterProperties, replicas);
        this.client = (KubernetesClient) clusterClientWrapper.getClient();
        this.kubernetesProperties = kubernetesProperties;
    }

    @Override
    public int prepare() {
        try {
            // createImagePatch();
            createImageReplacement();
            this.replicas = KubeUtils.getCurrentScale(client, kubernetesProperties);
            ContainerController.CONTAINERS_TO_UPDATE = this.replicas;

            procedureQueue.addToContainerRemoveQueue(this.replicas);
            this.queueFutureElements = procedureQueue.addToContainerUpdateQueue(this.replicas);
            return this.replicas;

        } catch (ApiException e) {
            throwApiException(e);
        }

        return 0;
    }

    @Override
    public void call() {
        try {
            // KubeUtils.patchDeployment(this.imageUpdatePatch);
            KubeUtils.replaceDeployment(client, kubernetesProperties, this.imageUpdateReplacement);
        } catch (ApiException e) {
            throwApiException(e);
        }
    }

    /**
     * creates deployment replacement:
     * deployment.spec.template.spec.containers[0].image
     * 
     * @throws ApiException
     */
    private void createImageReplacement() throws ApiException {
        String newUpdateImage;
        if (CURRENT_APP_VERSION.equals(AppVersion.V1)) {
            newUpdateImage = clusterProperties.getUpdateImage();
        } else {
            newUpdateImage = clusterProperties.getAppImage();
        }

        BaseProcedure.logger.info(
                "CURRENT_APP_VERSION: " + CURRENT_APP_VERSION.toString() + "  ; new update image: " + newUpdateImage);

        this.imageUpdateReplacement = KubeUtils
                .createUpdateReplaceDeployment(client, kubernetesProperties, newUpdateImage);
    }

    /*
    /**
     * CURRENTLY NOT IN USE
     * 
     * creates patch to replace:
     * deployment.spec.template.spec.containers[0].image
     * 
     * @throws ApiException
    private void createImagePatch() throws ApiException {
        V1Patch imageUpdatePatch = KubeUtils.createUpdatePatch(clusterProperties.getUpdateImage());
    }
     */
}
