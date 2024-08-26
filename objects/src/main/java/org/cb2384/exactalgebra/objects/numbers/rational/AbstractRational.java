package org.cb2384.exactalgebra.objects.numbers.rational;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.cb2384.exactalgebra.objects.numbers.AbstractAlgebraNumber;
import org.cb2384.exactalgebra.objects.numbers.AlgebraNumber;
import org.cb2384.exactalgebra.objects.numbers.integral.AlgebraInteger;
import org.cb2384.exactalgebra.objects.numbers.integral.ArbitraryInteger;
import org.cb2384.exactalgebra.objects.numbers.integral.IntegerFactory;
import org.cb2384.exactalgebra.objects.pair.NumberRemainderPair;
import org.cb2384.exactalgebra.util.BigMathObjectUtils;
import org.cb2384.exactalgebra.objects.Sigmagnum;
import org.cb2384.exactalgebra.util.corutils.functional.ObjectThenIntToObjectFunction;
import org.cb2384.exactalgebra.util.corutils.ternary.Signum;

import org.checkerframework.checker.nullness.qual.*;
import org.checkerframework.common.returnsreceiver.qual.*;
import org.checkerframework.dataflow.qual.*;

public abstract class AbstractRational
        extends AbstractAlgebraNumber
        implements Rational, Serializable {
    
    private static final int PRECISION_TO_ADD_FOR_FIRST_OP = Math.min(Long.SIZE - DEFAULT_PRECISION, 2);
    
    @Override
    @SideEffectFree
    public AlgebraInteger numeratorAI() {
        return IntegerFactory.fromBigInteger(numeratorBI());
    }
    
    @Override
    @SideEffectFree
    public AlgebraInteger denominatorAI() {
        return IntegerFactory.fromBigInteger(denominatorBI());
    }
    
    @Override
    @SideEffectFree
    public AlgebraInteger wholeAI() {
        return IntegerFactory.fromBigInteger(wholeBI());
    }
    
    @Override
    @SideEffectFree
    public BigInteger wholeBI() {
        return numeratorBI().divide(denominatorBI());
    }
    
    @Override
    @Pure
    public @This Rational roundQ() {
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
    public Rational roundQ(
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
    public Rational roundQ(
            MathContext mathContext
    ) {
        return RationalFactory.fromBigDecimal(toBigDecimal(mathContext));
    }
    
    @Override
    @SideEffectFree
    public BigDecimal toBigDecimal(
            MathContext mathContext
    ) {
        return getBigMathObject(
                x -> new BigDecimal(x, mathContext),
                (x, y) -> x.divide(y, mathContext)
        );
    }
    
    @Override
    @SideEffectFree
    public BigInteger toBigInteger(
            RoundingMode roundingMode
    ) {
        return getBigMathObject(
                Function.identity(),
                (x, y) -> x.divide(y, 0, roundingMode).toBigInteger()
        );
    }
    
    /**
     * Specifically designed for either {@link BigDecimal} or {@link BigInteger}.
     * If this is whole, returns its value according to the first function, otherwise
     * according to the second function.
     *
     * @param   methodIfWhole   the function to use if this is whole
     *
     * @param   opIfElse    a function that takes the {@link BigDecimal} interpretations
     *                      of the numerator and denominator and gets the value to return from those
     *
     * @return  a big math object according to the provided functions
     *
     * @param   <N> the type of big math object, {@link BigInteger} or {@link BigDecimal}
     */
    @SideEffectFree
    <N extends Number> N getBigMathObject(
            Function<BigInteger, N> methodIfWhole,
            BiFunction<BigDecimal, BigDecimal, N> opIfElse
    ) {
        if (isWhole()) {
            return methodIfWhole.apply(numeratorBI());
        }
        
        BigDecimal numBD = new BigDecimal(numeratorBI());
        BigDecimal denBD = new BigDecimal(denominatorBI());
        return opIfElse.apply(numBD, denBD);
    }
    
    @Override
    @Pure
    public double doubleValue() {
        return toBigDecimal().doubleValue();
    }
    
    @Override
    @Pure
    public float floatValue() {
        return toBigDecimal().floatValue();
    }
    
    @Override
    @SideEffectFree
    public String toString(
            int radix
    ) {
        String numS = numeratorAI().toString(radix);
        return isWhole() ?
                numS :
                numS + "/" + denominatorAI().toString(radix);
    }
    
    @Override
    @SideEffectFree
    public String asMixedNumber(
            @Nullable Integer radix
    ) {
        int radixPrim = (radix != null) ? radix : 10;
        AlgebraInteger whole = wholeAI();
        String wholeS = whole.toString(radixPrim);
        if (isWhole()) {
            return wholeS;
        }
        //else
        Rational part = difference(new ArbitraryInteger(whole.toBigInteger()));
        String sepString = isNegative() ? "-(" : "+(";
        return wholeS + sepString + part.toString(radixPrim) + ")";
    }
    
    @Override
    @Pure
    public boolean isZero() {
        return BigMathObjectUtils.isZero(numeratorBI());
    }
    
    @Override
    @Pure
    public boolean isOne() {
        return BigMathObjectUtils.isOne(numeratorBI()) && BigMathObjectUtils.isOne(denominatorBI());
    }
    
    @Override
    @Pure
    public boolean isNegative() {
        return BigMathObjectUtils.isNegative(numeratorBI());
    }
    
    @Override
    @Pure
    public boolean isWhole() {
        return BigMathObjectUtils.isOne(denominatorBI());
    }
    
    @Override
    @Pure
    public Signum signum() {
        return Signum.valueOf(numeratorBI());
    }
    
    @Override
    @Pure
    public Sigmagnum sigmagnum() {
        if (isZero()) {
            return Sigmagnum.ZERO;
        }
        
        if (isNegative()) {
            return switch (compare(Cache.get(1))) {
                case POSITIVE -> Sigmagnum.POSITIVE_SUP_ONE;
                case ZERO -> Sigmagnum.POSITIVE_ONE;
                case NEGATIVE -> Sigmagnum.POSITIVE_SUB_ONE;
            };
        }
        
        return switch (compare(Cache.get(-1))) {
            case POSITIVE -> Sigmagnum.NEGATIVE_SUP_MINUS_ONE;
            case ZERO -> Sigmagnum.NEGATIVE_ONE;
            case NEGATIVE -> Sigmagnum.NEGATIVE_SUB_MINUS_ONE;
        };
    }
    
    @Pure
    public boolean equiv(
            Rational that
    ) {
        return numeratorBI().equals(that.numeratorBI()) && denominatorBI().equals(that.denominatorBI());
    }
    
    @Pure
    public int compareTo(
            AlgebraInteger that
    ) {
        BigInteger thisNum = numeratorBI();
        BigInteger thisDen = denominatorBI();
        
        BigInteger thatNum = thisDen.multiply( that.toBigInteger() );
        
        return thisNum.compareTo(thatNum);
    }
    
    @Pure
    public int compareTo(
            Rational that
    ) {
        BigInteger thisNum = numeratorBI();
        BigInteger thisDen = denominatorBI();
        
        BigInteger thatNum = that.numeratorBI();
        BigInteger thatDen = that.denominatorBI();
        
        thisNum = thisNum.multiply(thatDen);
        thatNum = thatNum.multiply(thisDen);
        
        return thisNum.compareTo(thatNum);
    }
    
    @Override
    @Pure
    public int compareTo(
            AlgebraNumber that
    ) {
        return switch (that) {
            case AlgebraInteger thatAI -> compareTo(thatAI);
            case Rational thatR -> compareTo(thatR);
            default -> -that.compareTo(this);
        };
    }
    
    @Override
    @Pure
    public int hashCode() {
        return ~(0xAAAAAAAA - numeratorBI().hashCode()) ^ (0x55555555 * denominatorBI().hashCode());
    }
    
    @Override
    @Pure
    public boolean equals(
            @Nullable Object obj
    ) {
        return switch (obj) {
            case AlgebraInteger ignored -> false;
            case Rational oAN -> (hashCode() == oAN.hashCode()) && equiv(oAN);
            case null, default -> false;
        };
    }
    
    @Override
    @SideEffectFree
    public Rational negated() {
        return RationalFactory.fromBigIntegers(numeratorBI().negate(), denominatorBI());
    }
    
    @Override
    @SideEffectFree
    public Rational inverted() {
        return RationalFactory.fromBigIntegers(denominatorBI(), numeratorBI());
    }
    
    @SideEffectFree
    private AlgebraNumber biOpHandler(
            AlgebraNumber that,
            BiFunction<AbstractRational, AlgebraInteger, Rational> opIfAI,
            BiFunction<Rational, Rational, Rational> opIfRat,
            BiFunction<AlgebraNumber, Rational, AlgebraNumber> opElse
    ) {
        return switch (that) {
            case AlgebraInteger thatAI -> opIfAI.apply(this, thatAI);
            case Rational thatR -> opIfRat.apply(this, thatR);
            default -> opElse.apply(that, this);
        };
    }
    
    @Override
    @SideEffectFree
    public Rational sum(
            Rational augend
    ) {
        return arithRes(augend, false);
    }
    
    @Override
    @SideEffectFree
    public AlgebraNumber sum(
            AlgebraNumber augend
    ) {
        return biOpHandler(augend, Rational::sum, Rational::sum, AlgebraNumber::sum);
    }
    
    @Override
    @SideEffectFree
    public Rational difference(
            Rational subtrahend
    ) {
        return arithRes(subtrahend, true);
    }
    
    @Override
    @SideEffectFree
    public AlgebraNumber difference(
            AlgebraNumber subtrahend
    ) {
        return biOpHandler(
                subtrahend,
                Rational::difference,
                Rational::difference,
                (y, x) -> y.difference(x).negated()
        );
    }
    
    @SideEffectFree
    private Rational arithRes(
            Rational that,
            boolean subtract
    ) {
        BigInteger thisNum = numeratorBI();
        BigInteger thisDen = denominatorBI();
        
        BigInteger thatNum = that.numeratorBI();
        BigInteger thatDen = that.denominatorBI();
        
        BigInteger newDen = thisDen.multiply(thatDen);
        
        thisNum = thisNum.multiply(thatDen);
        thatNum = thatNum.multiply(thisDen);
        
        BigInteger newNum = (subtract) ?
                thisNum.subtract(thatNum) :
                thisNum.add(thatNum);
        
        return RationalFactory.fromBigIntegers(newNum, newDen);
    }
    
    @SideEffectFree
    public Rational product(
            AlgebraInteger multiplicand
    ) {
        BigInteger newNum = numeratorBI().multiply(multiplicand.toBigInteger());
        return RationalFactory.fromBigIntegers(newNum, denominatorBI());
    }
    
    @Override
    @SideEffectFree
    public Rational product(
            Rational multiplicand
    ) {
        return multRes(multiplicand, false);
    }
    
    @Override
    @SideEffectFree
    public AlgebraNumber product(
            AlgebraNumber multiplicand
    ) {
        return biOpHandler(multiplicand, AbstractRational::product, Rational::product, AlgebraNumber::product);
    }
    
    @Override
    @SideEffectFree
    public NumberRemainderPair<AlgebraInteger, ? extends Rational> quotientZWithRemainder(
            Rational divisor
    ) {
        AlgebraInteger floor = quotientZ(divisor);
        return new NumberRemainderPair<>(this, floor, floor.product(divisor));
    }
    
    @Override
    @SideEffectFree @SuppressWarnings("unchecked")
    public Rational remainder(
            Rational divisor
    ) {
        return quotientZWithRemainder(divisor).remainder();
    }
    
    @Override
    @SideEffectFree
    public Rational modulo(
            Rational modulus
    ) {
        return getModulo(modulus);
    }
    
    @SideEffectFree
    public Rational quotient(
            AlgebraInteger divisor
    ) {
        BigInteger newDen = denominatorBI().multiply(divisor.toBigInteger());
        return RationalFactory.fromBigIntegers(numeratorBI(), newDen);
    }
    
    @Override
    @SideEffectFree
    public Rational quotient(
            Rational divisor
    ) {
        return multRes(divisor, true);
    }
    
    @Override
    @SideEffectFree
    public AlgebraNumber quotient(
            AlgebraNumber divisor
    ) {
        if (isZero()) {
            return Cache.get(0);
        }
        
        return biOpHandler(
                divisor,
                AbstractRational::quotient,
                Rational::quotient,
                (y, x) -> y.quotient(x).inverted()
        );
    }
    
    @SideEffectFree
    private Rational multRes(
            Rational that,
            boolean divide
    ) {
        BigInteger newNum = numeratorBI();
        BigInteger newDen = denominatorBI();
        
        BigInteger thatNum = that.numeratorBI();
        BigInteger thatDen = that.denominatorBI();
        
        if (divide) {
            newNum = newNum.multiply(thatDen);
            newDen = newDen.multiply(thatNum);
        } else {
            newNum = newNum.multiply(thatNum);
            newDen = newDen.multiply(thatDen);
        }
        
        return RationalFactory.fromBigIntegers(newNum, newDen);
    }
    
    @Override
    @SideEffectFree
    public Rational raised(
            int exponent
    ) {
       ObjectThenIntToObjectFunction<Rational, Rational> raiser = (rational, integer) -> {
            BigInteger newNum = rational.numeratorBI().pow(integer);
            BigInteger newDen = rational.denominatorBI().pow(integer);
            return RationalFactory.fromBigIntegers(newNum, newDen);
        };
        
        return intRaiser(exponent, raiser);
    }
    
    @Override
    @SideEffectFree
    public Rational raised(
            AlgebraInteger exponent
    ) {
        return longRaiser(exponent.toBigInteger(), Rational::raised, true);
    }
    
    @Override
    @SideEffectFree
    public NumberRemainderPair<? extends AlgebraInteger, ? extends Rational> rootZWithRemainder(
            int index
    ) {
        AlgebraInteger floor = rootRoundZ(index, RoundingMode.DOWN);
        return new NumberRemainderPair<>(this, floor, floor.raised(index));
    }
    
    @Override
    @SideEffectFree
    public NumberRemainderPair<? extends AlgebraInteger, ? extends Rational> rootZWithRemainder(
            AlgebraInteger index
    ) {
        AlgebraInteger floor = rootRoundZ(index, RoundingMode.DOWN);
        return new NumberRemainderPair<>(this, floor, floor.raised(index));
    }
    
    @Override
    @SideEffectFree
    public NumberRemainderPair<? extends Rational, ? extends Rational> rootQWithRemainder(
            int index,
            @Nullable Integer precision
    ) {
        Rational floor = rootRoundQ(index, getContextFrom(precision, RoundingMode.DOWN));
        return new NumberRemainderPair<>(this, floor, floor.raised(index));
    }
    
    @Override
    @SideEffectFree
    public NumberRemainderPair<? extends Rational, ? extends Rational> rootQWithRemainder(
            AlgebraInteger index,
            @Nullable Integer precision
    ) {
        Rational floor = rootRoundQ(index, getContextFrom(precision, RoundingMode.DOWN));
        return new NumberRemainderPair<>(this, floor, floor.raised(index));
    }
}
