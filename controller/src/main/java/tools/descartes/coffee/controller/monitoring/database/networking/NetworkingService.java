package tools.descartes.coffee.controller.monitoring.database.networking;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import tools.descartes.coffee.controller.monitoring.database.models.NetworkTime;

@Service
public class NetworkingService {

    private final NetworkingRepo networkingRepo;

    public NetworkingService(NetworkingRepo networkingRepo) {
        this.networkingRepo = networkingRepo;
    }

    public List<NetworkTime> findAll() {
        var it = networkingRepo.findAll();

        var times = new ArrayList<NetworkTime>();
        it.forEach(times::add);

        return times;
    }

    public void add(NetworkTime time) {
        networkingRepo.save(time);
    }

    public Long count() {

        return networkingRepo.count();
    }

    public void deleteById(int userId) {

        networkingRepo.deleteById(userId);
    }

}
