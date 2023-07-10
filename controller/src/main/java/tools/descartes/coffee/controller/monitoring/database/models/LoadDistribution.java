package tools.descartes.coffee.controller.monitoring.database.models;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;

@Entity
public class LoadDistribution {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public long totalRunTime;

    public long receivedRequests;

    @Transient
    public List<Integer> requestNumbers;

    protected LoadDistribution() {

    }

    public LoadDistribution(long totalRunTime, long receivedRequests, List<Integer> requestNumbers) {
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

    public List<Integer> getRequestNumbers() {
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
