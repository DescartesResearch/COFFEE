package tools.descartes.coffee.controllertests.helpers;

import tools.descartes.coffee.controller.procedure.BaseProcedure;

public class CrashMock extends BaseProcedure {
    public CrashMock() {
        super(null);
    }

    @Override
    public void executeCommand() {

    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CrashMock) return true;
        return false;
    }
}
