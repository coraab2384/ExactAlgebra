package org.cb2384.exactalgebra.objects.numbers.rational;

import java.math.BigInteger;
import java.math.MathContext;

import org.cb2384.exactalgebra.objects.RationalField;
import org.cb2384.exactalgebra.objects.numbers.AlgebraNumber;
import org.cb2384.exactalgebra.objects.numbers.integral.AlgebraInteger;
import org.cb2384.exactalgebra.objects.pair.NumberRemainderPair;

import org.checkerframework.checker.nullness.qual.*;
import org.checkerframework.common.returnsreceiver.qual.*;
import org.checkerframework.dataflow.qual.*;

/**
 * <p>Rational numbers; numbers that can be represented by an irreducible numerator and denominator pair.</p>
 *
 * <p>Throws:&ensp;{@link NullPointerException} &ndash; on any {@code null} argument,
 * unless otherwise specified.</p>
 *
 * @author  Corinne Buxton
 */
public interface Rational
        extends AlgebraNumber, RationalField<Rational, AlgebraNumber> {
    
    /**
     * The numerator of this Rational value
     *
     * @return  the numerator, as an {@link AlgebraInteger}
     */
    @SideEffectFree
    AlgebraInteger numeratorAI();
    
    /**
     * The denominator of this Rational value
     *
     * @return  the denominator, as an {@link AlgebraInteger}
     */
    @SideEffectFree
    AlgebraInteger denominatorAI();
    
    /**
     * The greatest whole value less than or equal to this value (that is, the whole
     * value that would be used in a mixed number)
     *
     * @return  the numerator divided by the denominator, as an {@link AlgebraInteger}
     *          rounded towards 0
     */
    @SideEffectFree
    AlgebraInteger wholeAI();
    
    /**
     * The numerator of this Rational value
     *
     * @implNote    The default implementation calls {@link #numeratorAI()}{@link #toBigInteger() .toBigInteger()},
     *              but it will likely be more efficient to go the other way.
     *
     * @return  the numerator, as a {@link BigInteger}
     */
    @SideEffectFree
    default BigInteger numeratorBI() {
        return numeratorAI().toBigInteger();
    }
    
    /**
     * The denominator of this Rational value
     *
     * @implNote    The default implementation calls {@link #denominatorAI()}{@link #toBigInteger() .toBigInteger()},
     *              but it will likely be more efficient to go the other way.
     *
     * @return  the denominator, as a {@link BigInteger}
     */
    @SideEffectFree
    default BigInteger denominatorBI() {
        return denominatorAI().toBigInteger();
    }
    
    /**
     * The greatest whole value less than or equal to this value (that is, the whole
     * value that would be used in a mixed number)
     *
     * @implNote    The default implementation calls {@link #wholeAI()}{@link #toBigInteger() .toBigInteger()},
     *              but it will likely be more efficient to go the other way.
     *
     * @return  the numerator divided by the denominator, as a {@link BigInteger}
     *          rounded towards 0
     */
    @SideEffectFree
    default BigInteger wholeBI() {
        return wholeAI().toBigInteger();
    }
    
    /**
     * Similar to {@link #toString(int)}, or {@link #toString()} if the input is {@code null},
     * with the difference that this method returns the value in mixed number format rather than
     * improper fraction format.
     *
     * @param radix the radix to use in the returned representation; if {@code null} defaults to 10,
     *              the default base.
     *
     * @return  this as a mixed number string
     *
     * @throws NumberFormatException    if {@code radix < }{@link Character#MIN_RADIX} or
     *                                  {@code radix > }{@link Character#MAX_RADIX}
     *
     * @see Character#forDigit
     */
    @SideEffectFree
    String asMixedNumber(@Nullable Integer radix);
    
    /**
     * Just like {@link #equiv(AlgebraNumber)}, but specialized for members of this specific interface.
     *
     * @implNote    The default implementation simply calls {@link #compareTo compareTo(}{@code
     *              that}{@link #compareTo )&nbsp;}{@code == 0}.
     *
     * @param that  the value to check against this
     *
     * @return  true if these are equal in numerical value, otherwise false
     */
    @Pure
    default boolean equiv(Rational that) {
        return compareTo(that) == 0;
    }
    
    /**
     * Just like {@link #compareTo(AlgebraNumber)}, but an overload specialized for integer types.
     *
     * @implNote    Since {@link AlgebraInteger}s are {@link Rational}s, the default implementation is simply
     *              to pass the value to {@link #compareTo(Rational)}; it will likely be better to override this.
     *
     * @param that  the AlgebraInteger to be compared
     *
     * @return  a negative integer, zero, or a positive integer as this is less than,
     *          equal to, or greater than {@code that}
     */
    @Pure
    default int compareTo(
            AlgebraInteger that
    ) {
        return compareTo((Rational) that);
    }
    
    /**
     * Just like {@link #compareTo(AlgebraNumber)}, but an overload specialized for members of this specific interface.
     *
     * @implSpec    This result shall be the same as that returned by {@link #compareTo(AlgebraNumber)}
     *
     * @param that  the rational to be compared
     *
     * @return  a negative integer, zero, or a positive integer as this is less than,
     *          equal to, or greater than {@code that}
     */
    @Pure
    int compareTo(Rational that);
    
    /**
     * <p>The contract regarding {@link #equals} suggests that any two values in the same "Rank" (such as
     * any two Rationals that are not {@link AlgebraInteger}s) should be equal if they represent the same value.
     * Since {@link Object#equals} also has a contractual obligation with {@link Object#hashCode()},
     * The hashcodes of all Rationals should be standardized.</p>
     *
     * <p>However, an interface may not override a function from {@link Object},
     * so it cannot be specified through the interface. It should be the bitwise XOR operation ({@code ^})
     * of the hashCodes of the {@link BigInteger} representations of the numerator and the denominator.</p>
     *
     * @implSpec    The result shall be equivalent to that returned by {@link #numeratorBI()}{@link
     *              BigInteger#hashCode() .hashCode()}<code> ^ </code>{@link #denominatorBI()}{@link
     *              BigInteger#hashCode() .hashCode()}.
     *
     * @return  a hashcode for this Rational
     */
    @Override
    @Pure
    int hashCode();
    
    /**
     * As a rational type, trying to round this {@link Rational} simply returns itself
     * when rounded to a rational.
     *
     * @return  this very same rational value
     */
    @Override
    @Pure
    default @This Rational roundQ() {
        return this;
    }
    
    /**
     * Rounds this to a {@link Rational}, using the given {@code mathContext}
     *
     * @param mathContext   the {@link MathContext} to use; if {@code null} then it will be the
     *                      same as {@link #roundQ()}
     *
     * @return  this, as a Rational, rounded according to the given {@link MathContext}
     */
    @Override
    @SideEffectFree
    Rational roundQ(@Nullable MathContext mathContext);
    
    /**
     * {@inheritDoc}
     */
    @Override
    @SideEffectFree
    Rational negated();
    
    /**
     * {@inheritDoc}
     *
     * @implNote    The default implementation is simply {@code isNegative() ? }{@link
     *              #negated()}<code> : this;</code>
     */
    @Override
    @SideEffectFree
    default Rational magnitude() {
        return isNegative() ? negated() : this;
    }
    
    /**
     * {@inheritDoc}
     *
     * @throws ArithmeticException  if this is {@code 0}
     */
    @Override
    @SideEffectFree
    Rational inverted();
    
    /**
     * Yields the larger of these values, as if {@code max(this, that)}.
     *
     * @implNote    The default implementation simply calls {@code (}{@link #compareTo compareTo(}{@code
     *              that}{@link #compareTo )&nbsp;}{@code < 0) ? that : this}.
     *
     * @param that  the value to check against this
     *
     * @return  {@code true} if these are equal in numerical value, otherwise {@code false}
     */
    @Override
    @Pure
    default Rational max(
            Rational that
    ) {
        return (compareTo(that) < 0) ? that : this;
    }
    
    /**
     * Yields the larger of these values, as if {@code max(this, that)}.
     *
     * @implNote    The default implementation simply calls {@code (}{@link #compareTo compareTo(}{@code
     *              that}{@link #compareTo )&nbsp;}{@code > 0) ? that : this}.
     *
     * @param that  the value to check against this
     *
     * @return  {@code true} if these are equal in numerical value, otherwise {@code false}
     */
    @Override
    @Pure
    default Rational min(
            Rational that
    ) {
        return (compareTo(that) > 0) ? that : this;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @SideEffectFree
    Rational sum(Rational augend);
    
    /**
     * {@inheritDoc}
     */
    @Override
    @SideEffectFree
    Rational difference(Rational subtrahend);
    
    /**
     * {@inheritDoc}
     */
    @Override
    @SideEffectFree
    Rational product(Rational multiplicand);
    
    /**
     * Multiplies this by {@code multiplicand}
     *
     * @implNote    Since {@link AlgebraInteger} is an extension of {@link Rational}, the default implementation
     *              simply passes the divisor to {@link #product(Rational)}. This should be overridden.
     *
     * @param multiplicand  the value to multiply this by
     *
     * @return  {@code this * multiplicand}
     */
    @SideEffectFree
    default Rational product(
            AlgebraInteger multiplicand
    ) {
        return product((Rational) multiplicand);
    }
    
    /**
     * {@inheritDoc}
     *
     * @throws ArithmeticException  if {@code divisor == 0}
     */
    @Override
    @SideEffectFree
    Rational quotient(Rational divisor);
    
    /**
     * True division of this by divisor, with no remainder.
     *
     * @implNote    Since {@link AlgebraInteger} is an extension of {@link Rational}, the default implementation
     *              simply passes the divisor to {@link #quotient(Rational)}. This should be overridden.
     *
     * @param divisor   the value to divide this by
     *
     * @return  the quotient of divisor and this
     *
     * @throws ArithmeticException  if {@code divisor == 0}
     */
    default Rational quotient(
            AlgebraInteger divisor
    ) {
        return quotient((Rational) divisor);
    }
    
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
     * @throws ArithmeticException  if {@code divisor == 0}
     */
    @Override
    @SideEffectFree
    default NumberRemainderPair<? extends AlgebraInteger, ? extends Rational> quotientZWithRemainder(
            Rational divisor
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
    Rational remainder(Rational divisor);
    
    /**
     * Finds the modulo of this by {@code modulus}, similar to {@code %}. However, in line with other languages,
     * this operation is only valid when {@code modulus} is positive. Furthermore, the returned
     * value is also always positive.
     *
     * @param modulus the value to pretend divide this by for the purpose of finding the remainder.
     *
     * @return  the remainder as if from {@code this % modulus}
     *
     * @throws ArithmeticException  if {@code modulus <= 0}
     */
    @SideEffectFree
    Rational modulo(Rational modulus);
    
    /**
     * {@inheritDoc}
     *
     * @implNote    The default implementation simply calls {@link #product product(}{@code this}{@link #product )}.
     */
    @Override
    @SideEffectFree
    default Rational squared() {
        return product(this);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @SideEffectFree
    Rational raised(int exponent);
    
    /**
     * {@inheritDoc}
     */
    @Override
    @SideEffectFree
    Rational raised(AlgebraInteger exponent);
    
    /**
     * {@inheritDoc}
     *
     * @implNote    The default implementation simply calls {@link #rootZWithRemainder
     *              rootZWithRemainder(}{@code 2}{@link #rootZWithRemainder )}.
     *
     * @throws ArithmeticException  if this is negative
     */
    @Override
    @SideEffectFree
    default NumberRemainderPair<? extends AlgebraInteger, ? extends Rational> sqrtZWithRemainder() {
        return rootZWithRemainder(2);
    }
    
    /**
     * {@inheritDoc}
     *
     * @throws ArithmeticException  if {@code index} is even and this is negative
     */
    @Override
    @SideEffectFree
    NumberRemainderPair<? extends AlgebraInteger, ? extends Rational> rootZWithRemainder(int index);
    
    /**
     * {@inheritDoc}
     *
     * @throws ArithmeticException  if {@code index} is even and this is negative
     */
    @Override
    @SideEffectFree
    NumberRemainderPair<? extends AlgebraInteger, ? extends Rational> rootZWithRemainder(AlgebraInteger index);
    
//    /**
//     * Finds the square root of this, always rounded down to a {@link Rational},
//     * as well as the difference between the first returned value, squared,
//     * and this original value. The first value is the largest rational {@code x} (up to precision
//     * {@code precision}) such that {@code x * x <= this} and the second is {@code this - x * x}.
//     *
//     * @implNote    The default implementation simply calls {@link #rootQWithRemainder
//     *              rootQWithRemainder(}{@code 2, precision}{@link #rootQWithRemainder )}.
//     *
//     * @param precision   the precision to use for the approximation; since rationals support
//     *                      theoretically infinite precision (though {@link Rational}s only up to
//     *                      {@link #MAX_PRECISION}, but that is still quite large), the question becomes a
//     *                      matter of how accurate one wants or needs to be
//     *
//     * @return  an array of two values, the first being the highest {@link Rational} with the given
//     *          {@code precision}less than or equal to the real square root, and the second being
//     *          the remainder between that value squared and this
//     *
//     * @throws  ArithmeticException if this is negative
//     */
//    @Override
//    @SideEffectFree
//    default NumberRemainderPair<? extends Rational, ? extends Rational> sqrtQWithRemainder(
//            @Nullable Integer precision
//    ) {
//        return rootQWithRemainder(2, precision);
//    }
//
//    /**
//     * Finds the {@code index}<sup>th</sup> root of this, always rounded down to a {@link Rational},
//     * as well as the difference between the first returned value, squared,
//     * and this original value. The first value is the largest rational {@code x} (up to precision
//     * {@code precision}) such that {@code x * x <= this} and the second is {@code this - x * x}.
//     *
//     * @param precision   the precision to use for the approximation; since rationals support
//     *                      theoretically infinite precision (though {@link Rational}s only up to
//     *                      {@link #MAX_PRECISION}, but that is still quite large), the question becomes a
//     *                      matter of how accurate one wants or needs to be
//     *
//     * @return  an array of two values, the first being the highest {@link Rational} with the given
//     *          {@code precision}less than or equal to the real square root, and the second being
//     *          the remainder between that value squared and this
//     *
//     * @throws  ArithmeticException if {@code index} is even and this is negative
//     */
//    @Override
//    @SideEffectFree
//    NumberRemainderPair<? extends Rational, ? extends Rational>
//            rootQWithRemainder(int index, @Nullable Integer precision);
//
//    /**
//     * Finds the {@code index}<sup>th</sup> root of this, always rounded down to a {@link Rational},
//     * as well as the difference between the first returned value, squared,
//     * and this original value. The first value is the largest rational {@code x} (up to precision
//     * {@code precision}) such that {@code x * x <= this} and the second is {@code this - x * x}.
//     *
//     * @param precision   the precision to use for the approximation; since rationals support
//     *                      theoretically infinite precision (though {@link Rational}s only up to
//     *                      {@link #MAX_PRECISION}, but that is still quite large), the question becomes a
//     *                      matter of how accurate one wants or needs to be
//     *
//     * @return  an array of two values, the first being the highest {@link Rational} with the given
//     *          {@code precision}less than or equal to the real square root, and the second being
//     *          the remainder between that value squared and this
//     *
//     * @throws  ArithmeticException if {@code index} is even and this is negative
//     */
//    @Override
//    @SideEffectFree
//    NumberRemainderPair<? extends Rational, ? extends Rational>
//            rootQWithRemainder(AlgebraInteger index, @Nullable Integer precision);
}
