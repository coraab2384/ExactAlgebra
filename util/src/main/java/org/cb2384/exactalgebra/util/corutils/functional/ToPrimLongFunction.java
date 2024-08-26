package org.cb2384.exactalgebra.util.corutils.functional;

import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

/**
 * Extends {@link ToLongFunction} to also be able to output {@code int}s, {@code short}s, and {@code byte}s.
 * The functional method is still {@link ToLongFunction#applyAsLong(Object) applyAsLong}.
 *
 * @param   <T> the input type
 *
 * @author  Corinne Buxton
 */
@FunctionalInterface
public interface ToPrimLongFunction<T>
        extends ToLongFunction<T> {
    
    /**
     * Narrows the result of {@link #applyAsLong} to an {@code int}.
     * However, y'know that {@link ToIntFunction} has as its default functional method
     * {@link ToIntFunction#applyAsInt}, making this fairly pointless unless there are times the value
     * could be a {@code long}, and other situations where it is wanted as an {@code int}.
     *
     * @param   value   the functional input
     *
     * @return  the functional output, narrowed to an {@code int}
     */
    default int applyAsInt(
            T value
    ) {
        return (int) applyAsLong(value);
    }
    
    /**
     * Narrows the result of {@link #applyAsLong} to a {@code short}.
     *
     * @param   value   the functional input
     *
     * @return  the functional output, but narrowed to a {@code short}
     */
    default short applyAsShort(
            T value
    ) {
        return (short) applyAsLong(value);
    }
    
    /**
     * Narrows the result of {@link #applyAsLong} to a {@code byte}.
     *
     * @param   value   the functional input
     *
     * @return  the functional output, but narrowed to a {@code byte}
     */
    default byte applyAsByte(
            T value
    ) {
        return (byte) applyAsLong(value);
    }
}
