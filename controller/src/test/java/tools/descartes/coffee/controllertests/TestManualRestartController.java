package tools.descartes.coffee.controllertests;

import org.junit.Assert;
import org.junit.Test;
import tools.descartes.coffee.controller.monitoring.controller.restarts.ManualRestartController;
import tools.descartes.coffee.controller.monitoring.database.models.ManualRestartTime;
import tools.descartes.coffee.controllertests.helpers.GenericDatabaseMock;

import java.sql.Timestamp;
import java.util.Random;

public class TestManualRestartController {
    @Test
    public void test01checkOneCycle() {
        ManualRestartController ac = new ManualRestartController(null);
        GenericDatabaseMock<ManualRestartTime> db = new GenericDatabaseMock<>();
        ac.setManualRestartService(db);
        ac.addCrashShutDownTime(new Timestamp(2));
        ac.storeAppCrashRestartTime(new Timestamp(1), new Timestamp(3));
        Assert.assertEquals(1, db.getAddCounter());
    }

    @Test
    public void test02checkOneIncompleteShutdown() {
        ManualRestartController ac = new ManualRestartController(null);
        GenericDatabaseMock<ManualRestartTime> db = new GenericDatabaseMock<>();
        ac.setManualRestartService(db);
        Assert.assertThrows(IllegalStateException.class, () -> ac.storeAppCrashRestartTime(new Timestamp(1), new Timestamp(3)));
    }

    @Test
    public void test03checkMultipleCycles() {
        ManualRestartController ac = new ManualRestartController(null);
        GenericDatabaseMock<ManualRestartTime> db = new GenericDatabaseMock<>();
        ac.setManualRestartService(db);
        for (int i = 1; i <= 5; i++) {
            ac.addCrashShutDownTime(new Timestamp(2 * i));
            ac.storeAppCrashRestartTime(new Timestamp(i), new Timestamp(3 * i));
            Assert.assertEquals(i, db.getAddCounter());
        }
    }

    @Test
    public void test04randomOrderOperation1() {
        testRandomOrder(69);
    }

    @Test
    public void test05randomOrderOperation2() {
        testRandomOrder(420);
    }

    @Test
    public void test06randomOrderOperation3() {
        testRandomOrder(8808);
    }

    private void testRandomOrder(long seed) {
        ManualRestartController ac = new ManualRestartController(null);
        GenericDatabaseMock<ManualRestartTime> db = new GenericDatabaseMock<>();
        ac.setManualRestartService(db);
        Random random = new Random(seed);
        int shutdowns = 0;
        int adds = 0;
        for (int i = 1; i <= 200; i++) {
            int r = random.nextInt(2);
            if (r == 0) {
                ac.addCrashShutDownTime(new Timestamp(i * 2));
                shutdowns++;
            } else {
                if (shutdowns > 0) {
                    adds++;
                    shutdowns--;
                    ac.storeAppCrashRestartTime(new Timestamp(i), new Timestamp(4 * i));
                    Assert.assertEquals(adds, db.getAddCounter());
                } else {
                    Assert.assertThrows(IllegalStateException.class, () -> ac.storeAppCrashRestartTime(new Timestamp(1), new Timestamp(4)));
                }
            }
        }
    }
}
