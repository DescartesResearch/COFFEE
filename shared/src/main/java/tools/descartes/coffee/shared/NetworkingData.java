package tools.descartes.coffee.shared;

public class NetworkingData {

    /** source container ip */
    private String source;

    /** target container ip */
    private String target;

    /** time when requesting container sends request */
    private long startNetworking;

    /** arrival time of request at responding container */
    private long requestArrival;

    /** arrival time of response at requesting container */
    private long responseArrival;

    public NetworkingData(String source, String target, long startNetworking, long requestArrival,
            long responseArrival) {
        this.source = source;
        this.target = target;
        this.startNetworking = startNetworking;
        this.requestArrival = requestArrival;
        this.responseArrival = responseArrival;
    }

    // Needed for JSON deserialization
    public NetworkingData() {

    }

    public String getSource() {
        return source;
    }

    public String getTarget() {
        return target;
    }

    public long getStartNetworking() {
        return startNetworking;
    }

    public long getRequestArrival() {
        return requestArrival;
    }

    public long getResponseArrival() {
        return responseArrival;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public void setStartNetworking(long startNetworking) {
        this.startNetworking = startNetworking;
    }

    public void setRequestArrival(long requestArrival) {
        this.requestArrival = requestArrival;
    }

    public void setResponseArrival(long responseArrival) {
        this.responseArrival = responseArrival;
    }
}
