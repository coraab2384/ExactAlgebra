package org.cb2384.exactalgebra.text.parse;

/**
 * <p>Indicates a problem with the format of an inputted command</p>
 *
 * @author Corinne Buxton
 */
public final class CommandFormatException
        extends IllegalArgumentException {
    
    /**
     * Constructs a {@code NumberFormatException} with no detail message.
     */
    public CommandFormatException() {}
    
    /**
     * Constructs a {@code NumberFormatException} with the
     * specified detail message.
     *
     * @param   string  the detail message.
     */
    public CommandFormatException(
            String string
    ) {
        super(string);
    }
    
    static CommandFormatException forInputString(
            String string
    ) {
        return new CommandFormatException("Command: \"" + string + "\""
                + "is not formatted properly");
    }
    
    static CommandFormatException forInputStringWithCause(
            String string,
            Throwable cause
    ) {
        CommandFormatException exc = forInputString(string);
        return (CommandFormatException) exc.initCause(cause);
    }
    
    static CommandFormatException forInputStringWithFocus(
            String mainString,
            String focusString
    ) {
        return new CommandFormatException("Command: \"" + mainString + "\""
                + "is not formatted properly;\n" + focusString);
    }
}
