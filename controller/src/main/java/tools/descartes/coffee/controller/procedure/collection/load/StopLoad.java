package tools.descartes.coffee.controller.procedure.collection.load;

import tools.descartes.coffee.controller.load.LoadGenerator;
import tools.descartes.coffee.controller.procedure.SimpleProcedure;
import tools.descartes.coffee.controller.procedure.collection.Command;

public class StopLoad extends SimpleProcedure {

    private final LoadGenerator loadGenerator;

    public StopLoad(LoadGenerator loadGenerator) {
        super(Command.ENDLOAD);
        this.loadGenerator = loadGenerator;
    }

    @Override
    public void executeCommand() {
        loadGenerator.stopLoadProfile();
    }

}
