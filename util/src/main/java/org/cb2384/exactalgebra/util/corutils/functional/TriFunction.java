package org.cb2384.exactalgebra.util.corutils.functional;

import java.util.function.Function;

import org.checkerframework.checker.nullness.qual.*;

/**
 * Function of 3 arguments as input and one output.
 * The functional method is {@link #apply(Object, Object, Object)}.
 *
 * @param   <T> the first argument type
 *
 * @param   <U> the second argument type
 *
 * @param   <V> the third argument type
 *
 * @param   <R> the result type
 *
 * @author  Corinne Buxton
 */
@FunctionalInterface
public interface TriFunction<T, U, V, R> {
    
    /**
     * Applies this function with the given arguments
     *
     * @param   t   the first argument
     *
     * @param   u   the second argument
     *
     * @param   v   the third argument
     *
     * @return  whatever this function returns
     */
    R apply(T t, U u, V v);
    
    /**
     * Composes this function with {@code after}.
     * The new function would do this function, and then map its result using {@code after}.
     *
     * @param   after   the function that maps the result of this function to the output of the result function
     *
     * @return  a new {@link TriFunction} that takes the result of the first {@link #apply}, and then applies that
     *          as the input for {@code after}
     *
     * @param   <S> the result type of {@code after} and of the returned {@code TriFunction}
     *
     * @throws  NullPointerException    if {@code after == null}
     */
    default <S> @NonNull TriFunction<T, U, V, S> andThen(
            @NonNull Function<? super R, ? extends S> after
    ) {
        return (t, u, v) -> after.apply(apply(t, u, v));
    }
}
