package org.cb2384.exactalgebra.util.corutils.functional;

import java.util.function.Function;

import org.checkerframework.checker.nullness.qual.*;

/**
 * Function of a reference argument ({@link Object} subclass) followed by an {@code int} as input,
 * and a reference output.
 * The functional method is {@link #apply(Object, int)}.
 *
 * @param   <I> the first input reference argument type
 *
 * @param   <O> the output type
 *
 * @author  Corinne Buxton
 */
@FunctionalInterface
public interface ObjectThenIntToObjectFunction<I, O> {
    
    /**
     * Applies this function with the given arguments
     *
     * @param   object  the first argument, which is a reference argument ({@link Object}-subclass)
     *
     * @param   integer the second argument, which is of type {@code int} (or smaller type with implicit
     *                  widening conversion to {@code int}
     *
     * @return  whatever this function returns
     */
    O apply(I object, int integer);
    
    /**
     * Composes this function with {@code after}.
     * The new function would do this function, and then map its result using {@code after}.
     *
     * @param   after   the function that maps the result of this function to the output of the result function
     *
     * @return  a new {@link ObjectThenIntToObjectFunction} that takes the result of the first {@link #apply},
     *          and then applies that as the input for {@code after}
     *
     * @param   <R> the result type of {@code after} and of the returned {@code ObjectThenIntToObjectFunction}
     *
     * @throws  NullPointerException    if {@code after == null}
     */
    default <R> @NonNull ObjectThenIntToObjectFunction<I, R> andThen(
            @NonNull Function<? super O, ? extends R> after
    ) {
        return (object, integer) -> after.apply(apply(object, integer));
    }
}
