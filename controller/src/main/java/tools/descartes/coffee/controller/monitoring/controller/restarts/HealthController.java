package tools.descartes.coffee.controller.monitoring.controller.restarts;

import java.sql.Timestamp;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import tools.descartes.coffee.controller.monitoring.database.restart.health.HealthService;
import tools.descartes.coffee.controller.monitoring.database.models.HealthRestartTime;

@RestController
@RequestMapping("/health")
public class HealthController {
    private final Logger logger = Logger.getLogger(this.getClass().getName());

    private final HealthService healthService;

    public HealthController(HealthService healthService) {
        this.healthService = healthService;
    }

    /**
     * stores the timestamps when flag is set to unhealthy
     */
    private final ConcurrentLinkedQueue<Timestamp> unhealthyTimes = new ConcurrentLinkedQueue<>();

    /**
     * stores the timestamps of health check
     */
    private final ConcurrentLinkedQueue<Timestamp> healthCheckTimes = new ConcurrentLinkedQueue<>();

    /**
     * stores the shut down times of old container
     */
    private final ConcurrentLinkedQueue<Timestamp> containerShutDownTimes = new ConcurrentLinkedQueue<>();

    @PostMapping("/unhealthy")
    public void addUnhealthyTimestamp(@RequestBody Timestamp unhealthyTime) {
        this.unhealthyTimes.add(unhealthyTime);
    }

    @PostMapping("/check")
    public void addCheckingTimestamp(@RequestBody Timestamp checkTime) {
        this.healthCheckTimes.add(checkTime);
    }

    public void addHealthShutDownTime(Timestamp containerStopTime) {
        this.containerShutDownTimes.add(containerStopTime);
    }

    /**
     * creates the HealthRestartTime and stores it via the HealthService
     * 
     * @param commandStartTime
     * @param restartTime      execution finished
     */
    public void storeHealthRestartTime(Timestamp commandStartTime, Timestamp restartTime) {
        Timestamp unhealthyTime = this.unhealthyTimes.poll();
        Timestamp checkTime = this.healthCheckTimes.poll();
        Timestamp shutDownTime = this.containerShutDownTimes.poll();

        if (unhealthyTime == null) {
            throw new IllegalStateException(
                    "Error while storing health restart time: No unhealthy flag time available.");
        }

        if (checkTime == null) {
            throw new IllegalStateException("Error while storing health restart time: No health check time available.");
        }

        if (shutDownTime == null) {
            throw new IllegalStateException(
                    "Error while storing health restart time: No shut down time of old container available.");
        }

        HealthRestartTime healthRestartTime = new HealthRestartTime(commandStartTime, unhealthyTime,
                checkTime, shutDownTime, restartTime);
        this.healthService.add(healthRestartTime);
    }
}