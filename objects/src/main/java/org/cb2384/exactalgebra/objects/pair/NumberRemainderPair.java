package org.cb2384.exactalgebra.objects.pair;

import org.cb2384.exactalgebra.objects.numbers.AlgebraNumber;

import org.checkerframework.common.value.qual.*;
import org.checkerframework.dataflow.qual.*;

/**
 * <p>A {@link Record} implementation of {@link RemainderPair} that is specialized specifically for
 * {@link AlgebraNumber}. This specialization makes some of the type-checking less cumbersome.</p>
 *
 * <p>Throws:&ensp;{@link NullPointerException} &ndash; on any {@code null} input, unless otherwise noted</p>
 *
 * @param value     the primary answer, usually a quotient or root
 * @param remainder the secondary answer, usually an actual remainder from a division or root operation
 *
 * @param <Q>   the type of the quotient, which much be a subtype of {@link AlgebraNumber}
 * @param <R>   the type of the remainder, which much be a subtype of {@link AlgebraNumber}
 */
public record NumberRemainderPair<Q extends AlgebraNumber, R extends AlgebraNumber>(Q value, R remainder)
        implements RemainderPair<Q, R, AlgebraNumber, NumberRemainderPair<?, ?>> {
    
    /**
     * <p>Creates NumberRemainderPair given the value (primary answer) component as well as the original value
     * and the inversion of the value without the remainder. From these two, the remainder can
     * be found.</p>
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
     * @throws ClassCastException   if {@code R} is not closed under subtraction
     *                              ({@link AlgebraNumber#difference})
     */
    @SideEffectFree @SuppressWarnings("unchecked")
    public NumberRemainderPair(
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
    public boolean equiv(
            NumberRemainderPair<?, ?> that
    ) {
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
        return toString(10);
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
        return value.toString(radix) + ", remainder " + remainder.toString(radix);
    }
}
