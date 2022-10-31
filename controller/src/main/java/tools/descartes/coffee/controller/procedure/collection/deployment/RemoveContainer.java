package tools.descartes.coffee.controller.procedure.collection.deployment;

import java.util.concurrent.CompletableFuture;

import tools.descartes.coffee.controller.procedure.ProcedureQueue;
import tools.descartes.coffee.controller.procedure.SimpleReportProcedure;
import tools.descartes.coffee.controller.procedure.collection.Command;

public abstract class RemoveContainer extends SimpleReportProcedure {

    /** queue elements added in the 'prepare' stage to the ProcedureQueue */
    protected CompletableFuture<?>[] queueFutureElements;
    protected ProcedureQueue procedureQueue;
    /**
     * remove all containers: -1
     */
    protected int replicas;

    public RemoveContainer(ProcedureQueue procedureQueue, int replicas) {
        super(Command.REMOVE);
        this.procedureQueue = procedureQueue;
        this.replicas = replicas;
    }

    @Override
    public void subscribe() {
        procedureQueue.waitForContainerRemove(this.queueFutureElements);
    }

}
