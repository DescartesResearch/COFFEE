package tools.descartes.coffee.controller.orchestrator.nomad.commands;

import tools.descartes.coffee.controller.config.ClusterProperties;
import tools.descartes.coffee.controller.orchestrator.ClusterClientWrapper;
import tools.descartes.coffee.controller.orchestrator.nomad.NomadClient;
import tools.descartes.coffee.controller.orchestrator.nomad.NomadComponents;
import tools.descartes.coffee.controller.orchestrator.nomad.configuration.NomadProperties;
import tools.descartes.coffee.controller.orchestrator.nomad.util.NomadUtils;

import java.io.IOException;

import com.hashicorp.nomad.apimodel.Job;
import com.hashicorp.nomad.javasdk.NomadException;

import tools.descartes.coffee.controller.monitoring.controller.ContainerController;
import tools.descartes.coffee.controller.procedure.ProcedureQueue;
import tools.descartes.coffee.controller.procedure.collection.deployment.UpdateContainer;

/**
 * Update via Jobs:register
 */
public class NomadUpdateContainer extends UpdateContainer {

    private final NomadProperties nomadProperties;
    private final NomadClient nomadClient;
    private final NomadComponents nomadComponents;
    private Job updateJob;

    public NomadUpdateContainer(ProcedureQueue procedureQueue, ClusterClientWrapper clusterClientWrapper, NomadComponents nomadComponents, ClusterProperties clusterProperties, NomadProperties nomadProperties, int replicas) {
        super(procedureQueue, clusterProperties, replicas);
        this.nomadProperties = nomadProperties;
        this.nomadClient = (NomadClient) clusterClientWrapper.getClient();
        this.nomadComponents = nomadComponents;
    }

    @Override
    public int prepare() {
        nomadClient.clean();

        try {
            this.replicas = NomadUtils.getCurrentScale(nomadClient, nomadProperties);
            ContainerController.CONTAINERS_TO_UPDATE = this.replicas;
            this.updateJob = NomadUtils.prepareUpdateJob(nomadComponents, nomadProperties, this.replicas);

            procedureQueue.addToContainerRemoveQueue(this.replicas);
            this.queueFutureElements = procedureQueue.addToContainerUpdateQueue(this.replicas);
            return this.replicas;

        } catch (NomadException e1) {
            throwApiException(e1);
        } catch (IOException e2) {
            throwIoException(e2);
        }

        return 0;
    }

    @Override
    public void call() {
        try {
            NomadUtils.updateDeployment(nomadClient, this.updateJob);
        } catch (NomadException e1) {
            throwApiException(e1);
        } catch (IOException e2) {
            throwIoException(e2);
        }
    }

}
