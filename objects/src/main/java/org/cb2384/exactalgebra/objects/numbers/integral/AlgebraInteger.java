package org.cb2384.exactalgebra.objects.numbers.integral;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.cb2384.exactalgebra.objects.AlgebraicRing;
import org.cb2384.exactalgebra.objects.numbers.AlgebraNumber;
import org.cb2384.exactalgebra.objects.numbers.rational.Rational;
import org.cb2384.exactalgebra.objects.numbers.rational.RationalFactory;
import org.cb2384.exactalgebra.objects.pair.NumberRemainderPair;

import org.checkerframework.checker.nullness.qual.*;
import org.checkerframework.common.returnsreceiver.qual.*;
import org.checkerframework.dataflow.qual.*;

public interface AlgebraInteger
        extends AlgebraicRing<AlgebraInteger, AlgebraNumber>, Rational {
    
    @Override
    @Pure
    default @This AlgebraInteger numeratorAI() {
        return this;
    }
    
    @Override
    @Pure
    default AlgebraInteger denominatorAI() {
        return FiniteInteger.valueOf(1);
    }
    
    @Override
    @Pure
    default @This AlgebraInteger wholeAI() {
        return this;
    }
    
    @Override
    @SideEffectFree
    default BigInteger numeratorBI() {
        return toBigInteger();
    }
    
    @Override
    @Pure
    default BigInteger denominatorBI() {
        return BigInteger.ONE;
    }
    
    @Override
    @SideEffectFree
    default BigInteger wholeBI() {
        return toBigInteger();
    }
    
    /**
     * Normally would round this to an integer, but since this
     * already is an integer, simply returns this.
     *
     * @return  this
     */
    @Override
    @Pure
    default @This AlgebraInteger roundZ() {
        return this;
    }
    
    /**
     * Normally would round this to an integer according to the given rounding, but since this
     * already is an integer, simply returns this.
     *
     * @param   roundingMode    the rounding mode to use; is irrelevant since this is already
     *                          an integral type
     *
     * @return  this
     */
    @Override
    @Pure
    default @This AlgebraInteger roundZ(
            @Nullable RoundingMode roundingMode
    ) {
        return this;
    }
    
    /**
     * Essentially just returns this, since there is no rounding needed to go between an integer type
     * and a rational type. However, if {@code precision} is specified, and is smaller than
     * the length of the current precision, then the precision of the returned value
     * might be less; that is also the only time that the {@link RoundingMode} argument is actually
     * used.
     *
     * @param   precision   the precision to use, capped at {@link #MAX_PRECISION}; if {@code null} then
     *                      the necessary precision needed to convey this stored value is used (which
     *                      is possible because this is an integral type).
     *
     * @param   roundingMode    the {@link RoundingMode} to use &mdash if {@code null},
     *                          defaults to {@link #DEFAULT_ROUNDING}
     *
     * @return  a rational that either is this, or is a less precise representation of this value if
     *          for some reason that was specified.
     */
    @Override
    @SideEffectFree
    default Rational roundQ(
            @Nullable Integer precision,
            @Nullable RoundingMode roundingMode
    ) {
        if (precision == null) {
            return this;
        }
        
        MathContext context = new MathContext(
                Math.max(precision, MAX_PRECISION),
                Objects.requireNonNullElse(roundingMode, DEFAULT_ROUNDING)
        );
        
        return roundQ(context);
    }
    
    /**
     * Essentially just returns this, though as a new object, since there is no rounding needed
     * to go between an integer type and a rational type. However, if the precision specified is less
     * than the precision needed to represent this value, then precision will be lost in the
     * returned value as well.
     *
     * @param   mathContext the {@link MathContext} to use; this mainly just determines if any precision
     *                      should be lost, as if the precision of the given context is higher than
     *                      the precision of this value, nothing changes
     *
     * @return  a value that is either equivalent to this value, or is a less precise representation
     *          of this value
     */
    @Override
    @SideEffectFree
    default Rational roundQ(
            MathContext mathContext
    ) {
        return RationalFactory.fromBigDecimal(toBigDecimal(mathContext));
    }
    
    /**
     * Returns this integral value represented as a {@link BigDecimal}.
     *
     * @return  this, but as a {@link BigDecimal}
     */
    @Override
    @SideEffectFree
    default BigDecimal toBigDecimal() {
        return new BigDecimal(toBigInteger());
    }
    
    /**
     * Returns a {@link BigDecimal} representation of this value.
     * If {@code precision} is specified, and is smaller than
     * the length of the current precision, then the precision of the returned value
     * might be less; that is also the only time that the {@link RoundingMode} argument is actually
     * used.
     *
     * @param   precision   the precision to use; is capped at {@link #MAX_PRECISION} and if {@code null}
     *                      defaults to {@link #DEFAULT_PRECISION}
     *
     * @param   roundingMode    the {@link RoundingMode} to use &mdash if {@code null},
     *                          defaults to {@link #DEFAULT_ROUNDING}
     *
     * @return  {@link BigDecimal} representing this value, with the indicated precision
     */
    @Override
    @SideEffectFree
    default BigDecimal toBigDecimal(
            @Nullable Integer precision,
            @Nullable RoundingMode roundingMode
    ) {
        MathContext context = new MathContext(
                (precision != null) ? Math.max(precision, MAX_PRECISION) : DEFAULT_PRECISION,
                Objects.requireNonNullElse(roundingMode, DEFAULT_ROUNDING)
        );
        
        return toBigDecimal(context);
    }
    
    /**
     * Returns a {@link BigDecimal} representation of this value.
     * If {@code precision} is specified, and is smaller than
     * the length of the current precision, then the precision of the returned value
     * might be less; that is also the only time that the {@link RoundingMode} argument is actually
     * used.
     *
     * @param   mathContext the {@link MathContext} to use; this mainly just determines if any precision
     *                      should be lost
     *
     * @return  {@link BigDecimal} representing this value, with the indicated precision
     */
    @Override
    @SideEffectFree
    default BigDecimal toBigDecimal(
            MathContext mathContext
    ) {
        return new BigDecimal(toBigInteger());
    }
    
    /**
     * Returns a {@link BigInteger} representing this value.
     *
     * @return  this value as a {@link BigInteger}
     */
    @Override
    @Pure
    BigInteger toBigInteger();
    
    /**
     * Returns a {@link BigInteger} representing this value.
     *
     * @param   roundingMode    the rounding mode to use; is irrelevant since this is already
     *                          an integral type
     *
     * @return  this value as a {@link BigInteger}
     */
    @Override
    @Pure
    default BigInteger toBigInteger(
            @Nullable RoundingMode roundingMode
    ) {
        return toBigInteger();
    }
    
    long longValue();
    
    default int intValue() {
        return (int) longValue();
    }
    
    default short shortValue() {
        return (short) longValue();
    }
    
    default byte byteValue() {
        return (byte) longValue();
    }
    
    /**
     * {@inheritDoc}
     *
     * @implNote    This default implementation simply calls {@link #toBigInteger()}{@link
     *              BigInteger#floatValue() .floatValue()}.
     */
    @Override
    @Pure
    default float floatValue() {
        return toBigInteger().floatValue();
    }
    
    long longValueExact();
    
    int intValueExact();
    
    short shortValueExact();
    
    byte byteValueExact();
    
    char charValueExact();
    
    @Override
    @Pure
    default boolean isWhole() {
        return true;
    }
    
    @Override
    @SideEffectFree
    AlgebraInteger negated();
    
    @Override
    @SideEffectFree
    default AlgebraInteger magnitude() {
        return (AlgebraInteger) Rational.super.magnitude();
    }
    
    /**
     * {@inheritDoc}
     *
     * @implNote    This default implementation simply calls {@code (}{@link #compareTo compareTo(}{@code
     *              that}{@link #compareTo )&nbsp}{@code < 0) ? that : this}.
     */
    @Override
    @Pure
    default AlgebraInteger max(
            AlgebraInteger that
    ) {
        return (compareTo(that) < 0) ? that : this;
    }
    
    /**
     * Returns the larger of this and {@code that}.
     *
     * @param   that    the value to compare and possibly return
     *
     * @return  {@code that} or this, whichever is larger
     */
    @SideEffectFree
    AlgebraInteger max(long that);
    
    /**
     * Returns the larger of this and {@code that}.
     *
     * @param   that    the value to compare and possibly return
     *
     * @return  {@code that} or this, whichever is larger
     */
    @SideEffectFree
    AlgebraInteger max(BigInteger that);
    
    /**
     * {@inheritDoc}
     *
     * @implNote    This default implementation simply calls {@code (}{@link #compareTo compareTo(}{@code
     *              that}{@link #compareTo )&nbsp}{@code < 0) ? this : that}.
     */
    @Override
    @Pure
    default AlgebraInteger min(
            AlgebraInteger that
    ) {
        return (compareTo(that) < 0) ? this : that;
    }
    
    /**
     * Returns the smaller of this and {@code that}.
     *
     * @param   that    the value to compare and possibly return
     *
     * @return  {@code that} or this, whichever is smaller
     */
    @SideEffectFree
    AlgebraInteger min(long that);
    
    /**
     * Returns the smaller of this and {@code that}.
     *
     * @param   that    the value to compare and possibly return
     *
     * @return  {@code that} or this, whichever is smaller
     */
    @SideEffectFree
    AlgebraInteger min(BigInteger that);
    
    @SideEffectFree
    default AlgebraInteger gcf(
            AlgebraInteger that
    ) {
        return gcf(that.toBigInteger());
    }
    
    @SideEffectFree
    AlgebraInteger gcf(long that);
    
    @SideEffectFree
    AlgebraInteger gcf(BigInteger that);
    
    @SideEffectFree
    default AlgebraInteger lcm(
            AlgebraInteger that
    ) {
        return lcm(that.toBigInteger());
    }
    
    @SideEffectFree
    AlgebraInteger lcm(long that);
    
    @SideEffectFree
    AlgebraInteger lcm(BigInteger that);
    
    /**
     * Checks if {@code divisor} can evenly divide this.
     *
     * @param   divisor the prospective divisor to test
     *
     * @return  the boolean equivalent of {@code (divisor % this) == 0}
     */
    @Pure
    boolean canDivideBy(long divisor);
    
    /**
     * Checks if {@code divisor} can evenly divide this.
     *
     * @param   divisor the prospective divisor to test
     *
     * @return  the boolean equivalent of {@code (divisor % this) == 0}
     */
    @Pure
    boolean canDivideBy(BigInteger divisor);
    
    /**
     * Checks if {@code divisor} can evenly divide this.
     *
     * @implNote    This skeletal implementation uses {@link #canDivideBy(BigInteger) canDivideBy(}{@code
     *              divisor}{@link #toBigInteger() .toBigInteger()}{@link #canDivideBy(BigInteger) )}.
     *
     * @param   divisor the prospective divisor to test
     *
     * @return  the boolean equivalent of {@code (divisor % this) == 0}
     */
    
    @Pure
    default boolean canDivideBy(
            AlgebraInteger divisor
    ) {
        return canDivideBy(divisor.toBigInteger());
    }
    
    @Override
    @SideEffectFree
    default NumberRemainderPair<? extends AlgebraInteger, ? extends AlgebraInteger> quotientZWithRemainder(
            AlgebraInteger divisor
    ) {
        return new NumberRemainderPair<>(quotientZ(divisor), remainder(divisor));
    }
    
    /**
     * Finds the modulo of this by {@code modulus}, similar to {@code %}. However, in line with other languages,
     * this operation is only valid when {@code modulus} is positive. Furthermore, the returned
     * value is also always positive.
     *
     * @param   modulus the value to pretend divide this by for the purpose of finding the remainder.
     *
     * @return  the remainder as if from {@code this % modulus}
     *
     * @throws  ArithmeticException if {@code modulus <= 0}
     */
    @SideEffectFree
    AlgebraInteger modulo(AlgebraInteger modulus);
    
    /**
     * Returns a value {@code x} such that {@code (x * this) mod(modulus) = 1}.
     *
     * @param   modulus the modulus to use
     *
     * @return  the modular inverse of this with respect to {@code modulus}
     *
     * @throws  ArithmeticException if {@code modulus <= 0} or {@code modulus} is not
     *                              relatively prime with this
     */
    @SideEffectFree
    AlgebraInteger modInverse(AlgebraInteger modulus);
    
    @Override
    @SideEffectFree
    default AlgebraInteger squared() {
        return (AlgebraInteger) Rational.super.squared();
    }
    
    /**
     * Finds the square root of this, always rounded down to an
     * {@link Rational}, as well as the difference between the
     * first returned value, squared, and this original value. The first value is the largest
     * integer {@code x} such that {@code x * x <= this} and the second is {@code this - x * x}.
     *
     * @implNote    The default implementation simply calls {@link #rootZWithRemainder
     *              rootZWithRemainder(}{@code 2}{@link #rootZWithRemainder )}.
     *
     * @return  an array of two values, the first being the highest {@link AlgebraInteger}
     *          less than or equal to the real square root, and the second being the remainder between
     *          that value squared and this
     *
     * @throws  ArithmeticException if this is negative
     */
    @SideEffectFree
    default NumberRemainderPair<? extends AlgebraInteger, ? extends AlgebraInteger> sqrtZWithRemainder() {
        return rootZWithRemainder(2);
    }
    
    /**
     * Finds the {@code index}<sup>th</sup> root of this, always rounded down to an
     * {@link AlgebraInteger}, as well as the difference between the
     * first returned value, squared, and this original value. The first value is the largest
     * integer {@code x} such that {@code x * x <= this} and the second is {@code this - x * x}.
     *
     * @return  an array of two values, the first being the highest {@link AlgebraInteger}
     *          less than or equal to the real square root, and the second being the remainder between
     *          that value squared and this
     *
     * @throws  ArithmeticException if {@code index} is even and this is negative
     */
    @SideEffectFree
    NumberRemainderPair<? extends AlgebraInteger, ? extends AlgebraInteger> rootZWithRemainder(int index);
    
    /**
     * Finds the {@code index}<sup>th</sup> root of this, always rounded down to an
     * {@link AlgebraInteger}, as well as the difference between the
     * first returned value, squared, and this original value. The first value is the largest
     * integer {@code x} such that {@code x * x <= this} and the second is {@code this - x * x}.
     *
     * @return  an array of two values, the first being the highest {@link AlgebraInteger}
     *          less than or equal to the real square root, and the second being the remainder between
     *          that value squared and this
     *
     * @throws  ArithmeticException if {@code index} is even and this is negative
     */
    @SideEffectFree
    NumberRemainderPair<? extends AlgebraInteger, ? extends AlgebraInteger> rootZWithRemainder(AlgebraInteger index);
    
    /**
     * Returns a {@link List}, in ascending order, of all of the factors of this. This includes
     * all factors, so that the product of all factors in the list will be higher than the original
     * value unless the original value is prime. The returned list is,
     * like most lists, modifiable; wrap using {@link java.util.Collections#unmodifiableList
     * Collections.unmodifiableList} to change this.
     *
     * @return  a {@link List} of all of the factors
     */
    @SideEffectFree
    List<? extends AlgebraInteger> factors();
    
    /**
     * Checks if this is a prime number (anything less than 2 is an invalid candidate for primacy).
     *
     * @return  {@code true} if this is prime, otherwise {@code false}
     */
    @Pure
    boolean isPrime();
    
    /**
     * Returns a {@link List}, in ascending order, of all of the prime factors of this. The returned list is,
     * like most lists, modifiable; wrap using {@link Collections#unmodifiableList} to change this.
     *
     * @return  a {@link List} of all of the prime factors
     */
    @SideEffectFree
    List<? extends AlgebraInteger> primeFactorization();
}
