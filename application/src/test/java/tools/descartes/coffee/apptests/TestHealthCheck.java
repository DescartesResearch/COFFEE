package tools.descartes.coffee.apptests;

import org.junit.Assert;
import org.junit.Test;
import tools.descartes.coffee.application.AppController;
import tools.descartes.coffee.application.LoadCounter;
import tools.descartes.coffee.apptests.helpers.TelemetrySenderMock;

public class TestHealthCheck {
    @Test
    public void test01HealthDefaultTrue() {
        AppController ac = new AppController(new LoadCounter());
        TelemetrySenderMock sender = new TelemetrySenderMock();
        ac.setTelemetrySender(sender);
        Assert.assertEquals(Boolean.TRUE, ac.getHealth().getBody());
        Assert.assertEquals(Boolean.TRUE, ac.getHealth().getBody());
        Assert.assertEquals(0, sender.getCurrentTimestampCounter());
        Assert.assertEquals(0, sender.getContainerTimestampCounter());
        Assert.assertEquals(0, sender.getReceivedRequestsCounter());
    }

    @Test
    public void test02HealthChangesToFalseAfterUnhealthyCall() {
        AppController ac = new AppController(new LoadCounter());
        TelemetrySenderMock sender = new TelemetrySenderMock();
        ac.setTelemetrySender(sender);
        Assert.assertEquals(Boolean.TRUE, ac.getHealth().getBody());
        ac.setUnhealthy();
        Assert.assertEquals(400, ac.getHealth().getStatusCodeValue());
        Assert.assertEquals(2, sender.getCurrentTimestampCounter());
        Assert.assertEquals(0, sender.getContainerTimestampCounter());
        Assert.assertEquals(0, sender.getReceivedRequestsCounter());
    }

    @Test
    public void test03HealthChangesToFalseAfterUnhealthyGetCall() {
        AppController ac = new AppController(new LoadCounter());
        TelemetrySenderMock sender = new TelemetrySenderMock();
        ac.setTelemetrySender(sender);
        Assert.assertEquals(Boolean.TRUE, ac.getHealth().getBody());
        ac.setUnhealthyViaGet();
        Assert.assertEquals(400, ac.getHealth().getStatusCodeValue());
        Assert.assertEquals(2, sender.getCurrentTimestampCounter());
        Assert.assertEquals(0, sender.getContainerTimestampCounter());
        Assert.assertEquals(0, sender.getReceivedRequestsCounter());
    }

    @Test
    public void test04HealthStaysFalseAfterUnhealthyCall() {
        AppController ac = new AppController(new LoadCounter());
        TelemetrySenderMock sender = new TelemetrySenderMock();
        ac.setTelemetrySender(sender);
        Assert.assertEquals(Boolean.TRUE, ac.getHealth().getBody());
        ac.setUnhealthy();
        Assert.assertEquals(400, ac.getHealth().getStatusCodeValue());
        ac.setUnhealthy();
        Assert.assertEquals(400, ac.getHealth().getStatusCodeValue());
        Assert.assertEquals(3, sender.getCurrentTimestampCounter());
        Assert.assertEquals(0, sender.getContainerTimestampCounter());
        Assert.assertEquals(0, sender.getReceivedRequestsCounter());
    }

    @Test
    public void test05HealthStaysFalseAfterUnhealthyGetCall() {
        AppController ac = new AppController(new LoadCounter());
        TelemetrySenderMock sender = new TelemetrySenderMock();
        ac.setTelemetrySender(sender);
        Assert.assertEquals(Boolean.TRUE, ac.getHealth().getBody());
        ac.setUnhealthyViaGet();
        Assert.assertEquals(400, ac.getHealth().getStatusCodeValue());
        ac.setUnhealthyViaGet();
        Assert.assertEquals(400, ac.getHealth().getStatusCodeValue());
        Assert.assertEquals(3, sender.getCurrentTimestampCounter());
        Assert.assertEquals(0, sender.getContainerTimestampCounter());
        Assert.assertEquals(0, sender.getReceivedRequestsCounter());
    }

    @Test
    public void test06HealthStaysFalseAfterUnhealthyMixedCall1() {
        AppController ac = new AppController(new LoadCounter());
        TelemetrySenderMock sender = new TelemetrySenderMock();
        ac.setTelemetrySender(sender);
        Assert.assertEquals(Boolean.TRUE, ac.getHealth().getBody());
        ac.setUnhealthyViaGet();
        Assert.assertEquals(400, ac.getHealth().getStatusCodeValue());
        ac.setUnhealthy();
        Assert.assertEquals(400, ac.getHealth().getStatusCodeValue());
        Assert.assertEquals(3, sender.getCurrentTimestampCounter());
        Assert.assertEquals(0, sender.getContainerTimestampCounter());
        Assert.assertEquals(0, sender.getReceivedRequestsCounter());
    }

    @Test
    public void test07HealthStaysFalseAfterUnhealthyMixedCall2() {
        AppController ac = new AppController(new LoadCounter());
        TelemetrySenderMock sender = new TelemetrySenderMock();
        ac.setTelemetrySender(sender);
        Assert.assertEquals(Boolean.TRUE, ac.getHealth().getBody());
        ac.setUnhealthy();
        Assert.assertEquals(400, ac.getHealth().getStatusCodeValue());
        ac.setUnhealthyViaGet();
        Assert.assertEquals(400, ac.getHealth().getStatusCodeValue());
        Assert.assertEquals(3, sender.getCurrentTimestampCounter());
        Assert.assertEquals(0, sender.getContainerTimestampCounter());
        Assert.assertEquals(0, sender.getReceivedRequestsCounter());
    }
}
