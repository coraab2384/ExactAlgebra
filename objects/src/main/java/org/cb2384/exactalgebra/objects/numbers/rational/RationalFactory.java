package org.cb2384.exactalgebra.objects.numbers.rational;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.function.Function;

import org.cb2384.exactalgebra.objects.numbers.AlgebraNumber;
import org.cb2384.exactalgebra.objects.numbers.integral.AlgebraInteger;
import org.cb2384.exactalgebra.objects.numbers.integral.IntegerFactory;
import org.cb2384.exactalgebra.util.BigMathObjectUtils;
import org.cb2384.exactalgebra.util.PrimMathUtils;
import org.cb2384.exactalgebra.util.corutils.NullnessUtils;
import org.cb2384.exactalgebra.util.corutils.ternary.Signum;

import org.checkerframework.checker.nullness.qual.*;
import org.checkerframework.checker.signedness.qual.*;
import org.checkerframework.common.returnsreceiver.qual.*;
import org.checkerframework.dataflow.qual.*;

/**
 * <p>Factory for {@link Rational} types. This factory allows for three parameters: a whole, a numerator,
 * and a denominator (such as through {@link #whole(AlgebraInteger)}, {@link #numerator(BigInteger)}, or
 * {@link #denominator(long)}). The factory will normalize the given parameter to simplified improper fraction
 * form for actual construction.</p>
 *
 * <p>Unlike {@link org.cb2384.exactalgebra.objects.management.numberfactories.IntegerFactory}, which works a bit differently to allow the recycling of an input parameter
 * that happens to be pre-normalized, all other factories are subclasses of this factory. One can think
 * of this factory as being able to make only {@link Rational} types (which still includes
 * {@link AlgebraInteger} as a subclass) but as also being capable of being extended to create
 * more complicated types.</p>
 *
 * <p>Throws: {@link NullPointerException} &ndash; on any {@code null} input, unless otherwise noted</p>
 *
 * @author  Corinne Buxton
 */
public sealed abstract class RationalFactory<N extends AlgebraNumber>
        extends IntegerFactory<N> {
    
    /**
     * Standardized error message for 0 being a denominator, to prevent having to retype it
     */
    protected static final String div0Exc = "0 cannot be a denominator";
    
    /**
     * parameter for numerator
     */
    protected @MonotonicNonNull IntegralParameter numerator;
    
    /**
     * parameter for denominator
     */
    protected @MonotonicNonNull IntegralParameter denominator;
    
    /**
     * The default, no-argument constructor, explicitly specified in order to provide
     * this javadoc
     */
    @SideEffectFree
    protected RationalFactory() {}
    
    @SideEffectFree
    public static RationalFactory<Rational> newRational() {
        return new RationalFabricator();
    }
    
    /**
     * Shorthand for calling {@link RationalFactory#RationalFactory() new RationalFactory()}{@link
     * RationalFactory#numerator(BigInteger) .numerator(}{@code numerator}{@link
     * RationalFactory#numerator(BigInteger) )}{@link RationalFactory#denominator(BigInteger)
     * .denominator(}{@code denominator}{@link RationalFactory#denominator(BigInteger) )}{@link
     * RationalFactory#build() .build()}.
     *
     * @param   numerator   the numerator to give the factory, through
     *                      {@link RationalFactory#numerator(BigInteger)}
     *
     * @param   denominator the denominator to give the factory, through
     *                      {@link RationalFactory#denominator(BigInteger)}
     *
     * @return  the constructed {@link Rational}, per {@link RationalFactory#build()} with the given
     *          {@code numerator} and {@code denominator} parameters
     *
     * @throws  ArithmeticException if {@code denominator == 0}
     */
    @SideEffectFree
    public static Rational fromBigIntegers(
            BigInteger numerator,
            BigInteger denominator
    ) {
        return new RationalFabricator().numerator(numerator).denominator(denominator).build();
    }
    
    /**
     * Shorthand for calling {@link RationalFactory#RationalFactory() new RationalFactory()}{@link
     * RationalFactory#numerator(long) .numerator(}{@code numerator}{@link RationalFactory#numerator(long)
     * )}{@link RationalFactory#denominator(long) .denominator(}{@code denominator}{@link
     * RationalFactory#denominator(long) )}{@link RationalFactory#build() .build()}.
     *
     * @param   numerator   the numerator to give the factory, through
     *                      {@link RationalFactory#numerator(long)}
     *
     * @param   denominator the denominator to give the factory, through
     *                      {@link RationalFactory#denominator(long)}
     *
     * @return  the constructed {@link Rational}, per {@link RationalFactory#build()} with the given
     *          {@code numerator} and {@code denominator} parameters
     *
     * @throws  ArithmeticException if {@code denominator == 0}
     */
    @SideEffectFree
    public static Rational fromLongs(
            long numerator,
            long denominator
    ) {
        return new RationalFabricator().numerator(numerator).denominator(denominator).build();
    }
    
    @SideEffectFree
    public static Rational fromNumbers(
            @Nullable Number whole,
            @Nullable Number numerator,
            @Nullable Number denominator
    ) {
        Function<Number, Rational> valueOf = n -> (Rational) AlgebraNumber.valueOf(n);
        Rational wholeRat = NullnessUtils.applyIfNonNull(whole, valueOf);
        Rational numeratorRat = NullnessUtils.applyIfNonNull(numerator, valueOf);
        Rational denominatorRat = NullnessUtils.applyIfNonNull(denominator, valueOf);
        if (wholeRat == null) {
            if (numeratorRat == null) {
                if (denominatorRat == null) {
                    throw new NullPointerException("At least one argument cannot be null!");
                }
                return denominatorRat.inverted();
            }
            if (denominatorRat == null) {
                return numeratorRat;
            }
            return numeratorRat.quotient(denominatorRat);
        }
        if (numeratorRat == null) {
            return wholeRat;
        }
        Rational fractionPart = numeratorRat.quotient(denominatorRat);
        return (wholeRat.isNegative() && !fractionPart.isNegative())
                ? wholeRat.difference(fractionPart).roundQ()
                : wholeRat.sum(fractionPart).roundQ();
    }
    
    /**
     * Transforms {@code value} into a {@link Rational}, using the actual stored {@code double}
     * value rather than the likely approximation. In this way it is akin to
     * {@link BigDecimal#BigDecimal(double) new BigDecimal(double)} rather than
     * {@link BigDecimal#valueOf(double)}.
     *
     * @param   value   the {@code double} whose value to make the {@link Rational} from
     *
     * @return  a {@link Rational} representation of {@code value}
     *
     * @throws  NumberFormatException   if {@code value} is infinite or {@code NaN}
     */
    @SideEffectFree
    public static Rational fromDouble(
            double value
    ) {
        return fromBigDecimal(new BigDecimal(value));
    }
    
    /**
     * Transforms {@code value} into a {@link Rational}.
     *
     * @param   value   the {@link BigDecimal} whose value to make the {@link Rational} from
     *
     * @return  a {@link Rational} representation of {@code value}
     */
    @SideEffectFree
    public static Rational fromBigDecimal(
            BigDecimal value
    ) {
        BigInteger num = value.unscaledValue();
        int scale = value.scale();
        return switch (Signum.valueOf(scale)) {
            case NEGATIVE -> {
                BigInteger scalar = BigInteger.TEN.pow(-scale);
                yield fromBigInteger(num.multiply(scalar));
            }
            case ZERO -> fromBigInteger(num);
            case POSITIVE -> {
                // 10^scale
                BigInteger den = BigInteger.TEN.pow(scale);
                yield fromBigIntegers(num, den);
            }
        };
    }
    
    /**
     * Takes a numerator parameter for this factory
     *
     * @param   value  an {@link AlgebraInteger} representation of the value to use
     *
     * @return  this same factory, to allow chaining for possibly further parameters
     */
    @Deterministic
    public @This RationalFactory<N> numerator(
            AlgebraInteger value
    ) {
        return numerator(value.toBigInteger());
    }
    
    /**
     * Takes a numerator parameter for this factory
     *
     * @param   value   a {@link BigInteger} representation of the value to use
     *
     * @return  this same factory, to allow chaining for possibly further parameters
     */
    @Deterministic
    public @This RationalFactory<N> numerator(
            BigInteger value
    ) {
        numerator = new IntegralParameter(value);
        return this;
    }
    
    /**
     * Takes a numerator parameter for this factory
     *
     * @param   value   a {@code long} representation of the value to use
     *
     * @return  this same factory, to allow chaining for possibly further parameters
     */
    @Deterministic
    public @This RationalFactory<N> numerator(
            long value
    ) {
        numerator = new IntegralParameter(value);
        return this;
    }
    
    /**
     * Takes a denominator parameter for this factory
     *
     * @param   value   an {@link AlgebraInteger} representation of the value to use
     *
     * @return  this same factory, to allow chaining for possibly further parameters
     */
    @Deterministic
    public @This RationalFactory<N> denominator(
            AlgebraInteger value
    ) {
        return denominator(value.toBigInteger());
    }
    
    /**
     * Takes a denominator parameter for this factory
     *
     * @param   value   a {@link BigInteger} representation of the value to use
     *
     * @return  this same factory, to allow chaining for possibly further parameters
     */
    @Deterministic
    public @This RationalFactory<N> denominator(
            BigInteger value
    ) {
        if (BigMathObjectUtils.isZero(value)) {
            throw new ArithmeticException(div0Exc);
        }
        denominator = new IntegralParameter(value);
        return this;
    }
    
    /**
     * Takes a denominator parameter for this factory
     *
     * @param   value   a {@code long} representation of the value to use
     *
     * @return  this same factory, to allow chaining for possibly further parameters
     */
    @Deterministic
    public @This RationalFactory<N> denominator(
            long value
    ) {
        if (value == 0) {
            throw new ArithmeticException(div0Exc);
        }
        denominator = new IntegralParameter(value);
        return this;
    }
    
    /**
     * Normalizes and simplifies the parameters down to just a numerator and denominator, and then
     * builds a {@link Rational} of the appropriate size from those parameters
     *
     * @return  the constructed {@link Rational} (or rather subclass)
     *
     * @throws IllegalStateException    if no parameters have been given yet
     */
    @Override
    public N build() {
        normalize();
        
        if (currentDepth == NarrowTo.ARBITRARY) {
            reduceFromBI();
        } else {
            reduceFromPrim();
        }
        
        assert (numerator != null) && (denominator != null) && (denominator.value().signum() == 1);
        if (BigMathObjectUtils.isOne(denominator.value())) {
            return (N) numerator.asAlgebraObject();
        }
        boolean canBeFinite = switch(currentDepth) {
            case ARBITRARY -> false;
            case LONG -> (-PrimMathUtils.LONG_TO_INT_MASK <= numerator.primValue())
                    && (numerator.primValue() <= PrimMathUtils.LONG_TO_INT_MASK)
                    && (denominator.primValue() <= PrimMathUtils.NEG_INT_MIN);
            default -> true;
        };
        
        BigInteger numBI = numerator.value();
        BigInteger denomBI = denominator.value();
        
        if (canBeFinite) {
            @Unsigned int numPrim;
            int denomPrim;
            if (numerator.isNegative()) {
                numPrim = (int) -numerator.primValue();
                denomPrim = (int) -denominator.primValue();
            } else {
                numPrim = (int) numerator.primValue();
                // (int) NEG_INT_MIN = 0
                denomPrim = (int) denominator.primValue();
            }
            
            return (N) new FiniteRational(numPrim, denomPrim, numBI, denomBI);
        }
        
        return (N) new ArbitraryRational(numBI, denomBI);
    }
    
    /**
     * Normalizes the parameters; the denominator is made positive (and numerator sign switched if so),
     * the whole number is multiplied by the denominator and added to the numerator,
     * and the denominator of the 0 fraction is standardized to 1
     */
    @EnsuresNonNull({"numerator", "denominator"})
    private void normalize() {
        boolean noWhole = (whole == null);
        boolean noNumerator = (numerator == null);
        boolean noDenominator = (denominator == null);
        
        if (noWhole && noNumerator && noDenominator) {
            throw new IllegalStateException(IllStateExc);
        }
        
        boolean negativeDenom = false;
        if (noDenominator) {
            denominator = new IntegralParameter(1);
            currentDepth = NarrowTo.BYTE;
        } else {
            if (denominator.isNegative()) {
                negativeDenom = true;
                denominator.negate();
            }
            currentDepth = denominator.process(currentDepth);
        }
        
        if (noNumerator) {
            if (noWhole) {
                numerator = new IntegralParameter(0);
            } else {
                BigInteger newNumerator = whole.value().multiply(denominator.value());
                noWhole = true;
                numerator(newNumerator);
            }
        } else if (negativeDenom) {
            numerator.negate();
            currentDepth = numerator.process(currentDepth);
        }
        
        if (!noWhole) {
            BigInteger addToNumerator = whole.value().multiply(denominator.value());
            numerator.reset( numerator.value().add(addToNumerator) );
            currentDepth = numerator.process(currentDepth);
        }
    }
    
    /**
     * simplifies the numerator and denominator to lowest form, if at least one of them is large enough
     * to be a BigInteger
     */
    @RequiresNonNull({"numerator", "denominator"})
    private void reduceFromBI() {
        assert (numerator != null) && (denominator != null);
        BigInteger gcf = numerator.value().gcd(denominator.value());
        if (!BigMathObjectUtils.isOne(gcf)) {
            numerator.reset( numerator.value().divide(gcf) );
            denominator.reset( denominator.value().divide(gcf) );
            
            currentDepth = NarrowTo.NULL;
            currentDepth = numerator.process(currentDepth);
            currentDepth = denominator.process(currentDepth);
        }
    }
    
    /**
     * simplifies the numerator and denominator to lowest form, if both of them can be represented as longs
     * or smaller
     */
    @RequiresNonNull({"numerator", "denominator"})
    private void reduceFromPrim() {
        assert (numerator != null) && (denominator != null);
        long gcf = PrimMathUtils.gcf(numerator.primValue(), denominator.primValue());
        if (gcf != 1) {
            numerator.reset(numerator.primValue() / gcf);
            denominator.reset(denominator.primValue() / gcf);
            
            currentDepth = NarrowTo.NULL;
            currentDepth = numerator.process(currentDepth);
            currentDepth = denominator.process(currentDepth);
        }
    }
    
    private static final class RationalFabricator extends RationalFactory<Rational> {
        
        private RationalFabricator() {}
        
        /**
         * Takes the parameter for this factory in the form of a pre-existing {@link AlgebraInteger}.
         *
         * @param   value   an {@link AlgebraInteger} representation of the whole number to use; this same exact object
         *                  may be returned by this the factory, though it will attempt narrowing if possible
         *
         * @return  this same factory, to allow chaining to {@link #build()}
         */
        @Override
        @Deterministic
        public @This RationalFactory<Rational> whole(
                AlgebraInteger value
        ) {
            return whole(value.toBigInteger());
        }
        
        @Override
        @Deterministic
        public @This RationalFactory<Rational> whole(
                BigInteger value
        ) {
            return (RationalFactory<Rational>) super.whole(value);
        }
        
        @Override
        @Deterministic
        public @This RationalFactory<Rational> whole(
                long value
        ) {
            return (RationalFactory<Rational>) super.whole(value);
        }
    }
}
