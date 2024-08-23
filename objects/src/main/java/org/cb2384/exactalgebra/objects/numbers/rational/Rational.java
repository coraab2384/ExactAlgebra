package org.cb2384.exactalgebra.objects.numbers.rational;

import java.math.BigInteger;

import org.cb2384.exactalgebra.objects.RationalField;
import org.cb2384.exactalgebra.objects.numbers.AlgebraNumber;
import org.cb2384.exactalgebra.objects.numbers.integral.AlgebraInteger;
import org.cb2384.exactalgebra.objects.pair.NumberRemainderPair;

import org.checkerframework.checker.nullness.qual.*;
import org.checkerframework.common.returnsreceiver.qual.*;
import org.checkerframework.dataflow.qual.*;

public interface Rational
        extends AlgebraNumber, RationalField<Rational> {
    
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
     * @return  the numerator, as a {@link BigInteger}
     */
    @SideEffectFree
    BigInteger numeratorBI();
    
    /**
     * The denominator of this Rational value
     *
     * @return  the denominator, as a {@link BigInteger}
     */
    @SideEffectFree
    BigInteger denominatorBI();
    
    /**
     * The greatest whole value less than or equal to this value (that is, the whole
     * value that would be used in a mixed number)
     *
     * @return  the numerator divided by the denominator, as a {@link BigInteger}
     *          rounded towards 0
     */
    @SideEffectFree
    BigInteger wholeBI();
    
    @SideEffectFree
    String asMixedNumber(@Nullable Integer radix);
    
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
    
    @Override
    @SideEffectFree
    Rational negated();
    
    @Override
    @SideEffectFree
    default Rational magnitude() {
        return (Rational) AlgebraNumber.super.magnitude();
    }
    
    @Override
    @SideEffectFree
    Rational inverted();
    
    /**
     * Yields the larger of these values, as if {@code max(this, that)}.
     *
     * @implNote    The default implementation simply calls {@code (}{@link #compareTo compareTo(}{@code
     *              that}{@link #compareTo )&nbsp}{@code < 0) ? that : this}.
     *
     * @param   that    the value to check against this
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
     *              that}{@link #compareTo )&nbsp}{@code < 0) ? this : that}.
     *
     * @param   that    the value to check against this
     *
     * @return  {@code true} if these are equal in numerical value, otherwise {@code false}
     */
    @Override
    @Pure
    default Rational min(
            Rational that
    ) {
        return (compareTo(that) < 0) ? this : that;
    }
    
    @Override
    @SideEffectFree
    default NumberRemainderPair<? extends AlgebraInteger, ? extends Rational> quotientZWithRemainder(
            Rational divisor
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
    Rational modulo(Rational modulus);
    
    @Override
    @SideEffectFree
    default Rational squared() {
        return (Rational) AlgebraNumber.super.squared();
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
    default NumberRemainderPair<? extends AlgebraInteger, ? extends Rational> sqrtZWithRemainder() {
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
    NumberRemainderPair<? extends AlgebraInteger, ? extends Rational> rootZWithRemainder(int index);
    
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
    NumberRemainderPair<? extends AlgebraInteger, ? extends Rational> rootZWithRemainder(AlgebraInteger index);
    
    /**
     * Finds the square root of this, always rounded down to a {@link Rational},
     * as well as the difference between the first returned value, squared,
     * and this original value. The first value is the largest rational {@code x} (up to precision
     * {@code precision}) such that {@code x * x <= this} and the second is {@code this - x * x}.
     *
     * @implNote    The default implementation simply calls {@link #rootQWithRemainder
     *              rootQWithRemainder(}{@code 2, precision}{@link #rootQWithRemainder )}.
     *
     * @param   precision   the precision to use for the approximation; since rationals support
     *                      theoretically infinite precision (though {@link Rational}s only up to
     *                      {@link #MAX_PRECISION}, but that is still quite large), the question becomes a
     *                      matter of how accurate one wants or needs to be
     *
     * @return  an array of two values, the first being the highest {@link Rational} with the given
     *          {@code precision}less than or equal to the real square root, and the second being
     *          the remainder between that value squared and this
     *
     * @throws  ArithmeticException if this is negative
     */
    @SideEffectFree
    default NumberRemainderPair<? extends Rational, ? extends Rational> sqrtQWithRemainder(
            @Nullable Integer precision
    ) {
        return rootQWithRemainder(2, precision);
    }
    
    /**
     * Finds the {@code index}<sup>th</sup> root of this, always rounded down to a {@link Rational},
     * as well as the difference between the first returned value, squared,
     * and this original value. The first value is the largest rational {@code x} (up to precision
     * {@code precision}) such that {@code x * x <= this} and the second is {@code this - x * x}.
     *
     * @param   precision   the precision to use for the approximation; since rationals support
     *                      theoretically infinite precision (though {@link Rational}s only up to
     *                      {@link #MAX_PRECISION}, but that is still quite large), the question becomes a
     *                      matter of how accurate one wants or needs to be
     *
     * @return  an array of two values, the first being the highest {@link Rational} with the given
     *          {@code precision}less than or equal to the real square root, and the second being
     *          the remainder between that value squared and this
     *
     * @throws  ArithmeticException if {@code index} is even and this is negative
     */
    @SideEffectFree
    NumberRemainderPair<? extends Rational, ? extends Rational>
            rootQWithRemainder(int index, @Nullable Integer precision);
    
    /**
     * Finds the {@code index}<sup>th</sup> root of this, always rounded down to a {@link Rational},
     * as well as the difference between the first returned value, squared,
     * and this original value. The first value is the largest rational {@code x} (up to precision
     * {@code precision}) such that {@code x * x <= this} and the second is {@code this - x * x}.
     *
     * @param   precision   the precision to use for the approximation; since rationals support
     *                      theoretically infinite precision (though {@link Rational}s only up to
     *                      {@link #MAX_PRECISION}, but that is still quite large), the question becomes a
     *                      matter of how accurate one wants or needs to be
     *
     * @return  an array of two values, the first being the highest {@link Rational} with the given
     *          {@code precision}less than or equal to the real square root, and the second being
     *          the remainder between that value squared and this
     *
     * @throws  ArithmeticException if {@code index} is even and this is negative
     */
    @SideEffectFree
    NumberRemainderPair<? extends Rational, ? extends Rational>
            rootQWithRemainder(AlgebraInteger index, @Nullable Integer precision);
}
