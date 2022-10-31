package tools.descartes.coffee.controller.procedure.collection.deployment;

import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

import tools.descartes.coffee.controller.orchestrator.OrchestratorMap;
import tools.descartes.coffee.controller.procedure.ProcedureQueue;
import tools.descartes.coffee.controller.procedure.SimpleReportProcedure;
import tools.descartes.coffee.controller.procedure.collection.Command;
import tools.descartes.coffee.shared.HttpUtils;

public class HealthCheck extends SimpleReportProcedure {
    private static final Logger logger = Logger.getLogger(HealthCheck.class.getName());

    private final OrchestratorMap map;
    /** queue elements added in the 'prepare' stage to the ProcedureQueue */
    protected CompletableFuture<?>[] queueFutureElements;

    private List<String> targetAddresses;
    private final List<String> targetEndpoints = new ArrayList<>();
    private final ProcedureQueue procedureQueue;
    /**
     * set all containers unhealthy: -1
     */
    private int replicas;
    private String proxyAddress;

    public HealthCheck(ProcedureQueue procedureQueue, OrchestratorMap map, int replicas) {
        super(Command.HEALTH);
        this.map = map;
        this.procedureQueue = procedureQueue;
        this.replicas = replicas;
    }

    @Override
    public int prepare() {
        this.setProxyAddress();
        this.setTargetIps();

        if (this.targetAddresses != null) {
            this.createUnhealthyEndpoints();
        }

        procedureQueue.addToContainerRemoveQueue(this.replicas);
        this.queueFutureElements = procedureQueue.addToContainerStartQueue(Command.HEALTH, replicas);

        return this.replicas;
    }

    private void setProxyAddress() {
        try {
            proxyAddress = "http://" + map.getContainerAccessor().getProxyAddress()
                    + "/proxy/request";
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Error occurred getting the proxy application cluster address: " + e.getMessage());
        }
    }

    private void setTargetIps() {
        try {
            if (this.replicas == -1) {
                this.replicas = map.getContainerAccessor().getCurrentScale();
            }
            this.targetAddresses = map.getContainerAccessor().getRandomContainerAddresses(this.replicas);
        } catch (Exception exception) {
            throw new IllegalStateException("Error occurred instantiating the container accessor", exception);
        }

    }

    @Override
    public void call() {
        for (String endpoint : this.targetEndpoints) {
            logger.info("Sending unhealthy request to: " + proxyAddress);
            logger.info("with target address in body: " + endpoint);

            CompletableFuture<HttpResponse<String>> response = HttpUtils.getAsync(proxyAddress, endpoint);
            response.thenAccept((HttpResponse<String> res) -> logger.info("Health response: " + res));
        }
    }

    private void createUnhealthyEndpoints() {
        this.targetEndpoints.clear();
        for (String targetAddress : this.targetAddresses) {
            this.targetEndpoints.add("http://" + targetAddress + "/unhealthy");
        }
    }

    @Override
    public void subscribe() {
        procedureQueue.waitForContainerStart(this.queueFutureElements);
    }
}
