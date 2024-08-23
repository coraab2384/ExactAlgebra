package org.cb2384.exactalgebra.util;

import java.lang.reflect.Type;
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

public class MiscUtils {
    /**
     * Thrown to indicate that the requested return value does not fit within the return type
     */
    public static class DisallowedNarrowingException
            extends ArithmeticException {
        /**
         * Creates a new DisallowedNarrowingException
         * @param sourceType the Type of the source
         * @param targetType the Type of the target
         */
        @SideEffectFree
        public DisallowedNarrowingException(
                Type sourceType,
                Type targetType
        ) {
            super( getMsg(sourceType, targetType) );
        }
        
        /**
         * Returns an error message for a NullElementException
         */
        @SideEffectFree
        private static String getMsg(
                Type sourceType,
                Type targetType
        ) {
            String sourceName = StringUtils.getIdealName(sourceType);
            String targetName = StringUtils.getIdealName(targetType);
            
            return "This " + sourceName + " cannot be represented within the bounds of"
                    + StringUtils.aOrAnPrepend(targetName, true);
        }
        
        /**
         * Gets the simple name of a Type if it is a class,
         * or else the normal name if the simple name is empty,
         * or else the type name if it is not a class
         */
        @SideEffectFree
        private static String getTypeName(
                Type type
        ) {
            String name;
            if (type instanceof Class<?> typeClass) {
                name = typeClass.getSimpleName();
                if (name.isEmpty()) {
                    name = typeClass.getName();
                }
            } else {
                name = type.getTypeName();
            }
            return name;
        }
    }
    
    @SideEffectFree
    public static <T> Stream<T> streamOf(
            T first,
            T... next
    ) {
        return Stream.concat(Stream.of(first), Arrays.stream(next));
    }
    
    @SideEffectFree
    public static <T> Spliterator<T> spliteratorOrderedSizedNonnullImmutableSubsizedIterator(
            Iterator<T> iterator,
            long size
    ) {
        return Spliterators.spliterator(iterator, size, Spliterator.ORDERED | Spliterator.SIZED
                | Spliterator.NONNULL | Spliterator.IMMUTABLE | Spliterator.SUBSIZED);
    }
    
    @Pure
    public static void checkRadix(
            int radix
    ) {
        if ((radix < Character.MIN_RADIX) || (radix > Character.MAX_RADIX)) {
            throw new IllegalArgumentException("Improper radix");
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
