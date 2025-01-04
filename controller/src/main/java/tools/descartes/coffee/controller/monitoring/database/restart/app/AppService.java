package tools.descartes.coffee.controller.monitoring.database.restart.app;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import tools.descartes.coffee.controller.monitoring.database.GenericDatabaseService;
import tools.descartes.coffee.controller.monitoring.database.models.AppCrashRestartTime;

@Service
public class AppService implements GenericDatabaseService<AppCrashRestartTime> {

    private final AppRepo appRepo;

    public AppService(AppRepo appRepo) {
        this.appRepo = appRepo;
    }

    @Override
    public List<AppCrashRestartTime> findAll() {
        var it = appRepo.findAll();

        var times = new ArrayList<AppCrashRestartTime>();
        it.forEach(times::add);

        return times;
    }

    @Override
    public void add(AppCrashRestartTime time) {
        appRepo.save(time);
    }

    @Override
    public Long count() {
        return appRepo.count();
    }

}
