package tools.descartes.coffee.controller.procedure.parsing;

public class ScriptParsingException extends Exception {
    public ScriptParsingException(String message) {
        super(message);
    }

    public ScriptParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}
