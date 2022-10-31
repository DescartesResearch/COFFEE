package tools.descartes.coffee.controller.procedure.collection.load;

import java.sql.Timestamp;

import tools.descartes.coffee.controller.load.LoadGenerator;
import tools.descartes.coffee.controller.procedure.SimpleProcedure;
import tools.descartes.coffee.controller.procedure.collection.Command;
import tools.descartes.coffee.controller.utils.SpringPropertyHelper;
import tools.descartes.coffee.shared.HttpUtils;

public class GenerateLoad extends SimpleProcedure {

    protected static final String START_LOAD_COMMAND_ENDPOINT = "http://localhost:"
            + SpringPropertyHelper.getProperty("server.port") + "/command/start-load";

    private final LoadGenerator loadGenerator;

    public GenerateLoad(LoadGenerator loadGenerator) {
        super(Command.LOAD);
        this.loadGenerator = loadGenerator;
    }

    @Override
    public void executeCommand() {
        loadGenerator.startLoadProfile();
        this.storeLoadGenerationStartTime();
    }

    private void storeLoadGenerationStartTime() {
        long currentTime = System.currentTimeMillis();
        Timestamp timeStamp = new Timestamp(currentTime);
        HttpUtils.post(START_LOAD_COMMAND_ENDPOINT, timeStamp);
    }

    @Override
    public boolean needsLoadGenerator() {
        return true;
    }
}
