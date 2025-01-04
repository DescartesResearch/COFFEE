package tools.descartes.coffee.controllertests;

import org.junit.Assert;
import org.junit.Test;
import tools.descartes.coffee.controller.monitoring.controller.restarts.HealthController;
import tools.descartes.coffee.controller.monitoring.database.models.HealthRestartTime;
import tools.descartes.coffee.controllertests.helpers.GenericDatabaseMock;

import java.sql.Timestamp;
import java.util.Random;

public class TestHealthController {

    @Test
    public void test01checkOneCycle() {
        HealthController ac = new HealthController(null);
        GenericDatabaseMock<HealthRestartTime> db = new GenericDatabaseMock<>();
        ac.setHealthService(db);
        ac.addCheckingTimestamp(new Timestamp(5));
        ac.addUnhealthyTimestamp(new Timestamp(3));
        ac.addHealthShutDownTime(new Timestamp(4));
        ac.storeHealthRestartTime(new Timestamp(1), new Timestamp(6));
        Assert.assertEquals(1, db.getAddCounter());
    }

    @Test
    public void test02checkOneIncompleteUnhealthy() {
        HealthController ac = new HealthController(null);
        GenericDatabaseMock<HealthRestartTime> db = new GenericDatabaseMock<>();
        ac.setHealthService(db);
        ac.addCheckingTimestamp(new Timestamp(5));
        ac.addHealthShutDownTime(new Timestamp(4));
        Assert.assertThrows(IllegalStateException.class, () -> ac.storeHealthRestartTime(new Timestamp(1), new Timestamp(6)));
    }

    @Test
    public void test03checkOneIncompleteChecking() {
        HealthController ac = new HealthController(null);
        GenericDatabaseMock<HealthRestartTime> db = new GenericDatabaseMock<>();
        ac.setHealthService(db);
        ac.addUnhealthyTimestamp(new Timestamp(3));
        ac.addHealthShutDownTime(new Timestamp(4));
        Assert.assertThrows(IllegalStateException.class, () -> ac.storeHealthRestartTime(new Timestamp(1), new Timestamp(6)));
    }

    @Test
    public void test04checkOneIncompleteShutdown() {
        HealthController ac = new HealthController(null);
        GenericDatabaseMock<HealthRestartTime> db = new GenericDatabaseMock<>();
        ac.setHealthService(db);
        ac.addCheckingTimestamp(new Timestamp(5));
        ac.addUnhealthyTimestamp(new Timestamp(3));
        Assert.assertThrows(IllegalStateException.class, () -> ac.storeHealthRestartTime(new Timestamp(1), new Timestamp(6)));
    }

    @Test
    public void test05checkMultipleCrashs() {
        HealthController ac = new HealthController(null);
        GenericDatabaseMock<HealthRestartTime> db = new GenericDatabaseMock<>();
        ac.setHealthService(db);
        for (int i = 1; i <= 5; i++) {
            ac.addCheckingTimestamp(new Timestamp(5 * i));
            ac.addUnhealthyTimestamp(new Timestamp(3 * i));
            ac.addHealthShutDownTime(new Timestamp(4 * i));
            ac.storeHealthRestartTime(new Timestamp(i), new Timestamp(6 * i));
            Assert.assertEquals(i, db.getAddCounter());
        }
    }

    @Test
    public void test06randomOrderOperation1() {
        testRandomOrder(69);
    }

    @Test
    public void test07randomOrderOperation2() {
        testRandomOrder(420);
    }

    @Test
    public void test08randomOrderOperation3() {
        testRandomOrder(8808);
    }

    private void testRandomOrder(long seed) {
        HealthController ac = new HealthController(null);
        GenericDatabaseMock<HealthRestartTime> db = new GenericDatabaseMock<>();
        ac.setHealthService(db);
        Random random = new Random(seed);
        int unhealthy = 0;
        int shutdowns = 0;
        int checks = 0;
        int adds = 0;
        for (int i = 1; i <= 200; i++) {
            int r = random.nextInt(4);
            if (r == 0) {
                ac.addHealthShutDownTime(new Timestamp(i * 2));
                shutdowns++;
            } else if (r == 1) {
                ac.addUnhealthyTimestamp(new Timestamp(i * 3));
                unhealthy++;
            } else if (r == 2) {
                ac.addCheckingTimestamp(new Timestamp(i * 4));
                checks++;
            } else {
                if (unhealthy > 0 && shutdowns > 0 && checks > 0) {
                    adds++;
                    unhealthy--;
                    shutdowns--;
                    checks--;
                    ac.storeHealthRestartTime(new Timestamp(i), new Timestamp(4 * i));
                    Assert.assertEquals(adds, db.getAddCounter());
                } else {
                    unhealthy = Math.max(0, unhealthy-1);
                    shutdowns = Math.max(0, shutdowns-1);
                    checks = Math.max(0, checks-1);
                    Assert.assertThrows(IllegalStateException.class, () -> ac.storeHealthRestartTime(new Timestamp(1), new Timestamp(4)));
                }
            }
        }
    }
}
