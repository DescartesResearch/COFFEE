package tools.descartes.coffee.controller.procedure.collection.deployment;

public interface SubscribeDeployment {

    /**
     * Subscribes to the corresponding deployment events expected by this procedure.
     * 
     * Should be called directly after the API call/at the end of the call() method.
     */
    void subscribe();
}
