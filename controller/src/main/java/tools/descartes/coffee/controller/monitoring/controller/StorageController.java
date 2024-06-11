package tools.descartes.coffee.controller.monitoring.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import tools.descartes.coffee.controller.monitoring.database.storage.StorageService;
import tools.descartes.coffee.controller.monitoring.database.models.StorageTime;
import tools.descartes.coffee.shared.StorageData;

@RestController
@RequestMapping("/storage")
public class StorageController {

    private final StorageService storageService;

    public StorageController(StorageSerivce storageSerivce) {
        this.storageService = storageSerivce;
    }

    public void store(StorageTime storageTime) {
        this.storageService.add(storageTime);
    }

    @PostMapping("/store")
    public void storeStorageTimes(@RequestBody StorageData storageData) {
        for (int i = 0; i < storageData.getWrittenBytes().length; i++) {
            StorageTime storageTime = new StorageTime(storageData.getWrittenBytes()[i],
                    storageData.getWriteTimeMillis()[i], storageData.getReadBytes()[i],
                    storageData.getReadTimeMillis()[i]);
            this.storageService.add(storageTime);
        }
    }
}
