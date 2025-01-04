package tools.descartes.coffee.controllertests;

import org.junit.Assert;
import org.junit.Test;
import tools.descartes.coffee.controller.monitoring.controller.CommandController;
import tools.descartes.coffee.controller.monitoring.database.models.CommandExecutionTime;
import tools.descartes.coffee.controller.procedure.collection.Command;
import tools.descartes.coffee.controllertests.helpers.GenericDatabaseMock;

import java.sql.Timestamp;
import java.util.Random;

public class TestCommandController {

    @Test
    public void test01StoreOneLoad() {
        CommandController ac = new CommandController(null);
        GenericDatabaseMock<CommandExecutionTime> db = new GenericDatabaseMock<>();
        ac.setCommandExecutionService(db);
        ac.storeLoadCommand(new Timestamp(1));
        Assert.assertEquals(1, db.getAddCounter());
    }

    @Test
    public void test02StoreMultiLoad() {
        CommandController ac = new CommandController(null);
        GenericDatabaseMock<CommandExecutionTime> db = new GenericDatabaseMock<>();
        ac.setCommandExecutionService(db);
        for (int i = 1; i <= 5; i++) {
            ac.storeLoadCommand(new Timestamp(1));
            Assert.assertEquals(i, db.getAddCounter());
        }
    }

    @Test
    public void test03StoreOneStorage() {
        CommandController ac = new CommandController(null);
        GenericDatabaseMock<CommandExecutionTime> db = new GenericDatabaseMock<>();
        ac.setCommandExecutionService(db);
        ac.commandQueue.get(Command.STORAGE).add(new Timestamp(1));
        ac.storeStorageCommand(new Timestamp(2));
        Assert.assertEquals(1, db.getAddCounter());
    }

    @Test
    public void test04StorageIncompleteThrowsError() {
        CommandController ac = new CommandController(null);
        GenericDatabaseMock<CommandExecutionTime> db = new GenericDatabaseMock<>();
        ac.setCommandExecutionService(db);
        Assert.assertThrows(IllegalStateException.class, () -> ac.storeStorageCommand(new Timestamp(2)));
    }

    @Test
    public void test05StoreMultiStorage() {
        CommandController ac = new CommandController(null);
        GenericDatabaseMock<CommandExecutionTime> db = new GenericDatabaseMock<>();
        ac.setCommandExecutionService(db);
        for (int i = 1; i <= 5; i++) {
            ac.commandQueue.get(Command.STORAGE).add(new Timestamp(1));
            ac.storeStorageCommand(new Timestamp(2));
            Assert.assertEquals(i, db.getAddCounter());
        }
    }

    @Test
    public void test06RandomStorageOperations1() {
        randomStorageOperations(69);
    }

    @Test
    public void test07RandomStorageOperations2() {
        randomStorageOperations(420);
    }

    @Test
    public void test08RandomStorageOperations3() {
        randomStorageOperations(8808);
    }

    private void randomStorageOperations(long seed) {
        CommandController ac = new CommandController(null);
        GenericDatabaseMock<CommandExecutionTime> db = new GenericDatabaseMock<>();
        ac.setCommandExecutionService(db);
        Random random = new Random(seed);
        int len = 0;
        int added = 0;
        for (int i = 1; i <= 100; i++) {
            int r = random.nextInt(2);
            if (r == 0) {
                ac.commandQueue.get(Command.STORAGE).add(new Timestamp(1));
                len++;
            } else {
                if (len > 0) {
                    added++;
                    len--;
                    ac.storeStorageCommand(new Timestamp(2));
                    Assert.assertEquals(added, db.getAddCounter());
                } else {
                    Assert.assertThrows(IllegalStateException.class, () -> ac.storeStorageCommand(new Timestamp(2)));
                }
            }
        }
    }

    @Test
    public void test09StoreOneNetwork() {
        CommandController ac = new CommandController(null);
        GenericDatabaseMock<CommandExecutionTime> db = new GenericDatabaseMock<>();
        ac.setCommandExecutionService(db);
        ac.commandQueue.get(Command.NETWORK).add(new Timestamp(1));
        ac.storeNetworkingCommand(new Timestamp(2));
        Assert.assertEquals(1, db.getAddCounter());
    }

    @Test
    public void test10NetworkIncompleteThrowsError() {
        CommandController ac = new CommandController(null);
        GenericDatabaseMock<CommandExecutionTime> db = new GenericDatabaseMock<>();
        ac.setCommandExecutionService(db);
        Assert.assertThrows(IllegalStateException.class, () -> ac.storeNetworkingCommand(new Timestamp(2)));
    }

    @Test
    public void test11StoreMultiNetwork() {
        CommandController ac = new CommandController(null);
        GenericDatabaseMock<CommandExecutionTime> db = new GenericDatabaseMock<>();
        ac.setCommandExecutionService(db);
        for (int i = 1; i <= 5; i++) {
            ac.commandQueue.get(Command.NETWORK).add(new Timestamp(1));
            ac.storeNetworkingCommand(new Timestamp(2));
            Assert.assertEquals(i, db.getAddCounter());
        }
    }

    @Test
    public void test12RandomNetworkOperations1() {
        randomNetworkOperations(69);
    }

    @Test
    public void test13RandomNetworkOperations2() {
        randomNetworkOperations(420);
    }

    @Test
    public void test14RandomNetworkOperations3() {
        randomNetworkOperations(8808);
    }

    private void randomNetworkOperations(long seed) {
        CommandController ac = new CommandController(null);
        GenericDatabaseMock<CommandExecutionTime> db = new GenericDatabaseMock<>();
        ac.setCommandExecutionService(db);
        Random random = new Random(seed);
        int len = 0;
        int added = 0;
        for (int i = 1; i <= 100; i++) {
            int r = random.nextInt(2);
            if (r == 0) {
                ac.commandQueue.get(Command.NETWORK).add(new Timestamp(1));
                len++;
            } else {
                if (len > 0) {
                    added++;
                    len--;
                    ac.storeNetworkingCommand(new Timestamp(2));
                    Assert.assertEquals(added, db.getAddCounter());
                } else {
                    Assert.assertThrows(IllegalStateException.class, () -> ac.storeNetworkingCommand(new Timestamp(2)));
                }
            }
        }
    }
}
