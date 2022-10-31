package tools.descartes.coffee.controller.procedure.collection.deployment;

import java.util.concurrent.CompletableFuture;

import tools.descartes.coffee.controller.config.ClusterProperties;
import tools.descartes.coffee.controller.procedure.ProcedureQueue;
import tools.descartes.coffee.controller.procedure.SimpleReportProcedure;
import tools.descartes.coffee.controller.procedure.collection.Command;
import tools.descartes.coffee.shared.AppVersion;

public abstract class UpdateContainer extends SimpleReportProcedure {

    /** global counter to store with the corresponding updates */
    protected static int CURRENT_UPDATE_COUNT = 1;

    public static int getCurrentUpdateCount() {
        return CURRENT_UPDATE_COUNT;
    }

    public static void setCurrentUpdateCount(int currentUpdateCount) {
        CURRENT_UPDATE_COUNT = currentUpdateCount;
    }

    /**
     * global state to switch between images
     * 
     * - V1: regular version
     * - V2: update version
     */
    protected static AppVersion CURRENT_APP_VERSION = AppVersion.V1;

    public static AppVersion getCurrentAppVersion() {
        return CURRENT_APP_VERSION;
    }

    public static void setCurrentAppVersion(AppVersion currentAppVersion) {
        CURRENT_APP_VERSION = currentAppVersion;
    }

    /** queue elements added in the 'prepare' stage to the ProcedureQueue */
    protected CompletableFuture<?>[] queueFutureElements;

    /** replicas affected by the update, has to be set inside the prepare method */
    protected int replicas;

    protected ClusterProperties clusterProperties;
    protected ProcedureQueue procedureQueue;

    public UpdateContainer(ProcedureQueue procedureQueue, ClusterProperties clusterProperties, int replicas) {
        super(Command.UPDATE);
        this.procedureQueue = procedureQueue;
        this.clusterProperties = clusterProperties;
        this.replicas = replicas;
    }

    @Override
    public void subscribe() {
        procedureQueue.waitForContainerUpdate(this.queueFutureElements);
    }

}
