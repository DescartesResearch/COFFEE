package tools.descartes.coffee.controller.procedure.collection.networking;

import java.net.http.HttpResponse;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;

import tools.descartes.coffee.controller.config.ControllerProperties;
import tools.descartes.coffee.controller.utils.SpringPropertyHelper;
import org.springframework.data.util.Pair;

import tools.descartes.coffee.controller.orchestrator.OrchestratorMap;
import tools.descartes.coffee.controller.procedure.SimpleReportProcedure;
import tools.descartes.coffee.controller.procedure.collection.Command;
import tools.descartes.coffee.shared.HttpUtils;
import tools.descartes.coffee.shared.NetworkingData;

/**
 * TBD: configure number of requests per route
 */

public class RequestNetwork extends SimpleReportProcedure {
    private static final Logger logger = Logger.getLogger(RequestNetwork.class.getName());
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * global ID to differentiate networking procedures
     */
    private static int NETWORKING_ID = 1;

    public static int getNetworkingId() {
        return NETWORKING_ID;
    }

    private final String COMMAND_FINISHED_ENDPOINT = "http://localhost:"
            + SpringPropertyHelper.getProperty("server.port")
            + "/command/end-network";

    private final String STORE_NETWORK_DATA_ENDPOINT = "http://localhost:"
            + SpringPropertyHelper.getProperty("server.port")
            + "/network/store";

    private String proxyAddress;

    private final List<String> addresses = new ArrayList<>();
    private final OrchestratorMap map;
    private final int networkingTimeoutSeconds;

    /** pair: node name + inner pod ip */
    private List<Pair<String, String>> nodeContainerAddresses = new ArrayList<>();

    public RequestNetwork(OrchestratorMap map, ControllerProperties controllerProperties) {
        super(Command.NETWORK);
        this.map = map;
        networkingTimeoutSeconds = controllerProperties.getNetworkingTimeoutSeconds();
    }

    @Override
    public int prepare() {
        this.setProxyAddress();
        this.setTargetContainerIPs();

        if (this.nodeContainerAddresses.size() < 2) {
            throw new IllegalStateException(
                    "Not enough nodes to test pod-to-pod networking over different nodes; available nodes: "
                            + this.nodeContainerAddresses.size());
        }

        List<String> containerAddresses = this.nodeContainerAddresses.stream().map(Pair::getSecond).collect(Collectors.toList());
        List<Pair<String, String>> networkSrcTargetIpPairs = this.getPermutations(containerAddresses);
        this.generateRequestRoutes(networkSrcTargetIpPairs);

        return 1;
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

    /**
     * set pod ips for distinct nodes
     *
     */
    private void setTargetContainerIPs() {
        this.nodeContainerAddresses.clear();

        try {
            this.nodeContainerAddresses = map.getContainerAccessor().getNetworkingContainerAddresses();
        } catch (Exception exception) {
            throw new IllegalStateException("Error occurred instantiating the container accessor", exception);
        }

    }

    private List<Pair<String, String>> getPermutations(List<String> podIps) {
        List<Pair<String, String>> combinations = new ArrayList<>();

        for (int i = 0; i < podIps.size(); i++) {
            for (int j = 0; j < podIps.size(); j++) {
                if (j == i) {
                    continue;
                }

                combinations.add(Pair.of(podIps.get(i), podIps.get(j)));
            }
        }

        return combinations;
    }

    private void generateRequestRoutes(List<Pair<String, String>> networkSrcTargetAddressPairs) {
        this.addresses.clear();

        for (Pair<String, String> srcTargetPair : networkSrcTargetAddressPairs) {

            String endpoint = "http://" + srcTargetPair.getFirst()
                    + "/network"
                    + "?source=" + srcTargetPair.getFirst()
                    + "&target=" + srcTargetPair.getSecond();

            this.addresses.add(endpoint);
        }
    }

    @Override
    public void call() {
        for (String address : this.addresses) {

            logger.info("Sending network request to: " + proxyAddress);
            logger.info("with target address in body: " +
                    address);

            logger.info("Network Request to : " + address);

            HttpResponse<String> response = HttpUtils.get(proxyAddress, address, networkingTimeoutSeconds);

            // TODO: (Later) add error handling (e.g. timeout)
            logger.info("Network response: " + response);
            if (response != null) {
                logger.info("Network response: " + response.body());
                this.storeNetworkTimeData(response.body());
            }
        }

        this.storeNetworkCommandData();
        NETWORKING_ID++;
    }

    private void storeNetworkTimeData(String responseData) {
        NetworkingData networkingData;

        try {
            networkingData = objectMapper.readValue(responseData, NetworkingData.class);
        } catch (Exception e) {
            throw new IllegalStateException("Error occurred reading network response from container.", e);
        }

        // mapping pod ips to corresponding node names
        String srcNodeName = this.nodeContainerAddresses.stream()
                .filter(pair -> pair.getSecond().equals(networkingData.getSource()))
                .findFirst().get().getFirst();
        String targetNodeName = this.nodeContainerAddresses.stream()
                .filter(pair -> pair.getSecond().equals(networkingData.getTarget()))
                .findFirst().get().getFirst();

        networkingData.setSource(srcNodeName);
        networkingData.setTarget(targetNodeName);

        HttpUtils.post(STORE_NETWORK_DATA_ENDPOINT, networkingData);
    }

    private void storeNetworkCommandData() {
        long currentTime = System.currentTimeMillis();
        Timestamp currentTimestamp = new Timestamp(currentTime);

        HttpUtils.post(COMMAND_FINISHED_ENDPOINT, currentTimestamp);
    }

    @Override
    public void subscribe() {
        // TBD no cluster event subscription
    }

}
