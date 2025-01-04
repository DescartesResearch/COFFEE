package tools.descartes.coffee.controller.monitoring.database.storage;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import tools.descartes.coffee.controller.monitoring.database.GenericDatabaseService;
import tools.descartes.coffee.controller.monitoring.database.models.StorageTime;

@Service
public class StorageService implements GenericDatabaseService<StorageTime> {

    private final StorageRepo storageRepo;

    public StorageService(StorageRepo storageRepo) {
        this.storageRepo = storageRepo;
    }

    @Override
    public List<StorageTime> findAll() {
        var it = storageRepo.findAll();

        ArrayList<StorageTime> times = new ArrayList<>();
        it.forEach(times::add);

        return times;
    }

    @Override
    public void add(StorageTime time) {
        storageRepo.save(time);
    }

    @Override
    public Long count() {

        return storageRepo.count();
    }

}
