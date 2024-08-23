package org.cb2384.exactalgebra.util.corutils.functional;

import java.util.function.Function;

import org.checkerframework.checker.nullness.qual.*;

/**
 * Function of 3 int-valued arguments and one Object-type output.
 * The functional method is {@link #apply(int, int, int)}.
 *
 * @param   <R> result type
 *
 * @author  Corinne Buxton
 */
@FunctionalInterface
public interface TripleIntToObjFunction<R> {
    
    /**
     * Applies this function with the given arguments
     *
     * @param   a   the first argument
     *
     * @param   b   the second argument
     *
     * @param   c   the third argument
     *
     * @return  whatever this function returns
     */
    R apply(int a, int b, int c);
    
    /**
     * Composes this function with {@code after}.
     * The new function would do this function, and then map its result using {@code after}.
     *
     * @param   after   the function that maps the result of this function to the output of the result function
     *
     * @return  a new {@link TripleIntToObjFunction} that takes the result of the first {@link #apply},
     *          and then applies that as the input for {@code after}
     *
     * @param   <S> the result type of {@code after} and of the returned {@code TripleIntToObjFunction}
     *
     * @throws  NullPointerException    if {@code after == null}
     */
    default <S> @NonNull TripleIntToObjFunction<S> andThen(
            @NonNull Function<? super R, ? extends S> after
    ) {
        return (a, b, c) -> after.apply(apply(a, b, c));
    }
}
