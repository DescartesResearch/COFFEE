package tools.descartes.coffee.controllertests.helpers;

import tools.descartes.coffee.controller.procedure.BaseProcedure;

public class DelayMock extends BaseProcedure {

    public DelayMock() {
        super(null);
    }

    @Override
    public void executeCommand() {

    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DelayMock) return true;
        return false;
    }
}
