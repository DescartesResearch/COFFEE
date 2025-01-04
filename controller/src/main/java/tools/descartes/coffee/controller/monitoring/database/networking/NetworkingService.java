package tools.descartes.coffee.controller.monitoring.database.networking;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import tools.descartes.coffee.controller.monitoring.database.GenericDatabaseService;
import tools.descartes.coffee.controller.monitoring.database.models.NetworkTime;

@Service
public class NetworkingService implements GenericDatabaseService<NetworkTime> {

    private final NetworkingRepo networkingRepo;

    public NetworkingService(NetworkingRepo networkingRepo) {
        this.networkingRepo = networkingRepo;
    }

    @Override
    public List<NetworkTime> findAll() {
        var it = networkingRepo.findAll();

        var times = new ArrayList<NetworkTime>();
        it.forEach(times::add);

        return times;
    }

    @Override
    public void add(NetworkTime time) {
        networkingRepo.save(time);
    }

    @Override
    public Long count() {

        return networkingRepo.count();
    }
}
