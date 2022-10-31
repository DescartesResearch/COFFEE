package tools.descartes.coffee.controller.procedure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.logging.Logger;

import org.apache.commons.lang3.tuple.Pair;

import tools.descartes.coffee.controller.procedure.collection.Command;
import org.springframework.stereotype.Component;

/**
 * TBD: Timeouts ?
 */
@Component
public class ProcedureQueue {
    private static final Logger logger = Logger.getLogger(ProcedureQueue.class.getName());

    private final BlockingQueue<Pair<Command, CompletableFuture<?>>> containerStartQueue;
    private final BlockingQueue<CompletableFuture<?>> containerUpdateQueue;
    private final BlockingQueue<CompletableFuture<?>> containerRemoveQueue;

    private ProcedureQueue() {
        containerStartQueue = new LinkedBlockingDeque<>();
        containerUpdateQueue = new LinkedBlockingDeque<>();
        containerRemoveQueue = new LinkedBlockingDeque<>();
    }

    public Command peekNextStartCommand() {
        Pair<Command, CompletableFuture<?>> nextStartCommand = this.containerStartQueue.peek();

        if (nextStartCommand != null) {
            return nextStartCommand.getLeft();
        }

        return null;
    }

    public Command getNextStartCommand() {
        Pair<Command, CompletableFuture<?>> nextStartCommand = this.containerStartQueue.peek();

        if (nextStartCommand == null) {
            throw new IllegalStateException("Unexpected container start.");
        } else {
            return nextStartCommand.getLeft();
        }
    }

    public boolean isUpdateCommandActive() {
        return this.containerUpdateQueue.peek() != null;
    }

    public CompletableFuture<?>[] addToContainerStartQueue(Command command, int replicas) {
        CompletableFuture<?>[] futures = this.createFutures(replicas);

        logger.info("adding " + replicas + " futures to start queue");
        for (CompletableFuture<?> future : futures) {
            this.containerStartQueue.add(Pair.of(command, future));
        }
        return futures;
    }

    public CompletableFuture<?>[] addToContainerUpdateQueue(int replicas) {
        logger.info("adding " + replicas + " futures to update queue");
        CompletableFuture<?>[] futures = this.createFutures(replicas);
        this.containerUpdateQueue.addAll(Arrays.asList(futures));
        return futures;
    }

    public CompletableFuture<?>[] addToContainerRemoveQueue(int replicas) {
        logger.info("adding " + replicas + " futures to remove queue");
        CompletableFuture<?>[] futures = this.createFutures(replicas);
        this.containerRemoveQueue.addAll(Arrays.asList(futures));
        return futures;
    }

    public void waitForContainerStart(CompletableFuture<?>[] futures) {
        CompletableFuture.allOf(futures).join();
        logger.info("start queue finished; futures joined");
    }

    public void waitForContainerUpdate(CompletableFuture<?>[] futures) {
        CompletableFuture.allOf((futures)).join();
        logger.info("update queue finished; futures joined");
    }

    public void waitForContainerRemove(CompletableFuture<?>[] futures) {
        CompletableFuture.allOf(futures).join();
        logger.info("remove queue finished; futures joined");
    }

    public synchronized void onContainerStarted() {
        logger.info("polling next future from start queue ");
        Pair<Command, CompletableFuture<?>> nextCommand = this.containerStartQueue.poll();
        if (nextCommand == null) {
            throw new IllegalStateException("Unexpected container start.");
        }

        nextCommand.getRight().complete(null);
        logger.info("completed next future from start queue, remaining " + this.containerStartQueue.size());
    }

    public synchronized void onContainerUpdated() {
        CompletableFuture<?> nextCommand = this.containerUpdateQueue.poll();
        if (nextCommand == null) {
            throw new IllegalStateException("Unexpected container update.");
        }

        nextCommand.complete(null);
    }

    public synchronized void onContainerRemoved() {
        CompletableFuture<?> nextCommand = this.containerRemoveQueue.poll();
        if (nextCommand == null) {
            throw new IllegalStateException("Unexpected container removal.");
        }

        nextCommand.complete(null);
        logger.info("completed next future from remove queue, remaining " + this.containerRemoveQueue.size());
    }

    private CompletableFuture<?>[] createFutures(int replicas) {
        List<CompletableFuture<?>> futureList = new ArrayList<>();

        for (int i = 0; i < replicas; i++) {
            futureList.add(new CompletableFuture<>());
        }

        return futureList.toArray(new CompletableFuture[replicas]);
    }
}
