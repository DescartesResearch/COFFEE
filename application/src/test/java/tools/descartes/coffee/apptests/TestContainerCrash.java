package tools.descartes.coffee.apptests;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import tools.descartes.coffee.application.AppController;
import tools.descartes.coffee.application.LoadCounter;
import tools.descartes.coffee.apptests.helpers.NoExitSecurityManager;
import tools.descartes.coffee.apptests.helpers.TelemetrySenderMock;

public class TestContainerCrash {

    @Before
    public void setUp() {
        System.setSecurityManager(new NoExitSecurityManager());
    }

    @After
    public void tearDown() {
        System.setSecurityManager(new SecurityManager());
    }

    @Test
    public void test01containerCrashes() {
        AppController ac = new AppController(new LoadCounter());
        TelemetrySenderMock sender = new TelemetrySenderMock();
        ac.setTelemetrySender(sender);
        Assert.assertThrows(RuntimeException.class, ac::crashContainer);
        Assert.assertEquals(1, sender.getCurrentTimestampCounter());
        Assert.assertEquals(0, sender.getContainerTimestampCounter());
        Assert.assertEquals(0, sender.getReceivedRequestsCounter());
    }
}
