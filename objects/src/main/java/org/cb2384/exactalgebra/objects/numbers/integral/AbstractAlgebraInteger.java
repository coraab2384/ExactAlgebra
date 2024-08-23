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

import org.cb2384.exactalgebra.objects.numbers.rational.AbstractRational;
import org.cb2384.exactalgebra.objects.numbers.rational.Rational;
import org.cb2384.exactalgebra.objects.pair.NumberRemainderPair;
import org.cb2384.exactalgebra.util.BigMathObjectUtils;
import org.cb2384.exactalgebra.util.MiscUtils.DisallowedNarrowingException;
import org.cb2384.exactalgebra.util.PrimMathUtils;
import org.cb2384.exactalgebra.util.Sigmagnum;
import org.cb2384.exactalgebra.util.corutils.NullnessUtils;
import org.cb2384.exactalgebra.util.corutils.functional.ObjectThenIntToObjectFunction;
import org.cb2384.exactalgebra.util.corutils.ternary.Signum;

import org.checkerframework.checker.index.qual.*;
import org.checkerframework.checker.nullness.qual.*;
import org.checkerframework.common.returnsreceiver.qual.*;
import org.checkerframework.dataflow.qual.*;

public abstract class AbstractAlgebraInteger
        extends AbstractRational
        implements AlgebraInteger {
    
    static final ObjectThenIntToObjectFunction<Rational, Rational> RAT_RAISER
            = (rational, integer) -> ((AlgebraInteger) rational).raisedZ(integer);
    
    @Override
    @Pure
    public final @This AlgebraInteger numeratorAI() {
        return this;
    }
    
    @Override
    @Pure
    public final AlgebraInteger denominatorAI() {
        return Cache.get(1);
    }
    
    @Override
    @Pure
    public final @This AlgebraInteger wholeAI() {
        return this;
    }
    
    @Override
    @SideEffectFree
    public BigInteger wholeBI() {
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
    public final @This AlgebraInteger roundZ() {
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
    public final @This AlgebraInteger roundZ(
            @Nullable RoundingMode roundingMode
    ) {
        return this;
    }
    
    /**
     * Returns this integral value represented as a {@link BigDecimal}.
     *
     * @return  this, but as a {@link BigDecimal}
     */
    @Override
    @SideEffectFree
    public BigDecimal toBigDecimal() {
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
    public BigDecimal toBigDecimal(
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
    public BigDecimal toBigDecimal(
            @Nullable MathContext mathContext
    ) {
        BigDecimal res = new BigDecimal(toBigInteger());
        return NullnessUtils.returnDefaultIfNull(mathContext, res::round, res);
    }
    
    /**
     * Returns a {@link BigInteger} representing this value.
     *
     * @param   roundingMode    the rounding mode to use; is irrelevant since this is already
     *                          an integral type
     *
     * @return  this value as a {@link BigInteger}
     */
    @Override
    @SideEffectFree
    public BigInteger toBigInteger(
            @Nullable RoundingMode roundingMode
    ) {
        return toBigInteger();
    }
    
    @Override
    public String toString(int radix) {
        return toBigInteger().toString(radix);
    }
    
    @Override
    @Pure
    public long longValue() {
        return toBigInteger().longValue();
    }
    
    @Override
    @Pure
    public int intValue() {
        return toBigInteger().intValue();
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
    
    @Override
    @Pure
    public long longValueExact() {
        return getExactVal(BigInteger::longValueExact, long.class);
    }
    
    @Override
    @Pure
    public int intValueExact() {
        return (int) getExactVal(BigInteger::intValueExact, int.class);
    }
    
    @Override
    @Pure
    public short shortValueExact() {
        return (short) getExactVal(BigInteger::shortValueExact, short.class);
    }
    
    @Override
    @Pure
    public byte byteValueExact() {
        return (byte) getExactVal(BigInteger::byteValueExact, byte.class);
    }
    
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
    
    @Override
    @Pure
    public int hashCode() {
        return 0x55555555 ^ toBigInteger().hashCode();
    }
    
    @Override
    @Pure
    public boolean equals(
            @Nullable Object obj
    ) {
        return (obj instanceof AlgebraInteger oAI) && (hashCode() == oAI.hashCode()) && equiv(oAI);
    }
    
    @Override
    @SideEffectFree
    public AlgebraInteger negated() {
        return IntegerFactory.fromBigInteger( toBigInteger().negate() );
    }
    
    @Override
    @SideEffectFree
    public AlgebraInteger max(
            long that
    ) {
        return secondIsGreater(toBigInteger(), that)
                ? FiniteInteger.valueOf(that)
                : this;
    }
    
    @Override
    @SideEffectFree
    public AlgebraInteger max(
            BigInteger that
    ) {
        return (toBigInteger().compareTo(that) < 0)
                ? IntegerFactory.fromBigInteger(that)
                : this;
    }
    
    @Override
    @SideEffectFree
    public AlgebraInteger min(
            long that
    ) {
        return secondIsGreater(toBigInteger(), that)
                ? this
                : FiniteInteger.valueOf(that);
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
    
    @Override
    @SideEffectFree
    public AlgebraInteger min(
            BigInteger that
    ) {
        return (toBigInteger().compareTo(that) < 0)
                ? this
                : IntegerFactory.fromBigInteger(that);
    }
    
    @Override
    @SideEffectFree
    public AlgebraInteger gcf(
            long that
    ) {
        return getGCF(BigInteger.valueOf(that));
    }
    
    @Override
    @SideEffectFree
    public AlgebraInteger gcf(
            BigInteger that
    ) {
        return getGCF(that);
    }
    
    @SideEffectFree
    protected final AlgebraInteger getGCF(
            BigInteger that
    ) {
        if (isZero() && BigMathObjectUtils.isZero(that)) {
            throw new ArithmeticException("GCF of 0 and 0");
        }
        return IntegerFactory.fromBigInteger( toBigInteger().gcd(that) );
    }
    
    @Override
    @SideEffectFree
    public AlgebraInteger lcm(
            long that
    ) {
        return lcm(BigInteger.valueOf(that));
    }
    
    @Override
    @SideEffectFree
    public AlgebraInteger lcm(
            BigInteger that
    ) {
        return IntegerFactory.fromBigInteger( BigMathObjectUtils.lcm(toBigInteger(), that) );
    }
    
    @Override
    @Pure
    public boolean canDivideBy(
            long divisor
    ) {
        return BigMathObjectUtils.isDivisible(toBigInteger(), BigInteger.valueOf(divisor));
    }
    
    @Override
    @Pure
    public boolean canDivideBy(
            BigInteger divisor
    ) {
        return BigMathObjectUtils.isDivisible(toBigInteger(), divisor);
    }
    
    @Override
    @Pure
    public boolean isPrime() {
        BigInteger thisBI = toBigInteger();
        return (thisBI.signum() == 1) && isPrime(thisBI);
    }
    
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
    
    @Override
    @Pure
    public boolean isOne() {
        return toBigInteger().equals(BigInteger.ONE);
    }
    
    @Override
    @Pure
    public boolean isWhole() {
        return true;
    }
    
    @Override
    @Pure
    public Signum signum() {
        return Signum.valueOf(toBigInteger());
    }
    
    @Override
    @Pure
    public Sigmagnum sigmagnum() {
        return Sigmagnum.valueOf(toBigInteger());
    }
    
    @Override
    @Pure
    public int compareTo(
            AlgebraInteger that
    ) {
        return toBigInteger().compareTo(that.toBigInteger());
    }
    
    @Override
    @SideEffectFree
    public AlgebraInteger sum(
            AlgebraInteger augend
    ) {
        return IntegerFactory.fromBigInteger( toBigInteger().add(augend.toBigInteger()) );
    }
    
    @Override
    @SideEffectFree
    public AlgebraInteger difference(
            AlgebraInteger subtrahend
    ) {
        return IntegerFactory.fromBigInteger( toBigInteger().subtract(subtrahend.toBigInteger()) );
    }
    
    @Override
    @SideEffectFree
    public AlgebraInteger product(
            AlgebraInteger multiplicand
    ) {
        return IntegerFactory.fromBigInteger( toBigInteger().multiply(multiplicand.toBigInteger()) );
    }
    
    @Override
    @SideEffectFree
    public NumberRemainderPair<? extends AlgebraInteger, ? extends AlgebraInteger> quotientZWithRemainder(
            AlgebraInteger divisor
    ) {
        BigInteger[] res = toBigInteger().divideAndRemainder(divisor.toBigInteger());
        return new NumberRemainderPair<>(IntegerFactory.fromBigInteger(res[0]),
                IntegerFactory.fromBigInteger(res[1]));
    }
    
    @Override
    @SideEffectFree
    public AlgebraInteger quotientZ(
            AlgebraInteger divisor
    ) {
        return IntegerFactory.fromBigInteger( toBigInteger().divide(divisor.toBigInteger()) );
    }
    
    @Override
    @SideEffectFree
    public AlgebraInteger remainder(
            AlgebraInteger divisor
    ) {
        return IntegerFactory.fromBigInteger( toBigInteger().remainder(divisor.toBigInteger()) );
    }
    
    @Override
    @SideEffectFree
    public AlgebraInteger modulo(
            AlgebraInteger modulus
    ) {
        return IntegerFactory.fromBigInteger( toBigInteger().mod(modulus.toBigInteger()) );
    }
    
    @Override
    @SideEffectFree
    public AlgebraInteger modInverse(
            AlgebraInteger modulus
    ) {
        return IntegerFactory.fromBigInteger( toBigInteger().modInverse(modulus.toBigInteger()) );
    }
    
    @Override
    @SideEffectFree
    public Rational raised(
            int exponent
    ) {
        return intRaiser(exponent, RAT_RAISER);
    }
    
    @Override
    @SideEffectFree
    public AlgebraInteger raisedZ(
            int exponent
    ) {
        return IntegerFactory.fromBigInteger( toBigInteger().pow(exponent) );
    }
    
    @Override
    @SideEffectFree
    public Rational raised(
            AlgebraInteger exponent
    ) {
        return longRaiser(exponent.toBigInteger(), RAT_RAISER, true);
    }
    
    @Override
    @SideEffectFree
    public AlgebraInteger raisedZ(
            AlgebraInteger exponent
    ) {
        return longRaiser(exponent.toBigInteger(), AlgebraInteger::raisedZ, false);
    }
    
    @Override
    @SideEffectFree
    public NumberRemainderPair<? extends AlgebraInteger, ? extends AlgebraInteger> rootZWithRemainder(
            int index
    ) {
        AlgebraInteger floor = rootRoundZ(index, RoundingMode.DOWN);
        return new NumberRemainderPair<>(this, floor, floor.raisedZ(index));
    }
    
    @Override
    @SideEffectFree
    public NumberRemainderPair<? extends AlgebraInteger, ? extends AlgebraInteger> rootZWithRemainder(
            AlgebraInteger index
    ) {
        AlgebraInteger floor = rootRoundZ(index, RoundingMode.DOWN);
        return new NumberRemainderPair<>(this, floor, floor.raisedZ(index));
    }
    
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
    
    @SideEffectFree
    static List<FiniteInteger> primitiveFactorer(
            @Positive long positiveValue
    ) {
        long[] maxPair = PrimMathUtils.sqrtAndRemainder(positiveValue);
        long max = maxPair[0];
        boolean isPerfectSquare = (maxPair[1] == 0);
        
        if (positiveValue <= 8) {
            return new ArrayList<>(switch ((int) positiveValue) {
                case 1 -> List.of(Cache.get(1));
                case 2 -> List.of(Cache.get(1), Cache.get(2));
                case 3 -> List.of(Cache.get(1), Cache.get(3));
                case 4 -> List.of(Cache.get(1), Cache.get(2), Cache.get(4));
                case 5 -> List.of(Cache.get(1), Cache.get(5));
                case 6 -> List.of(Cache.get(1), Cache.get(2), Cache.get(3), Cache.get(6));
                case 7 -> List.of(Cache.get(1), Cache.get(7));
                case 8 -> List.of(Cache.get(1), Cache.get(2), Cache.get(4), Cache.get(8));
                default -> throw new RuntimeException("shouldn't be reachable");
            });
        }
        
        LongStream stream = isPerfectSquare
                ? LongStream.range(1, max)
                : LongStream.rangeClosed(1, max);
        
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
    
    @Override
    @SideEffectFree
    public List<? extends AlgebraInteger> primeFactorization() {
        ArrayList<AlgebraInteger> factorList = new ArrayList<>();
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
