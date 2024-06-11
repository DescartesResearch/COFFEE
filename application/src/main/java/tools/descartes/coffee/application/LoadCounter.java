package tools.descartes.coffee.application;

import org.springframework.stereotype.Component;
import java.util.List;
import java.util.ArrayList;

@Component
public class LoadCounter {
    private long receivedRequests;
    private List<Integer> requestNumbers;

    public LoadCounter() {
        receivedRequests = 0L;
        requestNumbers = new ArrayList<>(100);
    }

    public synchronized void addLoad(int requestNr) {
        receivedRequests++;
        requestNumbers.add(requestNr);
    }

    public long getReceivedRequests() {
        return receivedRequests;
    }

    public List<Integer> getRequestNumbers() {
        return requestNumbers;
    }
}
