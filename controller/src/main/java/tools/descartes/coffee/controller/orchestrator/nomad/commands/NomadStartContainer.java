package tools.descartes.coffee.controller.orchestrator.nomad.commands;

import java.io.IOException;

import com.hashicorp.nomad.javasdk.NomadException;

import tools.descartes.coffee.controller.orchestrator.ClusterClientWrapper;
import tools.descartes.coffee.controller.orchestrator.nomad.NomadClient;
import tools.descartes.coffee.controller.orchestrator.nomad.NomadComponents;
import tools.descartes.coffee.controller.orchestrator.nomad.configuration.NomadProperties;
import tools.descartes.coffee.controller.orchestrator.nomad.util.NomadUtils;
import tools.descartes.coffee.controller.procedure.ProcedureQueue;
import tools.descartes.coffee.controller.procedure.collection.Command;
import tools.descartes.coffee.controller.procedure.collection.deployment.StartContainer;

public class NomadStartContainer extends StartContainer {

    private final NomadProperties nomadProperties;
    private final NomadClient client;
    private final NomadComponents nomadComponents;
    private int replicasToScaleUp;

    public NomadStartContainer(ProcedureQueue procedureQueue, NomadComponents nomadComponents, ClusterClientWrapper clusterClientWrapper, NomadProperties nomadProperties, int replicas) {
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
            this.replicasToScaleUp = currentScale + this.replicas;
            this.queueFutureElements = procedureQueue.addToContainerStartQueue(Command.START, replicas);

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
            NomadUtils.scale(client, nomadComponents, nomadProperties, this.replicasToScaleUp);
        } catch (NomadException e1) {
            throwApiException(e1);
        } catch (IOException e2) {
            throwIoException(e2);
        }
    }
}
