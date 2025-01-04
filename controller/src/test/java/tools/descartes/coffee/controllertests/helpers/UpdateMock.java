package tools.descartes.coffee.controllertests.helpers;

import tools.descartes.coffee.controller.procedure.BaseProcedure;

public class UpdateMock extends BaseProcedure {
    public UpdateMock() {
        super(null);
    }

    @Override
    public void executeCommand() {

    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof UpdateMock) return true;
        return false;
    }
}
