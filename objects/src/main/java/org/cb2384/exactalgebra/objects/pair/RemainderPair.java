package org.cb2384.exactalgebra.objects.pair;

import org.cb2384.exactalgebra.objects.AlgebraObject;

import org.checkerframework.dataflow.qual.*;

public sealed interface RemainderPair<Q extends AlgebraObject<T>, R extends AlgebraObject<T>,
                T extends AlgebraObject<T>, S extends RemainderPair<?, ?, T, S>>
        extends AlgebraObject<S>
        permits NumberRemainderPair, FunctionRemainderPair {
    
    @SideEffectFree
    Q value();
    
    @SideEffectFree
    R remainder();
    
    /**
     * Checks if the remainder is {@code 0}
     *
     * @return  the {@code boolean} value of {@link #remainder remainder&nbsp;}{@code == 0}
     */
    @Pure
    default boolean isExact() {
        return remainder().isZero();
    }
    
    /**
     * {@inheritDoc}
     *
     * @implNote    For a {@link RemainderPair}, being {@code 0} means that both {@link #value}
     *              and {@link #remainder} are {@code 0}.
     */
    @Override
    @Pure
    default boolean isZero() {
        return value().isZero() && remainder().isZero();
    }
    
    /**
     * {@inheritDoc}
     *
     * @implNote    For a {@link RemainderPair}, being {@code 1} means that {@link #value
     *              value&nbsp;}{@code == 1} and {@link #remainder remainder&nbsp;}{@code == 0}.
     */
    @Override
    @Pure
    default boolean isOne() {
        return value().isOne() && remainder().isZero();
    }
    
    /**
     * {@inheritDoc}
     *
     * @implNote    For a {@link RemainderPair}, being negative means that {@link #value}{@link
     *              R#isNegative() .isNegative()}.
     */
    @Override
    @Pure
    default boolean isNegative() {
        return value().isNegative();
    }
    
    /**
     * {@inheritDoc}
     *
     * @implNote    For a {@link RemainderPair}, equivalence means that the components of this
     *              and {@code that} are equivalent to each other.
     */
    @Override
    @Pure
    default boolean equiv(
            S that
    ) {
        return value().equiv((T) that.value()) && remainder().equiv((T) that.remainder());
    }
}
