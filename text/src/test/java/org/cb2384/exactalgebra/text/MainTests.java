package org.cb2384.exactalgebra.text;

import java.util.stream.Stream;

import org.cb2384.exactalgebra.text.parse.Interfacer;
import org.junit.jupiter.api.Test;

public final class MainTests {
    
    @Test
    public void testInit() {
        Main.runLoop(new Interfacer(
                System.out::println,
                () -> "\\quit"
        ));
    }
}
