package tools.descartes.coffee.application;

import java.sql.Timestamp;
import java.util.logging.Logger;

import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import tools.descartes.coffee.shared.AppVersion;
import tools.descartes.coffee.shared.LoadDistributionDTO;

@SpringBootApplication
public class AppApplication {
    private static final Logger logger = Logger.getLogger(AppApplication.class.getName());

    public static final AppVersion version = AppVersion.V1;

    private static long startTime;

    public static void main(String[] args) {
        AppApplication.startTime = System.currentTimeMillis();
        Timestamp currentTimestamp = new Timestamp(AppApplication.startTime);
        logger.info("Start time: " + currentTimestamp);
        SpringApplication.run(AppApplication.class, args);
    }

    @Value("${app.controllerAddress}")
    private String appControllerAddress;

    @Value("${app.controllerPort}")
    private int appControllerPort;

    @Autowired
    private LoadCounter loadCounter;

    @EventListener
    public void onApplicationReady(ApplicationReadyEvent event) {
        AppUtils.sendContainerTimestamp(appControllerAddress, appControllerPort, "start", AppApplication.startTime);
    }

    @PreDestroy
    public void destroy() {
        long endTime = System.currentTimeMillis();
        long totalRuntime = endTime - startTime;
        LoadDistributionDTO loadDTO = new LoadDistributionDTO();
        loadDTO.setTotalRuntime(totalRuntime);
        loadDTO.setReceivedRequests(loadCounter.getReceivedRequests());
        AppUtils.sendReceivedRequests(appControllerAddress, appControllerPort, loadDTO);
        AppUtils.sendContainerTimestamp(appControllerAddress, appControllerPort, "stop", endTime);
    }
}
