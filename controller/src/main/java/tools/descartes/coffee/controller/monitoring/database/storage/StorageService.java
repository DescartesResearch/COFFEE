package tools.descartes.coffee.controller.monitoring.database.storage;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import tools.descartes.coffee.controller.monitoring.database.models.StorageTime;

@Service
public class StorageService {

    private final StorageRepo storageRepo;

    public StorageService(StorageRepo storageRepo) {
        this.storageRepo = storageRepo;
    }

    public List<StorageTime> findAll() {
        var it = storageRepo.findAll();

        ArrayList<StorageTime> times = new ArrayList<>();
        it.forEach(times::add);

        return times;
    }

    public void add(StorageTime time) {
        storageRepo.save(time);
    }

    public Long count() {

        return storageRepo.count();
    }

    public void deleteById(int userId) {

        storageRepo.deleteById(userId);
    }

}
