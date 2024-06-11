package tools.descartes.coffee.controller.procedure.collection;

import java.util.HashMap;
import java.util.Map;

import tools.descartes.coffee.controller.procedure.BaseProcedure;
import tools.descartes.coffee.controller.procedure.collection.deployment.HealthCheck;
import tools.descartes.coffee.controller.procedure.collection.deployment.CrashApp;
import tools.descartes.coffee.controller.procedure.collection.load.GenerateLoad;
import tools.descartes.coffee.controller.procedure.collection.load.StopLoad;
import tools.descartes.coffee.controller.procedure.collection.networking.RequestNetwork;
import tools.descartes.coffee.controller.procedure.collection.storage.RequestStorage;

public abstract class CommandClassMap {

    protected static final Map<Command, Class<? extends BaseProcedure>> DEFAULT_MAP = new HashMap<>() {
        {
            put(Command.DELAY, Delay.class);
            put(Command.HEALTH, HealthCheck.class);
            put(Command.CRASH, CrashApp.class);
            put(Command.NETWORK, RequestNetwork.class);
            put(Command.STORAGE, RequestStorage.class);
            put(Command.LOAD, GenerateLoad.class);
            put(Command.ENDLOAD, StopLoad.class);
        }
    };
}
