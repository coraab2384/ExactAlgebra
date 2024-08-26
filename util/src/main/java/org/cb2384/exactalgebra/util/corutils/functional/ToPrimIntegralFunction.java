package org.cb2384.exactalgebra.util.corutils.functional;

import java.util.function.ToIntFunction;

/**
 * Extends {@link ToIntFunction} to also be able to output {@code short}s and {@code byte}s.
 * The functional method is still {@link ToIntFunction#applyAsInt(Object) applyAsInt}.
 *
 * @param   <T> the input type
 *
 * @author  Corinne Buxton
 */
@FunctionalInterface
public interface ToPrimIntegralFunction<T>
        extends ToIntFunction<T> {
    
    /**
     * Narrows the result of {@link #applyAsInt} to a {@code short}.
     *
     * @param   value   the functional input
     *
     * @return  the functional output, but narrowed to a {@code short}
     */
    default short applyAsShort(
            T value
    ) {
        return (short) applyAsInt(value);
    }
    
    /**
     * Narrows the result of {@link #applyAsInt} to a {@code byte}.
     *
     * @param   value   the functional input
     *
     * @return  the functional output, but narrowed to a {@code byte}
     */
    default byte applyAsByte(
            T value
    ) {
        return (byte) applyAsInt(value);
    }
}
