package tools.descartes.coffee.application;

import tools.descartes.coffee.shared.LoadDistributionDTO;

public interface ITelemetrySender {
    void sendCurrentTimestamp(String controllerAddress, int controllerPort, String endpointType, String endpoint);

    void sendContainerTimestamp(String controllerAddress, int controllerPort, String containerEndpoint, long time);

    void sendReceivedRequests(String controllerAddress, int controllerPort, LoadDistributionDTO receivedRequests);

}
