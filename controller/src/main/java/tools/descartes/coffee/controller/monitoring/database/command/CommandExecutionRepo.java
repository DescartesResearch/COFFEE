package tools.descartes.coffee.controller.monitoring.database.command;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import tools.descartes.coffee.controller.monitoring.database.models.CommandExecutionTime;

@Repository
public interface CommandExecutionRepo extends CrudRepository<CommandExecutionTime, Long> {

}
