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

public class CrashApp extends SimpleReportProcedure {
    private static final Logger logger = Logger.getLogger(CrashApp.class.getName());

    private final OrchestratorMap map;
    private String proxyAddress;

    /** queue elements added in the 'prepare' stage to the ProcedureQueue */
    protected CompletableFuture<?>[] queueFutureElements;

    private List<String> targetContainerAddresses;
    private final List<String> targetEndpoints = new ArrayList<>();
    private final ProcedureQueue procedureQueue;

    /**
     * crash all containers: -1
     */
    private int replicas;

    public CrashApp(ProcedureQueue procedureQueue, OrchestratorMap map, int replicas) {
        super(Command.CRASH);
        this.map = map;
        this.procedureQueue = procedureQueue;
        this.replicas = replicas;
    }

    @Override
    public int prepare() {
        this.setTargetIps();
        this.setProxyAddress();

        if (this.targetContainerAddresses != null) {
            this.createCrashEndpoints();
        }

        /*
        deleted because it is never removed from that queue again
         */
        // procedureQueue.addToContainerRemoveQueue(this.replicas);
        this.queueFutureElements = procedureQueue.addToContainerStartQueue(Command.CRASH, replicas);

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
            this.targetContainerAddresses = map.getContainerAccessor().getRandomContainerAddresses(this.replicas);
        } catch (Exception exception) {
            throw new IllegalStateException("Error occurred instantiating the container accessor", exception);
        }
    }

    @Override
    public void call() {
        logger.info("Sending crash requests.");
        logger.info("Expecting no response error: header parser received no bytes.");
        logger.info("Expecting proxy response code 500.");

        for (String endpoint : this.targetEndpoints) {
            logger.info("Sending crash request to: " + proxyAddress);
            logger.info("with target address in body: " + endpoint);
            CompletableFuture<HttpResponse<String>> response = HttpUtils.getAsync(proxyAddress, endpoint);
            response.thenAccept((HttpResponse<String> res) -> logger.info("Crash response: " + res));
        }
    }

    private void createCrashEndpoints() {
        this.targetEndpoints.clear();
        for (String containerAddress : this.targetContainerAddresses) {
            this.targetEndpoints.add("http://" + containerAddress + "/crash");
        }
    }

    @Override
    public void subscribe() {
        procedureQueue.waitForContainerStart(this.queueFutureElements);
    }
}