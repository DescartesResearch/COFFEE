package tools.descartes.coffee.controller.monitoring.database.restart.app;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import tools.descartes.coffee.controller.monitoring.database.models.AppCrashRestartTime;

@Repository
public interface AppRepo extends CrudRepository<AppCrashRestartTime, Long> {

}
