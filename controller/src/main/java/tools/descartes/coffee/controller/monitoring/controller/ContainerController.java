package tools.descartes.coffee.controller.monitoring.controller;

import java.sql.Timestamp;
import java.util.logging.Logger;
import java.io.*;

import tools.descartes.coffee.controller.monitoring.database.loaddist.LoadDistributionService;
import tools.descartes.coffee.controller.monitoring.database.models.LoadDistribution;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import tools.descartes.coffee.controller.monitoring.controller.restarts.AppController;
import tools.descartes.coffee.controller.monitoring.controller.restarts.HealthController;
import tools.descartes.coffee.controller.monitoring.controller.restarts.ManualRestartController;
import tools.descartes.coffee.controller.monitoring.database.command.CommandExecutionService;
import tools.descartes.coffee.controller.monitoring.database.deployment.DeploymentService;
import tools.descartes.coffee.controller.monitoring.database.models.CommandExecutionTime;
import tools.descartes.coffee.controller.monitoring.database.models.UpdateRestartTime;
import tools.descartes.coffee.controller.procedure.ProcedureQueue;
import tools.descartes.coffee.controller.procedure.collection.Command;
import tools.descartes.coffee.controller.procedure.collection.deployment.UpdateContainer;
import tools.descartes.coffee.shared.AppVersion;
import tools.descartes.coffee.shared.LoadDistributionDTO;

@RestController
@RequestMapping("/container")
public class ContainerController {
    private final Logger logger = Logger.getLogger(this.getClass().getName());

    private final CommandExecutionService commandExecutionService;
    private final HealthController healthController;
    private final AppController appController;
    private final ManualRestartController manualRestartController;
    private final CommandController commandController;
    private final DeploymentService deploymentService;
    private final ProcedureQueue procedureQueue;
    private final LoadDistributionService loadDistributionService;
    private int temporaryLoadCounter;

    public ContainerController(CommandExecutionService commandExecutionService, HealthController healthController,
                               AppController appController, ManualRestartController manualRestartController,
                               CommandController commandController,
                               DeploymentService deploymentService, ProcedureQueue procedureQueue,
                               LoadDistributionService loadDistributionService) {
        this.commandExecutionService = commandExecutionService;
        this.healthController = healthController;
        this.appController = appController;
        this.manualRestartController = manualRestartController;
        this.commandController = commandController;
        this.deploymentService = deploymentService;
        this.procedureQueue = procedureQueue;
        this.loadDistributionService = loadDistributionService;
        temporaryLoadCounter = 0;
    }

    /**
     * Indicates if the test procedure is active or if controller start up or shut
     * down work is done.
     * 
     * If not active, dont process container start/remove times.
     */
    public static boolean isProcedureActive = false;

    /** update specific */
    private int updatedContainerCount = 0;
    public static int CONTAINERS_TO_UPDATE;

    @PostMapping("/start")
    public void addStartupTime(@RequestParam String version, @RequestBody Timestamp containerStartTime) {
        if (!ContainerController.isProcedureActive) {
            return;
        }

        if (this.isUpdateActive(version, true)) {
            this.checkUpdateShutDownTimes(containerStartTime);
        } else {
            Command furthestCommand = procedureQueue.getNextStartCommand();
            Timestamp commandStartTime = this.commandController.commandQueue.get(furthestCommand).poll();

            if (furthestCommand == Command.HEALTH) {
                this.storeAdditionalHealthData(commandStartTime, containerStartTime);
            }

            if (furthestCommand == Command.CRASH) {
                this.storeAdditionalCrashData(commandStartTime, containerStartTime);
            }

            CommandExecutionTime executionTime = new CommandExecutionTime(furthestCommand, commandStartTime,
                    containerStartTime);
            commandExecutionService.add(executionTime);

            procedureQueue.onContainerStarted();
        }
    }

    /**
     * @return if the update command is active and if a starting container has the
     *         new version or an shutting down container has the old version
     */
    private boolean isUpdateActive(String containerVersion, boolean isStartingContainer) {
        boolean isCurrentAppVersion = containerVersion.equals(UpdateContainer.getCurrentAppVersion().toString());

        return (isStartingContainer != isCurrentAppVersion) &&
                procedureQueue.isUpdateCommandActive();
    }

    private void checkUpdateShutDownTimes(Timestamp containerStartTime) {
        /*
         * start up and shut down times are stored separately because of their async
         * execution
         */
        Timestamp lastShutDownTime = this.commandController.updateShutDownQueue.poll();

        if (lastShutDownTime == null) {
            this.commandController.addUpdateStartUpTimestamp(containerStartTime);
            return;
        }

        UpdateRestartTime updateTime = new UpdateRestartTime(lastShutDownTime, containerStartTime);
        this.deploymentService.add(updateTime);

        logger.info("Update Status: " + (updatedContainerCount + 1) + " / " + CONTAINERS_TO_UPDATE + " updated");
        if (++updatedContainerCount == CONTAINERS_TO_UPDATE) {
            this.completeUpdateCommand(containerStartTime);
        }

        procedureQueue.onContainerUpdated();
    }

    private void completeUpdateCommand(Timestamp containerStartTime) {
        this.updatedContainerCount = 0;
        UpdateContainer.setCurrentUpdateCount(UpdateContainer.getCurrentUpdateCount() + 1);
        UpdateContainer.setCurrentAppVersion(UpdateContainer.getCurrentAppVersion().equals(AppVersion.V1)
                ? AppVersion.V2
                : AppVersion.V1);

        Timestamp updateCommandStartTime = this.commandController.commandQueue.get(Command.UPDATE).poll();

        if (updateCommandStartTime == null) {
            throw new IllegalStateException(
                    "Error while storing update time: No update command start time available.");
        }

        // take the last restart time as executionFinished parameter
        CommandExecutionTime executionTime = new CommandExecutionTime(Command.UPDATE, updateCommandStartTime,
                containerStartTime);
        commandExecutionService.add(executionTime);

    }

    private void storeAdditionalHealthData(Timestamp commandStartTime, Timestamp executionFinished) {
        this.healthController.storeHealthRestartTime(commandStartTime, executionFinished);

    }

    private void storeAdditionalCrashData(Timestamp commandStartTime, Timestamp executionFinished) {
        this.appController.storeAppCrashRestartTime(commandStartTime, executionFinished);
    }

    @PostMapping("/stop")
    public void addRemoveTime(@RequestParam String version, @RequestBody Timestamp containerStopTime) {
        logger.info("/stop is called");
        if (!ContainerController.isProcedureActive) {
            return;
        }

        if (this.isUpdateActive(version, false)) {
            this.checkUpdateStartUpTimes(containerStopTime);
        } else {
            if (this.checkRestartCommands(containerStopTime)) {
                return;
            }

            Timestamp commandStartTime = this.commandController.commandQueue.get(Command.REMOVE).poll();
            if (commandStartTime == null) {
                throw new IllegalStateException(
                        "Error while storing container remove time: No suitable command found.");
            }

            CommandExecutionTime executionTime = new CommandExecutionTime(Command.REMOVE, commandStartTime,
                    containerStopTime);
            commandExecutionService.add(executionTime);

        }

        procedureQueue.onContainerRemoved();
    }

    /**
     * TODO: centralize container shut down queue
     * 
     * assumes that next if the next command to waiting for a container start is a
     * restart, then the current shut down is referred to this command (type)
     * Then, stores the container stop time in the corresponding controller.
     * 
     * @param containerStopTime
     * @return if the container stop is part of a restart and has not to be
     *         processed further
     */
    private boolean checkRestartCommands(Timestamp containerStopTime) {
        Command furthestCommand = procedureQueue.peekNextStartCommand();

        if (furthestCommand == null) {
            return false;
        }

        if (furthestCommand == Command.HEALTH) {
            this.healthController.addHealthShutDownTime(containerStopTime);
            return true;
        }

        if (furthestCommand == Command.CRASH) {
            this.appController.addCrashShutDownTime(containerStopTime);
            return true;
        }

        if (furthestCommand == Command.RESTART) {
            this.manualRestartController.addCrashShutDownTime(containerStopTime);
            return true;
        }

        return false;
    }

    private void checkUpdateStartUpTimes(Timestamp containerStopTime) {
        /*
         * start up and shut down times are stored separately because of their async
         * execution
         */
        Timestamp lastStartUpTime = this.commandController.updateStartUpQueue.poll();

        if (lastStartUpTime == null) {
            this.commandController.addUpdateShutDownTimestamp(containerStopTime);
            return;
        }

        UpdateRestartTime updateTime = new UpdateRestartTime(containerStopTime, lastStartUpTime);
        this.deploymentService.add(updateTime);

        logger.info("Update Status: " + (updatedContainerCount + 1) + " / " + CONTAINERS_TO_UPDATE + " updated");
        if (++updatedContainerCount == CONTAINERS_TO_UPDATE) {
            this.completeUpdateCommand(lastStartUpTime);
        }

        procedureQueue.onContainerUpdated();
    }

    @PostMapping("/loaddist")
    public void addLoadDistribution(@RequestParam String version, @RequestBody LoadDistributionDTO loadDistributionDTO) {
        loadDistributionService.add(new LoadDistribution(loadDistributionDTO.getTotalRuntime(), loadDistributionDTO.getReceivedRequests(), loadDistributionDTO.getRequestNumbers()));
        File file = new File("./Instance" + temporaryLoadCounter + ".json");
        temporaryLoadCounter = temporaryLoadCounter + 1
        if (!file.exists()) {
            file.createNewFile();
        }
        FileWriter fw = new FileWriter(file);
        BufferedWriter bw = new BufferedWriter(fw);

        for (int i = 0; i < loadDistributionDTO.getRequestNumbers().size(); i++) {
            bw.write(loadDistributionDTO.getRequestNumbers().get(i).toString());
        }
        bw.flush();
        bw.close();
    }
}
