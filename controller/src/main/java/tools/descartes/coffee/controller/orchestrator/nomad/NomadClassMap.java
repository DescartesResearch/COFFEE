package tools.descartes.coffee.controller.orchestrator.nomad;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import tools.descartes.coffee.controller.orchestrator.nomad.commands.NomadRemoveContainer;
import tools.descartes.coffee.controller.orchestrator.nomad.commands.NomadRestartContainer;
import tools.descartes.coffee.controller.orchestrator.nomad.commands.NomadStartContainer;
import tools.descartes.coffee.controller.orchestrator.nomad.commands.NomadUpdateContainer;
import tools.descartes.coffee.controller.procedure.BaseProcedure;
import tools.descartes.coffee.controller.procedure.SimpleProcedure;
import tools.descartes.coffee.controller.procedure.collection.Command;
import tools.descartes.coffee.controller.procedure.collection.CommandClassMap;

public class NomadClassMap extends CommandClassMap {

    protected static Map<Command, Class<? extends SimpleProcedure>> COMMAND_MAP = new HashMap<>() {
        {
            put(Command.START, NomadStartContainer.class);
            put(Command.RESTART, NomadRestartContainer.class);
            put(Command.REMOVE, NomadRemoveContainer.class);
            put(Command.UPDATE, NomadUpdateContainer.class);
        }
    };

    public static Map<Command, Class<? extends BaseProcedure>> MAP = Stream
            .of(DEFAULT_MAP, COMMAND_MAP).flatMap(m -> m.entrySet().stream())
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

}
