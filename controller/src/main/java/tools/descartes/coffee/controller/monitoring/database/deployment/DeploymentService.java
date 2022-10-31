package tools.descartes.coffee.controller.monitoring.database.deployment;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import tools.descartes.coffee.controller.monitoring.database.models.UpdateRestartTime;

@Service
public class DeploymentService {

    private final DeploymentRepo deploymentRepo;

    public DeploymentService(DeploymentRepo deploymentRepo) {
        this.deploymentRepo = deploymentRepo;
    }

    public List<UpdateRestartTime> findAll() {
        var it = deploymentRepo.findAll();

        var times = new ArrayList<UpdateRestartTime>();
        it.forEach(times::add);

        return times;
    }

    public void add(UpdateRestartTime time) {
        deploymentRepo.save(time);
    }

    public Long count() {
        return deploymentRepo.count();
    }

    public void deleteById(Long userId) {
        deploymentRepo.deleteById(userId);
    }

}
