package tools.descartes.coffee.controller.procedure;

import java.sql.Timestamp;

import tools.descartes.coffee.controller.monitoring.database.models.CommandStartTime;
import tools.descartes.coffee.controller.procedure.collection.Command;
import tools.descartes.coffee.controller.procedure.collection.deployment.SubscribeDeployment;
import tools.descartes.coffee.shared.HttpUtils;

/**
 * TBD: post vs postAsync
 * 
 * class to auto send the command start time (and type) to the monitoring server
 */
public abstract class SimpleReportProcedure extends SimpleProcedure implements ReportInterface, SubscribeDeployment {

    public SimpleReportProcedure(Command type) {
        super(type);
    }

    @Override
    public void executeCommand() {
        this.sendCommandStartTime();
        this.call();
        this.subscribe();
    }

    protected void sendCommandStartTime() {
        int replicas = this.prepare();
        long currentTime = System.currentTimeMillis();

        Timestamp timeStamp = new Timestamp(currentTime);
        CommandStartTime startCommandTime = new CommandStartTime(type, timeStamp);

        // exceptional case - update: create only one command entry
        if (replicas == 1 || type.equals(Command.UPDATE)) {
            HttpUtils.post(START_COMMAND_ENDPOINT + "?replicas=1", startCommandTime);
        } else {
            HttpUtils.post(START_COMMAND_ENDPOINT + "?replicas=" + replicas, startCommandTime);
        }
    }
}
