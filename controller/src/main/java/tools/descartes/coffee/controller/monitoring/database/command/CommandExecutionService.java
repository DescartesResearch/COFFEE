package tools.descartes.coffee.controller.monitoring.database.command;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import tools.descartes.coffee.controller.monitoring.database.GenericDatabaseService;
import tools.descartes.coffee.controller.monitoring.database.models.CommandExecutionTime;

@Service
public class CommandExecutionService implements GenericDatabaseService<CommandExecutionTime> {

    private final CommandExecutionRepo commandExecutionRepo;

    public CommandExecutionService(CommandExecutionRepo commandExecutionRepo) {
        this.commandExecutionRepo = commandExecutionRepo;
    }

    @Override
    public List<CommandExecutionTime> findAll() {
        var it = commandExecutionRepo.findAll();

        var times = new ArrayList<CommandExecutionTime>();
        it.forEach(times::add);

        return times;
    }

    @Override
    public void add(CommandExecutionTime time) {
        commandExecutionRepo.save(time);
    }

    @Override
    public Long count() {
        return commandExecutionRepo.count();
    }

}
