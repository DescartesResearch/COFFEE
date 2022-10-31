package tools.descartes.coffee.application;

import org.springframework.stereotype.Component;

@Component
public class LoadCounter {
    private long receivedRequests;

    public LoadCounter() {
        receivedRequests = 0L;
    }

    public synchronized void addLoad() {
        receivedRequests++;
    }

    public long getReceivedRequests() {
        return receivedRequests;
    }
}
