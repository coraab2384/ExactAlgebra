package org.cb2384.exactalgebra.util;

import java.math.BigInteger;
import java.util.Objects;
import java.util.function.LongPredicate;

import org.cb2384.exactalgebra.util.corutils.StringUtils;

import org.checkerframework.checker.index.qual.*;
import org.checkerframework.checker.nullness.qual.*;
import org.checkerframework.checker.signedness.qual.*;
import org.checkerframework.common.value.qual.*;
import org.checkerframework.dataflow.qual.*;

/**
 * Static utility methods for primitives. Methods that could have been put in {@link Math}, but aren't included.
 *
 * @author  Corinne Buxton
 */
public final class PrimMathUtils {
    
    private static final String BAD_GCF = "Undefined input pair: 0, 0";
    
    static final String BAD_LCM = "Disallowed input: 0";
    
    /**
     * This should never be called
     *
     * @throws  IllegalAccessException    always
     */
    private PrimMathUtils() throws IllegalAccessException {
        throw new IllegalAccessException("This should never be called" + StringUtils.INTERROBANG);
    }
    
    /**
     * {@code &}ing this with a {@code long} results in only the lower 32 bits; that is, the {@code int}.
     * For when one wants to discard the higher 32 bits.
     * This also happens to be the maximum allowed {@link Unsigned unsigned&nbsp}{@code int}
     * value's {@code long} representation
     */
    public static final long LONG_TO_INT_MASK = 0xFF_FF_FF_FFL;
    
    /**
     * {@code -}{@link Integer#MIN_VALUE} but as a {@code long}
     */
    public static final long NEG_INT_MIN = Integer.MAX_VALUE + 1L;
    
    /**
     * Used for checking containededness of a value within a certain (integral) type.
     */
    public enum IntegralBoundaryTypes {
        /**
         * Indicates a range from {@code -MAX_VALUE} to {@code MAX_VALUE}, ignoring the {@code MIN_VALUE}
         * being as it cannot be negated
         */
        SHORTENED,
        /**
         * Standard boundaries; {@code MIN_VALUE} to {@code MAX_VALUE}
         */
        DEFAULT,
        /**
         * Indicates a range from {@code MIN_VALUE} to {@code -MIN_VALUE}; since {@code -MIN_VALUE}
         * is normally not contained within any integral type, some other action will need to be taken
         * to deal with this, but there are situation where this is feasible
         */
        EXTENDED,
        /**
         * Indicates an {@link Unsigned} range, from {@code 0} to {@code MAX_VALUE - MIN_VALUE}
         * (ignoring overflow for the moment)
         */
        UNSIGNED;
        
        /**
         * Returns the same value, unless it was {@code null}, in which {@link #DEFAULT} is returned.
         *
         * @param   boundaryType the possibly-{@code null} value
         *
         * @return  a specifically non-{@code null} {@link IntegralBoundaryTypes IntegralBoundaryType}
         */
        public static IntegralBoundaryTypes defaultNull(
                @Nullable IntegralBoundaryTypes boundaryType
        ) {
            return Objects.requireNonNullElse(boundaryType, DEFAULT);
        }
        
        /**
         * Like {@link #defaultNull defaultNull(}{@code boundaryType}{@link #defaultNull )}{@link
         * #ordinal() .ordinal()} but better.
         *
         * @param   boundaryType    the possibly-{@code null} value to get the ordinal of
         *
         * @return  the ordinal of the input, or {@code 1} if {@code boundaryType == null}
         */
        static @IntRange(from = 0, to = 3) int ordinal(
                @Nullable IntegralBoundaryTypes boundaryType
        ) {
            return (boundaryType != null) ? boundaryType.ordinal() : 1;
        }
    }
    
    /**
     * Checks if the {@link Unsigned unsigned&nbsp}{@code long} can be a {@link Signed signed&nbsp}{@code long}.
     * The boundary behavior (whether to include or exclude {@link Long#MAX_VALUE}, the upper bound,
     * for example) is dictated by {@code type}.
     *
     * @param   value   the {@link Unsigned UNSIGNED&nbsp}{@code long} to check
     *
     * @param   type    the {@link IntegralBoundaryTypes IntegralBoundaryType} determining which type
     *                  of boundary to use when checking. As {@code value} is unsigned,
     *                  {@link IntegralBoundaryTypes#SHORTENED SHORTENED} and
     *                  {@link IntegralBoundaryTypes#DEFAULT DEFAULT} have no difference here.
     *                  {@link IntegralBoundaryTypes#UNSIGNED UNSIGNED} will always return {@code true}, and
     *                  {@code null} defaults to {@link IntegralBoundaryTypes#DEFAULT DEFAULT}.
     *
     * @return  {@code true} if {@code value} fits within a {@code long} under the given boundary condition,
     *          otherwise {@code false}
     */
    @Pure
    public static boolean canBeLongUnsigned(
            @Unsigned long value,
            @Nullable IntegralBoundaryTypes type
    ) {
        @Unsigned long[] lower = new long[4];
        
        @Unsigned long[] upper = new long[]{Long.MAX_VALUE,
                Long.MAX_VALUE, Long.MIN_VALUE, ~0L};
        
        return canBe(value, lower, upper, type, false);
    }
    
    /**
     * Checks if the {@code long} can be an {@code int}. The boundary behavior
     * (whether to include or exclude the upper bound or lower bound, or whether an unsigned {@code int} is desired,
     * for example) is dictated by {@code type}.
     *
     * @param   value   the {@code long} to check
     *
     * @param   type    the {@link IntegralBoundaryTypes IntegralBoundaryType} determining which type
     *                  of boundary to use when checking; {@code null} defaults to
     *                  {@link IntegralBoundaryTypes#DEFAULT DEFAULT}
     *
     * @return  {@code true} if {@code value} fits within an {@code int} under the given boundary condition,
     *          otherwise {@code false}
     */
    @Pure
    public static boolean canBeInt(
            long value,
            @Nullable IntegralBoundaryTypes type
    ) {
        long[] lower = new long[]{-Integer.MAX_VALUE,
                Integer.MIN_VALUE, Integer.MIN_VALUE, 0};
        
        long[] upper = new long[]{Integer.MAX_VALUE,
                Integer.MAX_VALUE, NEG_INT_MIN, LONG_TO_INT_MASK};
        
        return canBe(value, lower, upper, type, true);
    }
    
    /**
     * Checks if the {@link Unsigned unsigned&nbsp}{@code long} can be a {@link Signed signed&nbsp}{@code int}.
     * The boundary behavior (whether to include or exclude {@link Integer#MAX_VALUE}, the upper bound,
     * for example) is dictated by {@code type}.
     *
     * @param   value   the {@link Unsigned UNSIGNED&nbsp}{@code long} to check
     *
     * @param   type    the {@link IntegralBoundaryTypes IntegralBoundaryType} determining which type
     *                  of boundary to use when checking. As {@code value} is unsigned,
     *                  {@link IntegralBoundaryTypes#SHORTENED SHORTENED} and
     *                  {@link IntegralBoundaryTypes#DEFAULT DEFAULT} have no difference here.
     *                  {@link IntegralBoundaryTypes#UNSIGNED UNSIGNED} will always return {@code true}, and
     *                  {@code null} defaults to {@link IntegralBoundaryTypes#DEFAULT DEFAULT}.
     *
     * @return  {@code true} if {@code value} fits within an {@code int} under the given boundary condition,
     *          otherwise {@code false}
     */
    @Pure
    public static boolean canBeIntUnsigned(
            @Unsigned long value,
            @Nullable IntegralBoundaryTypes type
    ) {
        @Unsigned long[] lower = new long[4];
        
        @Unsigned long[] upper = new long[]{Integer.MAX_VALUE,
                Integer.MAX_VALUE, NEG_INT_MIN, LONG_TO_INT_MASK};
        
        return canBe(value, lower, upper, type, false);
    }
    
    /**
     * Determined if value is within the range of the lower and upper bounds, chosen from the 2nd and 3rd arguments
     * based on the 4th and 5th argument.
     *
     * @param   value   the value to check for valid containment
     *
     * @param   lower   a {@code long[]} of length 4 with the lower bounds, in the same order as
     *                  {@link IntegralBoundaryTypes} is declared
     *
     * @param   upper   a {@code long[]} of length 4 with the upper bounds, in the same order as
     *                  {@link IntegralBoundaryTypes} is declared
     *
     * @param   type    the type of containededness to use; which bounds to use
     *
     * @param   signed  whether all primitives should be interpreted as signed or unsigned
     *
     * @return  {@code true} if the value is contained according to the other arguments, otherwise {@code false}
     */
    @Pure
    private static boolean canBe(
            @PolySigned long value,
            @PolySigned long@ArrayLen(4)[] lower,
            @PolySigned long@ArrayLen(4)[] upper,
            @Nullable IntegralBoundaryTypes type,
            boolean signed
    ) {
        int index = IntegralBoundaryTypes.ordinal(type);
        LongPredicate lowerPred, upperPred;
        if (signed) {
            lowerPred = l -> l <= value;
            upperPred = l -> value <= l;
        } else {
            lowerPred = l -> true;
            upperPred = l -> Long.compareUnsigned(value, l) <= 0;
        }
        
        return lowerPred.test(lower[index]) && upperPred.test(upper[index]);
    }
    
    
    /**
     * Finds the GCF of two numbers.
     * Only one of them can be {@code 0}, if any.
     * If the answer would be {@link Long#MIN_VALUE}, an {@link ArithmeticException}
     * is thrown due to it not being able to be made positive.
     *
     * @param   left    the first number to get the GCF of
     *
     * @param   right   the second number to get the GCF of
     *
     * @return  the GCF of the two numbers
     *
     * @throws  ArithmeticException if both arguments are {@code 0} or both are
     *                              {@link Long#MIN_VALUE}
     */
    @Pure
    public static @Positive long gcf(
            long left,
            long right
    ) {
        long lAbs = -Math.abs(left);
        long rAbs = -Math.abs(right);
        
        long res;
        if (lAbs == 0) {
            if (rAbs == 0) {
                throw new ArithmeticException(BAD_GCF);
            }
            res = rAbs;
        } else if (rAbs == 0) {
            res = lAbs;
        } else {
            res = recurEuclidNegGCF(lAbs, rAbs);
        }
        return Math.negateExact(res);
    }
    
    /**
     * Finds the LCM of two numbers.
     * Neither of them can be {@code 0} nor {@link Long#MIN_VALUE}.
     *
     * @param   left    the first number to get the LCM of
     *
     * @param   right   the second number to get the LCM of
     *
     * @return  the LCM of the two numbers
     *
     * @throws  ArithmeticException if either argument is {@code 0} or {@link Long#MIN_VALUE},
     *                              or if the result would be larger than {@link Long#MAX_VALUE}
     */
    @Pure
    public static @Positive long lcm(
            long left,
            long right
    ) {
        long lAbs = Math.absExact(left);
        long rAbs = Math.absExact(right);
        
        if ((lAbs == 0) || (rAbs == 0)) {
            throw new ArithmeticException(BAD_LCM);
        }
        
        try {
            long prod = Math.multiplyExact(lAbs, rAbs);
            return prod / recurEuclidGCF(lAbs, rAbs);
        } catch (ArithmeticException ignored) {
            BigInteger res = BigMathObjectUtils.lcm(lAbs, rAbs);
            if (BigMathObjectUtils.canBeLong(res, null)) {
                return res.longValue();
            }
            throw new ArithmeticException("Result too large for long!");
        }
    }
    
    /**
     * Finds the GCF of two {@link Unsigned unsigned&nbsp}{@code int}s.
     * Only one of them can be 0, if any.
     *
     * @param   left    the first {@link Unsigned UNSIGNED&nbsp}{@code int} to get the GCF of
     *
     * @param   right   the second {@link Unsigned UNSIGNED&nbsp}{@code int} to get the GCF of
     *
     * @return  the GCF of the two numbers, itself an {@link Unsigned unsigned&nbsp}{@code int}
     *
     * @throws  ArithmeticException if both arguments are 0
     */
    @Pure
    public static @Unsigned int GCFUnsigned(
            @Unsigned int left,
            @Unsigned int right
    ) {
        if (left == 0) {
            if (right == 0) {
                throw new ArithmeticException(BAD_GCF);
            }
            return right;
        }
        
        if (right == 0) {
            return left;
        }
        
        return recurEuclidGCFUnsigned(left, right);
    }
    
    /**
     * Finds the LCM of two {@link Unsigned unsigned&nbsp}{@code int}s, which is itself returned
     * as an unsigned {@code long}. This will most likely not be relevant, as both {@code int}s would need
     * to be quite close to the maximum, even for an unsigned value, for the signedness of the result
     * to actually matter. None of the arguments can be 0, if any.
     *
     * @param   left    the first {@link Unsigned UNSIGNED&nbsp}{@code int} to get the LCM of
     *
     * @param   right   the second {@link Unsigned UNSIGNED&nbsp}{@code int} to get the LCM of
     *
     * @return  the LCM of the two numbers, itself an {@link Unsigned unsigned&nbsp}{@code long}
     *
     * @throws  ArithmeticException if either argument is 0
     */
    @Pure
    public static @Unsigned long lcmUnsignedToLong(
            @Unsigned int left,
            @Unsigned int right
    ) {
        if ((left == 0) || (right == 0)) {
            throw new ArithmeticException(BAD_LCM);
        }
        
        long leftL = Integer.toUnsignedLong(left);
        long rightL = Integer.toUnsignedLong(right);
        
        return (leftL * rightL) / recurEuclidGCF(leftL, rightL);
    }
    
    /**
     * Generates an array of two {@code long}s, the first of which is the square root, or rather,
     * the largest-magnitude integer whose square is still below {@code value},
     * and the second being the remainder from {@code value - (square root)^2}.
     *
     * @param   value   the value to get the root of
     * @return  an array of two values, the first of which is the root, and the second the remainder
     *
     * @throws  ArithmeticException if attempting to root a negative
     *
     * @see BigInteger#sqrtAndRemainder
     */
    @SideEffectFree
    public static @NonNegative long@ArrayLen(2)[] sqrtAndRemainder(
            @NonNegative long value
    ) {
        if (value < 0) {
            throw new ArithmeticException("Root of negative not defined as primitive");
        }
        double rootApprox = Math.sqrt(value);
        long root = (long) Math.floor(rootApprox);
        long remainder = value - (root * root);
        return new long[] {root, remainder};
    }
    
    /**
     * Finds the gcf using Euclid's method.
     *
     * @param   a   the first number (negative long)
     *
     * @param   b   the second number (negative long)
     *
     * @return  the gcf
     */
    @Pure
    private static long recurEuclidNegGCF(
            long a,
            long b
    ) {
        return (b == 0)
                ? a
                : recurEuclidNegGCF(b, -(a % b));
    }
    
    /**
     * Finds the gcf using Euclid's method.
     *
     * @param   a   the first number (positive long)
     *
     * @param   b   the second number (positive long)
     *
     * @return  the gcf
     */
    @Pure
    private static long recurEuclidGCF(
            @Positive long a,
            @NonNegative long b
    ) {
        return (b == 0)
                ? a
                : recurEuclidGCF(b, a % b);
    }
    
    /**
     * Finds the gcf using Euclid's method.
     *
     * @param   a   the first number (unsigned int)
     *
     * @param   b   the second number (unsigned int)
     *
     * @return  the gcf
     */
    @Pure
    private static int recurEuclidGCFUnsigned(
            @Unsigned int a,
            @Unsigned int b
    ) {
        return (b == 0)
                ? a
                : recurEuclidGCFUnsigned(b, Integer.remainderUnsigned(a, b));
    }
    
}
