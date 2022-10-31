package tools.descartes.coffee.controller.procedure.collection.deployment;

import java.util.concurrent.CompletableFuture;

import tools.descartes.coffee.controller.procedure.ProcedureQueue;
import tools.descartes.coffee.controller.procedure.SimpleReportProcedure;
import tools.descartes.coffee.controller.procedure.collection.Command;

public abstract class RestartContainer extends SimpleReportProcedure {

    /** queue elements added in the 'prepare' stage to the ProcedureQueue */
    protected CompletableFuture<?>[] queueFutureElements;
    protected ProcedureQueue procedureQueue;
    /** -1 means all */
    protected int replicas;

    public RestartContainer(ProcedureQueue procedureQueue, int replicas) {
        super(Command.RESTART);
        this.procedureQueue = procedureQueue;
        this.replicas = replicas;
    }

    @Override
    public int prepare() {
        procedureQueue.addToContainerRemoveQueue(this.replicas);
        this.queueFutureElements = procedureQueue.addToContainerStartQueue(Command.RESTART,
                this.replicas);
        return this.replicas;
    }

    @Override
    public void subscribe() {
        procedureQueue.waitForContainerStart(this.queueFutureElements);
    }

}
