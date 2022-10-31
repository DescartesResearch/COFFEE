package tools.descartes.coffee.controller.monitoring.database.deployment;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import tools.descartes.coffee.controller.monitoring.database.models.UpdateRestartTime;

@Repository
public interface DeploymentRepo extends CrudRepository<UpdateRestartTime, Long> {

}
