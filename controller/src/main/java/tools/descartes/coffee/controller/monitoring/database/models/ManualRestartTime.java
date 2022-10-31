package tools.descartes.coffee.controller.monitoring.database.models;

import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import tools.descartes.coffee.controller.procedure.collection.Command;

/**
 * Partially copied from CommandExecutionTime to generate separate database
 * tables.
 */
@Entity
public class ManualRestartTime extends CommandStartTime {

    // ############# CommandExecutionTime START #############

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    /** time when new container is started and command execution finishes */
    public Timestamp executionFinished;

    // ############## CommandExecutionTime END ##############

    /** time when SpringBootApplication is shut down */
    public Timestamp appShutDownTime;

    public long timeToAppShutdown;
    public long executionTime;

    public ManualRestartTime() {
    }

    public ManualRestartTime(Timestamp start, Timestamp shutDown, Timestamp end) {
        this.command = Command.RESTART.toString();

        this.commandTime = start;
        this.appShutDownTime = shutDown;
        this.executionFinished = end;
        timeToAppShutdown = appShutDownTime.getTime() - commandTime.getTime();
        executionTime = executionFinished.getTime() - commandTime.getTime();
    }

    @Override
    public String toString() {
        return String.format(
                "HealthRestartTime[id=%d, command='%s', commandTime='%s', appShutDownTime='%s', executionFinished='%s']",
                id, command, commandTime, appShutDownTime, executionFinished);
    }

    public Long getId() {
        return id;
    }

    public Timestamp getAppShutDownTime() {
        return appShutDownTime;
    }

    public Timestamp getExecutionFinished() {
        return executionFinished;
    }
}
