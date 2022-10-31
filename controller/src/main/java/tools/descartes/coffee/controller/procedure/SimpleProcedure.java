package tools.descartes.coffee.controller.procedure;

import tools.descartes.coffee.controller.procedure.collection.Command;
import tools.descartes.coffee.controller.utils.SpringPropertyHelper;

public abstract class SimpleProcedure extends BaseProcedure {

    protected final String START_COMMAND_ENDPOINT = "http://localhost:"
            + SpringPropertyHelper.getProperty("server.port") + "/command/start";

    public SimpleProcedure(Command type) {
        super(type);
    }

    protected void throwApiException(Throwable e) {
        throw new RuntimeException(
                "procedure failed while accessing the cluster client api.\n" +
                        "the response signals an error or cannot be deserialized: " + e.getMessage(),
                e);
    }

    protected void throwIoException(Throwable e) {
        throw new RuntimeException(
                "procedure failed while accessing the cluster client api.\n" +
                        "there is an HTTP or lower-level problem: " + e.getMessage(),
                e);
    }
}
