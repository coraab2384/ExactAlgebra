package org.cb2384.exactalgebra.util.corutils.ternary;

import java.math.BigDecimal;
import java.math.BigInteger;


import org.checkerframework.checker.nullness.qual.*;
import org.checkerframework.common.value.qual.*;
import org.checkerframework.dataflow.qual.*;

/**
 * <p>An {@link Enum} representing the values of a signum operation.
 * These values are more cooperative in switch statements than {@code int}s,
 * which require the application of {@link Integer#signum} when being used with
 * comparisons, as well as requiring a default case always.</p>
 *
 * <p>The introduction of guarded patterns in Java 21 makes these not <i>as</i>
 * helpful as previously, but an unguarded pattern for one case and/or
 * a default case are still required. Using {@code case NEGATIVE} looks cleaner than
 * {@code case Integer i when i < 0} anyway.</p>
 *
 * @author  Corinne Buxton
 */
public enum Signum
        implements ThreeValued {
    /**
     * Less than 0
     */
    NEGATIVE(-1, Ternary.FALSE),
    
    /**
     * Neither positive nor negative; is its own signum
     */
    ZERO(0, Ternary.DEFAULT),
    
    /**
     * Greater than 0
     */
    POSITIVE(1, Ternary.TRUE);
    
    /**
     * Signum of a negative value
     */
    public static final Signum MINUS_ONE = NEGATIVE;
    
    /**
     * Signum of a negative value
     */
    public static final Signum NEGATIVE_ONE = NEGATIVE;
    
    /**
     * Signum of a positive value
     */
    public static final Signum ONE = POSITIVE;
    
    /**
     * Signum that this enum constant represents
     */
    private final byte signum;
    
    /**
     * value for ternary interoperability
     */
    private final Ternary ternary;
    
    /**
     * Puts the input signum as the signum field (making it a byte).
     *
     * @param   signum  the signum of this enum constant
     */
    @SideEffectFree
    Signum(
            int signum,
            Ternary ternary
    ) {
        this.signum = (byte) signum;
        this.ternary = ternary;
    }
    
    /**
     * Similar to {@link BigInteger#signum()}, but returns a {@link Signum}
     * enum constant instead of an {@code int}.
     *
     * @param   value   the {@link BigInteger} value to get the signum of
     *
     * @return  the {@link Signum} of the given value, as an {@link Enum} constant
     *
     * @throws  NullPointerException    if {value == null}
     */
    @Pure
    public static @NonNull Signum valueOf(
            @NonNull BigInteger value
    ) {
        return valueOf(value.signum());
    }
    
    /**
     * Similar to {@link Long#signum}, but returns a {@link Signum} enum constant instead of an {@code int}.
     *
     * @param   value   the {@code long} value to get the signum of
     *
     * @return  the {@link Signum} of the given value, as an {@link Enum} constant
     */
    @Pure
    public static @NonNull Signum valueOf(
            long value
    ) {
        return valueOf(Long.signum(value));
    }
    
    /**
     * Similar to {@link Integer#signum}, but returns a {@link Signum} enum constant instead of an {@code int}.
     *
     * @param   value   the {@code int} value to get the signum of
     *
     * @return  the {@link Signum} of the given value, as an {@link Enum} constant
     */
    @Pure
    public static @NonNull Signum valueOf(
            int value
    ) {
        switch (value) {
            case -1:
                return NEGATIVE;
                
            case 0:
                return ZERO;
                
            case 1:
                return POSITIVE;
                
            default:
                return valueOf(Integer.signum(value));
        }
    }
    
    /**
     * Similar to {@link Math#signum}, but returns a {@link Signum} enum constant instead of a primitive.
     *
     * @param   value   the {@code short} value to get the signum of
     *
     * @return  the {@link Signum} of the given value, as an {@link Enum} constant
     */
    @Pure
    public static @NonNull Signum valueOf(
            short value
    ) {
        return valueOf(Integer.signum(value));
    }
    
    /**
     * Similar to {@link Math#signum}, but returns a {@link Signum} enum constant instead of a primitive.
     *
     * @param   value   the {@code byte} value to get the signum of
     *
     * @return  the {@link Signum} of the given value, as an {@link Enum} constant
     */
    @Pure
    public static @NonNull Signum valueOf(
            byte value
    ) {
        return valueOf(Integer.signum(value));
    }
    
    /**
     * Similar in effect to {@link Math#signum(float)}, but returns a {@link Signum}
     * enum constant instead of a primitive. This version will throw an error at {@code NaN}.
     *
     * @param   value   the {@code float} to get the signum of
     *
     * @return  the {@link Signum} of the given value, as an {@link Enum} constant
     *
     * @throws  ArithmeticException if {@code value} is {@code NaN};
     *
     * @see #valueOfNaN_able(float)
     */
    @Pure
    public static @NonNull Signum valueOf(
            float value
    ) {
        if (Float.isNaN(value)) {
            throw new ArithmeticException("NaN is not positive, negative, or 0!");
        }
        return valueOf((int) Math.signum(value));
    }
    
    /**
     * Similar in effect to {@link Math#signum(double)}, but returns a {@link Signum}
     * enum constant instead of a primitive. This version will throw an error at {@code NaN}.
     *
     * @param   value   the {@code double} to get the signum of
     *
     * @return  the {@link Signum} of the given value, as an {@link Enum} constant
     *
     * @throws  ArithmeticException if {@code value} is {@code NaN};
     *
     * @see #valueOfNaN_able(double)
     */
    @Pure
    public static @NonNull Signum valueOf(
            double value
    ) {
        if (Double.isNaN(value)) {
            throw new ArithmeticException("NaN is not positive, negative, or 0!");
        }
        return valueOf((int) Math.signum(value));
    }
    
    /**
     * Similar to {@link BigDecimal#signum}, but returns a {@link Signum}
     * enum constant instead of an {@code int}.
     *
     * @param   value   the {@link BigDecimal} value to get the signum of
     *
     * @return  the {@link Signum} of the given value, as an {@link Enum} constant
     *
     * @throws  NullPointerException    if {value == null}
     */
    @Pure
    public static @NonNull Signum valueOf(
            @NonNull BigDecimal value
    ) {
        return valueOf(value.signum());
    }
    
    /**
     * <p>Similar in effect to {@link Math#signum(double) Math.signum(}{@code value}{@link Number#doubleValue()
     * .doubleValue()}{@link Math#signum(double) )}, but returns a {@link Signum}
     * enum constant instead of a primitive.</p>
     *
     * <p>Implementation Note:  {@link Number#doubleValue()} is used because it has the highest range of
     *                          supported values out of the functions offered from the abstract superclass
     *                          {@link Number}. But as that is an abstract class, some other implementation
     *                          (not one of the main Java ones) might throw errors that are not known about
     *                          here. Also, because {@code double}s have the possibility of a {@code NaN}
     *                          value, that will also throw an error if it is returned by {@link
     *                          Number#doubleValue()}. To return {@code null} instead of throwing an error,
     *                          see {@link #valueOfNaNOrUnsafe}</p>
     *
     * @param   value   the {@link Number} to get the signum of
     *
     * @return  the {@link Signum} of the given value, as an {@link Enum} constant
     *
     * @throws  NullPointerException    if {@code value == null}
     *
     * @throws  ArithmeticException if {@code value} represents something that, when turned into a {@code double},
     *                              is a {@code NaN} value.
     *
     * @see #valueOfNaNOrUnsafe(Number)
     */
    @Pure
    public static @NonNull Signum valueOf(
            @NonNull Number value
    ) {
        return valueOf(value.doubleValue());
    }
    
    /**
     * <p>Similar in effect to {@link Math#signum(double) Math.signum(}{@code value}{@link Number#doubleValue()
     * .doubleValue()}{@link Math#signum(double) )}, but returns a {@link Signum}
     * enum constant instead of a primitive. Unlike {@link #valueOf(Number)}, this function can return
     * {@code null} in the case that the input value is itself {@code null} or something else that would
     * throw an {@link ArithmeticException}&mdash;such as {@code NaN}.</p>
     *
     * <p>Implementation Note:  {@link Number#doubleValue()} is used because it has the highest range of
     *                          supported values out of the functions offered from the abstract superclass
     *                          {@link Number}. But as that is an abstract class, some other implementation
     *                          (not one of the main Java ones) might throw errors that are not known about here.</p>
     *
     * @param   value   the {@link Number} to get the signum of
     *
     * @return  the {@link Signum} of the given value, as an {@link Enum} constant, or {@code null}
     *          if the signum of {@code null} or {@code NaN} or something else that would trigger
     *          an {@link ArithmeticException} to be thrown
     *
     * @see #valueOf(Number)
     */
    @Pure
    public static @Nullable Signum valueOfNaNOrUnsafe(
            @Nullable Number value
    ) {
        try {
            return valueOfNaN_able(value.doubleValue());
        } catch (ArithmeticException | NullPointerException ignored) {
            return null;
        }
    }
    
    /**
     * Similar in effect to {@link Math#signum(float)}, but returns a {@link Signum}
     * enum constant instead of a primitive. This version will return {@code null} for {@code NaN}.
     *
     * @param   value   the {@code float} to get the signum of
     *
     * @return  the {@link Signum} of the given value, as an {@link Enum} constant, or {@code null}
     *          if {@code value} is {@code NaN}
     *
     * @see #valueOfNaN_able(float)
     */
    @Pure
    public static @Nullable Signum valueOfNaN_able(
            float value
    ) {
        return Float.isNaN(value) ? null : valueOf((int) Math.signum(value));
    }
    
    /**
     * Similar in effect to {@link Math#signum(double)}, but returns a {@link Signum}
     * enum constant instead of a primitive. This version will return {@code null} for {@code NaN}.
     *
     * @param   value   the {@code double} to get the signum of
     *
     * @return  the {@link Signum} of the given value, as an {@link Enum} constant, or {@code null}
     *          if {@code value} is {@code NaN}
     *
     * @see #valueOfNaN_able(double)
     */
    @Pure
    public static @Nullable Signum valueOfNaN_able(
            double value
    ) {
        return Double.isNaN(value) ? null : valueOf((int) Math.signum(value));
    }
    
    /**
     * Yields the signum value of this particular enum constant: {@code -1}, {@code 0}, or {@code 1}.
     *
     * @return  {@code -1} for {@link #NEGATIVE}, {@code 0} for {@link #ZERO}, and {@code 1} for {@link #POSITIVE}
     */
    @Pure
    public @IntRange(from = -1, to = 1) byte signum() {
        return signum;
    }
    
    /**
     * Yields a {@link Ternary} value tied to this {@link Signum}, for interoperability mainly.
     * This enables, for example, an {@code isPositive} or {@code isNonNegative} function
     * via {@link Ternary#booleanValue(boolean)}.
     *
     * @return  a {@link Ternary} value for this {@link Signum}:
     *          {@link #NEGATIVE} maps to {@link Ternary#FALSE}, {@link #ZERO} to {@link Ternary#DEFAULT},
     *          and {@link #POSITIVE} to {@link Ternary#TRUE}. {@link Ternary#not()} can reverse this, if desired.
     */
    @Pure
    public @NonNull Ternary ternaryValue() {
        return ternary;
    }
    
    /**
     * Yields a {@link Boolean} value tied to this {@link Signum}, which is itself derived from
     * the {@link Ternary} value returned from {@link #ternaryValue()}.
     * This enables, for example, an {@code isNegative} function using {@link
     * org.cb2384.corutils.NullnessUtils#nullToTrue(Boolean) !NullnessUtils.nullToTrue(}{@code Bool}{@link
     * org.cb2384.corutils.NullnessUtils#nullToTrue(Boolean) )}, where {@code Bool} is the return
     * from this function.
     *
     * @return  a {@link Boolean} value for this {@link Signum}:
     *          {@link #NEGATIVE} maps to {@link Boolean#FALSE}, {@link #ZERO} to {@code null},
     *          and {@link #POSITIVE} to {@link Boolean#TRUE}
     */
    @Override
    @Pure
    public @Nullable Boolean booleanValue() {
        return ternary.booleanValue();
    }
}
