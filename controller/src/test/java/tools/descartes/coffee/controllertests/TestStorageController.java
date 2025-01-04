package tools.descartes.coffee.controllertests;

import org.junit.Assert;
import org.junit.Test;
import tools.descartes.coffee.controller.monitoring.controller.StorageController;
import tools.descartes.coffee.controller.monitoring.database.models.StorageTime;
import tools.descartes.coffee.controllertests.helpers.GenericDatabaseMock;
import tools.descartes.coffee.shared.StorageData;

import java.util.Random;

public class TestStorageController {

    @Test
    public void test01NullDataHasNoEffect() {
        StorageController ac = new StorageController(null);
        GenericDatabaseMock<StorageTime> db = new GenericDatabaseMock<>();
        ac.setStorageService(db);
        ac.storeStorageTimes(null);
        Assert.assertEquals(0, db.getAddCounter());
    }

    @Test
    public void test02OneEntry() {
        StorageController ac = new StorageController(null);
        GenericDatabaseMock<StorageTime> db = new GenericDatabaseMock<>();
        ac.setStorageService(db);
        long[] testData = new long[]{1};
        StorageData data = new StorageData(testData, testData, testData, testData);
        ac.storeStorageTimes(data);
        Assert.assertEquals(1, db.getAddCounter());
    }

    @Test
    public void test03RandomEntries1() {
        testRandomData(69);
    }

    @Test
    public void test04RandomEntries2() {
        testRandomData(420);
    }

    @Test
    public void test05RandomEntries3() {
        testRandomData(8808);
    }

    private void testRandomData(long seed) {
        StorageController ac = new StorageController(null);
        GenericDatabaseMock<StorageTime> db = new GenericDatabaseMock<>();
        ac.setStorageService(db);
        Random random = new Random(seed);
        int stored = 0;
        for (int i = 1; i <= 50; i++) {
            int len = 1 + random.nextInt(50);
            long[] a1 = random.longs(len, 1, 1000).toArray();
            long[] a2 = random.longs(len, 1, 1000).toArray();
            long[] a3 = random.longs(len, 1, 1000).toArray();
            long[] a4 = random.longs(len, 1, 1000).toArray();
            StorageData data = new StorageData(a1, a2, a3, a4);
            ac.storeStorageTimes(data);
            stored += len;
            Assert.assertEquals(stored, db.getAddCounter());
        }
    }
}
