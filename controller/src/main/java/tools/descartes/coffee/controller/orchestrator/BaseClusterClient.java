package tools.descartes.coffee.controller.orchestrator;

import tools.descartes.coffee.controller.config.ClusterProperties;
import tools.descartes.coffee.controller.config.ControllerProperties;

public abstract class BaseClusterClient {

    protected ClusterProperties clusterProperties;
    protected ControllerProperties controllerProperties;

    /** sets the cluster configuration */
    public BaseClusterClient(ControllerProperties controllerProperties, ClusterProperties clusterConfig) {
        this.controllerProperties = controllerProperties;
        clusterProperties = clusterConfig;
    }

    /** connects to the cluster */
    public abstract void connect();

    /**
     * initializes the basic setup:
     * - creates the defined namespace
     * - creates the defined deployment
     * - sets up the replicas of the specified container image
     */
    public abstract void init();

    /** clears any deployment from the cluster */
    public abstract void clear();

    /** disconnects from the cluster */
    public abstract void disconnect();

}
