package tools.descartes.coffee.controller.monitoring.database.restart.health;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import tools.descartes.coffee.controller.monitoring.database.models.HealthRestartTime;

@Service
public class HealthService {

    private final HealthRepo healthRepo;

    public HealthService(HealthRepo healthRepo) {
        this.healthRepo = healthRepo;
    }

    public List<HealthRestartTime> findAll() {
        var it = healthRepo.findAll();

        var times = new ArrayList<HealthRestartTime>();
        it.forEach(times::add);

        return times;
    }

    public void add(HealthRestartTime time) {
        healthRepo.save(time);
    }

    public Long count() {
        return healthRepo.count();
    }

    public void deleteById(Long userId) {
        healthRepo.deleteById(userId);
    }

}
