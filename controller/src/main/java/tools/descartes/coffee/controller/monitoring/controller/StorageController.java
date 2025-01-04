package tools.descartes.coffee.controller.monitoring.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import tools.descartes.coffee.controller.monitoring.database.GenericDatabaseService;
import tools.descartes.coffee.controller.monitoring.database.storage.StorageService;
import tools.descartes.coffee.controller.monitoring.database.models.StorageTime;
import tools.descartes.coffee.shared.StorageData;

import java.util.logging.Logger;

@RestController
@RequestMapping("/storage")
public class StorageController {
    private final Logger logger = Logger.getLogger(this.getClass().getName());
    private GenericDatabaseService<StorageTime> storageService;

    public StorageController(StorageService storageService) {
        this.storageService = storageService;
    }

    public void setStorageService(GenericDatabaseService<StorageTime> storageService) {
        this.storageService = storageService;
    }

    public void store(StorageTime storageTime) {
        this.storageService.add(storageTime);
    }

    @PostMapping("/store")
    public void storeStorageTimes(@RequestBody StorageData storageData) {
        if (storageData != null) {
            for (int i = 0; i < storageData.getWrittenBytes().length; i++) {
                StorageTime storageTime = new StorageTime(storageData.getWrittenBytes()[i],
                        storageData.getWriteTimeMillis()[i], storageData.getReadBytes()[i],
                        storageData.getReadTimeMillis()[i]);
                this.storageService.add(storageTime);
            }
        } else {
            logger.warning("null data given to storaStorageTimes");
        }
    }
}
