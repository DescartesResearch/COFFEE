package tools.descartes.coffee.controllertests;

import org.junit.Assert;
import org.junit.Test;
import tools.descartes.coffee.controller.procedure.BaseProcedure;
import tools.descartes.coffee.controller.procedure.collection.Command;
import tools.descartes.coffee.controller.procedure.parsing.ScriptBuilder;
import tools.descartes.coffee.controller.procedure.parsing.ScriptParsingException;
import tools.descartes.coffee.controllertests.helpers.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class TestScriptBuilder {

    @Test
    public void test01SimpleScript() throws ScriptParsingException {
        String script = "OFFSET 0 START 5\n" +
                "OFFSET 30 CRASH 5\n" +
                "OFFSET 30 RESTART 5\n" +
                "OFFSET 30 UPDATE 5\n" +
                "OFFSET 60 LOAD\n" +
                "OFFSET 120 ENDLOAD\n" +
                "OFFSET 180 NETWORK\n" +
                "OFFSET 240 STORAGE\n" +
                "OFFSET 270 REMOVE 5\n";
        BaseProcedure[] expected = {new StartMock(), new CrashMock(), new RestartMock(), new UpdateMock(),
                new LoadMock(), new EndLoadMock(), new NetworkMock(), new StorageMock(), new RemoveMock()};
        BaseProcedure[] actual = testInputScript(script);
        Assert.assertEquals(expected.length, actual.length);
        for (int i = 0; i < expected.length; i++) {
            Assert.assertEquals(expected[i], actual[i]);
        }
    }

    private BaseProcedure[] testInputScript(String script) throws ScriptParsingException {
        Scanner sc = new Scanner(script);
        Map<Command, Class<? extends BaseProcedure>> mockCommandMap = new HashMap<>() {
            {
                put(Command.DELAY, DelayMock.class);
                put(Command.HEALTH, HealthMock.class);
                put(Command.CRASH, CrashMock.class);
                put(Command.NETWORK, NetworkMock.class);
                put(Command.STORAGE, StorageMock.class);
                put(Command.LOAD, LoadMock.class);
                put(Command.ENDLOAD, EndLoadMock.class);
                put(Command.UPDATE, UpdateMock.class);
                put(Command.REMOVE, RemoveMock.class);
                put(Command.RESTART, RestartMock.class);
                put(Command.START, StartMock.class);
            }
        };
        ScriptBuilder sb = new ScriptBuilder(null, null, null);
        sb.setCommandMap(mockCommandMap);
        return sb.parseScript(sc);
    }
}
