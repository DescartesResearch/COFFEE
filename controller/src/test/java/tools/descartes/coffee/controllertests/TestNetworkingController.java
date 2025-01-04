package tools.descartes.coffee.controllertests;

import org.junit.Assert;
import org.junit.Test;
import tools.descartes.coffee.controller.monitoring.controller.NetworkingController;
import tools.descartes.coffee.controller.monitoring.database.models.NetworkTime;
import tools.descartes.coffee.controllertests.helpers.GenericDatabaseMock;
import tools.descartes.coffee.shared.NetworkingData;

public class TestNetworkingController {

    @Test
    public void test01NullDataHasNoEffect() {
        NetworkingController ac = new NetworkingController(null);
        GenericDatabaseMock<NetworkTime> db = new GenericDatabaseMock<>();
        ac.setNetworkingService(db);
        ac.storeNetworkingTimes(null);
        Assert.assertEquals(0, db.getAddCounter());
    }

    @Test
    public void test02checkOneCycle() {
        NetworkingController ac = new NetworkingController(null);
        GenericDatabaseMock<NetworkTime> db = new GenericDatabaseMock<>();
        ac.setNetworkingService(db);
        NetworkingData data = new NetworkingData();
        ac.storeNetworkingTimes(data);
        Assert.assertEquals(1, db.getAddCounter());
    }

    @Test
    public void test03checkMultipleCycle() {
        NetworkingController ac = new NetworkingController(null);
        GenericDatabaseMock<NetworkTime> db = new GenericDatabaseMock<>();
        ac.setNetworkingService(db);
        NetworkingData data = new NetworkingData();
        for (int i = 1; i <= 5; i++) {
            ac.storeNetworkingTimes(data);
            Assert.assertEquals(i, db.getAddCounter());
        }
    }
}
