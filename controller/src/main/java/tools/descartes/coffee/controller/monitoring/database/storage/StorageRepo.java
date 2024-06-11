package tools.descartes.coffee.controller.monitoring.database.storage;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import tools.descartes.coffee.controller.monitoring.database.models.StorageTime;

@Repository
public interface StorageRepo extends CrudRepository<StorageTime, Integer> {

}
