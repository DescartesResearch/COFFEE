package tools.descartes.coffee.controller.monitoring.database.models;

import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import tools.descartes.coffee.controller.procedure.collection.Command;

@Entity
public class CommandExecutionTime extends CommandStartTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    /** time when command execution finished */
    public Timestamp executionFinished;

    public long executionTime;

    protected CommandExecutionTime() {
    }

    public CommandExecutionTime(Command command, Timestamp start, Timestamp end) {
        this.command = command.toString();
        this.commandTime = start;
        this.executionFinished = end;
        executionTime = executionFinished.getTime() - commandTime.getTime();
    }

    @Override
    public String toString() {
        return String.format(
                "CommandExecutionTime[id=%d, command='%s', commandTime='%s', executionFinished='%s']",
                id, command, commandTime, executionFinished);
    }

    public Long getId() {
        return id;
    }

    public Timestamp getExecutionFinished() {
        return executionFinished;
    }
}
