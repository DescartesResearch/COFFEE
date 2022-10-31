package tools.descartes.coffee.controller.orchestrator.nomad.commands;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import com.hashicorp.nomad.apimodel.AllocationListStub;
import com.hashicorp.nomad.javasdk.NomadException;

import tools.descartes.coffee.controller.orchestrator.ClusterClientWrapper;
import tools.descartes.coffee.controller.orchestrator.nomad.NomadClient;
import tools.descartes.coffee.controller.orchestrator.nomad.configuration.NomadProperties;
import tools.descartes.coffee.controller.orchestrator.nomad.util.NomadUtils;
import tools.descartes.coffee.controller.procedure.ProcedureQueue;
import tools.descartes.coffee.controller.procedure.collection.Command;
import tools.descartes.coffee.controller.procedure.collection.deployment.RestartContainer;

/**
 * TBD: restart configuration:
 * https://www.nomadproject.io/docs/job-specification/restart
 * 
 * Current Method:
 * Command: alloc stop
 * The alloc stop command allows a user to perform an in-place restart of an
 * entire allocation or individual task.
 * 
 * restart command -> not implemented (?)
 * Restart Nomad allocation by nomad alloc restart command:
 * https://www.nomadproject.io/docs/commands/alloc/restart
 */
public class NomadRestartContainer extends RestartContainer {

    private final NomadProperties nomadProperties;
    private final NomadClient client;
    private List<String> currentAllocIDs;

    public NomadRestartContainer(ProcedureQueue procedureQueue, ClusterClientWrapper clusterClientWrapper, NomadProperties nomadProperties, int replicas) {
        super(procedureQueue, replicas);
        this.client = (NomadClient) clusterClientWrapper.getClient();
        this.nomadProperties = nomadProperties;
    }

    @Override
    public int prepare() {
        client.clean();

        try {
            int currentScale = NomadUtils.getCurrentScale(client, nomadProperties);

            if (currentScale < this.replicas) {
                throw new IllegalStateException(
                        "Trying to restart more containers than currently available(to restart: "
                                + this.replicas + ", available: " + currentScale + ").");
            }

            // get all running allocations from task group
            List<AllocationListStub> allocations = NomadUtils.getCurrentAllocs(client, nomadProperties);

            this.currentAllocIDs = allocations.subList(0, this.replicas).stream().map(AllocationListStub::getId).collect(Collectors.toList());

            this.replicas = this.replicas == -1 ? currentScale : this.replicas;
            procedureQueue.addToContainerRemoveQueue(this.replicas);
            this.queueFutureElements = procedureQueue.addToContainerStartQueue(Command.RESTART, replicas);

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
            String currentAllocID;
            Iterator<String> allocIterator = this.currentAllocIDs.iterator();

            for (int i = 0; i < this.replicas; i++) {
                currentAllocID = allocIterator.next();
                client.getAllocationsAPI().stop(currentAllocID);
            }

        } catch (NomadException e1) {
            throwApiException(e1);
        } catch (IOException e2) {
            throwIoException(e2);
        }
    }
}
