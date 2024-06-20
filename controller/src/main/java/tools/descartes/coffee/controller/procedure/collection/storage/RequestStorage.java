package tools.descartes.coffee.controller.procedure.collection.storage;

import java.net.http.HttpResponse;
import java.sql.Timestamp;
import java.util.List;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import tools.descartes.coffee.controller.utils.SpringPropertyHelper;

import tools.descartes.coffee.controller.orchestrator.OrchestratorMap;
import tools.descartes.coffee.controller.procedure.SimpleReportProcedure;
import tools.descartes.coffee.controller.procedure.collection.Command;
import tools.descartes.coffee.shared.HttpUtils;
import tools.descartes.coffee.shared.StorageData;

public class RequestStorage extends SimpleReportProcedure {
    private static final Logger logger = Logger.getLogger(RequestStorage.class.getName());
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final String COMMAND_FINISHED_ENDPOINT = "http://localhost:"
            + SpringPropertyHelper.getProperty("server.port")
            + "/command/end-storage";

    private final String STORE_STORAGE_DATA_ENDPOINT = "http://localhost:"
            + SpringPropertyHelper.getProperty("server.port")
            + "/storage/store";

    private String proxyAddress;
    private List<String> addresses;

    private final OrchestratorMap map;

    public RequestStorage(OrchestratorMap map) {
        super(Command.STORAGE);
        this.map = map;
    }

    @Override
    public int prepare() {
        this.setProxyAddress();
        this.setTargetContainerIPs();
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
        try {
            this.addresses = map.getContainerAccessor().getRandomContainerAddresses(1);
        } catch (Exception exception) {
            throw new IllegalStateException("Error occurred instantiating the container accessor", exception);
        }
    }

    @Override
    public void call() {
        for (String address : this.addresses) {

            logger.info("Sending network request to: " + proxyAddress);
            logger.info("with target address in body: " +
                    address);

            logger.info("Network Request to : " + address);

            HttpResponse<String> response = HttpUtils.get(proxyAddress, "http://" + address + "/storage", 0);

            // TODO: (Later) add error handling (e.g. timeout)
            logger.info("Network response: " + response);
            if (response != null) {
                logger.info("Network response: " + response.body());
                this.storeStorageTimeData(response.body());
            }
        }

        this.storeStorageCommandData();
    }

    private void storeStorageTimeData(String responseData) {
        StorageData storageData;

        try {
            storageData = objectMapper.readValue(responseData, StorageData.class);
        } catch (Exception e) {
            throw new IllegalStateException("Error occurred reading network response from container.", e);
        }

        HttpUtils.post(STORE_STORAGE_DATA_ENDPOINT, storageData);
    }

    private void storeStorageCommandData() {
        long currentTime = System.currentTimeMillis();
        Timestamp currentTimestamp = new Timestamp(currentTime);

        HttpUtils.post(COMMAND_FINISHED_ENDPOINT, currentTimestamp);
    }

    @Override
    public void subscribe() {
        // TBD no cluster event subscription
    }

    @Override
    public boolean needsPersistentStorage() {
        return true;
    }

}
