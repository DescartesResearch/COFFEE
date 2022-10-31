package tools.descartes.coffee.controller;

import tools.descartes.coffee.controller.orchestrator.BaseClusterClient;
import tools.descartes.coffee.controller.orchestrator.ClusterClientWrapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PreDestroy;
import java.util.logging.Logger;

@SpringBootApplication
public class ControllerApplication {
    private static final Logger logger = Logger.getLogger(ControllerApplication.class.getName());

    public static void main(String[] args) {
        SpringApplication.run(ControllerApplication.class, args);
    }

    private BaseClusterClient client;

    public ControllerApplication(ClusterClientWrapper clusterClientWrapper) {
        client = clusterClientWrapper.getClient();
    }

    @PreDestroy
    public void cleanUp() {
        logger.info("Shutting down application, cleaning up...");
        try {
            client.clear();
            client.disconnect();
        } catch (Exception e) {
            logger.info("Error while cleaning up pre-destroy, exception is: " + e);
        }
    }
}
