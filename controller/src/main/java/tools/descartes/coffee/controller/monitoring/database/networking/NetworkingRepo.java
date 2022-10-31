package tools.descartes.coffee.controller.monitoring.database.networking;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import tools.descartes.coffee.controller.monitoring.database.models.NetworkTime;

@Repository
public interface NetworkingRepo extends CrudRepository<NetworkTime, Integer> {

}
