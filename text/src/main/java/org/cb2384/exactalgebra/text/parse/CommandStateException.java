package org.cb2384.exactalgebra.text.parse;

import java.io.Serial;

public final class CommandStateException
        extends IllegalStateException {
    
    @Serial
    private static final long serialVersionUID = 0xD40B3F91F2A46598L;
    
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
