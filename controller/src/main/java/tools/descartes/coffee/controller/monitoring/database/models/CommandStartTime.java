package tools.descartes.coffee.controller.monitoring.database.models;

import java.sql.Timestamp;

import javax.persistence.MappedSuperclass;

import tools.descartes.coffee.controller.procedure.collection.Command;

@MappedSuperclass
public class CommandStartTime {

    /** command type */
    public String command;

    /** time when command is started */
    public Timestamp commandTime;

    protected CommandStartTime() {
    }

    public CommandStartTime(Command command, Timestamp start) {
        this.command = command.toString();
        this.commandTime = start;
    }

    @Override
    public String toString() {
        return String.format(
                "CommandExecutionTime[command='%s', commandTime='%s']",
                command, commandTime);
    }

    public String getCommand() {
        return command;
    }

    public Timestamp getCommandTime() {
        return commandTime;
    }
}
