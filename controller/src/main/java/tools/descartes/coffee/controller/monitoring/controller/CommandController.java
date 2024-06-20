package tools.descartes.coffee.controller.monitoring.controller;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

import tools.descartes.coffee.controller.utils.EnumUtils;
import org.springframework.web.bind.annotation.*;

import tools.descartes.coffee.controller.monitoring.database.command.CommandExecutionService;
import tools.descartes.coffee.controller.monitoring.database.models.CommandExecutionTime;
import tools.descartes.coffee.controller.procedure.collection.Command;

@RestController
@RequestMapping("/command")
public class CommandController {
    private final Logger logger = Logger.getLogger(this.getClass().getName());

    private final CommandExecutionService commandExecutionService;

    public CommandController(CommandExecutionService commandExecutionService) {
        this.commandExecutionService = commandExecutionService;
    }

    /**
     * queue that holds the command timestamps and waits for the corresponding
     * execution times
     */
    public HashMap<Command, ConcurrentLinkedQueue<Timestamp>> commandQueue = new HashMap<>() {
        {
            put(Command.START, new ConcurrentLinkedQueue<>());
            put(Command.RESTART, new ConcurrentLinkedQueue<>());
            put(Command.HEALTH, new ConcurrentLinkedQueue<>());
            put(Command.CRASH, new ConcurrentLinkedQueue<>());

            put(Command.UPDATE, new ConcurrentLinkedQueue<>());

            put(Command.REMOVE, new ConcurrentLinkedQueue<>());
            put(Command.NETWORK, new ConcurrentLinkedQueue<>());
            put(Command.STORAGE, new ConcurrentLinkedQueue<>());
        }
    };

    /**
     * stores the timestamps when current containers are shut down or the new ones
     * are stared up separately. Because of their async execution it is not stated
     * if a container is shut down first or if the new one is stared up first.
     */
    public ConcurrentLinkedQueue<Timestamp> updateShutDownQueue = new ConcurrentLinkedQueue<>();
    public ConcurrentLinkedQueue<Timestamp> updateStartUpQueue = new ConcurrentLinkedQueue<>();

    public void addUpdateShutDownTimestamp(Timestamp shutDownTime) {
        this.updateShutDownQueue.add(shutDownTime);
    }

    public void addUpdateStartUpTimestamp(Timestamp startUpTime) {
        this.updateStartUpQueue.add(startUpTime);
    }

    @PostMapping("/start")
    public void addStartCommand(@RequestBody CommandExecutionTime commandStartTime,
            @RequestParam("replicas") int replicas) {
        commandQueue.get(EnumUtils.searchEnum(Command.class, commandStartTime.getCommand()))
                .addAll(Collections.nCopies(replicas, commandStartTime.getCommandTime()));
        logger.info("Added " + replicas + " command start times to " + commandStartTime.getCommand() + " queue");
    }

    @PostMapping("/end-network")
    public void storeNetworkingCommand(@RequestBody Timestamp networkingFinished) {
        Timestamp networkCommandStartTime = this.commandQueue.get(Command.NETWORK).poll();

        if (networkCommandStartTime == null) {
            throw new IllegalStateException(
                    "Error while storing in-cluster networking command finished time: No network command start time available.");

        }

        CommandExecutionTime executionTime = new CommandExecutionTime(Command.NETWORK, networkCommandStartTime,
                networkingFinished);
        commandExecutionService.add(executionTime);
    }

    @PostMapping("/end-storage")
    public void storeStorageCommand(@RequestBody Timestamp storageFinished) {
        Timestamp storageCommandStartTime = this.commandQueue.get(Command.STORAGE).poll();

        if (storageCommandStartTime == null) {
            throw new IllegalStateException(
                    "Error while storing storage command finished time: No storage command start time available.");

        }

        CommandExecutionTime executionTime = new CommandExecutionTime(Command.STORAGE, storageCommandStartTime,
                storageFinished);
        commandExecutionService.add(executionTime);
    }

    @PostMapping("/start-load")
    public void storeLoadCommand(@RequestBody Timestamp startLoadGeneration) {

        logger.info("storing load start time");

        CommandExecutionTime executionTime = new CommandExecutionTime(Command.LOAD, startLoadGeneration, startLoadGeneration);
        commandExecutionService.add(executionTime);
    }
}
