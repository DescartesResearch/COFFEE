package tools.descartes.coffee.controller.monitoring.database.deployment;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import tools.descartes.coffee.controller.monitoring.database.GenericDatabaseService;
import tools.descartes.coffee.controller.monitoring.database.models.UpdateRestartTime;

@Service
public class DeploymentService implements GenericDatabaseService<UpdateRestartTime> {

    private final DeploymentRepo deploymentRepo;

    public DeploymentService(DeploymentRepo deploymentRepo) {
        this.deploymentRepo = deploymentRepo;
    }

    @Override
    public List<UpdateRestartTime> findAll() {
        var it = deploymentRepo.findAll();

        var times = new ArrayList<UpdateRestartTime>();
        it.forEach(times::add);

        return times;
    }

    @Override
    public void add(UpdateRestartTime time) {
        deploymentRepo.save(time);
    }

    @Override
    public Long count() {
        return deploymentRepo.count();
    }

}
