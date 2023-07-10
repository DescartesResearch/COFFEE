package tools.descartes.coffee.controller.monitoring.database.models;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class LoadDistribution {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public long totalRunTime;

    public long receivedRequests;

    public String requestNumbers;

    protected LoadDistribution() {

    }

    public LoadDistribution(long totalRunTime, long receivedRequests, String requestNumbers) {
        this.totalRunTime = totalRunTime;
        this.receivedRequests = receivedRequests;
        this.requestNumbers = requestNumbers;
    }

    public long getTotalRunTime() {
        return totalRunTime;
    }

    public long getReceivedRequests() {
        return receivedRequests;
    }

    public String getRequestNumbers() {
        return requestNumbers;
    }

    @Override
    public String toString() {
        return "LoadDistribution[" +
                "id=" + id +
                ", totalRunTime=" + totalRunTime +
                ", receivedRequests=" + receivedRequests +
                ']';
    }
}
