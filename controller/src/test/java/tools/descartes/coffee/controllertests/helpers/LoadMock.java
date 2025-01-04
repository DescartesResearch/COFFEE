package tools.descartes.coffee.controllertests.helpers;

import tools.descartes.coffee.controller.procedure.BaseProcedure;

public class LoadMock extends BaseProcedure {
    public LoadMock() {
        super(null);
    }

    @Override
    public void executeCommand() {

    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof LoadMock) return true;
        return false;
    }
}
