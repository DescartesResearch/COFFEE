package tools.descartes.coffee.controller.procedure;

public interface ReportInterface {

    /**
     * prepares all data for the call
     * 
     * @return the replica count affected by the command
     */
    int prepare();

    /**
     * calls the cluster api
     */
    void call();

}
