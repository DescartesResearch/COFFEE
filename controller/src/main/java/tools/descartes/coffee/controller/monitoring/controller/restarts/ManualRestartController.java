package tools.descartes.coffee.controller.monitoring.controller.restarts;

import java.sql.Timestamp;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.springframework.stereotype.Component;

import tools.descartes.coffee.controller.monitoring.database.models.ManualRestartTime;
import tools.descartes.coffee.controller.monitoring.database.restart.manual.ManualRestartService;

/**
 * Since the manual restarts have no additional endpoint to sent data,
 * no extra Spring Controller is needed.
 */
@Component
public class ManualRestartController {

    private final ManualRestartService manualRestartService;

    public ManualRestartController(ManualRestartService manualRestartService) {
        this.manualRestartService = manualRestartService;
    }

    /**
     * stores the shut down times of old container
     */
    private final ConcurrentLinkedQueue<Timestamp> containerShutDownTimes = new ConcurrentLinkedQueue<>();

    public void addCrashShutDownTime(Timestamp containerStopTime) {
        this.containerShutDownTimes.add(containerStopTime);
    }

    public void storeAppCrashRestartTime(Timestamp commandStart, Timestamp restartTime) {
        Timestamp shutDownTime = this.containerShutDownTimes.poll();

        if (shutDownTime == null) {
            throw new IllegalStateException(
                    "Error while storing manual restart time: No shut down time of old container available.");
        }

        ManualRestartTime manualRestartTime = new ManualRestartTime(commandStart, shutDownTime, restartTime);
        this.manualRestartService.add(manualRestartTime);
    }
}
