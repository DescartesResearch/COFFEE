package tools.descartes.coffee.controller.procedure.collection.deployment;

import java.util.concurrent.CompletableFuture;

import tools.descartes.coffee.controller.procedure.ProcedureQueue;
import tools.descartes.coffee.controller.procedure.SimpleReportProcedure;
import tools.descartes.coffee.controller.procedure.collection.Command;

public abstract class StartContainer extends SimpleReportProcedure {

    /** queue elements added in the 'prepare' stage to the ProcedureQueue */
    protected CompletableFuture<?>[] queueFutureElements;
    protected ProcedureQueue procedureQueue;
    protected int replicas;

    public StartContainer(ProcedureQueue procedureQueue, int replicas) {
        super(Command.START);
        this.procedureQueue = procedureQueue;
        this.replicas = replicas;
    }

    @Override
    public void subscribe() {
        procedureQueue.waitForContainerStart(this.queueFutureElements);
    }

}
