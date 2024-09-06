package org.cb2384.exactalgebra.objects.numbers.integral;

import static org.cb2384.exactalgebra.util.PrimMathUtils.IntegralBoundaryTypes;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import org.cb2384.exactalgebra.objects.exceptions.DisallowedNarrowingException;
import org.cb2384.exactalgebra.objects.numbers.AlgebraNumber;
import org.cb2384.exactalgebra.objects.numbers.rational.AbstractRational;
import org.cb2384.exactalgebra.objects.numbers.rational.Rational;
import org.cb2384.exactalgebra.objects.numbers.rational.RationalFactory;
import org.cb2384.exactalgebra.objects.pair.NumberRemainderPair;
import org.cb2384.exactalgebra.util.BigMathObjectUtils;
import org.cb2384.exactalgebra.util.PrimMathUtils;
import org.cb2384.exactalgebra.objects.Sigmagnum;
import org.cb2384.exactalgebra.util.corutils.NullnessUtils;
import org.cb2384.exactalgebra.util.corutils.functional.ObjectThenIntToObjectFunction;
import org.cb2384.exactalgebra.util.corutils.ternary.Signum;

import org.checkerframework.checker.index.qual.*;
import org.checkerframework.checker.nullness.qual.*;
import org.checkerframework.common.returnsreceiver.qual.*;
import org.checkerframework.dataflow.qual.*;

/**
 * <p>Skeletal implementations for many of the methods in {@link AlgebraInteger}</p>
 *
 * <p>Throws:&ensp;{@link NullPointerException} &ndash; on any {@code null} argument,
 * unless otherwise specified.</p>
 *
 * @author  Corinne Buxton
 */
public abstract class AbstractAlgebraInteger
        extends AbstractRational
        implements AlgebraInteger {
    
    static final ObjectThenIntToObjectFunction<Rational, Rational> RAT_RAISER
            = (rational, integer) -> ((AlgebraInteger) rational).raisedZ(integer);
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Pure
    public final @This AlgebraInteger numeratorAI() {
        return this;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Pure
    public final AlgebraInteger denominatorAI() {
        return AlgebraInteger.super.denominatorAI();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Pure
    public final @This AlgebraInteger wholeAI() {
        return this;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @SideEffectFree
    public final BigInteger numeratorBI() {
        return toBigInteger();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Pure
    public final BigInteger denominatorBI() {
        return BigInteger.ONE;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @SideEffectFree
    public final BigInteger wholeBI() {
        return toBigInteger();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Pure
    public final @This AlgebraInteger roundZ() {
        return this;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Pure
    public final @This AlgebraInteger roundZ(
            @Nullable RoundingMode roundingMode
    ) {
        return this;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @SideEffectFree
    public BigDecimal toBigDecimal() {
        return new BigDecimal(toBigInteger());
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @SideEffectFree
    public BigDecimal toBigDecimal(
            @Nullable Integer precision,
            @Nullable RoundingMode roundingMode
    ) {
        return AlgebraInteger.super.toBigDecimal(precision, roundingMode);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @SideEffectFree
    public BigDecimal toBigDecimal(
            @Nullable MathContext mathContext
    ) {
        return AlgebraInteger.super.toBigDecimal(mathContext);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @SideEffectFree
    public final BigInteger toBigInteger(
            @Nullable RoundingMode roundingMode
    ) {
        return toBigInteger();
    }
    
    /**
     * {@inheritDoc}
     *
     * @implNote    This skeletal implementation simply calls {@link #toBigInteger()}{@link
     *              BigInteger#toString(int) .toString(}{@code radix}{@link BigInteger#toString(int) )}
     */
    @Override
    @SideEffectFree
    public String toString(int radix) {
        return toBigInteger().toString(radix);
    }
    
    /**
     * {@inheritDoc}
     *
     * @implNote    This skeletal implementation simpy calls {@link #toBigInteger()}{@link BigInteger#longValue()
     *              .longValue()}
     */
    @Override
    @Pure
    public long longValue() {
        return toBigInteger().longValue();
    }
    
    /**
     * {@inheritDoc}
     *
     * @implNote    This skeletal implementation simpy calls {@link #toBigInteger()}{@link BigInteger#intValue()
     *              .intValue()}
     */
    @Override
    @Pure
    public int intValue() {
        return toBigInteger().intValue();
    }
    
    /**
     * {@inheritDoc}
     *
     * @implNote    This skeletal implementation simpy calls {@link #toBigInteger()}{@link BigInteger#shortValue()
     *              .shortValue()}
     */
    @Override
    @Pure
    public short shortValue() {
        return toBigInteger().shortValue();
    }
    
    /**
     * {@inheritDoc}
     *
     * @implNote    This skeletal implementation simpy calls {@link #toBigInteger()}{@link BigInteger#byteValue()
     *              .byteValue()}
     */
    @Override
    @Pure
    public byte byteValue() {
        return toBigInteger().byteValue();
    }
    
    /**
     * {@inheritDoc}
     *
     * @implNote    This skeletal implementation simply calls {@link #toBigInteger()}{@link
     *              BigInteger#doubleValue() .doubleValue()}.
     */
    @Override
    @Pure
    public double doubleValue() {
        return toBigInteger().doubleValue();
    }
    
    /**
     * {@inheritDoc}
     *
     * @implNote    This default implementation simply calls {@link #toBigInteger()}{@link
     *              BigInteger#floatValue() .floatValue()}.
     */
    @Override
    @Pure
    public float floatValue() {
        return toBigInteger().floatValue();
    }
    
    /**
     * tries the given function that would return the exact value, and if it does not work,
     * throws an exception.
     *
     * @param primFunc      The function to try narrowing this with
     * @param targetType    The target class name, for the exception message
     *
     * @return  the narrowed value, if narrowing loses no information
     *
     * @throws DisallowedNarrowingException if narrowing would lose information
     */
    @Pure
    final long getExactVal(
            ToLongFunction<BigInteger> primFunc,
            Class<?> targetType
    ) {
        try {
            return primFunc.applyAsLong(toBigInteger());
        } catch (ArithmeticException oldAE) {
            DisallowedNarrowingException newDNE
                    = new DisallowedNarrowingException(AlgebraInteger.class, targetType);
            newDNE.initCause(oldAE);
            throw newDNE;
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Pure
    public long longValueExact() {
        return getExactVal(BigInteger::longValueExact, long.class);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Pure
    public int intValueExact() {
        return (int) getExactVal(BigInteger::intValueExact, int.class);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Pure
    public short shortValueExact() {
        return (short) getExactVal(BigInteger::shortValueExact, short.class);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Pure
    public byte byteValueExact() {
        return (byte) getExactVal(BigInteger::byteValueExact, byte.class);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Pure
    public char charValueExact() {
        ArithmeticException dummyAE = null;
        try {
            int valInt = intValueExact();
            if ((0 <= valInt) && (valInt <= Character.MAX_VALUE)) {
                return (char) valInt;
            }
        } catch (ArithmeticException oldAE) {
            dummyAE = oldAE;
        }
        DisallowedNarrowingException newDNE =
                new DisallowedNarrowingException(AlgebraInteger.class, char.class);
        NullnessUtils.applyIfNonNull(dummyAE, newDNE::initCause);
        throw newDNE;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Pure
    public int hashCode() {
        return ~toBigInteger().hashCode();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Pure
    public boolean equals(
            @Nullable Object obj
    ) {
        return (obj instanceof AlgebraInteger oAI) && (hashCode() == oAI.hashCode()) && equiv(oAI);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @SideEffectFree
    public AlgebraInteger negated() {
        return IntegerFactory.fromBigInteger( toBigInteger().negate() );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @SideEffectFree
    public AlgebraInteger max(
            long that
    ) {
        return secondIsGreater(toBigInteger(), that)
                ? FiniteInteger.valueOfStrict(that)
                : this;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @SideEffectFree
    public AlgebraInteger max(
            BigInteger that
    ) {
        return (toBigInteger().compareTo(that) < 0)
                ? IntegerFactory.fromBigInteger(that)
                : this;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @SideEffectFree
    public AlgebraInteger min(
            long that
    ) {
        return secondIsGreater(toBigInteger(), that)
                ? this
                : FiniteInteger.valueOfStrict(that);
    }
    
    /**
     * Determines if the second is greater than the first.
     *
     * @param   first   the first value
     *
     * @param   second  the second value
     *
     * @return  {@code this < that}
     */
    @Pure
    static boolean secondIsGreater(
            BigInteger first,
            long second
    ) {
        return BigMathObjectUtils.canBeLong(first, IntegralBoundaryTypes.DEFAULT)
                ? first.longValue() < second
                : BigMathObjectUtils.isNegative(first);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @SideEffectFree
    public AlgebraInteger min(
            BigInteger that
    ) {
        return (toBigInteger().compareTo(that) < 0)
                ? this
                : IntegerFactory.fromBigInteger(that);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @SideEffectFree
    public AlgebraInteger gcf(AlgebraInteger that) {
        return getGCF(that.toBigInteger());
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @SideEffectFree
    public AlgebraInteger gcf(
            long that
    ) {
        return getGCF(BigInteger.valueOf(that));
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @SideEffectFree
    public AlgebraInteger gcf(
            BigInteger that
    ) {
        return getGCF(that);
    }
    
    /**
     * Finds the gcf of this and {@code that}
     *
     * @param that  the value to find the gcf of this with
     *
     * @return  the gcf
     *
     * @throws ArithmeticException  if both this and {@code that} are 0
     */
    @SideEffectFree
    protected final AlgebraInteger getGCF(
            BigInteger that
    ) {
        if (isZero() && BigMathObjectUtils.isZero(that)) {
            throw new ArithmeticException("GCF of 0 and 0");
        }
        return IntegerFactory.fromBigInteger( toBigInteger().gcd(that) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @SideEffectFree
    public AlgebraInteger lcm(AlgebraInteger that) {
        return lcm(that.toBigInteger());
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @SideEffectFree
    public AlgebraInteger lcm(
            long that
    ) {
        return lcm(BigInteger.valueOf(that));
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @SideEffectFree
    public AlgebraInteger lcm(
            BigInteger that
    ) {
        return IntegerFactory.fromBigInteger( BigMathObjectUtils.lcm(toBigInteger(), that) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Pure
    public boolean canDivideBy(
            long divisor
    ) {
        return BigMathObjectUtils.isDivisible(toBigInteger(), BigInteger.valueOf(divisor));
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Pure
    public boolean canDivideBy(
            BigInteger divisor
    ) {
        return BigMathObjectUtils.isDivisible(toBigInteger(), divisor);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Pure
    public boolean isPrime() {
        BigInteger thisBI = toBigInteger();
        return (thisBI.signum() == 1) && isPrime(thisBI);
    }
    
    /**
     * Actual implementation of {@link #isPrime()}, working through BigIntegers
     *
     * @param value the value to check for primacy
     *
     * @return  {@code true} if {@code value} is prime, otherwise {@code false}
     */
    @Pure
    static boolean isPrime(
            BigInteger value
    ) {
        assert value.signum() == 1;
        // Only even prime
        if (value.equals(BigInteger.TWO)) {
            return true;
        }
        
        // If can be ruled out as not being prime
        if ( BigMathObjectUtils.isOne(value) || BigMathObjectUtils.isDivisible(value, BigInteger.TWO)) {
            return false;
        }
        
        BigInteger max = BigMathObjectUtils.inc( value.sqrt() );
        
        for (BigInteger i = BigInteger.valueOf(3); i.compareTo(max) <= 0; i = i.add(BigInteger.TWO)) {
            if (BigMathObjectUtils.isDivisible(value, i)) {
                return false;
            }
        }
        //else
        return true;
    }
    
    /**
     * {@inheritDoc}
     *
     * @implNote    This skeletal implementation relies on {@link #toBigInteger()}.
     */
    @Override
    @Pure
    public boolean isEven() {
        return BigMathObjectUtils.isEven(toBigInteger());
    }
    
    /**
     * {@inheritDoc}
     *
     * @implNote    This skeletal implementation simply calls {@link #toBigInteger()}{@link
     *              BigInteger#equals(Object) .equals(}{@link BigInteger#ONE}{@link BigInteger#equals(Object) )}.
     */
    @Override
    @Pure
    public boolean isOne() {
        return toBigInteger().equals(BigInteger.ONE);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Pure
    public boolean isWhole() {
        return true;
    }
    
    /**
     * {@inheritDoc}
     *
     * @implNote    This skeletal implementation relies on {@link #toBigInteger()}.
     */
    @Override
    @Pure
    public Signum signum() {
        return Signum.valueOf(toBigInteger());
    }
    
    /**
     * {@inheritDoc}
     *
     * @implNote    This skeletal implementation relies on {@link #toBigInteger()}.
     */
    @Override
    @Pure
    public Sigmagnum sigmagnum() {
        return Sigmagnum.valueOf(toBigInteger());
    }
    
    /**
     * {@inheritDoc}
     *
     * @implNote    This skeletal implementation relies on {@link #toBigInteger()}.
     */
    @Override
    @Pure
    public int compareTo(
            AlgebraInteger that
    ) {
        return toBigInteger().compareTo(that.toBigInteger());
    }
    
    /**
     * {@inheritDoc}
     *
     * @implNote    This skeletal implementation relies on {@link AlgebraInteger#toBigInteger()}.
     */
    @Override
    @SideEffectFree
    public AlgebraInteger sum(
            AlgebraInteger augend
    ) {
        return IntegerFactory.fromBigInteger( toBigInteger().add(augend.toBigInteger()) );
    }
    
    /**
     * {@inheritDoc}
     *
     * @implNote    This skeletal implementation relies on {@link AlgebraInteger#toBigInteger()}.
     */
    @Override
    @SideEffectFree
    public AlgebraInteger difference(
            AlgebraInteger subtrahend
    ) {
        return IntegerFactory.fromBigInteger( toBigInteger().subtract(subtrahend.toBigInteger()) );
    }
    
    /**
     * {@inheritDoc}
     *
     * @implNote    This skeletal implementation relies on {@link AlgebraInteger#toBigInteger()}.
     */
    @Override
    @SideEffectFree
    public AlgebraInteger product(
            AlgebraInteger multiplicand
    ) {
        return IntegerFactory.fromBigInteger( toBigInteger().multiply(multiplicand.toBigInteger()) );
    }
    
    /**
     * {@inheritDoc}
     *
     * @implNote    This skeletal implementation simply calls {@link
     *              RationalFactory#fromAlgebraIntegers(AlgebraInteger, AlgebraInteger)
     *              RationalFactory.fromAlgebraIntegers(}{@code this, divisor}{@link
     *              RationalFactory#fromAlgebraIntegers(AlgebraInteger, AlgebraInteger) )}
     *
     * @throws ArithmeticException  if {@code divisor == 0}
     */
    @Override
    @SideEffectFree
    public Rational quotient(
            AlgebraInteger divisor
    ) {
        return RationalFactory.fromAlgebraIntegers(this, divisor);
    }
    
    /**
     * {@inheritDoc}
     *
     * @implNote    This skeletal implementation relies on {@link AlgebraInteger#toBigInteger()}.
     *
     * @throws ArithmeticException  if {@code divisor == 0}
     */
    @Override
    @SideEffectFree
    public NumberRemainderPair<? extends AlgebraInteger, ? extends AlgebraInteger> quotientZWithRemainder(
            AlgebraInteger divisor
    ) {
        BigInteger[] res = toBigInteger().divideAndRemainder(divisor.toBigInteger());
        return new NumberRemainderPair<>(IntegerFactory.fromBigInteger(res[0]),
                IntegerFactory.fromBigInteger(res[1]));
    }
    
    /**
     * {@inheritDoc}
     *
     * @implNote    This skeletal implementation relies on {@link AlgebraInteger#toBigInteger()}.
     *
     * @throws ArithmeticException  if {@code divisor == 0}
     */
    @Override
    @SideEffectFree
    public AlgebraInteger quotientZ(
            AlgebraInteger divisor
    ) {
        return IntegerFactory.fromBigInteger( toBigInteger().divide(divisor.toBigInteger()) );
    }
    
    /**
     * {@inheritDoc}
     *
     * @implNote    This skeletal implementation relies on {@link AlgebraInteger#toBigInteger()}.
     *
     * @throws ArithmeticException  if {@code divisor == 0}
     */
    @Override
    @SideEffectFree
    public AlgebraInteger remainder(
            AlgebraInteger divisor
    ) {
        return IntegerFactory.fromBigInteger( toBigInteger().remainder(divisor.toBigInteger()) );
    }
    
    /**
     * {@inheritDoc}
     *
     * @implNote    This skeletal implementation relies on {@link AlgebraInteger#toBigInteger()}.
     *
     * @throws ArithmeticException  if {@code divisor == 0}
     */
    @Override
    @SideEffectFree
    public AlgebraInteger modulo(
            AlgebraInteger modulus
    ) {
        return IntegerFactory.fromBigInteger( toBigInteger().mod(modulus.toBigInteger()) );
    }
    
    /**
     * {@inheritDoc}
     *
     * @implNote    This skeletal implementation relies on {@link AlgebraInteger#toBigInteger()}.
     *
     * @throws ArithmeticException  if {@code divisor == 0}
     */
    @Override
    @SideEffectFree
    public AlgebraInteger modInverse(
            AlgebraInteger modulus
    ) {
        return IntegerFactory.fromBigInteger( toBigInteger().modInverse(modulus.toBigInteger()) );
    }
    
    /**
     * {@inheritDoc}
     *
     * @implNote    This skeletal implementation relies on {@link #raisedZ(int)} (which itself also uses {@link
     *              #toBigInteger()}), and may also use {@link AlgebraNumber#inverted()}, {@link
     *              AlgebraNumber#product(AlgebraNumber)}, and {@link AlgebraNumber#squared()}.
     *              None of those may call this function, and none of them overridden
     *              so as to not work as intended anymore.
     */
    @Override
    @SideEffectFree
    public Rational raised(
            int exponent
    ) {
        return intRaiser(exponent, RAT_RAISER);
    }
    
    /**
     * {@inheritDoc}
     *
     * @implNote    This skeletal implementation relies on {@link #toBigInteger()}.
     */
    @Override
    @SideEffectFree
    public AlgebraInteger raisedZ(
            int exponent
    ) {
        return IntegerFactory.fromBigInteger( toBigInteger().pow(exponent) );
    }
    
    /**
     * {@inheritDoc}
     *
     * @implNote    This skeletal implementation relies on {@link #raisedZ(int)}, {@link
     *              AlgebraNumber#product(AlgebraNumber)}, and {@link AlgebraInteger#toBigInteger()}.
     */
    @Override
    @SideEffectFree
    public Rational raised(
            AlgebraInteger exponent
    ) {
        return longRaiser(exponent.toBigInteger(), RAT_RAISER, true);
    }
    
    /**
     * {@inheritDoc}
     *
     * @implNote    This skeletal implementation relies on {@link #raisedZ(int)}, {@link
     *              AlgebraNumber#product(AlgebraNumber)}, and {@link AlgebraInteger#toBigInteger()}.
     */
    @Override
    @SideEffectFree
    public AlgebraInteger raisedZ(
            AlgebraInteger exponent
    ) {
        return longRaiser(exponent.toBigInteger(), AlgebraInteger::raisedZ, false);
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
    public NumberRemainderPair<? extends AlgebraInteger, ? extends AlgebraInteger> rootZWithRemainder(
            int index
    ) {
        AlgebraInteger floor = rootRoundZ(index, RoundingMode.DOWN);
        return new NumberRemainderPair<>(this, floor, floor.raisedZ(index));
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
    public NumberRemainderPair<? extends AlgebraInteger, ? extends AlgebraInteger> rootZWithRemainder(
            AlgebraInteger index
    ) {
        AlgebraInteger floor = rootRoundZ(index, RoundingMode.DOWN);
        return new NumberRemainderPair<>(this, floor, floor.raisedZ(index));
    }
    
    /**
     * {@inheritDoc}
     *
     * @implNote    This skeletal implementation is actually not very skeletal. It also only relies on
     *              {@link #toBigInteger()}.
     */
    @Override
    @SideEffectFree
    public List<? extends AlgebraInteger> factors() {
        if (isZero()) {
            return new ArrayList<>(0);
        }
        BigInteger absVal = toBigInteger().abs();
        if (BigMathObjectUtils.canBeLong(absVal, null)) {
            return primitiveFactorer(absVal.longValue());
        }
        BigInteger[] maxPair = absVal.sqrtAndRemainder();
        boolean isPerfectSquare = BigMathObjectUtils.isZero(maxPair[1]);
        
        BigInteger max = maxPair[0];
        
        Predicate<BigInteger> endIteration = isPerfectSquare
                ? bigint -> max.compareTo(bigint) < 0
                : bigint -> max.compareTo(bigint) <= 0;
        
        Stream<BigInteger> stream = Stream.iterate(BigInteger.ONE, endIteration, BigMathObjectUtils::inc);
        
        // If this isn't even, then even things can't be factors
        if (absVal.testBit(0)) {
            stream = stream.filter(i -> i.testBit(0));
        }
        
        stream = stream.parallel()
                .mapMulti((factor, pipe) -> {
                    if (BigMathObjectUtils.isDivisible(absVal, factor)) {
                        pipe.accept(factor);
                        pipe.accept(absVal.divide(factor));
                    }
                });
        
        if (isPerfectSquare) {
            stream = Stream.concat(Stream.of(max), stream);
        }
        
        return stream.sorted()
                .map(IntegerFactory::fromBigInteger)
                .collect(Collectors.toCollection(ArrayList::new));
    }
    
    /**
     * Factoring implementation for when the value can be represented as a primitive ({@code long}).
     * {@code value} must be positive
     *
     * @param positiveValue the positive value to factor
     *
     * @return  a {@link List} of factors; the list is mutable
     */
    @SideEffectFree
    static List<FiniteInteger> primitiveFactorer(
            @Positive long positiveValue
    ) {
        long[] maxPair = PrimMathUtils.sqrtAndRemainder(positiveValue);
        long max = maxPair[0];
        boolean isPerfectSquare = (maxPair[1] == 0);
        
        if (positiveValue <= 8) {
            return new ArrayList<>(switch ((int) positiveValue) {
                case 1 -> List.of(getFromCache(1));
                case 2 -> List.of(getFromCache(1), getFromCache(2));
                case 3 -> List.of(getFromCache(1), getFromCache(3));
                case 4 -> List.of(getFromCache(1), getFromCache(2), getFromCache(4));
                case 5 -> List.of(getFromCache(1), getFromCache(5));
                case 6 -> List.of(getFromCache(1), getFromCache(2),
                        getFromCache(3), getFromCache(6));
                case 7 -> List.of(getFromCache(1), getFromCache(7));
                case 8 -> List.of(getFromCache(1), getFromCache(2),
                        getFromCache(4), getFromCache(8));
                default -> throw new RuntimeException("shouldn't be reachable");
            });
        }
        
        LongStream stream = isPerfectSquare
                ? LongStream.range(1, max)
                : LongStream.rangeClosed(1, max);
        
        // If this isn't even, then even things can't be factors.
        if ((positiveValue & 1) == 1) {
            stream = stream.filter(l -> (l & 1) == 1);
        }
        
        stream = stream.parallel()
                .mapMulti((factor, pipe) -> {
                    if (positiveValue % factor == 0) {
                        pipe.accept(factor);
                        pipe.accept(positiveValue / factor);
                    }
                });
        
        if (isPerfectSquare) {
            stream = LongStream.concat(LongStream.of(max), stream);
        }
        
        return stream.sorted()
                .mapToObj(FiniteInteger::valueFactory)
                .collect(Collectors.toCollection(ArrayList::new));
    }
    
    /**
     * {@inheritDoc}
     *
     * @implNote    This skeletal implementation is actually not very skeletal. It also only relies on
     *              {@link #toBigInteger()}.
     */
    @Override
    @SideEffectFree
    public List<? extends AlgebraInteger> primeFactorization() {
        List<AlgebraInteger> factorList = new ArrayList<>();
        if (signum() == Signum.POSITIVE) {
            BigInteger currVal = toBigInteger();
            BigInteger factor = BigInteger.TWO;
            while (!isPrime(currVal)) {
                BigInteger[] quoRemain = currVal.divideAndRemainder(factor);
                if (BigMathObjectUtils.isZero(quoRemain[1])) {
                    factorList.add(IntegerFactory.fromBigInteger(factor));
                    currVal = quoRemain[0];
                } else {
                    factor = BigMathObjectUtils.inc(factor);
                }
            }
            factorList.add(IntegerFactory.fromBigInteger(currVal));
        }
        return factorList;
    }
}
