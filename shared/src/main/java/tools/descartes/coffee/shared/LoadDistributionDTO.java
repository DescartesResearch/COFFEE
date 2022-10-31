package tools.descartes.coffee.shared;

public class LoadDistributionDTO {
    private long receivedRequests;
    private long totalRuntime;

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
}
