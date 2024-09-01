package org.cb2384.exactalgebra.objects.internalaccess.factory;

import java.util.Objects;

import org.cb2384.exactalgebra.objects.AlgebraObject;

import org.checkerframework.dataflow.qual.*;

/**
 * A parameter, for use in factories.
 *
 * @param <R>   the type that the parameter actually outputs
 * @param <S>   the type family that the parameter outputs
 * @param <T>   the primary input type of the parameter
 *
 * @author  Corinne Buxton
 */
public interface Parameter<R extends S, S extends AlgebraObject<S>, T>
        extends AlgebraObject<Parameter<?, S, T>> {
    
    /**
     * standardized exception message, so as to not have to retype it
     */
    String EMPTY_STATE_EXC = "No Parameters given; cannot build!";
    
    /**
     * Access the value stored within this Parameter
     *
     * @return  the value that this Parameter represents
     */
    T value();
    
    /**
     * Resets this Parameter to have the new value given.
     *
     * @implSpec    This shall be equivalent to replacing this with a new Parameter constructed
     *              from {@code value}
     *
     * @param value the new value for this Parameter to represent
     */
    void reset(T value);
    
    /**
     * negate the value of this Parameter
     */
    void negate();
    
    /**
     * Converts the original Parameter value into the appropriate {@link AlgebraObject} subtype
     *
     * @return  an AlgebraObject subtype representing this Parameter's value
     */
    R asAlgebraObject();
    
    /**
     * Essentially a modified {@link Objects#requireNonNull(Object, String)} but with
     * a constant string rather than having to re-enter it each time.
     *
     * @param input the input that shouldn't be {@code null}
     *
     * @return  {@code input}
     *
     * @param <T>   the type of the input and return value
     */
    @Pure
    static <T> T confirmNonNull(
            T input
    ) {
        return Objects.requireNonNull(input, "Null input not allowed in Parameter!");
    }
}
