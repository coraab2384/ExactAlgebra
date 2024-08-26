package org.cb2384.exactalgebra.text.parse;

public final class CommandStateException
        extends IllegalStateException {
    
    /**
     * Constructs a {@code NumberFormatException} with no detail message.
     */
    public CommandStateException() {
        super();
    }
    
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
