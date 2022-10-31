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
public class HealthRestartTime extends CommandStartTime {

    // ############# CommandExecutionTime START #############

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    /** time when command execution finished */
    public Timestamp executionFinished;

    // ############## CommandExecutionTime END ##############

    /** time when health flag is actually set to false inside the container */
    public Timestamp unhealthyTime;

    /** time when health check recognizes that container is unhealthy */
    public Timestamp healthCheckTime;

    /** time when SpringBootApplication is shut down */
    public Timestamp appShutDownTime;

    public long timeToUnhealthy;
    public long timeToHealthCheck;
    public long timeToAppShutdown;
    public long executionTime;

    protected HealthRestartTime() {
    }

    public HealthRestartTime(Timestamp start, Timestamp unhealthy, Timestamp healthCheck, Timestamp shutDown,
            Timestamp end) {
        this.command = Command.HEALTH.toString();

        this.commandTime = start;
        this.unhealthyTime = unhealthy;
        this.healthCheckTime = healthCheck;
        this.appShutDownTime = shutDown;
        this.executionFinished = end;
        timeToUnhealthy = unhealthy.getTime() - commandTime.getTime();
        timeToHealthCheck = healthCheckTime.getTime() - commandTime.getTime();
        timeToAppShutdown = appShutDownTime.getTime() - commandTime.getTime();
        executionTime = executionFinished.getTime() - commandTime.getTime();
    }

    @Override
    public String toString() {
        return String.format(
                "HealthRestartTime[id=%d, command='%s', commandTime='%s', unhealthyTime='%s', healthCheckTime='%s', appShutDownTime='%s', executionFinished='%s']",
                id, command, commandTime, unhealthyTime, healthCheckTime, appShutDownTime, executionFinished);
    }

    public Long getId() {
        return id;
    }

    public Timestamp getExecutionFinished() {
        return executionFinished;
    }

    public Timestamp getUnhealthyTime() {
        return unhealthyTime;
    }

    public Timestamp getHealthCheckTime() {
        return healthCheckTime;
    }

    public Timestamp getAppShutDownTime() {
        return appShutDownTime;
    }
}
