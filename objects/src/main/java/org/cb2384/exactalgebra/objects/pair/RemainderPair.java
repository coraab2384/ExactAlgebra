package org.cb2384.exactalgebra.objects.pair;

import org.cb2384.exactalgebra.objects.AlgebraObject;

import org.checkerframework.dataflow.qual.*;

/**
 * <p>Overarching interface for pair types. A pair stores a main answer and some remainder.
 * The primary type, the value, is obtained through {@link #value()} and the remainder through {@link #remainder()}.
 * The {@link AlgebraObject} methods are tied more to the value than the remainder.</p>
 *
 * <p>This interface is designed to be implemented by records, which is why there are so many default methods.
 * It is being used like an abstract superclass, but all records already are subclasses of {@link Record}.
 * It is not, strictly speaking, necessary that they be records though.</p>
 *
 * <p>Throws:&ensp;{@link NullPointerException} &ndash; on any {@code null} input, unless otherwise noted</p>
 *
 * @param <Q>   the type of the primary value, the one from {@link #value()}
 * @param <R>   the type of the remainder value, from {@link #remainder()}
 * @param <T>   the overarching type of {@link AlgebraObject} (to ensure that value and remainder are compatible)
 * @param <S>   the implementation of this interface (serves the same purpose as the type parameter of
 *              {@link Comparable})
 *
 * @author  Corinne Buxton
 */
public interface RemainderPair<Q extends AlgebraObject<T>, R extends AlgebraObject<T>,
                T extends AlgebraObject<T>, S extends RemainderPair<?, ?, T, S>>
        extends AlgebraObject<S> {
    
    /**
     * Get the primary answer from this pair. For a {@code quotientZWithRemainder} function, for example, the value
     * would be the quotient.
     *
     * @implSpec    The answer should already be known, so it should be an immutable reference that never changes
     *              over the lifetime of the object.
     *
     * @return  the primary answer, the value
     */
    @Pure
    Q value();
    
    /**
     * Get the remainder from this pair. For a {@code quotientZWithRemainder} function, for example, the remainder
     * is, well, the remainder.
     *
     * @implSpec    The answer should already be known, so it should be an immutable reference that never changes
     *              over the lifetime of the object.
     *
     * @return  the remainder, the secondary answer
     */
    @Pure
    R remainder();
    
    /**
     * Checks if the remainder is {@code 0} (or that one could say there is no remainder).
     *
     * @implNote    The default implementation is just {@link #remainder()}{@link AlgebraObject#isZero() .isZero()}.
     *
     * @return  {@code true} if {@link #remainder()}<code> == 0</code>, otherwise {@code false}
     */
    @Pure
    default boolean isExact() {
        return remainder().isZero();
    }
    
    /**
     * For a RemainderPair, to be 0 means that both value and remainder are 0
     *
     * @implNote    The default implementation is just {@link #value()}{@link AlgebraObject#isZero()
     *              .isZero()&nbsp;}{@code && }{@link #remainder()}{@link
     *              AlgebraObject#isZero() .isZero()}.
     *
     * @return  {@code true} if this is 0, otherwise {@code false}
     */
    @Override
    @Pure
    default boolean isZero() {
        return value().isZero() && remainder().isZero();
    }
    
    /**
     * For a RemainderPair, to be 1 means that both value is 1 and the remainder 0
     *
     * @implNote    The default implementation is just {@link #value()}{@link AlgebraObject#isOne()
     *              .isOne()&nbsp;}{@code && }{@link #remainder()}{@link
     *              AlgebraObject#isZero() .isZero()}.
     *
     * @return  {@code true} if {@link #value()}<code> == 1</code>, and {@link #remainder()}<code> == 0</code>,
     *          otherwise {@code false}
     */
    @Override
    @Pure
    default boolean isOne() {
        return value().isOne() && remainder().isZero();
    }
    
    /**
     * For a RemainderPair, to be negative means that either the value is negative, or that the value is 0 and#
     * the remainder is negative.
     *
     * @implNote    The default implementation is {@link #value()}{@link AlgebraObject#isNegative()
     *              .isNegative()&nbsp;}{@code || (}{@link #value()}{@link AlgebraObject#isZero()
     *              .isZero&nbsp;}{@code && }{@link #remainder()}{@link AlgebraObject#isNegative()
     *              .isNegative()}{@code )}.
     *
     * @return  {@code true} if this is negative, otherwise {@code false}
     */
    @Override
    @Pure
    default boolean isNegative() {
        return value().isNegative() || (value().isZero() && remainder().isNegative());
    }
}
