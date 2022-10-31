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
public class AppCrashRestartTime extends CommandStartTime {

    // ############# CommandExecutionTime START #############

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    /** time when command execution finished */
    public Timestamp executionFinished;

    // ############## CommandExecutionTime END ##############

    /** time when System.exit(1) is called inside the container */
    public Timestamp appCrashTime;

    /** time when SpringBootApplication is shut down */
    public Timestamp appShutDownTime;

    public long crashTime;
    public long shutdownTime;
    public long executionTime;

    public AppCrashRestartTime() {
    }

    public AppCrashRestartTime(Timestamp start, Timestamp crash, Timestamp shutDown, Timestamp end) {
        this.command = Command.CRASH.toString();
        this.commandTime = start;
        this.appCrashTime = crash;
        this.appShutDownTime = shutDown;
        this.executionFinished = end;
        crashTime = appCrashTime.getTime() - commandTime.getTime();
        shutdownTime = appShutDownTime != null ? appShutDownTime.getTime() - commandTime.getTime() : -1;
        executionTime = executionFinished.getTime() - commandTime.getTime();
    }

    @Override
    public String toString() {
        return String.format(
                "HealthRestartTime[id=%d, command='%s', commandTime='%s', appCrashTime='%s', appShutDownTime='%s', executionFinished='%s']",
                id, command, commandTime, appCrashTime, appShutDownTime, executionFinished);
    }

    public Long getId() {
        return id;
    }

    public Timestamp getExecutionFinished() {
        return executionFinished;
    }

    public Timestamp getAppCrashTime() {
        return appCrashTime;
    }

    public Timestamp getAppShutDownTime() {
        return appShutDownTime;
    }
}
