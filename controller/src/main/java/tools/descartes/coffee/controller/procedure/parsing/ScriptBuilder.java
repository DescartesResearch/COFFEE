package tools.descartes.coffee.controller.procedure.parsing;

import tools.descartes.coffee.controller.config.ClusterProperties;
import tools.descartes.coffee.controller.orchestrator.OrchestratorMap;
import tools.descartes.coffee.controller.orchestrator.Orchestrators;
import tools.descartes.coffee.controller.procedure.BaseProcedure;
import tools.descartes.coffee.controller.procedure.ComplexProcedure;
import tools.descartes.coffee.controller.procedure.collection.Command;
import tools.descartes.coffee.controller.utils.EnumUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class ScriptBuilder {
    private static final Logger logger = Logger.getLogger(ScriptBuilder.class.getName());

    private final ApplicationContext applicationContext;
    private Map<Command, Class<? extends BaseProcedure>> commandMap;

    public ScriptBuilder(ApplicationContext applicationContext, OrchestratorMap orchestratorMap,
                         ClusterProperties clusterProperties) {
        this.applicationContext = applicationContext;
        if (orchestratorMap != null && clusterProperties != null) {
            commandMap = orchestratorMap.getMap().get(EnumUtils.searchEnum(Orchestrators.class, clusterProperties.getOrchestrator()));
        }
    }

    public void setCommandMap(Map<Command, Class<? extends BaseProcedure>> commandMap) {
        this.commandMap = commandMap;
    }

    public BaseProcedure[] loadScript(String scriptPath) throws ScriptParsingException {
        Scanner sc;
        try {
            sc = new Scanner(new FileInputStream(scriptPath));
        } catch (IOException ioe) {
            logger.log(Level.SEVERE, "Error while reading script", ioe);
            throw new IllegalStateException("Error while reading script", ioe);
        }
        return parseScript(sc);
    }

    public BaseProcedure[] parseScript(Scanner sc) throws ScriptParsingException {
        List<BaseProcedure> procedures = new ArrayList<>();
        while (sc.hasNextLine()) {
            // remove leading and trailing whitespaces, normalize middle spaces
            String line = sc.nextLine().trim().replaceAll(" +", " ");
            if (line.length() == 0) continue;
            String[] args = line.split(" ");
            procedures.add(parseOffset(sc, args));
        }
        return procedures.toArray(new BaseProcedure[0]);
    }

    private BaseProcedure parseOffset(Scanner sc, String[] args) throws ScriptParsingException {
        double offset;
        int startIndex;
        if (args[0].equalsIgnoreCase("offset")) {
            offset = Double.parseDouble(args[1]);
            startIndex = 2;
        } else {
            offset = 0.0;
            startIndex = 0;
        }
        BaseProcedure result = parse(sc, args, startIndex);
        result.setOffset(offset);
        return result;
    }

    private BaseProcedure parse(Scanner sc, String[] args, int startIndex) throws ScriptParsingException {
        BaseProcedure result;
        switch (args[startIndex]) {
            case "seq":
                result = parseSequence(1, sc);
                break;
            case "loop":
                result = parseSequence(Integer.parseInt(args[startIndex+1]), sc);
                break;
            case "endseq":
                throw new ScriptParsingException("illegal usage of endseq");
            case "endloop":
                throw new ScriptParsingException("illegal usage of endloop");
            case "offset":
                throw new ScriptParsingException("illegal usage of offset");
            default:
                result = parsePrimitiveCommand(args, startIndex);
        }
        return result;
    }

    private BaseProcedure parseSequence(int loop, Scanner sc) throws ScriptParsingException {
        ComplexProcedure result = new ComplexProcedure(loop);
        String stopToken = loop == 1 ? "endseq" : "endloop";
        String[] line = sc.nextLine().trim().replaceAll(" +", " ").split(" ");
        while (!line[0].equals(stopToken)) {
            result.addCommand(parse(sc, line, 0));
            line = sc.nextLine().trim().replaceAll(" +", " ").split(" ");
        }
        return result;
    }

    private BaseProcedure parsePrimitiveCommand(String[] args, int startIndex) throws ScriptParsingException {
        BaseProcedure parsed;
        switch (args[startIndex].toLowerCase()) {
            case "start":
            case "restart":
            case "remove":
            case "health":
            case "crash":
            case "network":
            case "storage":
            case "update":
            case "delay":
            case "load":
            case "endload":
                try {
                    Constructor<?> constructor = commandMap.get(EnumUtils.searchEnum(Command.class, args[startIndex])).getConstructors()[0];
                    Class<?>[] requiredParams = constructor.getParameterTypes();
                    if (requiredParams.length > 0) {
                        Object[] params = new Object[requiredParams.length];
                        for (int i = 0; i < requiredParams.length; i++) {
                            if (requiredParams[i].isPrimitive()) {
                                params[i] = resolvePrimitiveConstructorParam(requiredParams[i], args[++startIndex]);
                            } else if (requiredParams[i].equals(String.class)) {
                                params[i] = args[++startIndex];
                            } else {
                                params[i] = applicationContext.getBean(requiredParams[i]);
                            }
                        }
                        parsed = (BaseProcedure) constructor.newInstance(params);
                    } else {
                        parsed = (BaseProcedure) constructor.newInstance();
                    }
                    break;
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException exception) {
                    throw new ScriptParsingException("Cannot create procedure command for input " + args[startIndex].toLowerCase(), exception);
                }
            default:
                throw new ScriptParsingException("Unknown command: " + args[startIndex]);
        }
        return parsed;
    }

    private Object resolvePrimitiveConstructorParam(Class<?> primitiveTargetClass, String valueAsString) throws ScriptParsingException {
        if (primitiveTargetClass.equals(Byte.TYPE)) {
            return Byte.parseByte(valueAsString);
        } else if (primitiveTargetClass.equals(Short.TYPE)) {
            return Short.parseShort(valueAsString);
        } else if (primitiveTargetClass.equals(Integer.TYPE)) {
            return Integer.parseInt(valueAsString);
        } else if (primitiveTargetClass.equals(Long.TYPE)) {
            return Long.parseLong(valueAsString);
        } else if (primitiveTargetClass.equals(Float.TYPE)) {
            return Float.parseFloat(valueAsString);
        } else if (primitiveTargetClass.equals(Double.TYPE)) {
            return Double.parseDouble(valueAsString);
        } else if (primitiveTargetClass.equals(Boolean.TYPE)) {
            return Boolean.parseBoolean(valueAsString);
        } else if (primitiveTargetClass.equals(Character.TYPE)) {
            if (valueAsString.length() == 1) {
                return valueAsString.charAt(0);
            } else {
                throw new ScriptParsingException("Expecting char but got string " + valueAsString);
            }
        } else {
            throw new IllegalStateException("Not a primitive type");
        }
    }
}
