package tools.descartes.coffee.application;

import java.sql.Timestamp;
import java.util.logging.Logger;

import tools.descartes.coffee.shared.HttpUtils;
import tools.descartes.coffee.shared.LoadDistributionDTO;

// TODO: post vs postAsync
public final class DefaultTelemetrySender implements ITelemetrySender {
    private static final Logger logger = Logger.getLogger(DefaultTelemetrySender.class.getName());

    public void sendCurrentTimestamp(String controllerAddress, int controllerPort, String endpointType, String endpoint) {
        long currentTime = System.currentTimeMillis();
        Timestamp currentTimestamp = new Timestamp(currentTime);
        String target = getURL(controllerAddress, controllerPort, endpointType, endpoint);
        logger.info("Sending POST request to " + target);
        HttpUtils.post(target, currentTimestamp);
    }

    public void sendContainerTimestamp(String controllerAddress, int controllerPort, String containerEndpoint, long time) {
        Timestamp timestamp = new Timestamp(time);
        String target = getURL(controllerAddress, controllerPort, "container", containerEndpoint);
        logger.info("Sending POST request to " + target);
        HttpUtils.post(target, timestamp);
    }

    public void sendReceivedRequests(String controllerAddress, int controllerPort, LoadDistributionDTO receivedRequests) {
        String target = getURL(controllerAddress, controllerPort, "container", "loaddist");
        logger.info("Sending POST request to " + target);
        HttpUtils.post(target, receivedRequests);
    }

    private String getURL(String controllerAddress, int controllerPort, String type, String endpoint) {
        return "http://" + controllerAddress + ":" + controllerPort
                + "/" + type + "/" + endpoint + "?version=" + AppApplication.version;
    }

}
