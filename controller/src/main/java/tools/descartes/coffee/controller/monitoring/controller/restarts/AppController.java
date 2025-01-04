package tools.descartes.coffee.controller.monitoring.controller.restarts;

import java.sql.Timestamp;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import tools.descartes.coffee.controller.monitoring.database.GenericDatabaseService;
import tools.descartes.coffee.controller.monitoring.database.restart.app.AppService;
import tools.descartes.coffee.controller.monitoring.database.models.AppCrashRestartTime;

@RestController
@RequestMapping("/app")
public class AppController {
    private static final Logger logger = Logger.getLogger(AppController.class.getName());

    private GenericDatabaseService<AppCrashRestartTime> appService;

    public AppController(AppService appService) {
        this.appService = appService;
    }

    public void setAppService(GenericDatabaseService<AppCrashRestartTime> appService) {
        this.appService = appService;
    }

    /**
     * stores the timestamps when app crashed
     */
    private final ConcurrentLinkedQueue<Timestamp> crashTimes = new ConcurrentLinkedQueue<>();

    /**
     * stores the shut down times of old container
     */
    private final ConcurrentLinkedQueue<Timestamp> containerShutDownTimes = new ConcurrentLinkedQueue<>();

    public void addCrashShutDownTime(Timestamp containerStopTime) {
        this.containerShutDownTimes.add(containerStopTime);
    }

    @PostMapping("/crash")
    public void addUnhealthyTimestamp(@RequestBody Timestamp crashTime) {
        logger.info("/crash is called");
        this.crashTimes.add(crashTime);
    }

    public void storeAppCrashRestartTime(Timestamp commandStart, Timestamp restartTime) {
        Timestamp crashTime = this.crashTimes.poll();
        Timestamp shutDownTime = this.containerShutDownTimes.poll();

        if (crashTime == null) {
            throw new IllegalStateException(
                    "Error while storing crash restart time: No container crash time available.");
        }

        // In rare cases it can happen that the predestroy method of the test container is not executed, therefore no shutdown time is reported
        if (shutDownTime == null) {
            logger.warning("Error while storing crash restart time: No shut down time of old container available.");
        }

        AppCrashRestartTime appCrashRestartTime = new AppCrashRestartTime(commandStart, crashTime, shutDownTime,
                restartTime);
        this.appService.add(appCrashRestartTime);
    }
}
