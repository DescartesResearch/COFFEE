package tools.descartes.coffee.controller.monitoring.database.models;

import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import tools.descartes.coffee.shared.NetworkingData;

@Entity
public class NetworkTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    /** networking number to which this entry/route refers */
    public int networkingNo;

    /** source container ip */
    public String source;

    /** target container ip */
    public String target;

    /** time when requesting container sends request */
    public Timestamp startNetworking;

    /** arrival time of request at responding container */
    public Timestamp requestArrival;

    /** arrival time of response at requesting container */
    public Timestamp responseArrival;

    public long firstMessage;
    public long secondMessage;
    public long roundTripTime;

    protected NetworkTime() {
    }

    public NetworkTime(int networkingNo, NetworkingData networkingData) {
        this.networkingNo = networkingNo;
        this.source = networkingData.getSource();
        this.target = networkingData.getTarget();
        this.startNetworking = new Timestamp(networkingData.getStartNetworking());
        this.requestArrival = new Timestamp(networkingData.getRequestArrival());
        this.responseArrival = new Timestamp(networkingData.getResponseArrival());
        firstMessage = requestArrival.getTime() - startNetworking.getTime();
        secondMessage = responseArrival.getTime() - requestArrival.getTime();
        roundTripTime = responseArrival.getTime() - startNetworking.getTime();
    }

    @Override
    public String toString() {
        return String.format(
                "NetworkTime[id=%d, networkingNo='%d', source=%s, target=%s, startNetworking='%s', requestArrival='%s', responseArrival='%s']",
                id, networkingNo, source, target, startNetworking, requestArrival, responseArrival);
    }

    public int getNetworkingNo() {
        return networkingNo;
    }

    public String getSource() {
        return source;
    }

    public String getTarget() {
        return target;
    }

    public Timestamp getStartNetworking() {
        return startNetworking;
    }

    public Timestamp getRequestArrival() {
        return requestArrival;
    }

    public Timestamp getResponseArrival() {
        return responseArrival;
    }
}
