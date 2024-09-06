package org.cb2384.exactalgebra.objects.numbers.rational;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.function.Function;

import org.cb2384.exactalgebra.objects.internalaccess.factory.IntValuedParameter;
import org.cb2384.exactalgebra.objects.internalaccess.factory.NarrowTo;
import org.cb2384.exactalgebra.objects.internalaccess.factory.Parameter;
import org.cb2384.exactalgebra.objects.numbers.AlgebraNumber;
import org.cb2384.exactalgebra.objects.numbers.integral.AlgebraInteger;
import org.cb2384.exactalgebra.objects.numbers.integral.FiniteInteger;
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
 * {@link #denominator(long)}; all three input types work for all three parameter locations). The factory will
 * normalize the given parameter to simplified improper fraction form for actual construction.</p>
 *
 * <p>If a whole value is not specified, then there is no whole value / it is not a mixed number / the whole
 * part is 0. If the numerator is not specified, it is taken to be 0. If the denominator is not specified, it is
 * taken to be 1.</p>
 *
 * <p>Throws:&ensp;{@link NullPointerException} &ndash; on any {@code null} input, unless otherwise noted</p>
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
    protected @MonotonicNonNull IntValuedParameter numerator;
    
    /**
     * parameter for denominator
     */
    protected @MonotonicNonNull IntValuedParameter denominator;
    
    /**
     * The default, no-argument constructor, explicitly specified in order to provide
     * this javadoc
     */
    @SideEffectFree
    protected RationalFactory() {}
    
    /**
     * Grabs an instance of a factory implementation that is reified to always produce an {@link Rational}.
     *
     * @return  an instance of a factory implementation specifically for {@link Rational}s
     */
    @SideEffectFree
    public static RationalFactory<Rational> newRational() {
        return new RationalFabricator();
    }
    
    /**
     * Shorthand for calling {@link RationalFactory#RationalFactory() new RationalFactory()}{@link
     * RationalFactory#numerator(AlgebraInteger) .numerator(}{@code numerator}{@link
     * RationalFactory#numerator(AlgebraInteger) )}{@link RationalFactory#denominator(AlgebraInteger)
     * .denominator(}{@code denominator}{@link RationalFactory#denominator(AlgebraInteger) )}{@link
     * RationalFactory#build() .build()}.
     *
     * @param numerator     the numerator to give the factory, through
     *                      {@link RationalFactory#numerator(AlgebraInteger)}
     * @param denominator   the denominator to give the factory, through
     *                      {@link RationalFactory#denominator(AlgebraInteger)}
     *
     * @return  the constructed {@link Rational}, per {@link RationalFactory#build()} with the given
     *          {@code numerator} and {@code denominator} parameters
     *
     * @throws ArithmeticException  if {@code denominator == 0}
     */
    @SideEffectFree
    public static Rational fromAlgebraIntegers(
            AlgebraInteger numerator,
            AlgebraInteger denominator
    ) {
        return new RationalFabricator().numerator(numerator).denominator(denominator).build();
    }
    
    /**
     * Shorthand for calling {@link RationalFactory#RationalFactory() new RationalFactory()}{@link
     * RationalFactory#numerator(BigInteger) .numerator(}{@code numerator}{@link
     * RationalFactory#numerator(BigInteger) )}{@link RationalFactory#denominator(BigInteger)
     * .denominator(}{@code denominator}{@link RationalFactory#denominator(BigInteger) )}{@link
     * RationalFactory#build() .build()}.
     *
     * @param numerator     the numerator to give the factory, through
     *                      {@link RationalFactory#numerator(BigInteger)}
     * @param denominator   the denominator to give the factory, through
     *                      {@link RationalFactory#denominator(BigInteger)}
     *
     * @return  the constructed {@link Rational}, per {@link RationalFactory#build()} with the given
     *          {@code numerator} and {@code denominator} parameters
     *
     * @throws ArithmeticException  if {@code denominator == 0}
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
    
    /**
     * <p>All-in-one method for calling {@link #newRational()} and adding 1 to 3 of the given arguments,
     * then {@link #build()}. While all 3 are denoted as being {@link Nullable}, at least one
     * must not be {@code null}. For flexibility, this method accepts any {@link Number},
     * but this does present some issues; any custom number implementation that might hold
     * values that cannot be converted to a finite {@code double} will cause an exception.</p>
     *
     * <p>Since all default {@link Number} implementations are Rational,
     * this also assumes that any input can be converted to a {@code double} and that the {@code double}
     * representation is complete. Each argument is checked for if it is a {@link BigInteger} or
     * {@link BigDecimal}, or a {@link Long} or other long-valued type
     * (like {@link java.util.concurrent.atomic.LongAccumulator}) first, to mitigate the loss of precision,
     * but that only works for the built-in Number subclasses. <b><em>In such a case, precision may
     * be lost without exception or other indication!</b></em></p>
     *
     * @param whole         the whole value to use for the resulting Rational, if {@code null} then it is
     *                      simply not a mixed number
     * @param numerator     the numerator to use for the resulting Rational; if {@code null} defaults to 0
     * @param denominator   the denominator to use for the resulting Rational; if {@code null} defaults to 1
     *
     * @return  the rational constructed from the non-{@code null} arguments
     *
     * @throws NullPointerException     if all three arguments are {@code null}
     * @throws NumberFormatException    if any argument holds a value that, when turned to a double
     *                                  through {@link Number#doubleValue()}, results in a non-finite value
     */
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
     * @param value the {@code double} whose value to make the {@link Rational} from
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
     * @param value the {@link BigDecimal} whose value to make the {@link Rational} from
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
     * Takes a whole parameter for this factory. If there already is a whole parameter given,
     * this will overwrite it.
     *
     * @param value the whole number to use
     *
     * @return  this same factory, to allow chaining to {@link #build()}
     */
    @Override
    @Deterministic
    public @This RationalFactory<N> whole(
            AlgebraInteger value
    ) {
        return (value instanceof FiniteInteger valueFI)
                ? whole(valueFI.longValue())
                : whole(value.toBigInteger());
    }
    
    /**
     * Takes a whole parameter for this factory. If there already is a whole parameter given,
     * this will overwrite it.
     *
     * @param value the whole number to use
     *
     * @return  this same factory, to allow chaining to {@link #build()}
     */
    @Deterministic
    public @This RationalFactory<N> whole(
            BigInteger value
    ) {
        whole = new IntValuedParameter(value);
        return this;
    }
    
    /**
     * Takes a whole parameter for this factory. If there already is a whole parameter given,
     * this will overwrite it.
     *
     * @param value the whole number to use
     *
     * @return  this same factory, to allow chaining to {@link #build()}
     */
    @Deterministic
    public @This RationalFactory<N> whole(
            long value
    ) {
        whole = new IntValuedParameter(value);
        return this;
    }
    
    /**
     * Takes a numerator parameter for this factory. If there already is a numerator parameter given,
     * this will overwrite it.
     *
     * @param value the numerator to use
     *
     * @return  this same factory, to allow chaining for possibly further parameters
     */
    @Deterministic
    public @This RationalFactory<N> numerator(
            AlgebraInteger value
    ) {
        return (value instanceof FiniteInteger valueFI)
                ? numerator(valueFI.longValue())
                : numerator(value.toBigInteger());
    }
    
    /**
     * Takes a numerator parameter for this factory. If there already is a numerator parameter given,
     * this will overwrite it.
     *
     * @param value the numerator to use
     *
     * @return  this same factory, to allow chaining for possibly further parameters
     */
    @Deterministic
    public @This RationalFactory<N> numerator(
            BigInteger value
    ) {
        numerator = new IntValuedParameter(value);
        return this;
    }
    
    /**
     * Takes a numerator parameter for this factory. If there already is a numerator parameter given,
     * this will overwrite it.
     *
     * @param value the numerator to use
     *
     * @return  this same factory, to allow chaining for possibly further parameters
     */
    @Deterministic
    public @This RationalFactory<N> numerator(
            long value
    ) {
        numerator = new IntValuedParameter(value);
        return this;
    }
    
    /**
     * Takes a denominator parameter for this factory. If there already is a denominator parameter given,
     * this will overwrite it.
     *
     * @param value the denominator to use
     *
     * @return  this same factory, to allow chaining for possibly further parameters
     */
    @Deterministic
    public @This RationalFactory<N> denominator(
            AlgebraInteger value
    ) {
        return (value instanceof FiniteInteger valueFI)
                ? denominator(valueFI.longValue())
                : denominator(value.toBigInteger());
    }
    
    /**
     * Takes a denominator parameter for this factory. If there already is a denominator parameter given,
     * this will overwrite it.
     *
     * @param value the denominator to use
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
        denominator = new IntValuedParameter(value);
        return this;
    }
    
    /**
     * Takes a denominator parameter for this factory. If there already is a denominator parameter given,
     * this will overwrite it.
     *
     * @param value the denominator to use
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
        denominator = new IntValuedParameter(value);
        return this;
    }
    
    /**
     * Returns the simplest or smallest {@link Rational} representation of the parameters given previously,
     * after normalization and simplification. Since this function is not strict, in the case that
     * there is no denominator or the denominator is 1 (after normalization), what is actually returned
     * will be an {@link AlgebraInteger}.
     *
     * @return  an {@link Rational} representation of the value of the values given
     *
     * @throws  IllegalStateException   if no parameters have been given yet
     */
    @Override
    public abstract N build();
    
    /**
     * Returns the simplest or smallest {@link Rational} representation of the parameters given previously,
     * after normalization and simplification. Since this function is strict, in the case that
     * there is no denominator or the denominator is 1 (after normalization), a Rational implementation (and
     * not an {@link AlgebraInteger} implementation) will be returned.
     *
     * @return  an {@link Rational} representation of the value of the values given
     *
     * @throws  IllegalStateException   if no parameters have been given yet
     */
    @Override
    public abstract N buildStrict();
    
    /**
     * Actual implementation of parts that aren't meant to be inherited.
     */
    private static final class RationalFabricator
            extends RationalFactory<Rational> {
        
        /**
         * Used for tracking the size of the parameters, to determine the output from {@link #builder}
         */
        private NarrowTo currentDepth = NarrowTo.NULL;
        
        /**
         * basic constructor
         */
        private RationalFabricator() {}
        
        @Override
        public void clear() {
            whole = null;
            numerator = null;
            denominator = null;
            currentDepth = NarrowTo.NULL;
        }
        
        /**
         * {@inheritDoc}
         *
         * @throws  IllegalStateException   if no parameters have been given yet
         */
        @Override
        public Rational build() {
            return builder(true);
        }
        
        /**
         * {@inheritDoc}
         *
         * @throws  IllegalStateException   if no parameters have been given yet
         */
        @Override
        public Rational buildStrict() {
            return builder(false);
        }
        
        /**
         * Normalizes and simplifies the parameters down to just a numerator and denominator, and then
         * builds a {@link Rational} of the appropriate size from those parameters
         *
         * @param unstrict  whether to build strict or not
         *
         * @return  the constructed {@link Rational} (or rather subclass)
         *
         * @throws IllegalStateException    if no parameters have been given yet
         */
        private Rational builder(
                boolean unstrict
        ) {
            normalize();
            
            if (currentDepth == NarrowTo.ARBITRARY) {
                reduceFromBI();
            } else {
                reduceFromPrim();
            }
            
            assert (numerator != null) && (denominator != null) && (denominator.value().signum() == 1);
            if (unstrict && BigMathObjectUtils.isOne(denominator.value())) {
                return numerator.asAlgebraObject();
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
                
                return new FiniteRational(numPrim, denomPrim, numBI, denomBI);
            }
            
            return new ArbitraryRational(numBI, denomBI);
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
                throw new IllegalStateException(Parameter.EMPTY_STATE_EXC);
            }
            
            boolean negativeDenom = false;
            if (noDenominator) {
                denominator = new IntValuedParameter(1);
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
                    numerator = new IntValuedParameter(0);
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
                
                currentDepth = NarrowTo.getPrimNarrow(numerator).getAndCompPrimNarrow(denominator);
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
                
                currentDepth = NarrowTo.getPrimNarrow(numerator).getAndCompPrimNarrow(denominator);
            }
        }
    }
}
