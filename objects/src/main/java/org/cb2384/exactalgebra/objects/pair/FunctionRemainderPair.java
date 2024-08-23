package org.cb2384.exactalgebra.objects.pair;

import org.cb2384.exactalgebra.objects.RealField;
import org.cb2384.exactalgebra.objects.relations.polynomial.Polynomial;

import org.checkerframework.checker.nullness.qual.*;
import org.checkerframework.common.value.qual.*;
import org.checkerframework.dataflow.qual.*;

public record FunctionRemainderPair<Q extends R, R extends Polynomial<?>>(Q value, R remainder)
        implements RemainderPair<Q, R, Polynomial<?>, FunctionRemainderPair<Q, R>> {
    
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
     * @throws ClassCastException if {@link R} is not closed under subtraction
     *                            ({@link RealField#difference})
     */
    public FunctionRemainderPair(
            R original,
            Q floorAnswer,
            R reverseAnswer
    ) {
        this(floorAnswer, (R) original.difference(reverseAnswer));
    }
    
    @Override
    @SideEffectFree
    public String toString() {
        return toString(10, (String[]) null);
    }
    
    @Override
    @SideEffectFree
    public String toString(
            @IntRange(from = Character.MIN_RADIX, to = Character.MAX_RADIX) int radix
    ) {
        return toString(radix, (String[]) null);
    }
    
    @Override
    @SideEffectFree
    public String toString(
            @IntRange(from = Character.MIN_RADIX, to = Character.MAX_RADIX) int radix,
            String @Nullable ... variables
    ) {
        return value.toString(radix, variables) + ", remainder " + remainder.toString(radix, variables);
    }
}
