package tools.descartes.coffee.controller.orchestrator.nomad.commands;

import java.io.IOException;

import com.hashicorp.nomad.javasdk.NomadException;

import tools.descartes.coffee.controller.orchestrator.ClusterClientWrapper;
import tools.descartes.coffee.controller.orchestrator.nomad.NomadClient;
import tools.descartes.coffee.controller.orchestrator.nomad.NomadComponents;
import tools.descartes.coffee.controller.orchestrator.nomad.configuration.NomadProperties;
import tools.descartes.coffee.controller.orchestrator.nomad.util.NomadUtils;
import tools.descartes.coffee.controller.procedure.ProcedureQueue;
import tools.descartes.coffee.controller.procedure.collection.deployment.RemoveContainer;

public class NomadRemoveContainer extends RemoveContainer {

    private final NomadClient client;
    private final NomadComponents nomadComponents;
    private final NomadProperties nomadProperties;
    private int replicasToScaleDown;

    public NomadRemoveContainer(ProcedureQueue procedureQueue, ClusterClientWrapper clusterClientWrapper, NomadComponents nomadComponents, NomadProperties nomadProperties, int replicas) {
        super(procedureQueue, replicas);
        this.client = (NomadClient) clusterClientWrapper.getClient();
        this.nomadComponents = nomadComponents;
        this.nomadProperties = nomadProperties;
    }

    @Override
    public int prepare() {
        client.clean();

        try {
            int currentScale = NomadUtils.getCurrentScale(client, nomadProperties);

            if (currentScale < this.replicas) {
                // TBD: warning vs exception

                throw new IllegalStateException("Trying to delete more containers than currently available(to delete: "
                        + this.replicas + ", available: " + currentScale + ").");
            }

            this.replicasToScaleDown = this.replicas == -1 ? 0 : currentScale - this.replicas;
            this.replicas = this.replicas == -1 ? currentScale : this.replicas;

            this.queueFutureElements = procedureQueue.addToContainerRemoveQueue(this.replicas);
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
            NomadUtils.scale(client, nomadComponents, nomadProperties, this.replicasToScaleDown);
        } catch (NomadException e1) {
            throwApiException(e1);
        } catch (IOException e2) {
            throwIoException(e2);
        }
    }
}
