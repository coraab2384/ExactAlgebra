package org.cb2384.exactalgebra.objects.pair;

import org.cb2384.exactalgebra.objects.relations.AlgebraFunction;
import org.cb2384.exactalgebra.objects.relations.polynomial.Polynomial;

import org.checkerframework.checker.nullness.qual.*;
import org.checkerframework.common.value.qual.*;
import org.checkerframework.dataflow.qual.*;

/**
 * <p>A {@link Record} implementation of {@link RemainderPair} that is specialized specifically for
 * {@link AlgebraFunction}. This specialization makes some of the type-checking less cumbersome.</p>
 *
 * <p>Throws:&ensp;{@link NullPointerException} &ndash; on any {@code null} input, unless otherwise noted</p>
 *
 * @param value     the primary answer, usually a quotient or root
 * @param remainder the secondary answer, usually an actual remainder from a division or root operation
 *
 * @param <Q>   the type of the quotient, which much be a subtype of {@link Polynomial}
 * @param <R>   the type of the remainder, which much be a subtype of {@link Polynomial}
 */
public record FunctionRemainderPair<Q extends Polynomial<?>, R extends Polynomial<?>>(Q value, R remainder)
        implements RemainderPair<Q, R, Polynomial<?>, FunctionRemainderPair<?, ?>> {
    
    /**
     * <p>Creates the {@link RemainderPair} given the value component as well as the original value
     * and the inversion of the value without the remainder. From these two, the remainder can
     * be constructed.</p>
     *
     * <p>For example, to find the quotient and remainder for {@code 17 / 3}, the value part would be
     * 5, since {@code 17 / 3 = 5} using floor or integral division. Then {@code original = 17},
     * {@code floorAnswer = 5}, and {@code reverseAnswer = 15}, since {@code 5 * 3 = 15}.
     * The remainder is obtained via subtraction, so it would be {@code 2}.</p>
     *
     * @param original      the original value; the dividend in division, or the number being rooted in a root
     * @param floorAnswer   the value component, called {@code floorAnswer} because in division it is the quotient
     *                      rounded towards 0
     * @param reverseAnswer the inverse if one were to use {@code floorAnswer} instead of the exact value
     *
     * @throws ClassCastException if {@code R} is not closed under subtraction
     *                            ({@link Polynomial#difference})
     */
    @SideEffectFree @SuppressWarnings("unchecked")
    public FunctionRemainderPair(
            R original,
            Q floorAnswer,
            R reverseAnswer
    ) {
        this(floorAnswer, (R) original.difference(reverseAnswer));
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Pure
    public boolean equiv(FunctionRemainderPair<?, ?> that) {
        return value.equiv(that.value) && remainder.equiv(that.remainder);
    }
    
    /**
     * {@inheritDoc}
     *
     * @throws NumberFormatException    if {@code radix < }{@link Character#MIN_RADIX} or
     *                                  {@code radix > }{@link Character#MAX_RADIX}
     */
    @Override
    @SideEffectFree
    public String toString() {
        return toString(10, (String[]) null);
    }
    
    /**
     * {@inheritDoc}
     *
     * @throws NumberFormatException    if {@code radix < }{@link Character#MIN_RADIX} or
     *                                  {@code radix > }{@link Character#MAX_RADIX}
     */
    @Override
    @SideEffectFree
    public String toString(
            @IntRange(from = Character.MIN_RADIX, to = Character.MAX_RADIX) int radix
    ) {
        return toString(radix, (String[]) null);
    }
    
    /**
     * Represents this FunctionRemainderPair as a string, with the given {@code radix} and
     * {@code variables}.
     *
     * @param radix     the radix for the representation
     * @param variables the variable(s) for this representation
     *
     * @return  a string representation of this
     *
     * @throws NumberFormatException    if {@code radix < }{@link Character#MIN_RADIX} or
     *                                  {@code radix > }{@link Character#MAX_RADIX}
     */
    @Override
    @SideEffectFree
    public String toString(
            @IntRange(from = Character.MIN_RADIX, to = Character.MAX_RADIX) int radix,
            String @Nullable ... variables
    ) {
        return value.toString(radix, variables) + ", remainder " + remainder.toString(radix, variables);
    }
}
