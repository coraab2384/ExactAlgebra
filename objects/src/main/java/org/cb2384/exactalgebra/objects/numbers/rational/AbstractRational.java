package org.cb2384.exactalgebra.objects.numbers.rational;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;

import org.cb2384.exactalgebra.objects.numbers.AbstractAlgebraNumber;
import org.cb2384.exactalgebra.objects.numbers.AlgebraNumber;
import org.cb2384.exactalgebra.objects.numbers.integral.AlgebraInteger;
import org.cb2384.exactalgebra.objects.numbers.integral.IntegerFactory;
import org.cb2384.exactalgebra.objects.pair.NumberRemainderPair;
import org.cb2384.exactalgebra.util.BigMathObjectUtils;
import org.cb2384.exactalgebra.objects.Sigmagnum;
import org.cb2384.exactalgebra.util.corutils.functional.ObjectThenIntToObjectFunction;
import org.cb2384.exactalgebra.util.corutils.functional.TriFunction;
import org.cb2384.exactalgebra.util.corutils.ternary.ComparableSwitchSignum;
import org.cb2384.exactalgebra.util.corutils.ternary.Signum;

import org.checkerframework.checker.nullness.qual.*;
import org.checkerframework.common.returnsreceiver.qual.*;
import org.checkerframework.dataflow.qual.*;

/**
 * <p>Skeletal implementations for many parts of the {@link Rational} interface, to make implementation easier.
 * The majority of them rely upon {@link #numeratorBI()} and {@link #denominatorBI()} and
 * to their logic in {@link BigInteger}s.</p>
 *
 * <p>Throws:&ensp;{@link NullPointerException} &ndash; on any {@code null} argument,
 * unless otherwise specified.</p>
 *
 * @author  Corinne Buxton
 */
public abstract class AbstractRational
        extends AbstractAlgebraNumber
        implements Rational {
    
    /**
     * {@inheritDoc}
     *
     * @implNote    This skeletal implementation creates an AlgebraInteger from {@link #numeratorBI()}.
     */
    @Override
    @SideEffectFree
    public AlgebraInteger numeratorAI() {
        return IntegerFactory.fromBigInteger(numeratorBI());
    }
    
    /**
     * {@inheritDoc}
     *
     * @implNote    This skeletal implementation creates an AlgebraInteger from {@link #denominatorBI()}.
     */
    @Override
    @SideEffectFree
    public AlgebraInteger denominatorAI() {
        return IntegerFactory.fromBigInteger(denominatorBI());
    }
    
    /**
     * {@inheritDoc}
     *
     * @implNote    This skeletal implementation creates an AlgebraInteger from {@link #wholeBI()}.
     */
    @Override
    @SideEffectFree
    public AlgebraInteger wholeAI() {
        return IntegerFactory.fromBigInteger(wholeBI());
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @SideEffectFree
    public abstract BigInteger numeratorBI();
    
    /**
     * {@inheritDoc}
     */
    @Override
    @SideEffectFree
    public abstract BigInteger denominatorBI();
    
    /**
     * {@inheritDoc}
     *
     * @implNote    This skeletal implementation takes the truncated quotient {@link #numeratorBI()}{@link
     *              BigInteger#divide(BigInteger) .divide(}{@link #denominatorBI()}{@link
     *              BigInteger#divide(BigInteger) );}
     */
    @Override
    @SideEffectFree
    public BigInteger wholeBI() {
        return numeratorBI().divide(denominatorBI());
    }
    
    /**
     * {@inheritDoc}
     */
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
     * @param precision     the precision to use, capped at {@link #MAX_PRECISION}; if {@code null} then
     *                      the necessary precision needed to convey this stored value is used (which
     *                      is possible because this is an integral type)
     * @param roundingMode  the {@link RoundingMode} to use &mdash if {@code null},
     *                      defaults to {@link #DEFAULT_ROUNDING}
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
    
    /**
     * {@inheritDoc}
     *
     * @implNote    This skeletal implementation relies on {@link #numeratorBI()} and {@link #denominatorBI()}.
     */
    @Override
    @SideEffectFree
    public BigDecimal toBigDecimal(
            MathContext mathContext
    ) {
        return isWhole()
                ? new BigDecimal(numeratorBI(), mathContext)
                : getBMOIfNotWhole(BigDecimal::divide, mathContext);
    }
    
    /**
     * {@inheritDoc}
     *
     * @implNote    This skeletal implementation relies on {@link #numeratorBI()} and {@link #denominatorBI()}.
     */
    @Override
    @SideEffectFree
    public BigInteger toBigInteger(
            RoundingMode roundingMode
    ) {
        return isWhole()
                ? numeratorBI()
                : getBMOIfNotWhole( (num, den, rounder)
                        -> num.divide(den, 0, rounder).toBigInteger(), roundingMode );
    }
    
    /**
     * Specifically designed for either {@link BigDecimal} or {@link BigInteger}.
     * If this is whole, returns its value according to the first function, otherwise
     * according to the second function.
     *
     * @param op            a function that takes the {@link BigDecimal} interpretations
     *                      of the numerator and denominator as well as a context argument
     *                      and gets the value to return from those
     * @param contextArg    the third argument, intended to be a {@link MathContext} or a {@link RoundingMode}
     *                      but really it's whatever makes the function work, including possibly even {@code null}
     *
     * @return  a big math object according to the provided function
     *
     * @param <I>   the type of the context or rounding argument, intended to be
     *              {@link MathContext} or a {@link RoundingMode}
     * @param <O>   the type of big math object, {@link BigInteger} or {@link BigDecimal}
     */
    @SideEffectFree
    private <I, O> O getBMOIfNotWhole(
            TriFunction<BigDecimal, BigDecimal, @PolyNull I, O> op,
            @PolyNull I contextArg
    ) {
        BigDecimal numBD = new BigDecimal(numeratorBI());
        BigDecimal denBD = new BigDecimal(denominatorBI());
        return op.apply(numBD, denBD, contextArg);
    }
    
    /**
     * {@inheritDoc}
     *
     * @implNote    This skeletal implementation simply calls {@link #toBigDecimal()}{@link
     *              BigDecimal#doubleValue() .doubleValue()}.
     */
    @Override
    @Pure
    public double doubleValue() {
        return toBigDecimal().doubleValue();
    }
    
    /**
     * {@inheritDoc}
     *
     * @implNote    This skeletal implementation simply calls {@link #toBigDecimal()}{@link
     *              BigDecimal#floatValue() .floatValue()}.
     */
    @Override
    @Pure
    public float floatValue() {
        return toBigDecimal().floatValue();
    }
    
    /**
     * {@inheritDoc}
     *
     * @implNote    This skeletal implementation relies on {@link #numeratorBI()} and {@link #denominatorBI()},
     *              as well as {@link #isWhole()}.
     */
    @Override
    @SideEffectFree
    public String toString(
            int radix
    ) {
        String numS = numeratorBI().toString(radix);
        return isWhole() ?
                numS :
                numS + "/" + denominatorBI().toString(radix);
    }
    
    /**
     * {@inheritDoc}
     *
     * @implNote    This skeletal implementation relies on {@link #wholeAI()} to find the whole value
     *              and {@link #difference(Rational)} to find the fraction part, which then uses
     *              {@link #toString(int)} for building the fraction part.
     *              It also uses {@link #isWhole()} and {@link #isNegative()}.
     */
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
        Rational part = difference(whole);
        String sepString = isNegative() ? "-(" : "+(";
        return wholeS + sepString + part.toString(radixPrim) + ")";
    }
    
    /**
     * {@inheritDoc}
     *
     * @implNote    This skeletal implementation simply checks if {@link #numeratorBI()} is 0.
     */
    @Override
    @Pure
    public boolean isZero() {
        return BigMathObjectUtils.isZero(numeratorBI());
    }
    
    /**
     * {@inheritDoc}
     *
     * @implNote    This skeletal implementation simply gets {@link #numeratorBI()} and {@link #denominatorBI()}
     *              and checks if they are both 1.
     */
    @Override
    @Pure
    public boolean isOne() {
        return BigMathObjectUtils.isOne(numeratorBI()) && BigMathObjectUtils.isOne(denominatorBI());
    }
    
    /**
     * {@inheritDoc}
     *
     * @implNote    This skeletal implementation simply checks if {@link #numeratorBI()} is negative.
     */
    @Override
    @Pure
    public boolean isNegative() {
        return BigMathObjectUtils.isNegative(numeratorBI());
    }
    
    /**
     * {@inheritDoc}
     *
     * @implNote    This skeletal implementation simply checks if {@link #denominatorBI()} is 1.
     */
    @Override
    @Pure
    public boolean isWhole() {
        return BigMathObjectUtils.isOne(denominatorBI());
    }
    
    /**
     * {@inheritDoc}
     *
     * @implNote    This skeletal implementation relies on {@link #numeratorBI()} and gets the signum from that.
     */
    @Override
    @Pure
    public Signum signum() {
        return Signum.valueOf(numeratorBI());
    }
    
    /**
     * {@inheritDoc}
     *
     * @implNote    This skeletal implementation relies on {@link #isNegative()} and
     *              {@link #compare(ComparableSwitchSignum) compare}.
     */
    @Override
    @Pure
    public Sigmagnum sigmagnum() {
        if (isZero()) {
            return Sigmagnum.ZERO;
        }
        
        if (isNegative()) {
            return switch (compare(getFromCache(1))) {
                case POSITIVE -> Sigmagnum.POSITIVE_SUP_ONE;
                case ZERO -> Sigmagnum.POSITIVE_ONE;
                case NEGATIVE -> Sigmagnum.POSITIVE_SUB_ONE;
            };
        }
        
        return switch (compare(getFromCache(-1))) {
            case POSITIVE -> Sigmagnum.NEGATIVE_SUP_MINUS_ONE;
            case ZERO -> Sigmagnum.NEGATIVE_ONE;
            case NEGATIVE -> Sigmagnum.NEGATIVE_SUB_MINUS_ONE;
        };
    }
    
    /**
     * {@inheritDoc}
     *
     * @implNote    This skeletal implementation relies on {@link #numeratorBI()} and {@link #denominatorBI()}.
     */
    @Override
    @Pure
    public boolean equiv(
            Rational that
    ) {
        return numeratorBI().equals(that.numeratorBI()) && denominatorBI().equals(that.denominatorBI());
    }
    
    /**
     * {@inheritDoc}
     *
     * @implNote    This skeletal implementation relies on {@link #numeratorBI()} and {@link #denominatorBI()},
     *              as well is {@link AlgebraInteger#toBigInteger()} for {@code that}.
     */
    @Override
    @Pure
    public int compareTo(
            AlgebraInteger that
    ) {
        BigInteger thisNum = numeratorBI();
        BigInteger thisDen = denominatorBI();
        
        BigInteger thatNum = thisDen.multiply( that.toBigInteger() );
        
        return thisNum.compareTo(thatNum);
    }
    
    /**
     * {@inheritDoc}
     *
     * @implNote    This skeletal implementation relies on {@link #numeratorBI()} and {@link #denominatorBI()}.
     */
    @Override
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
    
    /**
     * {@inheritDoc}
     *
     * @implNote    This skeletal implementation calls {@link #compareTo(AlgebraInteger)} or {@link
     *              #compareTo(Rational)}, depending on the class of {@code o}. If {@code o} is neither,
     *              then it becomes the receiver (and the result reversed); since {@code o} is an
     *              {@link AlgebraNumber}, it is also required to implement this function after all.
     */
    @Override
    @Pure
    public int compareTo(
            AlgebraNumber o
    ) {
        return switch (o) {
            case AlgebraInteger oAI -> compareTo(oAI);
            case Rational oR -> compareTo(oR);
            default -> -o.compareTo(this);
        };
    }
    
    /**
     * {@inheritDoc}
     *
     * @implNote    In line with the interface specification, this method calls {@link #numeratorBI()}{@link
     *              BigInteger#hashCode() .hashCode()}<code> ^ </code>{@link #denominatorBI()}{@link
     *              BigInteger#hashCode() .hashCode()}.
     */
    @Override
    @Pure
    public int hashCode() {
        return numeratorBI().hashCode() ^ denominatorBI().hashCode();
    }
    
    /**
     * {@inheritDoc}
     *
     * @implNote    In line with the interface specification, this method actually calls {@link #equiv(Rational)}
     *              after checking the class and "Rank" requirements.
     */
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
    
    /**
     * {@inheritDoc}
     *
     * @implNote    This skeletal implementation relies on {@link #numeratorBI()} and {@link #denominatorBI()}.
     */
    @Override
    @SideEffectFree
    public Rational negated() {
        return RationalFactory.fromBigIntegers(numeratorBI().negate(), denominatorBI());
    }
    
    /**
     * {@inheritDoc}
     *
     * @implNote    This skeletal implementation relies on {@link #numeratorBI()} and {@link #denominatorBI()}.
     *
     * @throws ArithmeticException  if this is {@code 0}
     */
    @Override
    @SideEffectFree
    public Rational inverted() {
        return RationalFactory.fromBigIntegers(denominatorBI(), numeratorBI());
    }
    
    /**
     * {@inheritDoc}
     *
     * @implNote    This skeletal implementation relies on {@link #numeratorBI()} and {@link #denominatorBI()}.
     */
    @Override
    @SideEffectFree
    public Rational sum(
            Rational augend
    ) {
        return arithRes(augend, false);
    }
    
    /**
     * {@inheritDoc}
     *
     * @implNote    This skeletal implementation calls {@link #sum(Rational)} if {@code augend} is a Rational,
     *              otherwise it calls {@code augend}{@link AlgebraNumber#sum(AlgebraNumber) .sum(}{@code
     *              this}{@link AlgebraNumber#sum(AlgebraNumber) )}. This is possible because {@code augend},
     *              itself being an {AlgebraNumber}, is also required to have an implementation.
     */
    @Override
    @SideEffectFree
    public AlgebraNumber sum(
            AlgebraNumber augend
    ) {
        return (augend instanceof Rational augendR)
                ? arithRes(augendR, false)
                : augend.sum(this);
    }
    
    /**
     * {@inheritDoc}
     *
     * @implNote    This skeletal implementation relies on {@link #numeratorBI()} and {@link #denominatorBI()}.
     */
    @Override
    @SideEffectFree
    public Rational difference(
            Rational subtrahend
    ) {
        return arithRes(subtrahend, true);
    }
    
    /**
     * {@inheritDoc}
     *
     * @implNote    This skeletal implementation calls {@link #difference(Rational)} if {@code subtrahend} is
     *              a Rational, otherwise it calls {@code subtrahend}{@link
     *              AlgebraNumber#difference(AlgebraNumber) .difference(}{@code this}{@link
     *              AlgebraNumber#difference(AlgebraNumber) )}{@link AlgebraNumber#negated() .negated()}.
     *              This is possible because {@code subtrahend}, itself being an {AlgebraNumber},
     *              is also required to have an implementation.
     */
    @Override
    @SideEffectFree
    public AlgebraNumber difference(
            AlgebraNumber subtrahend
    ) {
        return (subtrahend instanceof Rational subtrahendR)
                ? arithRes(subtrahendR, true)
                : subtrahend.difference(this).negated();
    }
    
    /**
     * adds or subtracts this and that
     *
     * @param that      the value to add or subtract from this
     * @param subtract  whether to add or subtract
     *
     * @return  the sum or difference
     */
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
    
    /**
     * {@inheritDoc}
     *
     * @implNote    This skeletal implementation relies on {@link #numeratorBI()} and {@link #denominatorBI()},
     *              as well as {@link AlgebraInteger#toBigInteger()}.
     */
    @Override
    @SideEffectFree
    public Rational product(
            AlgebraInteger multiplicand
    ) {
        BigInteger newNum = numeratorBI().multiply(multiplicand.toBigInteger());
        return RationalFactory.fromBigIntegers(newNum, denominatorBI());
    }
    
    /**
     * {@inheritDoc}
     *
     * @implNote    This skeletal implementation relies on {@link #numeratorBI()} and {@link #denominatorBI()}.
     */
    @Override
    @SideEffectFree
    public Rational product(
            Rational multiplicand
    ) {
        return multRes(multiplicand, false);
    }
    
    /**
     * {@inheritDoc}
     *
     * @implNote    This skeletal implementation calls {@link #product(AlgebraInteger)} if {@code multiplicand}
     *              is an {@link AlgebraInteger} or {@link #product(Rational)} if {@code multiplicand} is a Rational;
     *              otherwise it calls {@code multiplicand}{@link AlgebraNumber#product(AlgebraNumber)
     *              .product(}{@code this}{@link AlgebraNumber#product(AlgebraNumber) )}. This is possible
     *              because {@code multiplicand}, itself being an {AlgebraNumber},
     *              is also required to have an implementation.
     */
    @Override
    @SideEffectFree
    public AlgebraNumber product(
            AlgebraNumber multiplicand
    ) {
        return biOpHandler(multiplicand, AbstractRational::product, Rational::product, AlgebraNumber::product);
    }
    
    /**
     * Takes three functions and switches to the correct one based on the class of that
     *
     * @param that      the value to do the operation on this with
     * @param opIfAI    operation to use if that is an AlgebraInteger
     * @param opIfRat   operation to use if that is Rational
     * @param opElse    operation to use if neither; it is reversed so as to make that the reciever
     *
     * @return  the result of the relevant operation
     */
    @SideEffectFree
    private AlgebraNumber biOpHandler(
            AlgebraNumber that,
            BiFunction<AbstractRational, AlgebraInteger, Rational> opIfAI,
            BinaryOperator<Rational> opIfRat,
            BiFunction<AlgebraNumber, Rational, AlgebraNumber> opElse
    ) {
        return switch (that) {
            case AlgebraInteger thatAI -> opIfAI.apply(this, thatAI);
            case Rational thatR -> opIfRat.apply(this, thatR);
            default -> opElse.apply(that, this);
        };
    }
    
    /**
     * {@inheritDoc}
     *
     * @implNote    This skeletal implementation uses {@link #quotientZ(AlgebraNumber)} to find the quotient
     *              and {@link Rational#product(Rational)} and {@link Rational#difference(Rational)} to find
     *              the remainder.
     *
     * @throws ArithmeticException  if {@code divisor == 0}
     */
    @Override
    @SideEffectFree
    public NumberRemainderPair<AlgebraInteger, ? extends Rational> quotientZWithRemainder(
            Rational divisor
    ) {
        AlgebraInteger floor = quotientZ(divisor);
        return new NumberRemainderPair<>(this, floor, floor.product(divisor));
    }
    
    /**
     * {@inheritDoc}
     *
     * @implNote    This skeletal implementation simply calls {@link #quotientZWithRemainder(Rational)
     *              quotientZWithRemainder(}{@code divisor}{@link #quotientZWithRemainder(Rational) )}{@link
     *              NumberRemainderPair#remainder() .remainder()}.
     *
     * @throws ArithmeticException  if {@code divisor == 0}
     */
    @Override
    @SideEffectFree
    public Rational remainder(
            Rational divisor
    ) {
        return quotientZWithRemainder(divisor).remainder();
    }
    
    /**
     * {@inheritDoc}
     *
     * @implNote    This skeletal implementation checks the inputs (using {@link Rational#isNegative()}),
     *              but then just uses {@link #remainder(Rational)}.
     *
     * @throws ArithmeticException  if {@code modulus == 0}
     */
    @Override
    @SideEffectFree
    public Rational modulo(
            Rational modulus
    ) {
        return getModulo(modulus);
    }
    
    /**
     * {@inheritDoc}
     *
     * @implNote    This skeletal implementation relies on {@link #numeratorBI()} and {@link #denominatorBI()},
     *              as well as {@link AlgebraInteger#toBigInteger()}.
     */
    @Override
    @SideEffectFree
    public Rational quotient(
            AlgebraInteger divisor
    ) {
        BigInteger newDen = denominatorBI().multiply(divisor.toBigInteger());
        return RationalFactory.fromBigIntegers(numeratorBI(), newDen);
    }
    
    /**
     * {@inheritDoc}
     *
     * @implNote    This skeletal implementation relies on {@link #numeratorBI()} and {@link #denominatorBI()}.
     */
    @Override
    @SideEffectFree
    public Rational quotient(
            Rational divisor
    ) {
        return multRes(divisor, true);
    }
    
    /**
     * {@inheritDoc}
     *
     * @implNote    This skeletal implementation first checks for zeroes, returning 0 or throwing an exception
     *              depending on which is 0, and if not calls {@link #quotient(AlgebraInteger)} if {@code divisor}
     *              is an {@link AlgebraInteger} or {@link #quotient(Rational)} if {@code divisor} is a Rational;
     *              otherwise it calls {@code divisor}{@link AlgebraNumber#quotient(AlgebraNumber)
     *              .quotient(}{@code this}{@link AlgebraNumber#quotient(AlgebraNumber) )}{@link
     *              AlgebraNumber#inverted() .inverted()}. This is possible
     *              because {@code divisor}, itself being an {AlgebraNumber},
     *              is also required to have an implementation.
     */
    @Override
    @SideEffectFree
    public AlgebraNumber quotient(
            AlgebraNumber divisor
    ) {
        if (divisor.isZero()) {
            throw new ArithmeticException(DIV_0_EXC_MSG);
        }
        if (isZero()) {
            return getFromCache(0);
        }
        
        return biOpHandler(
                divisor,
                AbstractRational::quotient,
                Rational::quotient,
                (y, x) -> y.quotient(x).inverted()
        );
    }
    
    /**
     * Multiplies or divides this by that
     *
     * @param that      to multiply or divide by
     * @param divide    whether to multiply or divide
     *
     * @return  the product/quotient
     */
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
    
    /**
     * {@inheritDoc}
     *
     * @implNote    This skeletal implementation relies on {@link #numeratorBI()} and {@link #denominatorBI()},
     *              and may also use {@link AlgebraNumber#inverted()}, {@link AlgebraNumber#product(AlgebraNumber)},
     *              and {@link AlgebraNumber#squared()}. None of those may call this function,
     *              and none of them overridden so as to not work as intended anymore.
     */
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
    
    /**
     * {@inheritDoc}
     *
     * @implNote    This skeletal implementation relies on {@link #raised(int)} and {@link
     *              AlgebraNumber#product(AlgebraNumber)}. If {@link #raised(int)} is not overridden,
     *              then this also relies indirectly on everything that {@link #raised(int)} relies on,
     *              as well as {@link AlgebraInteger#toBigInteger()}.
     */
    @Override
    @SideEffectFree
    public Rational raised(
            AlgebraInteger exponent
    ) {
        return longRaiser(exponent.toBigInteger(), Rational::raised, true);
    }
    
    /**
     * {@inheritDoc}
     *
     * @implNote    This skeletal implementation uses {@link #rootRoundZ(int, RoundingMode)} to find the root
     *              and {@link AlgebraInteger#raised(int)} and {@link Rational#difference(Rational)}
     *              to find the remainder.
     *
     * @throws ArithmeticException  if {@code index} is even and this is negative
     */
    @Override
    @SideEffectFree
    public NumberRemainderPair<? extends AlgebraInteger, ? extends Rational> rootZWithRemainder(
            int index
    ) {
        AlgebraInteger floor = rootRoundZ(index, RoundingMode.DOWN);
        return new NumberRemainderPair<>(this, floor, floor.raised(index));
    }
    
    /**
     * {@inheritDoc}
     *
     * @implNote    This skeletal implementation uses {@link #rootRoundZ(AlgebraInteger, RoundingMode)} to
     *              find the root and {@link AlgebraInteger#raised(AlgebraInteger)} and {@link
     *              Rational#difference(Rational)} to find the remainder.
     *
     * @throws ArithmeticException  if {@code index} is even and this is negative
     */
    @Override
    @SideEffectFree
    public NumberRemainderPair<? extends AlgebraInteger, ? extends Rational> rootZWithRemainder(
            AlgebraInteger index
    ) {
        AlgebraInteger floor = rootRoundZ(index, RoundingMode.DOWN);
        return new NumberRemainderPair<>(this, floor, floor.raised(index));
    }
    
//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    @SideEffectFree
//    public NumberRemainderPair<? extends Rational, ? extends Rational> rootQWithRemainder(
//            int index,
//            @Nullable Integer precision
//    ) {
//        Rational floor = rootRoundQ(index, getContextFrom(precision, RoundingMode.DOWN));
//        return new NumberRemainderPair<>(this, floor, floor.raised(index));
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    @SideEffectFree
//    public NumberRemainderPair<? extends Rational, ? extends Rational> rootQWithRemainder(
//            AlgebraInteger index,
//            @Nullable Integer precision
//    ) {
//        Rational floor = rootRoundQ(index, getContextFrom(precision, RoundingMode.DOWN));
//        return new NumberRemainderPair<>(this, floor, floor.raised(index));
//    }
}
