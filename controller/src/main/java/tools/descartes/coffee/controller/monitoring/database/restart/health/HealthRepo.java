package tools.descartes.coffee.controller.monitoring.database.restart.health;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import tools.descartes.coffee.controller.monitoring.database.models.HealthRestartTime;

@Repository
public interface HealthRepo extends CrudRepository<HealthRestartTime, Long> {

}
