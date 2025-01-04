package tools.descartes.coffee.controller.monitoring.database.restart.manual;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import tools.descartes.coffee.controller.monitoring.database.GenericDatabaseService;
import tools.descartes.coffee.controller.monitoring.database.models.ManualRestartTime;

@Service
public class ManualRestartService implements GenericDatabaseService<ManualRestartTime> {

    private final ManualRestartRepo manualRestartRepo;

    public ManualRestartService(ManualRestartRepo manualRestartRepo) {
        this.manualRestartRepo = manualRestartRepo;
    }

    @Override
    public List<ManualRestartTime> findAll() {
        var it = manualRestartRepo.findAll();

        var times = new ArrayList<ManualRestartTime>();
        it.forEach(times::add);

        return times;
    }

    @Override
    public void add(ManualRestartTime time) {
        manualRestartRepo.save(time);
    }

    @Override
    public Long count() {
        return manualRestartRepo.count();
    }

}
