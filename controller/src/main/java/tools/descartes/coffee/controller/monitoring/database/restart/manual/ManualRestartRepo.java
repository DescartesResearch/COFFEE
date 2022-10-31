package tools.descartes.coffee.controller.monitoring.database.restart.manual;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import tools.descartes.coffee.controller.monitoring.database.models.ManualRestartTime;

@Repository
public interface ManualRestartRepo extends CrudRepository<ManualRestartTime, Long> {

}
