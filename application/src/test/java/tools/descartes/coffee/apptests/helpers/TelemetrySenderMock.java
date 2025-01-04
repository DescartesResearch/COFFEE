package tools.descartes.coffee.apptests.helpers;

import tools.descartes.coffee.application.ITelemetrySender;
import tools.descartes.coffee.shared.LoadDistributionDTO;

public class TelemetrySenderMock implements ITelemetrySender {

    private int currentTimestampCounter;
    private int containerTimestampCounter;
    private int receivedRequestsCounter;

    public TelemetrySenderMock() {
        currentTimestampCounter = 0;
        containerTimestampCounter = 0;
        receivedRequestsCounter = 0;
    }

    public int getCurrentTimestampCounter() {
        return currentTimestampCounter;
    }

    public int getContainerTimestampCounter() {
        return containerTimestampCounter;
    }

    public int getReceivedRequestsCounter() {
        return receivedRequestsCounter;
    }

    @Override
    public void sendCurrentTimestamp(String controllerAddress, int controllerPort, String endpointType, String endpoint) {
        currentTimestampCounter++;
    }

    @Override
    public void sendContainerTimestamp(String controllerAddress, int controllerPort, String containerEndpoint, long time) {
        containerTimestampCounter++;
    }

    @Override
    public void sendReceivedRequests(String controllerAddress, int controllerPort, LoadDistributionDTO receivedRequests) {
        receivedRequestsCounter++;
    }
}
