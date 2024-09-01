package org.cb2384.exactalgebra.objects.numbers;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAccumulator;
import java.util.concurrent.atomic.LongAdder;

import org.cb2384.exactalgebra.objects.AlgebraObject;
import org.cb2384.exactalgebra.objects.RealField;
import org.cb2384.exactalgebra.objects.numbers.integral.AlgebraInteger;
import org.cb2384.exactalgebra.objects.numbers.integral.FiniteInteger;
import org.cb2384.exactalgebra.objects.numbers.integral.IntegerFactory;
import org.cb2384.exactalgebra.objects.numbers.rational.Rational;
import org.cb2384.exactalgebra.objects.numbers.rational.RationalFactory;
import org.cb2384.exactalgebra.objects.pair.NumberRemainderPair;
import org.cb2384.exactalgebra.util.MiscUtils;
import org.cb2384.exactalgebra.objects.Sigmagnum;
import org.cb2384.exactalgebra.util.corutils.ternary.ComparableSwitchSignum;
import org.cb2384.exactalgebra.util.corutils.ternary.Signum;

import org.checkerframework.checker.nullness.qual.*;
import org.checkerframework.common.value.qual.*;
import org.checkerframework.dataflow.qual.*;

/**
 * <p>The interface of all numbers. All numbers are implementations of this interface.
 * This interface guarantees closure for all mathematical operations performed on implementations</p>
 *
 * <p>Throws:&ensp;{@link NullPointerException} &ndash; on any {@code null} argument,
 * unless otherwise specified.</p>
 *
 * @author  Corinne Buxton
 */
public interface AlgebraNumber
        extends AlgebraObject<AlgebraNumber>, ComparableSwitchSignum<AlgebraNumber>,
        RealField<AlgebraInteger, AlgebraNumber> {
    
    /**
     * The default rounding mode. Most arguments asking for a {@link RoundingMode} input will accept
     * {@code null}, and this is the default used in {@code null}'s place.
     */
    RoundingMode DEFAULT_ROUNDING = RoundingMode.HALF_EVEN;
    
    /**
     * The default precision when none is specified; when the precision argument is a {@link Nullable
     * Nullable&nbsp}{@link Integer} instead of a normal {@code int}, this is the value that is substituted
     * for {@code null}.
     */
    Integer DEFAULT_PRECISION = Double.PRECISION - 1;
    
    /**
     * The maximum possible precision value; this value is specifically {@code 2^26}
     * ({@code ^} denotes exponentiation, not xor here), as that is the maxiumum size of a
     * {@link BigInteger}, and thus also the maximum precision of a {@link BigDecimal}.
     */
    Integer MAX_PRECISION = Integer.MAX_VALUE / Integer.SIZE + 1;
    
    /**
     * The number of values, in both the positive and negative directions, that are cached on load.
     * Specifically only integer values are counted in this cache limit. Positive numbers from {@code 1}
     * to this limit, inclusive, are cached, as are negative numbers from {@code -1} to the negation of this limit.
     * {@code 0} is also cached, of course, but as it is neither positive nor negative, it counts against
     * neither direction (unlike default Java integral value primitives).
     */
    short CACHE_DEPTH = 128;
    
    /**
     * Transforms this number into an {@link AlgebraNumber}. This is written with all known Java subclasses
     * of {@link Number}; if a different subclass appears, then it will also work so long as its implementation
     * of {@link Number#doubleValue()} works.
     *
     * @implNote    This function specifically returns a {@link Rational} subclass, since all {@link Number}
     *              subclasses are in fact rational. Even something like {@link Double}, which does attempt
     *              to model possibly-irrational numbers, can only actually store rational values.
     *              However, this should not be relied upon.
     *
     * @param value the {@link Number} input
     *
     * @return  an AlgebraNumber with of the same value
     *
     * @throws NumberFormatException    if {@code value} is a {@link Float} or {@link Double} (or a
     *                                  {@link java.util.concurrent.atomic.DoubleAdder DoubleAdder} or
     *                                  {@link java.util.concurrent.atomic.DoubleAccumulator DoubleAccumulator}
     *                                  or other floating-point class) whose value is not finite,
     *                                  and so cannot be stored within an {@link AlgebraNumber}
     */
    @SideEffectFree
    static AlgebraNumber valueOf(
            Number value
    ) {
        if (value instanceof BigDecimal valBD) {
            return RationalFactory.fromBigDecimal(valBD);
        }
        if (value instanceof BigInteger valBI) {
            return IntegerFactory.fromBigInteger(valBI);
        }
        
        if ((value instanceof Long) || (value instanceof Integer) || (value instanceof Short)
                || (value instanceof Byte) || (value instanceof LongAccumulator)
                || (value instanceof LongAdder) || (value instanceof AtomicInteger)
                || (value instanceof AtomicLong)) {
            return FiniteInteger.valueOfStrict(value.longValue());
        }
        
        // All remaining classes are floating point classes, which can all be converted to double
        return RationalFactory.fromDouble(value.doubleValue());
    }
    
    @SideEffectFree
    static AlgebraNumber valueOf(
            String val
    ) {
        String valNormed = val.replaceAll("[ _]", "");
        return RationalFactory.fromBigDecimal(new BigDecimal(valNormed));
    }
    
    @SideEffectFree
    static AlgebraNumber valueOf(
            String val,
            @IntRange(from = Character.MIN_RADIX, to = Character.MAX_RADIX) int radix
    ) {
        String valNormed = val.replaceAll("[ _]", "");
        return (radix == 10)
                ? RationalFactory.fromBigDecimal(new BigDecimal(valNormed))
                : valueOf( MiscUtils.conformRadixToTen(valNormed, radix) );
    }
    
    /**
     * Yield this number in string form, in normal base 10
     *
     * @implSpec    This shall be equivalent to {@link #toString(int) toString(}{@code 10}{@link #toString(int) )}.
     *
     * @return  a string representing the value of this object
     */
    @Override
    @SideEffectFree
    String toString();
    
    /**
     * Represents this number as a string, for the given {@code radix}
     *
     * @param radix the radix for the representation
     *
     * @return  a string representation of this
     *
     * @throws NumberFormatException    if {@code radix < }{@link Character#MIN_RADIX} or
     *                                  {@code radix > }{@link Character#MAX_RADIX}
     *
     * @see Character#forDigit
     */
    @Override
    @SideEffectFree
    String toString(@IntRange(from = Character.MIN_RADIX, to = Character.MAX_RADIX) int radix);
    
    /**
     * Represents this AlgebraObject as a string, with the given {@code radix} and
     * {@code variables} (to be used if this a functional or relational type of AlgebraObject).
     *
     * @implNote    The default implementation ignores the {@code variables} and simply calls {@link
     *              #toString(int) toString(}{@code radix}{@link #toString(int) )}; and should be overwritten
     *              if this type is actually to be represented with variables.
     *
     * @param radix     the radix for the representation
     * @param variables the variable/-s for this representation; ignored if there are
     *                  no variables used in this type
     *
     * @return  a string representation of this
     *
     * @throws NumberFormatException    if {@code radix < }{@link Character#MIN_RADIX} or
     *                                  {@code radix > }{@link Character#MAX_RADIX}
     *
     * @see Character#forDigit
     */
    @Override
    @SideEffectFree
    default String toString(
            @IntRange(from = Character.MIN_RADIX, to = Character.MAX_RADIX) int radix,
            String@Nullable... variables
    ) {
        return toString(radix);
    }
    
    /**
     * Determines if this is whole&mdash;in which case, rounding does nothing.
     *
     * @return  {@code true} if this is whole, {@code false} otherwise
     */
    @Pure
    boolean isWhole();
    
    /**
     * Negates this
     *
     * @return  this, but with the opposite sign with respect to addition; negated
     */
    @SideEffectFree
    AlgebraNumber negated();
    
    /**
     * The magnitude (usually, absolute value) of this.
     *
     * @implNote    The default implementation is simply {@code isNegative() ? }{@link
     *              #negated()}<code> : this;</code>
     *
     * @return  an AlgebraNumber that is the magnitude of this
     */
    @SideEffectFree
    default AlgebraNumber magnitude() {
        return isNegative() ? negated() : this;
    }
    
    /**
     * Inverts this, that is, yields {@code 1 / this}.
     *
     * @return  the multiplicative reciprocal of this
     *
     * @throws ArithmeticException  if this is {@code 0}
     */
    @SideEffectFree
    AlgebraNumber inverted();
    
    /**
     * Rounds this, using the default {@link RoundingMode} {@link #DEFAULT_ROUNDING}.
     *
     * @implNote    The default implementation simply calls {@link #roundZ(RoundingMode) roundZ(}{@link
     *              #DEFAULT_ROUNDING}{@link #roundZ(RoundingMode) )}
     *
     * @return  this, as an {@link AlgebraInteger}
     *          and rounded according to {@link #DEFAULT_ROUNDING}.
     */
    @SideEffectFree
    default AlgebraInteger roundZ() {
        return roundZ(DEFAULT_ROUNDING);
    }
    
    /**
     * Rounds this, using the specified {@link RoundingMode}.
     *
     * @param roundingMode    the rounding mode to use
     *
     * @return  this, as an AlgebraInteger
     *          and rounded according to {@code roundingMode}.
     */
    @SideEffectFree
    AlgebraInteger roundZ(RoundingMode roundingMode);
    
    /**
     * Rounds this to a {@link Rational}, using the default {@code precision} of {@link #DEFAULT_PRECISION}
     * and {@code roundingMode} of {@link #DEFAULT_ROUNDING}.
     *
     * @return  this rounded according to {@code precision} and {@code roundingMode}
     *
     * @see MathContext#MathContext(int, RoundingMode)
     */
    @SideEffectFree
    Rational roundQ();
    
    /**
     * Rounds this to a {@link Rational}, using the specified {@code precision} and {@code roundingMode}.
     *
     * @implSpec    The result returned here should be identical to that from {@link #roundQ(MathContext)
     *              roundQ(}{@link MathContext#MathContext(int, RoundingMode) new MathContext(}{@code
     *              precision, roundingMode}{@link MathContext#MathContext(int, RoundingMode) )}{@link
     *              #roundQ(MathContext) )} assuming both arguments are non-{@code null}. For {@code
     *              null}, just substitute in the corresponding default(s) first.
     *
     * @param precision     the precision to use; if {@code null} defaults to {@link #DEFAULT_PRECISION}
     *                      and capped at {@link #MAX_PRECISION}
     * @param roundingMode  the {@link RoundingMode} to use&mdash;if {@code null},
     *                      defaults to {@link #DEFAULT_ROUNDING}
     *
     * @return  this rounded according to {@code precision} and {@code roundingMode}
     */
    @SideEffectFree
    Rational roundQ(@Nullable Integer precision, @Nullable RoundingMode roundingMode);
    
    /**
     * Rounds this to a {@link Rational}, using the given {@code mathContext}
     *
     * @param mathContext the {@link MathContext} to use
     *
     * @return  this, as a Rational, rounded according to the given {@link MathContext}.
     */
    @SideEffectFree
    Rational roundQ(MathContext mathContext);
    
    /**
     * Transforms this {@link AlgebraNumber} into a {@link BigDecimal} using default
     * rounding and precision ({@link #DEFAULT_PRECISION} and {@link #DEFAULT_ROUNDING}).
     *
     * @return  a {@link BigDecimal} representing this
     */
    @SideEffectFree
    BigDecimal toBigDecimal();
    
    /**
     * Transforms this into a BigDecimal,
     * using the specified {@code precision} and {@code roundingMode}.
     *
     * @param precision     the precision to use; capped at, and if {@code null} defaults to,
     *                      {@link #DEFAULT_PRECISION}
     * @param roundingMode  the {@link RoundingMode} to use&mdash;if {@code null},
     *                      defaults to {@link #DEFAULT_ROUNDING}
     *
     * @return  a {@link BigDecimal} representing this
     *
     * @see MathContext#MathContext(int, RoundingMode)
     */
    @SideEffectFree
    BigDecimal toBigDecimal(@Nullable Integer precision, @Nullable RoundingMode roundingMode);
    
    /**
     * Transforms this into a BigDecimal using
     * the given {@code mathContext}.
     *
     * @param mathContext   the {@link MathContext} to use
     *
     * @return  a {@link BigDecimal} representing this
     */
    @SideEffectFree
    BigDecimal toBigDecimal(MathContext mathContext);
    
    /**
     * Transforms this into a BigInteger using
     * the default {@link RoundingMode} {@link #DEFAULT_ROUNDING}.
     *
     * @implNote    the default implementation simply calls {@link #toBigInteger(RoundingMode)
     *              toBigInteger(}{@link #DEFAULT_ROUNDING}{@link #toBigInteger(RoundingMode) )}
     *
     * @return  a {@link BigInteger} representing this
     */
    @SideEffectFree
    default BigInteger toBigInteger() {
        return toBigInteger(DEFAULT_ROUNDING);
    }
    
    /**
     * Transforms this into a BigInteger using
     * the given {@code roundingMode}.
     *
     * @param roundingMode  the {@link RoundingMode} to use
     *
     * @return  a {@link BigInteger} representing this
     */
    @SideEffectFree
    BigInteger toBigInteger(RoundingMode roundingMode);
    
    /**
     * Transforms this into a {@code double}.
     *
     * @return  a {@code double} representing this
     */
    @Pure
    double doubleValue();
    
    /**
     * Transforms this into a {@code float}.
     *
     * @return  a {@code float} representing this
     */
    @Pure
    float floatValue();
    
    /**
     * The signum function, such as in {@link Math#signum}, but using {@link Signum}
     * rather than an {@code int} for slightly better switch expression compatibility.
     *
     * @return  {@link Signum#NEGATIVE_ONE} if this is negative, {@link Signum#ZERO} if {@code 0},
     *          and {@link Signum#ONE} if positive
     */
    @Pure
    Signum signum();
    
    /**
     * A more granular form of signum function. Unlike {@link #signum()}, this function also indicates if the
     * value is greater than, less than, or equal to {@code 1} (or {@code -1} if negative).
     *
     * @return  a {@link Sigmagnum} {@link Enum} constant corresponding to the sign and magnitude of
     *          this value.
     */
    @Pure
    Sigmagnum sigmagnum();
    
    /**
     * Checks that these are equivalent in value;
     * that is, that their difference when compared is {@code 0}.
     *
     * @implNote    The default implementation simply calls {@link #compareTo compareTo(}{@code
     *              that}{@link #compareTo )&nbsp;}{@code == 0}.
     *
     * @param that    the value to check against this
     *
     * @return  {@code true} if these are equal in numerical value, otherwise {@code false}
     */
    @Override
    @Pure
    default boolean equiv(
            AlgebraNumber that
    ) {
        return compareTo(that) == 0;
    }
    
    /**
     * Compares this and that, using standard numeric order for real numbers.
     *
     * @param o the number to be compared
     *
     * @return  a negative integer, zero, or a positive integer as this is less than, equal to,
     *          or greater than {@code o}
     */
    @Override
    @Pure
    int compareTo(AlgebraNumber o);
    
    /**
     * <p>Two AlgebraNumbers are equal if they represent the same value and do so as part of the same "Rank".
     * A {@link Rational} subclass which is not in fact an {@link AlgebraInteger} subclass thus cannot equal
     * any {@link AlgebraInteger} subclass, even if both represent the same whole number value.</p>
     *
     * <p>Excepting the above and the fact that this permits (though will return {@code false} for) {@code null},
     * this is equivalent to {@link #equiv} in output for the same input</p>
     *
     * @param obj   The object to test for equality
     *
     * @return  {@code true} if these values are equal and have the same "Rank" or over-arching
     *          interface membership; otherwise {@code false}
     */
    @Override
    @Pure
    boolean equals(@Nullable Object obj);
    
    /**
     * Yields the larger of these values, as if {@link Math#max max(}{@code this, that}{@link Math#max )}.
     *
     * @implNote    The default implementation simply returns {@code (}{@link #compareTo compareTo(}{@code
     *              that}{@link #compareTo )&nbsp}{@code < 0) ? that : this}.
     *
     * @param that  the value to check against this
     *
     * @return  {@code true} if these are equal in numerical value, otherwise {@code false}
     */
    @Pure
    default AlgebraNumber max(
            AlgebraNumber that
    ) {
        return (compareTo(that) < 0) ? that : this;
    }
    
    /**
     * Yields the smaller of these values, as if {@link Math#min min(}{@code this, that}{@link Math#min )}.
     *
     * @implNote    The default implementation simply calls {@code (}{@link #compareTo compareTo(}{@code
     *              that}{@link #compareTo )&nbsp;}{@code > 0) ? that : this}.
     *
     * @param that  the value to check against this
     *
     * @return  {@code true} if these are equal in numerical value, otherwise {@code false}
     */
    @Pure
    default AlgebraNumber min(
            AlgebraNumber that
    ) {
        return (compareTo(that) > 0) ? that : this;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @SideEffectFree
    AlgebraNumber sum(AlgebraNumber augend);
    
    /**
     * {@inheritDoc}
     */
    @Override
    @SideEffectFree
    AlgebraNumber difference(AlgebraNumber subtrahend);
    
    /**
     * {@inheritDoc}
     */
    @Override
    @SideEffectFree
    AlgebraNumber product(AlgebraNumber multiplicand);
    
    /**
     * {@inheritDoc}
     */
    @Override
    @SideEffectFree
    AlgebraNumber quotient(AlgebraNumber divisor);
    
    /**
     * Divides this by {@code divisor}, with the caveat that the quotient is a whole number
     * ({@link AlgebraInteger}).
     * That is, the quotient is the highest value such divisor {@code divisor * quotient <= this}.
     * The second value is the remainder, which is {@code this - (divisor * quotient)}.
     *
     * @implNote    The default implementation finds both quotient and remainder and returns them as a pair.
     *              Many implementations grab the remainder for {@link #remainder} out of this pair,
     *              and so they override this method.
     *
     * @param divisor   the value to divide this by
     *
     * @return  a {@link Record} with the quotient being obtained through
     *          {@link NumberRemainderPair#value() value()}
     *          and the remainder through {@link NumberRemainderPair#remainder() remainder()}
     *
     * @throws ArithmeticException  if dividing by {@code 0}
     */
    @Override
    @SideEffectFree
    default NumberRemainderPair<? extends AlgebraInteger, ? extends AlgebraNumber> quotientZWithRemainder(
            AlgebraNumber divisor
    ) {
        return new NumberRemainderPair<>(quotientZ(divisor), remainder(divisor));
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @SideEffectFree
    AlgebraInteger quotientZ(AlgebraNumber divisor);
    
    /**
     * Yields the quotient of this divided by {@code divisor}, rounded according to {@code roundingMode}.
     * Unlike {@link #quotientZ}, this function allows choice of more types of truncation than just
     * the equivalent to {@link RoundingMode#DOWN}.
     *
     * @implSpec    If {@code roundingMode == }{@link RoundingMode#DOWN}, the result shall be equivalent to
     *              {@link #quotientZ}. If {@code roundingMode == null}, it must default to
     *              {@link #DEFAULT_ROUNDING}.
     *
     * @param divisor       the value to divide this by
     * @param roundingMode  the {@link RoundingMode} to use for the rounded quotient
     *                      &mdash;if {@code null}, defaults to {@link #DEFAULT_ROUNDING}
     *
     * @return  the rounded quotient of this divided by {@code divisor}
     *
     * @throws ArithmeticException  if {@code divisor} is {@code 0}
     */
    @SideEffectFree
    AlgebraInteger quotientRoundZ(AlgebraNumber divisor, @Nullable RoundingMode roundingMode);
    
    /**
     * {@inheritDoc}
     */
    @Override
    @SideEffectFree
    AlgebraNumber remainder(AlgebraNumber divisor);
    
    /**
     * Finds the modulo of this by {@code modulus}, similar to {@code %}. However, in line with other languages,
     * this operation is only valid when {@code modulus} is positive. Furthermore, the returned
     * value is also always positive.
     *
     * @param modulus   the value to pretend divide this by for the purpose of finding the remainder.
     *
     * @return  the remainder as if from {@code this % modulus}
     *
     * @throws ArithmeticException  if {@code modulus <= 0}
     */
    @SideEffectFree
    AlgebraNumber modulo(AlgebraNumber modulus);
    
    /**
     * {@inheritDoc}
     *
     * @implNote    The default implementation simply calls {@link #product product(}{@code this}{@link #product )}.
     */
    @Override
    @SideEffectFree
    default AlgebraNumber squared() {
        return product(this);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @SideEffectFree
    AlgebraNumber raised(int exponent);
    
    /**
     * {@inheritDoc}
     */
    @Override
    @SideEffectFree
    AlgebraNumber raised(AlgebraInteger exponent);
    
//    /**
//     * {@inheritDoc}
//     *
//     * @implNote    the default implementation simply calls {@link #root(int) root(}{@code 2}{@link #root(int) )}
//     */
//    @Override
//    @SideEffectFree
//    default AlgebraNumber sqRoot() {
//        return root(2);
//    }
    
    /**
     * Finds the square root of this, always rounded down to an
     * {@link AlgebraInteger}, as well as the difference between the
     * first returned value, squared, and this original value. The first value is the largest
     * integer {@code x} such that {@code x * x <= this} and the second is {@code this - x * x}.
     *
     * @implNote    The default implementation simply calls {@link #rootZWithRemainder
     *              rootZWithRemainder(}{@code 2}{@link #rootZWithRemainder )}.
     *
     * @return  a {@link Record} of two values, the first ({@link NumberRemainderPair#value() value()})
     *          being the highest {@link AlgebraInteger} less than or equal to the real square root,
     *          and the second ({@link NumberRemainderPair#remainder() remainder()}) being the remainder between
     *          that value squared and this
     *
     * @throws ArithmeticException  if this is negative
     */
    @SideEffectFree
    default NumberRemainderPair<? extends AlgebraInteger, ? extends AlgebraNumber> sqrtZWithRemainder() {
        return rootZWithRemainder(2);
    }
    
    /**
     * Finds the square root of this, rounded to an {@link AlgebraInteger}
     * according to the given {@code roundingMode}.
     * If {@code roundingMode == }{@link RoundingMode#DOWN}, the result is equivalent to
     * the first value in the pair from {@link #sqrtZWithRemainder()}.
     *
     * @implNote    The default implementation simply calls {@link #rootRoundZ
     *              rootRoundZ(}{@code 2, roundingMode}{@link #rootRoundZ )}.
     *
     * @param roundingMode  the {@link RoundingMode} to use for the rounded quotient
     *                      &mdash;if {@code null}, defaults to {@link #DEFAULT_ROUNDING}
     *
     * @return  the rounded square root of this
     *
     * @throws ArithmeticException  if this is negative
     */
    @SideEffectFree
    default AlgebraInteger sqrtRoundZ(
            @Nullable RoundingMode roundingMode
    ) {
        return rootRoundZ(2, roundingMode);
    }
    
    /**
     * Finds the {@code index}<sup>th</sup> root of this, always rounded down to an
     * {@link AlgebraInteger}, as well as the difference between the
     * first returned value, squared, and this original value. The first value is the largest
     * integer {@code x} such that {@code x ^ index <= this} and the second is {@code this - x ^ index}.
     * Here, {@code ^} refers to exponentiation, not logical XOR.
     *
     * @return  a {@link Record} of two values, the first ({@link NumberRemainderPair#value() value()})
     *          being the highest {@link AlgebraInteger} less than or equal to the real root,
     *          and the second ({@link NumberRemainderPair#remainder() remainder()}) being the remainder between
     *          that value squared and this
     *
     * @throws ArithmeticException  if {@code index < 0} and this is greater in magnitude than {@code 1},
     *                              or if {@code index} is even and this is negative
     */
    @SideEffectFree
    NumberRemainderPair<? extends AlgebraInteger, ? extends AlgebraNumber> rootZWithRemainder(int index);
    
    /**
     * Finds the {@code index}<sup>th</sup> root of this, rounded to an
     * {@link AlgebraInteger} according to the given {@link RoundingMode}.
     *
     * @implSpec    If {@code roundingMode == }{@link RoundingMode#DOWN}, the result shall be equivalent to
     *              the first value in the pair from {@link #rootZWithRemainder}.
     *              If {@code roundingMode == null}, it must default to {@link #DEFAULT_ROUNDING}.
     *
     * @param index         the index of the root to take
     * @param roundingMode  the {@link RoundingMode} to use for the rounded quotient
     *                      &mdash;if {@code null}, defaults to {@link #DEFAULT_ROUNDING}
     *
     * @return  the rounded {@code index}<sup>th</sup> root of this
     *
     * @throws ArithmeticException  if {@code index < 0} and this is greater in magnitude than {@code 1},
     *                              or if {@code index} is even and this is negative
     */
    @SideEffectFree
    AlgebraInteger rootRoundZ(int index, @Nullable RoundingMode roundingMode);
    
    /**
     * Finds the {@code index}<sup>th</sup> root of this, always rounded down to an
     * {@link AlgebraInteger}, as well as the difference between the
     * first returned value, squared, and this original value. The first value is the largest
     * integer {@code x} such that {@code x * x <= this} and the second is {@code this - x * x}.
     *
     * @return  a {@link Record} of two values, the first ({@link NumberRemainderPair#value() value()})
     *          being the highest {@link AlgebraInteger} less than or equal to the real root,
     *          and the second ({@link NumberRemainderPair#remainder() remainder()}) being the remainder between
     *          that value squared and this
     *
     * @throws ArithmeticException  if {@code index < 0} and this is greater in magnitude than {@code 1},
     *                              or if {@code index} is even and this is negative
     */
    @SideEffectFree
    NumberRemainderPair<? extends AlgebraInteger, ? extends AlgebraNumber>
            rootZWithRemainder(AlgebraInteger index);
    
    /**
     * Finds the {@code index}<sup>th</sup> root of this, rounded to an {@link AlgebraInteger}
     * according to the given {@link RoundingMode}.
     *
     * @implSpec    If {@code roundingMode == }{@link RoundingMode#DOWN}, the result shall be equivalent to
     *              the first value in the array from {@link #rootZWithRemainder}.
     *              If {@code roundingMode == null}, it must default to {@link #DEFAULT_ROUNDING}.
     *
     * @param index         the index of the root to take
     * @param roundingMode  the {@link RoundingMode} to use for the rounded quotient
     *                      &mdash;if {@code null}, defaults to {@link #DEFAULT_ROUNDING}
     *
     * @return  the rounded {@code index}<sup>th</sup> root of this
     *
     * @throws ArithmeticException  if {@code index < 0} and this is greater in magnitude than {@code 1},
     *                              or if {@code index} is even and this is negative
     */
    @SideEffectFree
    AlgebraInteger rootRoundZ(AlgebraInteger index, @Nullable RoundingMode roundingMode);
    
//    /**
//     * Finds the square root of this, always rounded down to a {@link Rational},
//     * as well as the difference between the first returned value, squared,
//     * and this original value. The first value is the largest rational {@code x} (up to precision
//     * {@code precision}) such that {@code x * x <= this} and the second is {@code this - x * x}.
//     *
//     * @implNote    The default implementation simply calls {@link #rootQWithRemainder
//     *              rootQWithRemainder(}{@code 2, precision}{@link #rootQWithRemainder )}.
//     *
//     * @param precision the precision to use for the approximation; since rationals support
//     *                  theoretically infinite precision (though {@link Rational}s only up to
//     *                  {@link #MAX_PRECISION}, but that is still quite large), the question becomes a
//     *                  matter of how accurate one wants or needs to be
//     *
//     * @return  a {@link Record} of two values, the first ({@link NumberRemainderPair#value() value()})
//     *          being the highest {@link Rational} with the given {@code precision} less than or equal
//     *          to the real square root, and the second ({@link NumberRemainderPair#remainder() remainder()})
//     *          being the remainder between that value squared and this
//     *
//     * @throws ArithmeticException  if this is negative
//     */
//    @SideEffectFree
//    default NumberRemainderPair<? extends Rational, ? extends AlgebraNumber> sqrtQWithRemainder(
//            @Nullable Integer precision
//    ) {
//        return rootQWithRemainder(2, precision);
//    }
//
//    /**
//     * Finds the square root of this, rounded to a
//     * {@link Rational} according to the given {@link MathContext}.
//     * If {@code mathContext} uses {@link RoundingMode#DOWN} and the same precision, the result is equivalent to
//     * the first value in the array from {@link #sqrtQWithRemainder}.
//     *
//     * @implNote    The default implementation simply calls {@link #rootRoundQ rootRoundZ(}{@code
//     *              2, mathContext}{@link #rootRoundQ )}.
//     *
//     * @param mathContext   the {@link MathContext} to use for the rounded quotient
//     *                      &mdash;if {@code null}, defaults to {@link AbstractAlgebraNumber#DEFAULT_CONTEXT}
//     *
//     * @return  the rounded square root of this
//     *
//     * @throws ArithmeticException  if this is negative
//     */
//    @SideEffectFree
//    default Rational sqrtRoundQ(
//            @Nullable MathContext mathContext
//    ) {
//        return rootRoundQ(2, mathContext);
//    }
//
//    /**
//     * Finds the {@code index}<sup>th</sup> root of this, always rounded down to a {@link Rational},
//     * as well as the difference between the first returned value, squared,
//     * and this original value. The first value is the largest rational {@code x} (up to precision
//     * {@code precision}) such that {@code x ^ index <= this} and the second is {@code this - x ^ index}.
//     *
//     * @param index     the index of the root to take
//     * @param precision the precision to use for the approximation; since rationals support
//     *                  theoretically infinite precision (though {@link Rational}s only up to
//     *                  {@link #MAX_PRECISION}, but that is still quite large), the question becomes a
//     *                  matter of how accurate one wants or needs to be
//     *
//     * @return  a {@link Record} of two values, the first ({@link NumberRemainderPair#value() value()})
//     *          being the highest {@link Rational} with the given {@code precision} less than or equal
//     *          to the real root, and the second ({@link NumberRemainderPair#remainder() remainder()})
//     *          being the remainder between that value to the index<sup>th</sup> power and this
//     *
//     * @throws ArithmeticException  if {@code index < 0} and this is greater in magnitude than {@code 1},
//     *                              or if {@code index} is even and this is negative
//     *
//     * @see #rootRoundQ(int, MathContext)
//     */
//    @SideEffectFree
//    NumberRemainderPair<? extends Rational, ? extends AlgebraNumber>
//            rootQWithRemainder(int index, @Nullable Integer precision);
//
//    /**
//     * Finds the {@code index}<sup>th</sup> root of this, rounded to an
//     * {@link Rational} according to the given {@link MathContext}.
//     *
//     * @implSpec    If {@code roundingMode == }{@link RoundingMode#DOWN}, the result shall be equivalent to
//     *              the first value in the array from {@link #rootQWithRemainder}.
//     *              If {@code roundingMode == null}, it must default to {@link #DEFAULT_ROUNDING}.
//     *
//     * @param index         the index of the root to take
//     * @param mathContext   the {@link MathContext} to use for the rounded root
//     *                      &mdash;if {@code null}, defaults to {@link #DEFAULT_ROUNDING}
//     *
//     * @return  the rounded {@code index}<sup>th</sup> root of this
//     *
//     * @throws ArithmeticException  if {@code index < 0} and this is greater in magnitude than {@code 1},
//     *                              or if {@code index} is even and this is negative
//     */
//    @SideEffectFree
//    Rational rootRoundQ(int index, @Nullable MathContext mathContext);
//
//    /**
//     * Finds the {@code index}<sup>th</sup> root of this, always rounded down to a {@link Rational},
//     * as well as the difference between the first returned value, squared,
//     * and this original value. The first value is the largest rational {@code x} (up to precision
//     * {@code precision}) such that {@code x ^ index <= this} and the second is {@code this - x ^ index}.
//     *
//     * @param index     the index of the root to take
//     * @param precision the precision to use for the approximation; since rationals support
//     *                  theoretically infinite precision (though {@link Rational}s only up to
//     *                  {@link #MAX_PRECISION}, but that is still quite large), the question becomes a
//     *                  matter of how accurate one wants or needs to be
//     *
//     * @return  a {@link Record} of two values, the first ({@link NumberRemainderPair#value() value()})
//     *          being the highest {@link Rational} with the given {@code precision} less than or equal
//     *          to the real root, and the second ({@link NumberRemainderPair#remainder() remainder()})
//     *          being the remainder between that value to the index<sup>th</sup> power and this
//     *
//     * @throws ArithmeticException  if {@code index < 0} and this is greater in magnitude than {@code 1},
//     *                              or if {@code index} is even and this is negative
//     *
//     * @see #rootRoundQ(AlgebraInteger, MathContext)
//     */
//    @SideEffectFree
//    NumberRemainderPair<? extends Rational, ? extends AlgebraNumber>
//            rootQWithRemainder(AlgebraInteger index, @Nullable Integer precision);
//
//    /**
//     * Finds the {@code index}<sup>th</sup> root of this, rounded to an
//     * {@link Rational} according to the given {@link MathContext}.
//     *
//     * @implSpec    If the {@link RoundingMode} of {@code mathContext} is {@link RoundingMode#DOWN},
//     *              the result shall be equivalent to
//     *              the value value from {@link RemainderPair#value()} called on {@link #rootQWithRemainder}
//     *              with the same precision. If {@code mathContext == null},
//     *              it must default to one given by {@link #DEFAULT_PRECISION} and {@link #DEFAULT_ROUNDING}.
//     *
//     * @param index         the index of the root to take
//     * @param mathContext   the {@link MathContext} to use for the rounded root&mdash;if
//     *                      {@code null}, defaults to {@link #DEFAULT_ROUNDING}
//     *
//     * @return  the rounded {@code index}<sup>th</sup> root of this
//     *
//     * @throws ArithmeticException  if {@code index < 0} and this is greater in magnitude than {@code 1},
//     *                              or if {@code index} is even and this is negative
//     */
//    @SideEffectFree
//    Rational rootRoundQ(AlgebraInteger index, @Nullable MathContext mathContext);
//
//    /**
//     * Finds the exponentiation of this, always rounded down to an
//     * {@link AlgebraInteger}, as well as the difference between the
//     * first returned value and the real value. The first value is the largest
//     * integer {@code x} such that {@code x <= }{@link Math#E e&nbsp;}{@code ^ this}
//     * and the second is {@link Math#E e&nbsp;}{@code ^ this - x}.
//     *
//     * @return  a {@link Record} of two values, the first being the highest {@link AlgebraInteger}
//     *          less than or equal to the real power result, and the second being the remainder between
//     *          the real result and that value
//     *
//     * @see #expRoundZ
//     */
//    @SideEffectFree
//    NumberRemainderPair<? extends AlgebraInteger, ? extends AlgebraNumber> expZWithRemainder();
//
//    /**
//     * Finds the exponentiation of this, rounded to an
//     * {@link AlgebraInteger} according to the given {@link RoundingMode}.
//     *
//     * @implSpec    If {@code roundingMode == }{@link RoundingMode#DOWN}, the result shall be equivalent to
//     *              the first value in the array from {@link #expZWithRemainder}.
//     *              If {@code roundingMode == null}, it must default to {@link #DEFAULT_ROUNDING}.
//     *
//     * @param roundingMode  the {@link RoundingMode} to use for the rounded result&mdash;if
//     *                      {@code null}, defaults to {@link #DEFAULT_ROUNDING}
//     *
//     * @return  {@link Math#E e}<sup>{@code this}</sup>, rounded accordingly
//     */
//    @SideEffectFree
//    AlgebraInteger expRoundZ(@Nullable RoundingMode roundingMode);
//
//    /**
//     * Finds this to the power of the given {@code power}, always rounded down to an
//     * {@link AlgebraInteger}, as well as the difference between the
//     * first returned value with the inverse power applied, and this original value. The first value is the largest
//     * integer {@code x} such that {@code x ^ power <= this}
//     * and the second is this minus {@code x ^ power}<sup>{@code -1}</sup>
//     *
//     * @param power the power to raise this to
//     *
//     * @return  a {@link Record} of two values, the first being the highest {@link AlgebraInteger}
//     *          less than or equal to the real power result, and the second being the remainder between
//     *          that value given the inverted power and this
//     *
//     * @throws ArithmeticException  if this is negative and {@code power == 0}, or if
//     *                              this is {@code 0} and {@code power <= 0}
//     *
//     * @see #powerRoundZ
//     */
//    @SideEffectFree
//    NumberRemainderPair<? extends AlgebraInteger, ? extends AlgebraNumber> powerZWithRemainder(AlgebraNumber power);
//
//    /**
//     * Finds this to the power of the given {@code power}, rounded to an
//     * {@link AlgebraInteger} according to the given {@link RoundingMode}.
//     *
//     * @implSpec    If {@code roundingMode == }{@link RoundingMode#DOWN}, the result shall be equivalent to
//     *              the first value in the array from {@link #powerZWithRemainder}.
//     *              If {@code roundingMode == null}, it must default to {@link #DEFAULT_ROUNDING}.
//     *
//     * @param power         the power to raise this to
//     * @param roundingMode  the {@link RoundingMode} to use for the rounded result&mdash;if
//     *                      {@code null}, defaults to {@link #DEFAULT_ROUNDING}
//     *
//     * @return  {@code this}<sup>{@code power}</sup>, rounded accordingly
//     *
//     * @throws ArithmeticException  if this is negative and {@code power == 0}, or if
//     *                              this is {@code 0} and {@code power <= 0}
//     */
//    @SideEffectFree
//    AlgebraInteger powerRoundZ(AlgebraNumber power, @Nullable RoundingMode roundingMode);
//
//    /**
//     * Finds the natural log ({@code ln}, or log base {@code e}) of this, always rounded down to an
//     * {@link AlgebraInteger}, as well as the difference between the
//     * first returned value and the real value. The first value is the largest
//     * integer {@code x} such that {@code x <= ln(this)}
//     * and the second is {@code ln(this) - x}.
//     *
//     * @return  a {@link Record} of two values, the first being the highest {@link AlgebraInteger}
//     *          less than or equal to the real logarithm result, and the second being the remainder between
//     *          that value and this
//     *
//     * @throws ArithmeticException  if this is not positive
//     *
//     * @see #lnRoundZ
//     */
//    @SideEffectFree
//    NumberRemainderPair<? extends AlgebraInteger, ? extends AlgebraNumber> lnZWithRemainder();
//
//    /**
//     * Finds the natural log ({@code ln}, or log base {@code e}) of this, rounded to an
//     * {@link AlgebraInteger} according to the given {@link RoundingMode}.
//     *
//     * @implSpec    If {@code roundingMode == }{@link RoundingMode#DOWN}, the result shall be equivalent to
//     *              the first value in the array from {@link #lnZWithRemainder}.
//     *              If {@code roundingMode == null}, it must default to {@link #DEFAULT_ROUNDING}.
//     *
//     * @param roundingMode  the {@link RoundingMode} to use for the rounded result&mdash;if
//     *                      {@code null}, defaults to {@link #DEFAULT_ROUNDING}
//     *
//     * @return  {@code ln(this)}, rounded accordingly
//     *
//     * @throws ArithmeticException  if this is not positive
//     */
//    @SideEffectFree
//    AlgebraInteger lnRoundZ(@Nullable RoundingMode roundingMode);
//
//    /**
//     * Finds the log with base {@code base} ({@code log}<sub>{@code base}</sub>) of this, always rounded down to an
//     * {@link AlgebraInteger}, as well as the difference between the
//     * first returned value and the real value. The first value is the largest
//     * integer {@code x} such that {@code x <= log}<sub>{@code base}</sub>{@code (this)}
//     * and the second is {@code log}<sub>{@code base}</sub>{@code (this) - x}.
//     *
//     * @param base  the base to use for the logarithm, which must itself be positive
//     *
//     * @return  a {@link Record} of two values, the first being the highest {@link AlgebraInteger}
//     *          less than or equal to the real logarithm result, and the second being the remainder between
//     *          that value and this
//     *
//     * @throws ArithmeticException  if either this or {@code base} is not positive
//     *
//     * @see #logBaseRoundZ
//     */
//    @SideEffectFree
//    NumberRemainderPair<? extends AlgebraInteger, ? extends AlgebraNumber> logBaseZWithRemainder(AlgebraNumber base);
//
//    /**
//     * Finds the log with base {@code base} ({@code log}<sub>{@code base}</sub>) of this, rounded to an
//     * {@link AlgebraInteger} according to the given {@link RoundingMode}.
//     *
//     * @implSpec    If {@code roundingMode == }{@link RoundingMode#DOWN}, the result shall be equivalent to
//     *              the first value in the array from {@link #logBaseZWithRemainder}.
//     *              If {@code roundingMode == null}, it must default to {@link #DEFAULT_ROUNDING}.
//     *
//     * @param base          the base to use for the logarithm, which must itself be positive
//     * @param roundingMode  the {@link RoundingMode} to use for the rounded result&mdash;if
//     *                      {@code null}, defaults to {@link #DEFAULT_ROUNDING}
//     *
//     * @return  {@code log}<sub>{@code base}</sub>{@code (this)}, rounded accordingly
//     *
//     * @throws ArithmeticException  if either this or {@code base} is not positive
//     */
//    @SideEffectFree
//    AlgebraInteger logBaseRoundZ(AlgebraNumber base, @Nullable RoundingMode roundingMode);
//
//    /**
//     * Finds the exponentiation of this, always rounded down to a
//     * {@link Rational}, as well as the difference between the
//     * first returned value and the real value. The first value is the largest
//     * rational number {@code x} such that {@code x <= }{@link Math#E e&nbsp;}{@code ^ this}
//     * and the second is {@link Math#E e&nbsp;}{@code ^ this - x}.
//     *
//     * @param precision the precision to use for the approximation; since rationals support
//     *                  theoretically infinite precision (though {@link Rational}s only up to
//     *                  {@link #MAX_PRECISION}, but that is still quite large), the question becomes a
//     *                  matter of how accurate one wants or needs to be
//     *
//     * @return  a {@link Record} of two values, the first being the highest {@link AlgebraInteger}
//     *          less than or equal to the real power result, and the second being the remainder between
//     *          the real result and that value
//     *
//     * @see #expRoundQ
//     */
//    @SideEffectFree
//    NumberRemainderPair<? extends Rational, ? extends AlgebraNumber> expQWithRemainder(@Nullable Integer precision);
//
//    /**
//     * Finds the exponentiation of this, rounded to a
//     * {@link Rational} according to the given {@link MathContext}.
//     *
//     * @implSpec    If the {@link RoundingMode} of {@code mathContext} is {@link RoundingMode#DOWN},
//     *              the result shall be equivalent to
//     *              the value from {@link RemainderPair#value()} called on {@link #expQWithRemainder}
//     *              with the same precision. If {@code mathContext == null},
//     *              it must default to {@link AbstractAlgebraNumber#DEFAULT_CONTEXT}.
//     *
//     * @param mathContext   the {@link MathContext} to use for the rounded result&mdash;if
//     *                      {@code null}, defaults to one given by {@link #DEFAULT_PRECISION}
//     *                      and {@link #DEFAULT_ROUNDING}
//     *
//     * @return  {@link Math#E e}<sup>{@code this}</sup>, rounded accordingly
//     */
//    @SideEffectFree
//    Rational expRoundQ(@Nullable MathContext mathContext);
//
//    /**
//     * Finds this to the power of the given {@code power}, always rounded down to a
//     * {@link Rational}, as well as the difference between the
//     * first returned value with the inverse power applied, and this original value. The first value is the largest
//     * rational number {@code x} such that {@code x ^ power <= this}
//     * and the second is this minus {@code x ^ power}<sup>{@code -1}</sup>
//     *
//     * @param power     the power to raise this to
//     * @param precision the precision to use for the approximation; since rationals support
//     *                  theoretically infinite precision (though {@link Rational}s only up to
//     *                  {@link #MAX_PRECISION}, but that is still quite large), the question becomes a
//     *                  matter of how accurate one wants or needs to be
//     *
//     * @return  a {@link Record} of two values, the first being the highest {@link Rational}
//     *          less than or equal to the real power result, and the second being the remainder between
//     *          that value given the inverted power and this
//     *
//     * @throws ArithmeticException  if this is negative and {@code power == 0}, or if
//     *                              this is {@code 0} and {@code power <= 0}
//     *
//     * @see #powerRoundQ
//     */
//    @SideEffectFree
//    NumberRemainderPair<? extends Rational, ? extends AlgebraNumber>
//            powerQWithRemainder(AlgebraNumber power, @Nullable Integer precision);
//
//    /**
//     * Finds this to the power of the given {@code power}, rounded to a
//     * {@link Rational} according to the given {@link MathContext}.
//     *
//     * @implSpec    If the {@link RoundingMode} of {@code mathContext} is {@link RoundingMode#DOWN},
//     *              the result shall be equivalent to
//     *              the value from {@link RemainderPair#value()} called on {@link #powerQWithRemainder}
//     *              with the same precision. If {@code mathContext == null},
//     *              it must default to {@link AbstractAlgebraNumber#DEFAULT_CONTEXT}.
//     *
//     * @param power         the power to raise this to
//     * @param mathContext   the {@link MathContext} to use for the rounded result&mdash;if
//     *                      {@code null}, defaults to one given by {@link #DEFAULT_PRECISION}
//     *                      and {@link #DEFAULT_ROUNDING}
//     *
//     * @return  {@code this}<sup>{@code power}</sup>, rounded accordingly
//     *
//     * @throws ArithmeticException  if this is negative and {@code power == 0}, or if
//     *                              this is {@code 0} and {@code power <= 0}
//     */
//    @SideEffectFree
//    Rational powerRoundQ(AlgebraNumber power, @Nullable MathContext mathContext);
//
//    /**
//     * Finds the natural log ({@code ln}, or log base {@code e}) of this, always rounded down to a
//     * {@link Rational}, as well as the difference between the
//     * first returned value and the real value. The first value is the largest
//     * rational number {@code x} such that {@code x <= ln(this)}
//     * and the second is {@code ln(this) - x}.
//     *
//     * @param precision the precision to use for the approximation; since rationals support
//     *                  theoretically infinite precision (though {@link Rational}s only up to
//     *                  {@link #MAX_PRECISION}, but that is still quite large), the question becomes a
//     *                  matter of how accurate one wants or needs to be
//     *
//     * @return  a {@link Record} of two values, the first being the highest {@link AlgebraInteger}
//     *          less than or equal to the real logarithm result, and the second being the remainder between
//     *          that value and this
//     *
//     * @throws ArithmeticException  if this is not positive
//     *
//     * @see #lnRoundQ
//     */
//    @SideEffectFree
//    NumberRemainderPair<? extends Rational, ? extends AlgebraNumber>
//            lnQWithRemainder(@Nullable Integer precision);
//
//    /**
//     * Finds the natural log ({@code ln}, or log base {@code e}) of this, rounded to a
//     * {@link Rational} according to the given {@link MathContext}.
//     *
//     * @implSpec    If the {@link RoundingMode} of {@code mathContext} is {@link RoundingMode#DOWN},
//     *              the result shall be equivalent to
//     *              the value from {@link RemainderPair#value()} called on {@link #lnQWithRemainder}
//     *              with the same precision. If {@code mathContext == null},
//     *              it must default to {@link AbstractAlgebraNumber#DEFAULT_CONTEXT}.
//     *
//     * @param mathContext   the {@link MathContext} to use for the rounded result&mdash;if
//     *                      {@code null}, defaults to one given by {@link #DEFAULT_PRECISION}
//     *                      and {@link #DEFAULT_ROUNDING}
//     *
//     * @return  {@code ln(this)}, rounded accordingly
//     *
//     * @throws ArithmeticException  if this is not positive
//     */
//    @SideEffectFree
//    Rational lnRoundQ(@Nullable MathContext mathContext);
//
//    /**
//     * Finds this to the power of the given {@code power}, always rounded down to a
//     * {@link Rational}, as well as the difference between the
//     * first returned value and the real value. The first value is the largest
//     * integer {@code x} such that {@code x <= log}<sub>{@code base}</sub>{@code (this)}
//     * and the second is {@code log}<sub>{@code base}</sub>{@code (this) - x}.
//     *
//     * @param base      the base to use for the logarithm, which must itself be positive
//     * @param precision the precision to use for the approximation; since rationals support
//     *                  theoretically infinite precision (though {@link Rational}s only up to
//     *                  {@link #MAX_PRECISION}, but that is still quite large), the question becomes a
//     *                  matter of how accurate one wants or needs to be
//     *
//     * @return  a {@link Record} of two values, the first being the highest {@link AlgebraInteger}
//     *          less than or equal to the real logarithm result, and the second being the remainder between
//     *          that value and this
//     *
//     * @throws ArithmeticException  if either this or {@code base} is not positive
//     *
//     * @see #logBaseRoundQ
//     */
//    @SideEffectFree
//    NumberRemainderPair<? extends Rational, ? extends AlgebraNumber>
//            logBaseQWithRemainder(AlgebraNumber base, @Nullable Integer precision);
//
//    /**
//     * Finds the natural log ({@code ln}, or log base {@code e}) of this, rounded to a
//     * {@link Rational} according to the given {@link MathContext}.
//     *
//     * @implSpec    If the {@link RoundingMode} of {@code mathContext} is {@link RoundingMode#DOWN},
//     *              the result shall be equivalent to
//     *              the value from {@link RemainderPair#value()} called on
//     *              {@link #logBaseQWithRemainder} with the same precision. If {@code mathContext == null},
//     *              it must default to {@link AbstractAlgebraNumber#DEFAULT_CONTEXT}.
//     *
//     * @param base          the base to use for the logarithm, which must itself be positive
//     * @param mathContext   the {@link MathContext} to use for the rounded result&mdash;if
//     *                      {@code null}, defaults to one given by {@link #DEFAULT_PRECISION}
//     *                      and {@link #DEFAULT_ROUNDING}
//     *
//     * @return  {@code log}<sub>{@code base}</sub>{@code (this)}, rounded accordingly
//     *
//     * @throws ArithmeticException  if either this or {@code base} is not positive
//     */
//    @SideEffectFree
//    Rational logBaseRoundQ(AlgebraNumber base, @Nullable MathContext mathContext);
}
