package org.cb2384.exactalgebra.objects;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.cb2384.exactalgebra.util.BigMathObjectUtils;
import org.cb2384.exactalgebra.util.PrimMathUtils;
import org.cb2384.exactalgebra.util.corutils.ternary.Signum;

import org.checkerframework.checker.nullness.qual.*;
import org.checkerframework.common.value.qual.*;
import org.checkerframework.dataflow.qual.*;

public enum Sigmagnum {
    /**
     * Indicates a value that is {@link Signum#NEGATIVE negative} and below {@code -1}
     */
    NEGATIVE_SUB_MINUS_ONE((byte) -3),
    /**
     * Indicates the value of {@code -1}
     */
    NEGATIVE_ONE((byte) -2),
    /**
     * Indicates a value that is {@link Signum#NEGATIVE negative} but above {@code -1}
     */
    NEGATIVE_SUP_MINUS_ONE((byte) -1),
    /**
     * Indicates the value of {@code 0}
     */
    ZERO((byte) 0),
    /**
     * Indicates a value that is {@link Signum#POSITIVE positive} but below {@code 1}
     */
    POSITIVE_SUB_ONE((byte) 1),
    /**
     * Indicates the value of {@code 1}
     */
    POSITIVE_ONE((byte) 2),
    /**
     * Indicates a value that is {@link Signum#POSITIVE positive} and above {@code 1}
     */
    POSITIVE_SUP_ONE((byte) 3);
    /**
     * the byte value that this enum has replaced
     */
    private final byte byteKey;
    /**
     * the signum of this sigmagnum
     */
    private final Signum signum;
    
    /**
     * Maps byteKey input to byteKey field, and also stores its signum too
     *
     * @param   sigmagnum   the byte value that this enum has replaced
     */
    @SideEffectFree
    Sigmagnum(
            byte sigmagnum
    ) {
        byteKey = sigmagnum;
        signum = Signum.valueOf(sigmagnum);
    }
    
    /**
     * Returns the {@link Sigmagnum} enum constant associated with this {@code byte}. Fun fact:
     * before the creation of this class, the {@code sigmagnum} functions of number-type classes
     * used these {@code byte} values instead. This function, and {@link #byteKey}, can translate
     * between them.
     *
     * @return  {@link #NEGATIVE_SUB_MINUS_ONE} for -3,
     *          {@link #NEGATIVE_ONE} for -2,
     *          {@link #NEGATIVE_SUP_MINUS_ONE} for -1,
     *          {@link #ZERO} for 0,
     *          {@link #POSITIVE_SUB_ONE} for 1,
     *          {@link #POSITIVE_ONE} for 2, and
     *          {@link #POSITIVE_SUP_ONE} for 3
     *
     * @throws  IllegalArgumentException    if {@code value} is not in the mathematical range {@code [-3, 3]}
     */
    @Pure
    public static Sigmagnum fromByteKey(
            @IntRange(from = -3, to = 3) byte value
    ) {
        return switch (value) {
            case -3 -> NEGATIVE_SUB_MINUS_ONE;
            case -2 -> NEGATIVE_ONE;
            case -1 -> NEGATIVE_SUP_MINUS_ONE;
            case 0 -> ZERO;
            case 1 -> POSITIVE_SUB_ONE;
            case 2 -> POSITIVE_ONE;
            case 3 -> POSITIVE_SUP_ONE;
            default -> throw new IllegalArgumentException(
                    "Value is not a valid byteKey value (must be in [-3, 3])");
        };
    }
    
    /**
     * Similar to {@link Signum#valueOf(BigInteger)}, but for {@link Sigmagnum} instead.
     *
     * @param   value   the {@link BigInteger} value to get the sigmagnum of
     *
     * @return  the {@link Sigmagnum} of the given value, as an {@link Enum} constant
     *
     * @throws  NullPointerException    if {value == null}
     */
    @Pure
    public static Sigmagnum valueOf(
            BigInteger value
    ) {
        BigInteger minusOne = BigInteger.ONE.negate();
        return switch (value) {
            case BigInteger bi when bi.compareTo(minusOne) < 0 -> NEGATIVE_SUB_MINUS_ONE;
            case BigInteger bi when bi.compareTo(minusOne) == 0 -> NEGATIVE_ONE;
            case BigInteger bi when (bi.compareTo(minusOne) > 0)
                    && BigMathObjectUtils.isNegative(bi) -> NEGATIVE_SUP_MINUS_ONE;
            
            case BigInteger bi when bi.compareTo(BigInteger.ZERO) == 0 -> ZERO;
            
            case BigInteger bi when (bi.compareTo(BigInteger.ONE) < 0)
                    && (bi.signum() > 0) -> POSITIVE_SUB_ONE;
            case BigInteger bi when bi.compareTo(BigInteger.ONE) == 0 -> POSITIVE_ONE;
            case BigInteger bi when bi.compareTo(BigInteger.ONE) > 0 -> POSITIVE_SUP_ONE;
            
            default -> throw new ArithmeticException(
                    "value is not comparing properly with other BigIntegers‽");
        };
    }
    
    /**
     * Similar to {@link Signum#valueOf(long)}, but for {@link Sigmagnum} instead.
     *
     * @param   value   the {@code long} value to get the sigmagnum of
     *
     * @return  the {@link Sigmagnum} of the given value, as an {@link Enum} constant
     */
    @Pure
    public static Sigmagnum valueOf(
            long value
    ) {
        if (PrimMathUtils.canBeInt(value, PrimMathUtils.IntegralBoundaryTypes.DEFAULT)) {
            return valueOf((int) value);
        } else if (value < 0) {
            return NEGATIVE_SUB_MINUS_ONE;
        } else {
            return POSITIVE_SUP_ONE;
        }
    }
    
    /**
     * Similar to {@link Signum#valueOf(int)} (or {@link Signum#valueOf(short)} or {@link Signum#valueOf(byte)}
     * due to automatic widening), but for {@link Sigmagnum} instead.
     *
     * @param   value   the {@code int} value to get the sigmagnum of
     *
     * @return  the {@link Sigmagnum} of the given value, as an {@link Enum} constant
     */
    @Pure
    public static Sigmagnum valueOf(
            int value
    ) {
        return switch (value) {
            case -1 -> NEGATIVE_ONE;
            case 0 -> ZERO;
            case 1 -> POSITIVE_ONE;
            default -> (value < 0) ? NEGATIVE_SUB_MINUS_ONE : POSITIVE_SUP_ONE;
        };
    }
    
    /**
     * Similar to {@link Signum#valueOf(double)} (or {@link Signum#valueOf(float)}
     * due to automatic widening), but for {@link Sigmagnum} instead.
     *
     * @param   value   the {@code double} to get the signum of
     *
     * @return  the {@link Sigmagnum} of the given value, as an {@link Enum} constant
     *
     * @throws  ArithmeticException if {@code value} is {@code NaN};
     *
     * @see #valueOfNaN_able(double)
     */
    @Pure
    public static Sigmagnum valueOf(
            double value
    ) {
        if (Double.isNaN(value)) {
            throw new ArithmeticException("NaN is not positive, negative, or 0!");
        }
        return mapDouble(value);
    }
    
    /**
     * Maps a double to the corresponding sigmagnum, assuming the double isn't NaN.
     *
     * @param   value   the double to map
     *
     * @return  the sigmagnum of the double
     */
    private static Sigmagnum mapDouble(
            double value
    ) {
        assert !Double.isNaN(value);
        if (value < -1) {
            return NEGATIVE_SUB_MINUS_ONE;
        } else if (value == -1) {
            return NEGATIVE_ONE;
        } else if (value < 0) {
            return NEGATIVE_SUP_MINUS_ONE;
        } else if (value > 1) {
            return POSITIVE_SUP_ONE;
        } else if (value == 1) {
            return POSITIVE_ONE;
        } else if (value > 0) {
            return POSITIVE_SUB_ONE;
        } else {
            return ZERO;
        }
    }
    
    /**
     * Similar to {@link Signum#valueOf(BigDecimal)}, but for {@link Sigmagnum} instead.
     *
     * @param   value   the {@link BigDecimal} value to get the signum of
     *
     * @return  the {@link Sigmagnum} of the given value, as an {@link Enum} constant
     *
     * @throws  NullPointerException    if {value == null}
     */
    @Pure
    public static Sigmagnum valueOf(
            BigDecimal value
    ) {
        BigDecimal minusOne = BigDecimal.ONE.negate();
        return switch (value) {
            case BigDecimal bd when bd.compareTo(minusOne) < 0 -> NEGATIVE_SUB_MINUS_ONE;
            case BigDecimal bd when bd.compareTo(minusOne) == 0 -> NEGATIVE_ONE;
            case BigDecimal bd when (bd.compareTo(minusOne) > 0)
                    && BigMathObjectUtils.isNegative(bd) -> NEGATIVE_SUP_MINUS_ONE;
            
            case BigDecimal bd when bd.compareTo(BigDecimal.ZERO) == 0 -> ZERO;
            
            case BigDecimal bd when (bd.compareTo(BigDecimal.ONE) < 0)
                    && (bd.signum() > 0) -> POSITIVE_SUB_ONE;
            case BigDecimal bd when bd.compareTo(BigDecimal.ONE) == 0 -> POSITIVE_ONE;
            case BigDecimal bd when bd.compareTo(BigDecimal.ONE) > 0 -> POSITIVE_SUP_ONE;
            
            default -> throw new ArithmeticException(
                    "value is not comparing properly with other BigDecimals‽");
        };
    }
    
    /**
     * Similar to {@link Signum#valueOf(Number)}, but for {@link Sigmagnum} instead.
     *
     * @implNote    {@link Number#doubleValue()} is used because it has the highest range of supported values
     *              out of the functions offered from the abstract superclass {@link Number}.
     *              But as that is an abstract class, some other implementation (not one of the main Java ones)
     *              might throw errors that are not known about here. Also, because {@code double}s have
     *              the possibility of a {@code NaN} value, that will also throw an error if it is returned
     *              by {@link Number#doubleValue()}. To return {@code null} instead of throwing an error, see
     *              {@link #valueOfNaNOrUnsafe}
     *
     * @param   value   the {@link Number} to get the signum of
     *
     * @return  the {@link Sigmagnum} of the given value, as an {@link Enum} constant
     *
     * @throws  NullPointerException    if {@code value == null}
     *
     * @throws  ArithmeticException if {@code value} represents something that, when turned into a {@code double},
     *                              is a {@code NaN} value.
     *
     * @see #valueOfNaNOrUnsafe(Number)
     */
    @Pure
    public static Sigmagnum valueOf(
            Number value
    ) {
        return valueOf(value.doubleValue());
    }
    
    /**
     * Similar to {@link Signum#valueOfNaNOrUnsafe(Number)}, but for {@link Sigmagnum} instead.
     * Unlike {@link #valueOf(Number)}, this function can return {@code null} in the case that
     * the input value is itself {@code null} or something else that would
     * throw an {@link ArithmeticException}&mdash such as {@code NaN}.
     *
     * @implNote    {@link Number#doubleValue()} is used because it has the highest range of supported values
     *              out of the functions offered from the abstract superclass {@link Number}.
     *              But as that is an abstract class, some other implementation (not one of the main Java ones)
     *              might throw errors that are not known about here.
     *
     * @param   value   the {@link Number} to get the signum of
     *
     * @return  the {@link Sigmagnum} of the given value, as an {@link Enum} constant, or {@code null}
     *          if the signum of {@code null} or {@code NaN} or something else that would trigger
     *          an {@link ArithmeticException} to be thrown
     *
     * @see #valueOf(Number)
     */
    @Pure
    public static @Nullable Sigmagnum valueOfNaNOrUnsafe(
            @Nullable Number value
    ) {
        try {
            return valueOfNaN_able(value.doubleValue());
        } catch (ArithmeticException | NullPointerException ignored) {
            return null;
        }
    }
    
    /**
     * Similar to {@link Signum#valueOfNaN_able(double)}, but for {@link Sigmagnum} instead.
     *
     * @param   value   the {@code double} to get the signum of
     *
     * @return  the {@link Sigmagnum} of the given value, as an {@link Enum} constant, or {@code null}
     *          if {@code value} is {@code NaN}
     *
     *
     * @see #valueOf(double)
     */
    @Pure
    public static @Nullable Sigmagnum valueOfNaN_able(
            double value
    ) {
        return Double.isNaN(value) ? null : mapDouble(value);
    }
    
    /**
     * Returns the {@code byte} key value associated with this {@link Enum} constant. Fun fact:
     * before the creation of this class, the {@code sigmagnum} functions of number-type classes
     * used these values instead of an enum.
     *
     * @return  -3 for {@link #NEGATIVE_SUB_MINUS_ONE},
     *          -2 for {@link #NEGATIVE_ONE},
     *          -1 for {@link #NEGATIVE_SUP_MINUS_ONE},
     *          0 for {@link #ZERO},
     *          1 for {@link #POSITIVE_SUB_ONE},
     *          2 for {@link #POSITIVE_ONE}, and
     *          3 for {@link #POSITIVE_SUP_ONE}
     */
    @Pure
    public byte byteKey() {
        return byteKey;
    }
    
    /**
     * Returns the signum associated with this sigmagnum.
     *
     * @return  {@link Signum#NEGATIVE} for {@link #NEGATIVE_SUB_MINUS_ONE}, {@link #NEGATIVE_ONE},
     *          and {@link #NEGATIVE_SUP_MINUS_ONE};
     *          {@link Signum#POSITIVE} for {@link #POSITIVE_SUB_ONE}, {@link #POSITIVE_ONE},
     *          and {@link #POSITIVE_SUP_ONE}; and
     *          {@link Signum#ZERO} for {@link #ZERO}
     */
    @Pure
    public Signum signum() {
        return signum;
    }
}
