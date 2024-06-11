package tools.descartes.coffee.shared;

import java.util.List;

public class LoadDistributionDTO {
    private long receivedRequests;
    private long totalRuntime;
    private List<Integer> requestNumbers;

    public long getReceivedRequests() {
        return receivedRequests;
    }

    public long getTotalRuntime() {
        return totalRuntime;
    }

    public void setReceivedRequests(long receivedRequests) {
        this.receivedRequests = receivedRequests;
    }

    public void setTotalRuntime(long totalRuntime) {
        this.totalRuntime = totalRuntime;
    }

    public List<Integer> getRequestNumbers() {
        return requestNumbers;
    }

    public void setRequestNumbers(List<Integer> requestNumbers) {
        this.requestNumbers = requestNumbers;
    }
}
