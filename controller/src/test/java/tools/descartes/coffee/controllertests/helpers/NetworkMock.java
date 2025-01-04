package tools.descartes.coffee.controllertests.helpers;

import tools.descartes.coffee.controller.procedure.BaseProcedure;

public class NetworkMock extends BaseProcedure {
    public NetworkMock() {
        super(null);
    }

    @Override
    public void executeCommand() {

    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof NetworkMock) return true;
        return false;
    }
}
