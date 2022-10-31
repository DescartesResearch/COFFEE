package tools.descartes.coffee.controller.orchestrator.kubernetes;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import tools.descartes.coffee.controller.orchestrator.kubernetes.commands.KubeRemoveContainer;
import tools.descartes.coffee.controller.orchestrator.kubernetes.commands.KubeRestartContainer;
import tools.descartes.coffee.controller.orchestrator.kubernetes.commands.KubeStartContainer;
import tools.descartes.coffee.controller.orchestrator.kubernetes.commands.KubeUpdateContainer;
import tools.descartes.coffee.controller.procedure.BaseProcedure;
import tools.descartes.coffee.controller.procedure.SimpleProcedure;
import tools.descartes.coffee.controller.procedure.collection.Command;
import tools.descartes.coffee.controller.procedure.collection.CommandClassMap;

public class KubeClassMap extends CommandClassMap {

    protected static Map<Command, Class<? extends SimpleProcedure>> COMMAND_MAP = new HashMap<>() {
        {
            put(Command.START, KubeStartContainer.class);
            put(Command.RESTART, KubeRestartContainer.class);
            put(Command.REMOVE, KubeRemoveContainer.class);
            put(Command.UPDATE, KubeUpdateContainer.class);
        }
    };

    public static Map<Command, Class<? extends BaseProcedure>> MAP = Stream
            .of(DEFAULT_MAP, COMMAND_MAP).flatMap(m -> m.entrySet().stream())
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

}
