package org.cb2384.exactalgebra.util;

import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;


import org.cb2384.exactalgebra.util.corutils.StringUtils;

import org.checkerframework.dataflow.qual.*;

public final class MiscUtils {
    
    /**
     * This should never be called
     *
     * @throws  IllegalAccessException    always
     */
    private MiscUtils() throws IllegalAccessException {
        throw new IllegalAccessException("This should never be called" + StringUtils.INTERROBANG);
    }
    
    @SideEffectFree
    public static <T> Stream<T> streamOf(
            T first,
            T... next
    ) {
        return Stream.concat(Stream.of(first), Arrays.stream(next));
    }
    
    @SideEffectFree
    public static <T> Spliterator<T> spliteratorOrderedNonnullImmutableIterator(
            Iterator<T> iterator,
            long size
    ) {
        return Spliterators.spliterator(iterator, size, Spliterator.ORDERED
                | Spliterator.NONNULL | Spliterator.IMMUTABLE);
    }
    
    @Pure
    public static void checkRadix(
            int radix
    ) {
        if ((radix < Character.MIN_RADIX) || (radix > Character.MAX_RADIX)) {
            throw new NumberFormatException("Improper radix");
        }
    }
    
    @SideEffectFree
    public static String conformRadixToTen(
            String oldString,
            int radix
    ) {
        checkRadix(radix);
        Deque<BigInteger> parts = new ArrayDeque<>();
        Deque<String> misc = new ArrayDeque<>();
        Deque<Boolean> instr = new ArrayDeque<>();
        
        BigInteger counter = BigInteger.ZERO;
        StringBuilder othBuilder = new StringBuilder();
        boolean currentIsNumber = true;
        long place = 1;
        for (int i = oldString.length() - 1; i >= 0; i--) {
            char thisChar = oldString.charAt(i);
            int val = Character.digit(thisChar, radix);
            if (val == -1) {
                if (currentIsNumber) {
                    currentIsNumber = false;
                    parts.add(counter);
                    instr.add(Boolean.TRUE);
                    counter = BigInteger.ZERO;
                }
                othBuilder.insert(0, thisChar);
            } else {
                if (!currentIsNumber) {
                    currentIsNumber = true;
                    misc.add(othBuilder.toString());
                    instr.add(Boolean.FALSE);
                    othBuilder = new StringBuilder();
                }
                counter = counter.add(BigInteger.valueOf(val * place));
                place *= radix;
            }
        }
        
        StringBuilder ansBuilder = new StringBuilder();
        while (instr.peekLast() != null) {
            String part = instr.removeLast() ?
                    parts.removeLast().toString() :
                    misc.removeLast();
            ansBuilder.append(part);
        }
        
        return ansBuilder.toString();
    }
}
