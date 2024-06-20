package tools.descartes.coffee.controller.procedure;

import tools.descartes.coffee.controller.procedure.collection.Command;

import java.util.ArrayList;
import java.util.List;

public class ComplexProcedure extends BaseProcedure {

    private final int loop;

    /** collection of child procedures */
    private final List<BaseProcedure> procedures;

    public ComplexProcedure(int loop) {
        super(Command.COMPLEX);
        this.loop = loop;
        procedures = new ArrayList<>();
    }

    @Override
    public void run() {
        if (offset > 0) {
            try {
                Thread.sleep((long) (this.offset * 1000));
            } catch (InterruptedException e) {
                throw new IllegalStateException("procedure interrupted while waiting the offset " + offset, e);
            }
        }
        for (int i = 1; i <= this.loop; i++) {
            logger.info("Executing procedure : " + i + " / " + this.loop);
            this.executeCommand();
        }
    }

    @Override
    public void executeCommand() {
        for (BaseProcedure procedure : procedures) {
            procedure.run();
        }
    }

    public ComplexProcedure addCommand(BaseProcedure procedure) {
        procedures.add(procedure);
        return this;
    }

    @Override
    public boolean needsLoadGenerator() {
        return procedures.stream().anyMatch(BaseProcedure::needsLoadGenerator);
    }

    @Override
    public boolean needsPersistentStorage() {
        return procedures.stream().anyMatch(BaseProcedure::needsPersistentStorage);
    }
}
