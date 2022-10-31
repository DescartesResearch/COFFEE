package tools.descartes.coffee.controller.monitoring.database.loaddist;

import tools.descartes.coffee.controller.monitoring.database.models.LoadDistribution;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoadDistributionRepo extends CrudRepository<LoadDistribution, Long> {
}
