package tools.descartes.coffee.controller.monitoring.database.restart.health;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import tools.descartes.coffee.controller.monitoring.database.GenericDatabaseService;
import tools.descartes.coffee.controller.monitoring.database.models.HealthRestartTime;

@Service
public class HealthService implements GenericDatabaseService<HealthRestartTime> {

    private final HealthRepo healthRepo;

    public HealthService(HealthRepo healthRepo) {
        this.healthRepo = healthRepo;
    }

    @Override
    public List<HealthRestartTime> findAll() {
        var it = healthRepo.findAll();

        var times = new ArrayList<HealthRestartTime>();
        it.forEach(times::add);

        return times;
    }

    @Override
    public void add(HealthRestartTime time) {
        healthRepo.save(time);
    }

    @Override
    public Long count() {
        return healthRepo.count();
    }

}
