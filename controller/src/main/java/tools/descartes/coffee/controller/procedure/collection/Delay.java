package tools.descartes.coffee.controller.procedure.collection;

import tools.descartes.coffee.controller.procedure.SimpleProcedure;

public class Delay extends SimpleProcedure {

    /** delay duration in s */
    private final long duration;

    public Delay(long duration) {
        super(Command.DELAY);
        this.duration = duration;
    }

    @Override
    public void executeCommand() {
        try {
            Thread.sleep(this.duration * 1000);
        } catch (InterruptedException e) {
            throw new IllegalStateException("procedure interrupted while delay of " + duration, e);
        }
    }
}
