package tools.descartes.coffee.controller.monitoring.database.loaddist;

import tools.descartes.coffee.controller.monitoring.database.models.LoadDistribution;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class LoadDistributionService {
    private final LoadDistributionRepo loadDistributionRepo;

    public LoadDistributionService(LoadDistributionRepo loadDistributionRepo) {
        this.loadDistributionRepo = loadDistributionRepo;
    }

    public List<LoadDistribution> findAll() {
        var it = loadDistributionRepo.findAll();

        var times = new ArrayList<LoadDistribution>();
        it.forEach(times::add);

        return times;
    }

    public void add(LoadDistribution time) {
        loadDistributionRepo.save(time);
    }

    public Long count() {
        return loadDistributionRepo.count();
    }

    public void deleteById(long userId) {
        loadDistributionRepo.deleteById(userId);
    }
}
