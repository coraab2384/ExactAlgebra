package org.cb2384.exactalgebra.text;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Iterator;
import java.util.List;

import org.cb2384.exactalgebra.objects.numbers.integral.AlgebraInteger;
import org.cb2384.exactalgebra.objects.numbers.integral.FiniteInteger;
import org.cb2384.exactalgebra.text.parse.Interfacer;
import org.junit.jupiter.api.Test;

public final class MainTests {
    
    @Test
    public void testInit() {
        Main.runLoop(Interfacer.getInstance(
                System.out::println,
                () -> "\\quit",
                null
        ));
    }
    
    @Test
    public void testHandles() {
        MethodType type = MethodType.methodType(AlgebraInteger.class, AlgebraInteger.class);
        try {
            MethodHandle handle = MethodHandles.publicLookup().findVirtual(
                    AlgebraInteger.class,
                    "sum",
                    type
            );
            Object ans = handle.invoke(FiniteInteger.valueOf(1), FiniteInteger.valueOf(2));
            System.out.println(ans);
        } catch (Throwable passed) {
            throw new RuntimeException(passed);
        }
    }
    
    @Test
    public void testAdd() {
        String[] answerBox = new String[1];
        Iterator<String> supp = List.of("\\show{\\add{(1), (2)}}", "\\quit").iterator();
        Main.runLoop(Interfacer.getInstance(
                s -> answerBox[0] = s,
                supp::next,
                null
        ));
        assertEquals("3", answerBox[0]);
    }
}
