package tools.descartes.coffee.controller.procedure;

import tools.descartes.coffee.controller.procedure.collection.Command;

import java.util.logging.Logger;

public abstract class BaseProcedure implements Runnable {
    protected static final Logger logger = Logger.getLogger(BaseProcedure.class.getName());

    /** offset in seconds */
    protected double offset;
    protected final Command type;

    public BaseProcedure(Command type) {
        this.type = type;
        offset = 0.0;
    }

    public double getOffset() {
        return offset;
    }

    public void setOffset(double offset) {
        this.offset = offset;
    }

    public void run() {
        if (offset > 0) {
            try {
                Thread.sleep((long) (this.offset * 1000));
            } catch (InterruptedException e) {
                throw new IllegalStateException("procedure interrupted while waiting the offset " + offset, e);
            }
        }
        executeCommand();
    }

    public abstract void executeCommand();

    public boolean needsLoadGenerator() {
        return false;
    }

    public boolean needsPersistentStorage() {
        return false;
    }
}
