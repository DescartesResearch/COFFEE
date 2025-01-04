package tools.descartes.coffee.controllertests;

import org.junit.Assert;
import org.junit.Test;
import tools.descartes.coffee.controller.monitoring.controller.restarts.AppController;
import tools.descartes.coffee.controller.monitoring.database.models.AppCrashRestartTime;
import tools.descartes.coffee.controllertests.helpers.GenericDatabaseMock;

import java.sql.Timestamp;
import java.util.Random;

public class TestAppController {

    @Test
    public void test01checkOneCrash() {
        AppController ac = new AppController(null);
        GenericDatabaseMock<AppCrashRestartTime> db = new GenericDatabaseMock<>();
        ac.setAppService(db);
        ac.addCrashShutDownTime(new Timestamp(2));
        ac.addUnhealthyTimestamp(new Timestamp(3));
        ac.storeAppCrashRestartTime(new Timestamp(1), new Timestamp(4));
        Assert.assertEquals(1, db.getAddCounter());
    }

    @Test
    public void test02checkOneIncompleteUnhealthy() {
        AppController ac = new AppController(null);
        GenericDatabaseMock<AppCrashRestartTime> db = new GenericDatabaseMock<>();
        ac.setAppService(db);
        ac.addCrashShutDownTime(new Timestamp(2));
        Assert.assertThrows(IllegalStateException.class, () -> ac.storeAppCrashRestartTime(new Timestamp(1), new Timestamp(4)));
    }

    @Test
    public void test03checkMultipleCrashs() {
        AppController ac = new AppController(null);
        GenericDatabaseMock<AppCrashRestartTime> db = new GenericDatabaseMock<>();
        ac.setAppService(db);
        for (int i = 1; i <= 5; i++) {
            ac.addCrashShutDownTime(new Timestamp(i * 2));
            ac.addUnhealthyTimestamp(new Timestamp(i * 3));
            ac.storeAppCrashRestartTime(new Timestamp(i), new Timestamp(4 * i));
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
        AppController ac = new AppController(null);
        GenericDatabaseMock<AppCrashRestartTime> db = new GenericDatabaseMock<>();
        ac.setAppService(db);
        Random random = new Random(seed);
        int unhealthy = 0;
        int adds = 0;
        for (int i = 1; i <= 100; i++) {
            int r = random.nextInt(3);
            if (r == 0) {
                ac.addCrashShutDownTime(new Timestamp(i * 2));
            } else if (r == 1) {
                ac.addUnhealthyTimestamp(new Timestamp(i * 3));
                unhealthy++;
            } else {
                if (unhealthy > 0) {
                    adds++;
                    unhealthy--;
                    ac.storeAppCrashRestartTime(new Timestamp(i), new Timestamp(4 * i));
                    Assert.assertEquals(adds, db.getAddCounter());
                } else {
                    Assert.assertThrows(IllegalStateException.class, () -> ac.storeAppCrashRestartTime(new Timestamp(1), new Timestamp(4)));
                }
            }
        }
    }
}
