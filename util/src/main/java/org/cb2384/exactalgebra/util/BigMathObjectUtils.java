package org.cb2384.exactalgebra.util;

import static org.cb2384.exactalgebra.util.PrimMathUtils.IntegralBoundaryTypes;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.cb2384.exactalgebra.util.corutils.StringUtils;

import org.checkerframework.checker.nullness.qual.*;
import org.checkerframework.checker.signedness.qual.*;
import org.checkerframework.common.value.qual.*;
import org.checkerframework.dataflow.qual.*;

/**
 * <p>Static utility class, mostly for {@link BigInteger} but also some for {@link BigDecimal}.
 * Like in {@link BigInteger}, pseudocode expressions such as {@code val == 1} actually mean
 * {@code val}{@link BigInteger#equals .equals(}{@link BigInteger#ONE}{@link BigInteger#equals )};
 * similarly for {@code /} with {@link BigInteger#divide} and so on.</p>
 *
 * <p>Throws:&ensp{@link NullPointerException} on any {@code null} argument, unless otherwise specified</p>
 *
 * @author  Corinne Buxton
 */
public final class BigMathObjectUtils {
    private static final String NEG_EXP_EXC = "Negative exponent!";
    /**
     * {@link Long#MAX_VALUE} as a {@link BigInteger}
     */
    public static final BigInteger LONG_MAX_BI = BigInteger.valueOf(Long.MAX_VALUE);
    
    /**
     * The {@link BigInteger} of {@code -}{@link Long#MAX_VALUE}
     */
    public static final BigInteger LONG_NEG_MAX_BI = LONG_MAX_BI.negate();
    
    /**
     * {@link Long#MIN_VALUE} as a {@link BigInteger}
     */
    public static final BigInteger LONG_MIN_BI = BigInteger.valueOf(Long.MIN_VALUE);
    
    /**
     * The {@link BigInteger} of {@code -}{@link Long#MIN_VALUE}
     */
    public static final BigInteger LONG_NEG_MIN_BI = LONG_MIN_BI.negate();
    
    /**
     * The {@link BigInteger} of the largest {@link
     * Unsigned unsigned&nbsp}{@code long} value
     */
    public static final BigInteger LONG_UNSIGNED_MAX_BI = LONG_MAX_BI.subtract(LONG_MIN_BI);
    
    /**
     * {@link Integer#MAX_VALUE} as a {@link BigInteger}
     */
    public static final BigInteger INTEGER_MAX_BI = BigInteger.valueOf(Integer.MAX_VALUE);
    
    /**
     * The {@link BigInteger} of {@code -}{@link Integer#MAX_VALUE}
     */
    public static final BigInteger INTEGER_NEG_MAX_BI = INTEGER_MAX_BI.negate();
    
    /**
     * {@link Integer#MIN_VALUE} as a {@link BigInteger}
     */
    public static final BigInteger INTEGER_MIN_BI = BigInteger.valueOf(Integer.MAX_VALUE);
    
    /**
     * The {@link BigInteger} of {@code -}{@link Integer#MIN_VALUE}
     */
    public static final BigInteger INTEGER_NEG_MIN_BI = BigInteger.valueOf(PrimMathUtils.NEG_INT_MIN);
    
    /**
     * The {@link BigInteger} of the largest {@link
     * Unsigned unsigned&nbsp}{@code int} value,
     * which is for reference {@link PrimMathUtils#LONG_TO_INT_MASK} when a {@code long}
     */
    public static final BigInteger INTEGER_UNSIGNED_MAX_BI = BigInteger.valueOf(PrimMathUtils.LONG_TO_INT_MASK);
    
    /**
     * This should never be called
     *
     * @throws  IllegalAccessException    always
     */
    private BigMathObjectUtils() throws IllegalAccessException {
        throw new IllegalAccessException("This should never be called" + StringUtils.INTERROBANG);
    }
    
    /**
     * Checks if the {@link BigInteger} can be a {@code long}. The boundary behavior
     * (whether to include or exclude the upper bound or lower bound, or whether an unsigned
     * {@code long} is desired, for example) is dictated by {@code type}.
     *
     * @param   value   the {@link BigInteger} to check
     *
     * @param   type    the {@link IntegralBoundaryTypes IntegralBoundaryType} determining which type
     *                  of boundary to use when checking; {@code null} defaults to
     *                  {@link IntegralBoundaryTypes#DEFAULT DEFAULT}
     *
     * @return  {@code true} if {@code value} fits within a {@code int} under the given boundary condition,
     *          otherwise {@code false}
     */
    @Pure
    public static boolean canBeLong(
            BigInteger value,
            @Nullable IntegralBoundaryTypes type
    ) {
        BigInteger[] lower = new BigInteger[]{LONG_NEG_MAX_BI,
                LONG_MIN_BI, LONG_MIN_BI, BigInteger.ZERO};
        
        BigInteger[] upper = new BigInteger[]{LONG_MAX_BI,
                LONG_MAX_BI, LONG_NEG_MIN_BI, LONG_UNSIGNED_MAX_BI};
        
        return canBe(value, lower, upper, type);
    }
    
    /**
     * Checks if the {@link BigInteger} can be an {@code int}. The boundary behavior
     * (whether to include or exclude the upper bound or lower bound, or whether an unsigned
     * {@code int} is desired, for example) is dictated by {@code type}.
     *
     * @param   value   the {@link BigInteger} to check
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
            BigInteger value,
            @Nullable IntegralBoundaryTypes type
    ) {
        BigInteger[] lower = new BigInteger[]{INTEGER_NEG_MAX_BI,
                INTEGER_MIN_BI, INTEGER_MIN_BI, BigInteger.ZERO};
        
        BigInteger[] upper = new BigInteger[]{INTEGER_MAX_BI,
                INTEGER_MAX_BI, INTEGER_NEG_MIN_BI, INTEGER_UNSIGNED_MAX_BI};
        
        return canBe(value, lower, upper, type);
    }
    
    /**
     * Determined if value is within the range of the lower and upper bounds, chosen from the 2nd and 3rd arguments
     * based on the 4th and 5th argument.
     *
     * @param   value   the value to check for valid containment
     *
     * @param   lower   a {@code BigInteger[]} of length 4 with the lower bounds, in the same order as
     *                  {@link IntegralBoundaryTypes} is declared
     *
     * @param   upper   a {@code BigInteger[]} of length 4 with the upper bounds, in the same order as
     *                  {@link IntegralBoundaryTypes} is declared
     *
     * @param   type    the type of containededness to use; which bounds to use
     *
     * @return  {@code true} if the value is contained according to the other arguments, otherwise {@code false}
     */
    @Pure
    private static boolean canBe(
            BigInteger value,
            BigInteger@ArrayLen(4)[] lower,
            BigInteger@ArrayLen(4)[] upper,
            @Nullable IntegralBoundaryTypes type
    ) {
        int index = IntegralBoundaryTypes.ordinal(type);
        return (lower[index].compareTo(value) <= 0) && (value.compareTo(upper[index]) <= 0);
    }
    
    /**
     * Checks if {@code dividend} is evenly divisible by {@code divisor}; that is, without remainder.
     *
     * @param   dividend    the {@link BigInteger} to check division of
     *
     * @param   divisor the {@link BigInteger} to check division by
     *
     * @return  {@code boolean} result of {@code dividend % divisor == 0}
     */
    @Pure
    public static boolean isDivisible(
            BigInteger dividend,
            BigInteger divisor
    ) {
        return isZero( dividend.remainder(divisor) );
    }
    
    /**
     * Checks if these {@link BigDecimal}s are equivalent, working in line with {@link BigDecimal#compareTo} rather
     * than {@link BigDecimal#equals}. Thus, irregularities regarding mathematically equivalent values
     * at different scales are ignored.
     *
     * @param   left    the first {@link BigDecimal} to check
     *
     * @param   right   the second {@link BigDecimal} to check
     *
     * @return  {@code left}{@link BigDecimal#compareTo .compareTo(}{@code right}{@link BigDecimal#compareTo
     * )&nbsp}{@code == 0}
     */
    @Pure
    public static boolean equiv(
            BigDecimal left,
            BigDecimal right
    ) {
        return left.compareTo(right) == 0;
    }
    
    /**
     * Checks if this {@code value} is equivalent to {@link BigDecimal#ZERO} (ignoring scale irregularities).
     *
     * @param   value   the {@link BigDecimal} to check
     *
     * @return  {@code value}{@link BigDecimal#signum() .signum()&nbsp}{@code == 0}
     */
    @Pure
    public static boolean isZero(
            BigDecimal value
    ) {
        return value.signum() == 0;
    }
    
    /**
     * Checks if this {@code value} is {@link BigInteger#ZERO}.
     *
     * @param   value   the {@link BigInteger} to check
     *
     * @return  {@code true} if {@code value == 0}, otherwise {@code false}
     */
    @Pure
    public static boolean isZero(
            BigInteger value
    ) {
        return value.signum() == 0;
    }
    
    /**
     * Checks if this {@code value} is equivalent to {@link BigDecimal#ONE},
     * specifically ignoring differing scales and focusing on actual value (so {@link BigDecimal#compareTo} would
     * return {@code 0}, rather than {@link BigDecimal#equals} necessarily returning {@code true})
     *
     * @param   value   the {@link BigDecimal} to check
     *
     * @return  {@code true} if {@code value} is equivalent to {@code 1}, otherwise {@code false}
     */
    @Pure
    public static boolean isOne(
            BigDecimal value
    ) {
        return equiv(value, BigDecimal.ONE);
    }
    
    /**
     * Checks if this {@code value} is {@link BigInteger#ONE}
     *
     * @param   value   the {@link BigInteger} to check
     *
     * @return  {@code true} if {@code value == 1}, otherwise {@code false}
     */
    @Pure
    public static boolean isOne(
            BigInteger value
    ) {
        return value.equals(BigInteger.ONE);
    }
    
    /**
     * Checks if this {@code value} is negative
     *
     * @param   value   the {@link BigDecimal} to check
     *
     * @return  {@code true} if {@code value < 0}, otherwise {@code false}
     */
    @Pure
    public static boolean isNegative(
            BigDecimal value
    ) {
        return value.signum() == -1;
    }
    
    /**
     * Checks if this {@code value} is negative
     *
     * @param   value   the {@link BigInteger} to check
     *
     * @return  {@code true} if {@code value < 0}, otherwise {@code false}
     */
    @Pure
    public static boolean isNegative(
            BigInteger value
    ) {
        return value.signum() == -1;
    }
    
    /**
     * Finds the LCM of two {@link BigInteger}s, neither of which can be {@link BigInteger#ZERO}.
     *
     * @param   left    the first number to get the LCM of
     *
     * @param   right   the second number to get the LCM of
     *
     * @return  the LCM of the two numbers
     *
     * @throws  ArithmeticException if either argument is {@code 0}
     */
    @SideEffectFree
    public static BigInteger lcm(
            BigInteger left,
            BigInteger right
    ) {
        return inputAbsLCM(left.abs(), right.abs());
    }
    
    /**
     * Finds the LCM of two {@code long}s as a {@link BigInteger} (in case of {@code long} overflow),
     * neither of which can be {@code 0}.
     *
     * @param   left    the first number to get the LCM of
     *
     * @param   right   the second number to get the LCM of
     *
     * @return  the LCM of the two numbers
     *
     * @throws  ArithmeticException if either argument is {@code 0}
     */
    @SideEffectFree
    public static BigInteger lcm(
            long left,
            long right
    ) {
        BigInteger aAbsBI = BigInteger.valueOf(left).abs();
        BigInteger bAbsBI = BigInteger.valueOf(right).abs();
        return inputAbsLCM(aAbsBI, bAbsBI);
    }
    
    /**
     * Gets the LCM of the two inputs, assumed to be positive.
     *
     * @param   left    first input
     *
     * @param   right   second input
     *
     * @return  the LCM
     */
    @SideEffectFree
    private static BigInteger inputAbsLCM(
            BigInteger left,
            BigInteger right
    ) {
        if (left.equals(BigInteger.ZERO) || right.equals(BigInteger.ZERO)) {
            throw new ArithmeticException(PrimMathUtils.BAD_LCM);
        }
        assert (left.signum() == 1) && (right.signum() == 1);
        
        return ( left.multiply(right) ).divide( left.gcd(right) );
    }
    
    /**
     * Increments {@code value}, though unlike a primitive, it is not mutable,
     * so the return value has to overwrite the original to work
     *
     * @param   value the {@link BigInteger} to increment
     *
     * @return  {@code value++}, though without the side-effect of the original being incremented
     *          unless the returned value also overwrites the original
     */
    @SideEffectFree
    public static BigInteger inc(
            BigInteger value
    ) {
        return value.add(BigInteger.ONE);
    }
    
    /**
     * Decrements {@code value}, though unlike a primitive, it is not mutable,
     * so the return value has to overwrite the original to work
     *
     * @param   value the {@link BigInteger} to decrement
     *
     * @return  {@code value--}, though without the side-effect of the original being incremented
     *          unless the returned value also overwrites the original
     */
    @SideEffectFree
    public static BigInteger dec(
            BigInteger value
    ) {
        return value.subtract(BigInteger.ONE);
    }
    
    /**
     * Performs {@code base ^ exponent}, but in this case mathematical power ({@link Math#pow}
     * or {@link BigInteger#pow}), not in the sense of bitwise xor. In another language,
     * {@code base ** exponent}. While similar to {@link BigInteger#pow}, the difference is that here
     * the {@code exponent} does not need to be an {@code int} (though if it isn't, then {@code base} shouldn't
     * be too big).
     *
     * @param   base    the {@link BigInteger} base of the power operation
     *
     * @param   exponent    the {@link BigInteger} exponent of the power operation
     *
     * @return  {@code base}{@link BigInteger#pow .pow(}{@code exponent}{@link BigInteger#pow )},
     *          except with {@code exponent} not needing to be an {@code int}
     *
     * @throws  ArithmeticException if {@code (exponent < 0) && (base != 1)}
     */
    @SideEffectFree
    public static BigInteger pow(
            BigInteger base,
            BigInteger exponent
    ) {
        // To guarantee failfast for null
        if (isOne(base) && (exponent != null)) {
            return base;
        }
        if (isNegative(exponent)) {
            throw new ArithmeticException(NEG_EXP_EXC);
        }
        
        BigInteger res = BigInteger.ONE;
        while (exponent.compareTo(INTEGER_MAX_BI) > 0) {
            exponent = exponent.subtract(INTEGER_MAX_BI);
            res = res.multiply( base.pow(Integer.MAX_VALUE) );
        }
        return res.multiply( base.pow(exponent.intValue()) );
    }
}
