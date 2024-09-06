package org.cb2384.exactalgebra.objects.numbers.integral;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.cb2384.exactalgebra.objects.AlgebraicRing;
import org.cb2384.exactalgebra.objects.internalaccess.CacheInteger;
import org.cb2384.exactalgebra.objects.numbers.AlgebraNumber;
import org.cb2384.exactalgebra.objects.numbers.rational.Rational;
import org.cb2384.exactalgebra.objects.numbers.rational.RationalFactory;
import org.cb2384.exactalgebra.objects.pair.NumberRemainderPair;

import org.checkerframework.checker.nullness.qual.*;
import org.checkerframework.common.returnsreceiver.qual.*;
import org.checkerframework.dataflow.qual.*;

/**
 * <p>Integer values. The implementations of this class are wrappers or adapters for integer-valued-types
 * or arbitrary integer types like {@link BigInteger}. This interface is an extension of {@link Rational},
 * but Rational-specific seeming functions have default implementations here.</p>
 *
 * <p>Throws:&ensp;{@link NullPointerException} &ndash; on any {@code null} argument,
 * unless otherwise specified.</p>
 *
 * @author  Corinne Buxton
 */
public interface AlgebraInteger
        extends AlgebraicRing<AlgebraInteger, AlgebraNumber>, Rational {
    
    /**
     * As this is an {@link AlgebraInteger} and therefore a whole value, its numerator is itself.
     *
     * @return  this, as it is its own numerator due to being whole
     */
    @Override
    @Pure
    default @This AlgebraInteger numeratorAI() {
        return this;
    }
    
    /**
     * As this is an {@link AlgebraInteger} and therefore a whole value, its denominator is 1
     *
     * @return  1 (as a {@link FiniteInteger}), as it is its own numerator due to being whole
     */
    @Override
    @Pure
    default AlgebraInteger denominatorAI() {
        return CacheInteger.CACHE.get(1).getFirst();
    }
    
    /**
     * As this is an {@link AlgebraInteger} it is therefore its own whole value
     *
     * @return  this, as it is its own whole value
     */
    @Override
    @Pure
    default @This AlgebraInteger wholeAI() {
        return this;
    }
    
    /**
     * As this is an {@link AlgebraInteger} and therefore a whole value, its numerator is itself.
     * Thus, the numerator as a {@link BigInteger} is the same as {@link #toBigInteger()}.
     *
     * @return  this as a BigInteger, as it is its own numerator due to being whole
     */
    @Override
    @SideEffectFree
    default BigInteger numeratorBI() {
        return toBigInteger();
    }
    
    /**
     * As this is an {@link AlgebraInteger} and therefore a whole value, its denominator is 1
     *
     * @return  {@link BigInteger#ONE}
     */
    @Override
    @Pure
    default BigInteger denominatorBI() {
        return BigInteger.ONE;
    }
    
    /**
     * As this is an {@link AlgebraInteger} it is therefore its own whole value. Thus, it as a {@link BigInteger}
     * is the same as simply {@link #toBigInteger()}.
     *
     * @return  this as a BigInteger, as it is its own whole value
     */
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
     * @param roundingMode    the rounding mode to use; is irrelevant since this is already
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
     * @param precision   the precision to use, capped at {@link #MAX_PRECISION}; if {@code null} then
     *                      the necessary precision needed to convey this stored value is used (which
     *                      is possible because this is an integral type).
     *
     * @param roundingMode    the {@link RoundingMode} to use &mdash if {@code null},
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
     * @param mathContext the {@link MathContext} to use; this mainly just determines if any precision
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
     * @implNote    The default implementation simply calls {@link BigDecimal#BigDecimal(BigInteger)
     *              new BigDecimal(}{@link #toBigInteger()}{@link BigDecimal#BigDecimal(BigInteger) )}.
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
     * @param precision   the precision to use; is capped at {@link #MAX_PRECISION} and if {@code null}
     *                      defaults to {@link #DEFAULT_PRECISION}
     *
     * @param roundingMode    the {@link RoundingMode} to use &mdash if {@code null},
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
     * @param mathContext the {@link MathContext} to use; this mainly just determines if any precision
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
     * Returns a {@link BigInteger} representing this value. Since AlgebraIntegers are integer types,
     * this result shall be the same as that from {@link #toBigInteger()}.
     *
     * @param roundingMode    the rounding mode to use; is irrelevant since this is already
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
    
    /**
     * returns a {@code long} representation of this
     *
     * @return  this as a {@code long}
     */
    @Pure
    long longValue();
    
    /**
     * Returns a {@code int} representation of this.
     *
     * @implNote    The default implementation simply calls <code>(int)&nbsp;</code>{@link #longValue()}.
     *
     * @return  this as a {@code int}
     */
    @Pure
    default int intValue() {
        return (int) longValue();
    }
    
    /**
     * Returns a {@code short} representation of this.
     *
     * @implNote    The default implementation simply calls <code>(short)&nbsp;</code>{@link #longValue()}.
     *
     * @return  this as a {@code short}
     */
    @Pure
    default short shortValue() {
        return (short) longValue();
    }
    
    /**
     * Returns a {@code long} representation of this.
     *
     * @implNote    The default implementation simply calls <code>(byte)&nbsp;</code>{@link #longValue()}.
     *
     * @return  this as a {@code long}
     */
    @Pure
    default byte byteValue() {
        return (byte) longValue();
    }
    
    /**
     * Yields this as a {@code long}, but throwing an exception if information would be lost
     *
     * @return  a {@code long} representing this value
     *
     * @throws org.cb2384.exactalgebra.objects.exceptions.DisallowedNarrowingException  if information would
     *                                                                                  be lost
     */
    long longValueExact();
    
    /**
     * Yields this as a {@code int}, but throwing an exception if information would be lost
     *
     * @return  a {@code int} representing this value
     *
     * @throws org.cb2384.exactalgebra.objects.exceptions.DisallowedNarrowingException  if information would
     *                                                                                  be lost
     */
    int intValueExact();
    
    /**
     * Yields this as a {@code short}, but throwing an exception if information would be lost
     *
     * @return  a {@code short} representing this value
     *
     * @throws org.cb2384.exactalgebra.objects.exceptions.DisallowedNarrowingException  if information would
     *                                                                                  be lost
     */
    short shortValueExact();
    
    /**
     * Yields this as a {@code byte}, but throwing an exception if information would be lost
     *
     * @return  a {@code byte} representing this value
     *
     * @throws org.cb2384.exactalgebra.objects.exceptions.DisallowedNarrowingException  if information would
     *                                                                                  be lost
     */
    byte byteValueExact();
    
    /**
     * Yields this as a {@code char}, but throwing an exception if information would be lost
     *
     * @return  a {@code char} representing this value
     *
     * @throws org.cb2384.exactalgebra.objects.exceptions.DisallowedNarrowingException  if information would
     *                                                                                  be lost
     */
    char charValueExact();
    
    /**
     * As an integer type, all AlgebraIntegers are whole
     *
     * @return  {@code true}
     */
    @Override
    @Pure
    default boolean isWhole() {
        return true;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @SideEffectFree
    AlgebraInteger negated();
    
    /**
     * {@inheritDoc}
     *
     * @implNote    The default implementation simply calls {@link #isNegative()}<code> ? </code>{@link
     *              #negated()}<code> : this</code>
     */
    @Override
    @SideEffectFree
    default AlgebraInteger magnitude() {
        return isNegative() ? negated() : this;
    }
    
    /**
     * <p>The contract regarding {@link #equals} suggests that any two values in the same "Rank" (such as
     * any two AlgebraIntegers) should be equal if they represent the same value.
     * Since {@link Object#equals} also has a contractual obligation with {@link Object#hashCode()},
     * The hashcodes of all Rationals should be standardized.</p>
     *
     * <p>However, an interface may not override a function from {@link Object},
     * so it cannot be specified through the interface. It should be the bitwise negation operation ({@code ~})
     * of hashcode of the {@link BigInteger} representation of this value.</p>
     *
     * @implSpec    The result shall be equivalent to that returned by {@code ~ }{@link
     *              #toBigInteger()}{@link BigInteger#hashCode() .hashCode()}.
     *
     * @return  a hashcode for this Rational
     */
    @Override
    @Pure
    int hashCode();
    
    /**
     * {@inheritDoc}
     *
     * @implNote    This default implementation simply calls {@code (}{@link #compareTo compareTo(}{@code
     *              that}{@link #compareTo )&nbsp;}{@code < 0) ? that : this}.
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
     * @param that    the value to compare and possibly return
     *
     * @return  {@code that} or this, whichever is larger
     */
    @SideEffectFree
    AlgebraInteger max(long that);
    
    /**
     * Returns the larger of this and {@code that}.
     *
     * @param that    the value to compare and possibly return
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
     * @param that    the value to compare and possibly return
     *
     * @return  {@code that} or this, whichever is smaller
     */
    @SideEffectFree
    AlgebraInteger min(long that);
    
    /**
     * Returns the smaller of this and {@code that}.
     *
     * @param that    the value to compare and possibly return
     *
     * @return  {@code that} or this, whichever is smaller
     */
    @SideEffectFree
    AlgebraInteger min(BigInteger that);
    
    /**
     * Finds the greatest common factor of this and {@code that}..
     *
     * @param that  the value to find the gcf with
     *
     * @return  the gcf
     *
     * @throws ArithmeticException  if both this and {@code that} are 0
     */
    @SideEffectFree
    AlgebraInteger gcf(AlgebraInteger that);
    
    /**
     * Finds the greatest common factor of this and {@code that}..
     *
     * @param that  the value to find the gcf with
     *
     * @return  the gcf
     *
     * @throws ArithmeticException  if both this and {@code that} are 0
     */
    @SideEffectFree
    AlgebraInteger gcf(long that);
    
    /**
     * Finds the greatest common factor of this and {@code that}..
     *
     * @param that  the value to find the gcf with
     *
     * @return  the gcf
     *
     * @throws ArithmeticException  if both this and {@code that} are 0
     */
    @SideEffectFree
    AlgebraInteger gcf(BigInteger that);
    
    /**
     * Finds the least common multiple of this and {@code that}.
     *
     * @param that  the value to find the lcm with
     *
     * @return  the lcm
     *
     * @throws ArithmeticException  if either this or {@code that} are 0
     */
    @SideEffectFree
    AlgebraInteger lcm(AlgebraInteger that);
    
    /**
     * Finds the least common multiple of this and {@code that}.
     *
     * @param that  the value to find the lcm with
     *
     * @return  the lcm
     *
     * @throws ArithmeticException  if either this or {@code that} are 0
     */
    @SideEffectFree
    AlgebraInteger lcm(long that);
    
    /**
     * Finds the least common multiple of this and {@code that}.
     *
     * @param that  the value to find the lcm with
     *
     * @return  the lcm
     *
     * @throws ArithmeticException  if either this or {@code that} are 0
     */
    @SideEffectFree
    AlgebraInteger lcm(BigInteger that);
    
    /**
     * Checks if {@code divisor} can evenly divide this.
     *
     * @param divisor   the prospective divisor to test
     *
     * @return  the boolean equivalent of {@code (divisor % this) == 0}
     */
    @Pure
    boolean canDivideBy(long divisor);
    
    /**
     * Checks if {@code divisor} can evenly divide this.
     *
     * @param divisor   the prospective divisor to test
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
     * @param divisor   the prospective divisor to test
     *
     * @return  the boolean equivalent of {@code (divisor % this) == 0}
     */
    
    @Pure
    default boolean canDivideBy(
            AlgebraInteger divisor
    ) {
        return canDivideBy(divisor.toBigInteger());
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @SideEffectFree
    AlgebraInteger sum(AlgebraInteger augend);
    
    /**
     * {@inheritDoc}
     */
    @Override
    @SideEffectFree
    AlgebraInteger difference(AlgebraInteger subtrahend);
    
    /**
     * {@inheritDoc}
     */
    @Override
    @SideEffectFree
    AlgebraInteger product(AlgebraInteger multiplicand);
    
    /**
     * {@inheritDoc}
     *
     * @implNote    The default implementation finds both quotient and remainder and returns them as a pair.
     *              Many implementations grab the remainder for {@link #remainder} out of this pair,
     *              and so they override this method.
     *
     * @throws ArithmeticException  if {@code divisor == 0}
     */
    @Override
    @SideEffectFree
    default NumberRemainderPair<? extends AlgebraInteger, ? extends AlgebraInteger> quotientZWithRemainder(
            AlgebraInteger divisor
    ) {
        return new NumberRemainderPair<>(quotientZ(divisor), remainder(divisor));
    }
    
    /**
     * {@inheritDoc}
     *
     * @throws ArithmeticException  if {@code divisor == 0}
     */
    @Override
    @SideEffectFree
    AlgebraInteger remainder(AlgebraInteger divisor);
    
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
    AlgebraInteger modulo(AlgebraInteger modulus);
    
    /**
     * Returns a value {@code x} such that {@code (x * this) mod(modulus) = 1}.
     *
     * @param modulus   the modulus to use
     *
     * @return  the modular inverse of this with respect to {@code modulus}
     *
     * @throws ArithmeticException  if {@code modulus <= 0} or {@code modulus} is not
     *                              relatively prime with this
     */
    @SideEffectFree
    AlgebraInteger modInverse(AlgebraInteger modulus);
    
    /**
     * {@inheritDoc}
     *
     * @implNote    The default implementation simply calls {@link #product product(}{@code this}{@link #product )}.
     */
    @Override
    @SideEffectFree
    default AlgebraInteger squared() {
        return product(this);
    }
    
    /**
     * {@inheritDoc}
     *
     * @throws  ArithmeticException if {@code exponent < 0}
     */
    @Override
    @SideEffectFree
    AlgebraInteger raisedZ(int exponent);
    
    /**
     * {@inheritDoc}
     *
     * @throws  ArithmeticException if {@code exponent < 0}
     */
    @Override
    @SideEffectFree
    AlgebraInteger raisedZ(AlgebraInteger exponent);
    
    /**
     * {@inheritDoc}
     *
     * @implNote    The default implementation simply calls {@link #rootZWithRemainder
     *              rootZWithRemainder(}{@code 2}{@link #rootZWithRemainder )}.
     *
     * @throws ArithmeticException  if this is negative
     */
    @SideEffectFree
    default NumberRemainderPair<? extends AlgebraInteger, ? extends AlgebraInteger> sqrtZWithRemainder() {
        return rootZWithRemainder(2);
    }
    
    /**
     * {@inheritDoc}
     *
     * @throws ArithmeticException  if {@code index} is even and this is negative
     */
    @SideEffectFree
    NumberRemainderPair<? extends AlgebraInteger, ? extends AlgebraInteger> rootZWithRemainder(int index);
    
    /**
     * {@inheritDoc}
     *
     * @throws ArithmeticException  if {@code index} is even and this is negative
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
     * Checks if this is an even number, divisible by 2
     *
     * @return  {@code} true if this is even, otherwise {@code false}
     */
    @Pure
    boolean isEven();
    
    /**
     * Returns a {@link List}, in ascending order, of all of the prime factors of this. The returned list is,
     * like most lists, modifiable; wrap using {@link Collections#unmodifiableList} to change this.
     *
     * @return  a {@link List} of all of the prime factors
     */
    @SideEffectFree
    List<? extends AlgebraInteger> primeFactorization();
}
