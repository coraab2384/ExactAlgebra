package org.cb2384.exactalgebra.text.parse;

/**
 * <p>Indicates that the program is not currently in a state compatible with the given command.
 * For example, a command referencing a last answer being used as the first command.</p>
 *
 * @author Corinne Buxton
 */
public final class CommandStateException
        extends IllegalStateException {
    
    /**
     * Constructs a {@code NumberFormatException} with no detail message.
     */
    public CommandStateException() {}
    
    /**
     * Constructs a {@code NumberFormatException} with the
     * specified detail message.
     *
     * @param   string  the detail message.
     */
    public CommandStateException(
            String string
    ) {
        super(string);
    }
    
    static CommandStateException nameUnavailable(
            String name
    ) {
        return new CommandStateException("Name " + name
                + " is already in use, and overwriting is not indicated as being allowed!");
    }
}
