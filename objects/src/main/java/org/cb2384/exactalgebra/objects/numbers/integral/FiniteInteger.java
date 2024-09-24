package org.cb2384.exactalgebra.objects.numbers.integral;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.function.LongPredicate;

import org.cb2384.exactalgebra.objects.exceptions.DisallowedNarrowingException;
import org.cb2384.exactalgebra.objects.internalaccess.CacheInteger;
import org.cb2384.exactalgebra.objects.numbers.AlgebraNumber;
import org.cb2384.exactalgebra.objects.pair.NumberRemainderPair;
import org.cb2384.exactalgebra.util.BigMathObjectUtils;
import org.cb2384.exactalgebra.util.PrimMathUtils;
import org.cb2384.exactalgebra.objects.Sigmagnum;
import org.cb2384.exactalgebra.util.corutils.NullnessUtils;
import org.cb2384.exactalgebra.util.corutils.StringUtils;
import org.cb2384.exactalgebra.util.corutils.ternary.Signum;

import org.checkerframework.checker.index.qual.*;
import org.checkerframework.checker.nullness.qual.*;
import org.checkerframework.common.returnsreceiver.qual.*;
import org.checkerframework.common.value.qual.*;
import org.checkerframework.dataflow.qual.*;

/**
 * <p>Essentially a wrapper around a {@code long}, though not supporting {@link Long#MIN_VALUE}, and with
 * more functions and integration into the rest of the {@link
 * org.cb2384.exactalgebra.objects.numbers.AlgebraNumber AlgebraNumber} hierarchy.</p>
 *
 * <p>Throws:&ensp{@link NullPointerException} on any {@code null} argument, unless otherwise specified</p>
 *
 * @author  Corinne Buxton
 */
public sealed class FiniteInteger
        extends AbstractAlgebraInteger
        implements Serializable
        permits CacheInteger {
    
    /**
     * A tribute for the serial gods
     */
    @Serial
    private static final long serialVersionUID = 0xBE0A89B78FE0C0DAL;
    
    private static final int LONG_MAX_LEN = Long.toString(Long.MAX_VALUE).length();
    
    /**
     * {@code -}{@link Long#MAX_VALUE}, the lowest support {@code long} value, for the sake of symmetry.
     */
    protected static final long MIN_VALUE = Long.MIN_VALUE + 1;
    
    /**
     * The actual value
     */
    protected final @IntRange(from = MIN_VALUE) long value;
    
    /**
     * A stable cache of the {@link BigInteger} representation of this,
     * since that class is often used for calculations
     */
    protected transient @MonotonicNonNull BigInteger valueBI;
    
    /**
     * Exists solely for {@link CacheInteger}, which has the {@link BigInteger} cache pre-filled.
     *
     * @param value   the value to represent
     *
     * @param valueBI the value to represent, as a {@link BigInteger} for the cache
     */
    @SideEffectFree
    protected FiniteInteger(
            @IntRange(from = MIN_VALUE) long value,
            BigInteger valueBI
    ) {
        this.value = value;
        this.valueBI = valueBI;
    }
    
    /**
     * Creates the object from the value
     *
     * @param value   the value to represent
     */
    @SideEffectFree
    private FiniteInteger(
            @IntRange(from = MIN_VALUE) long value
    ) {
        this.value = value;
    }
    
    /**
     * Yields a FiniteInteger to represent the given value.
     *
     * @param value the {@code long} to represent
     *
     * @return  {@code value} as a FiniteInteger
     */
    public static AlgebraInteger valueOf(
            long value
    ) {
        return (value == Long.MIN_VALUE)
                ? new ArbitraryInteger(value)
                : valueFactory(value);
    }
    
    /**
     * Yields a FiniteInteger to represent the given value, but first checks the Cache.
     *
     * @param value   the {@code long} value to represent, though it cannot be {@link Long#MIN_VALUE}
     *
     * @return  {@code value} as a FiniteInteger
     *
     * @throws IllegalArgumentException if {@code value == }{@link Long#MIN_VALUE}
     */
    @SideEffectFree
    public static FiniteInteger valueOfStrict(
            @IntRange(from = MIN_VALUE) long value
    ) {
        if (value == Long.MIN_VALUE) {
            throw new IllegalArgumentException(
                    "This {Long.MIN_VALUE} is the one unsupported long value, for the sake of symmetry");
        }
        return valueFactory(value);
    }
    
    @SideEffectFree
    public static AlgebraInteger valueOf(
            BigInteger value
    ) {
        return valueFactory(value, false);
    }
    
    /**
     * Creates a {@link FiniteInteger} to represent the given value, but first checks the Cache.
     *
     * @param value   the {@code long} value to represent, though it cannot be {@link Long#MIN_VALUE}
     *
     * @return  {@code value} as a {@link FiniteInteger}
     *
     * @throws IllegalArgumentException if {@code value == }{@link Long#MIN_VALUE}
     */
    @SideEffectFree
    public static FiniteInteger valueOfStrict(
            BigInteger value
    ) {
        return (FiniteInteger) valueFactory(value, true);
    }
    
    /**
     * Checks if the value is cached, and calls the constructor if not.
     *
     * @param value the value of the result
     *
     * @return  a FiniteInteger with the given value
     */
    @SideEffectFree
    static FiniteInteger valueFactory(
            @IntRange(from = MIN_VALUE) long value
    ) {
        if (Math.abs(value) <= CACHE_DEPTH) {
            return getFromCache((int) value);
        }
        return new FiniteInteger(value);
    }
    
    /**
     * Checks if the value is cached, and calls the constructor of the appropriate size if not.
     *
     * @param value     the value of the result
     * @param strict    whether to allow results larger than a FiniteInteger
     *
     * @return  an AlgebraInteger with the given value
     */
    @SideEffectFree
    static AlgebraInteger valueFactory(
            BigInteger value,
            boolean strict
    ) {
        if (BigMathObjectUtils.canBeLong(value, PrimMathUtils.IntegralBoundaryTypes.SHORTENED)) {
            long valLong = value.longValue();
            if (Math.abs(valLong) <= CACHE_DEPTH) {
                return getFromCache((int) valLong);
            }
            return new CacheInteger(valLong, value);
        }
        if (strict) {
            throw new IllegalArgumentException("This value is too large for "
                    + StringUtils.getIdealName(FiniteInteger.class));
        }
        return new ArbitraryInteger(value);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Pure
    public @This FiniteInteger roundQ() {
        return this;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @SideEffectFree
    public FiniteInteger roundQ(
            @Nullable Integer precision,
            @Nullable RoundingMode roundingMode
    ) {
        if ((precision == null) || (precision >= LONG_MAX_LEN)) {
            return this;
        }
        return valueOfStrict(toBigDecimal(precision, roundingMode).longValue());
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @SideEffectFree
    public FiniteInteger roundQ(
            @Nullable MathContext mathContext
    ) {
        if ((mathContext == null) || (mathContext.getPrecision() >= LONG_MAX_LEN)) {
            return this;
        }
        return valueOfStrict(toBigDecimal(mathContext).longValue());
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @SideEffectFree
    public BigDecimal toBigDecimal() {
        return BigDecimal.valueOf(value);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @SideEffectFree
    public BigDecimal toBigDecimal(
            @Nullable MathContext mathContext
    ) {
        BigDecimal result = BigDecimal.valueOf(value);
        return NullnessUtils.returnDefaultIfNull(mathContext, result::round, result);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @SideEffectFree
    public String toString(int radix) {
        if ((Character.MIN_RADIX <= radix) && (radix <= Character.MAX_RADIX)) {
            return Long.toString(value, radix);
        }
        throw new NumberFormatException("radix " + radix + "is not an allowed radix!");
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Pure
    public BigInteger toBigInteger() {
        BigInteger valueBI = this.valueBI;
        return (valueBI == null)
                ? this.valueBI = BigInteger.valueOf(value)
                : valueBI;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Pure
    public long longValue() {
        return value;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Pure
    public int intValue() {
        return (int) value;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Pure
    public short shortValue() {
        return (short) value;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Pure
    public byte byteValue() {
        return (byte) value;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Pure
    public double doubleValue() {
        return value;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Pure
    public float floatValue() {
        return value;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Pure
    public long longValueExact() {
        return value;
    }
    
    /**
     * Similar to {@link AbstractAlgebraInteger#getExactVal} in effect, but designed to go from long to int
     * rather than BigInteger to long
     *
     * @param testNarrowPred    the function that does the narrowing
     * @param targetType    the target narrowing type, for use with the exception message
     *
     * @return  the narrowed value, if no information will be lost
     *
     * @throws DisallowedNarrowingException if information would be lost
     */
    @Pure
    private int getExactVal(
            LongPredicate testNarrowPred,
            Class<?> targetType
    ) {
        if (testNarrowPred.test(value)) {
            return (int) value;
        }
        throw new DisallowedNarrowingException(FiniteInteger.class, targetType);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Pure
    public int intValueExact() {
        LongPredicate test = l -> PrimMathUtils.canBeInt(l, null);
        return getExactVal(test, int.class);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Pure
    public short shortValueExact() {
        LongPredicate test = l -> (Short.MIN_VALUE <= l) && (l <= Short.MAX_VALUE);
        return (short) getExactVal(test, short.class);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Pure
    public byte byteValueExact() {
        LongPredicate test = l -> (Byte.MIN_VALUE <= l) && (l <= Byte.MAX_VALUE);
        return (byte) getExactVal(test, byte.class);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Pure
    public char charValueExact() {
        LongPredicate test = l -> (Character.MIN_VALUE <= l) && (l <= Character.MAX_VALUE);
        return (char) getExactVal(test, char.class);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Pure
    public boolean isZero() {
        return value == 0;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Pure
    public boolean isOne() {
        return value == 1;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Pure
    public boolean isNegative() {
        return value < 0;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Pure
    public Signum signum() {
        return Signum.valueOf(value);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Pure
    public Sigmagnum sigmagnum() {
        return Sigmagnum.valueOf(value);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @SideEffectFree
    public FiniteInteger negated() {
        return valueFactory(-value);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @SideEffectFree
    public FiniteInteger magnitude() {
        return isNegative() ? negated() : this;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @SideEffectFree
    public FiniteInteger max(
            long that
    ) {
        return (value < that)
                ? valueFactory(that)
                : this;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @SideEffectFree
    public AlgebraInteger gcf(
            AlgebraInteger that
    ) {
        return (that instanceof FiniteInteger thatFI)
                ? gcf(thatFI.value)
                : super.gcf(that);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @SideEffectFree
    public FiniteInteger gcf(
            long that
    ) {
        return valueFactory(PrimMathUtils.gcf(value, that));
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @SideEffectFree
    public AlgebraInteger lcm(
            AlgebraInteger that
    ) {
        return (that instanceof FiniteInteger thatFI)
                ? lcm(thatFI.value)
                : super.lcm(that);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @SideEffectFree
    public AlgebraInteger lcm(
            long that
    ) {
        return IntegerFactory.fromBigInteger(BigMathObjectUtils.lcm(value, that));
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Pure
    public boolean canDivideBy(
            AlgebraInteger divisor
    ) {
        return (divisor instanceof FiniteInteger divisorFI)
                ? canDivideBy(divisorFI.value)
                : super.canDivideBy(divisor);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Pure
    public boolean canDivideBy(
            long divisor
    ) {
        return (divisor != 0) && (value % divisor == 0);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Pure
    public boolean canDivideBy(
            BigInteger divisor
    ) {
        return BigMathObjectUtils.canBeLong(divisor, PrimMathUtils.IntegralBoundaryTypes.SHORTENED)
                && !BigMathObjectUtils.isZero(divisor) && canDivideBy(divisor.longValue());
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Pure
    public boolean isPrime() {
        return (value > 0) && isPrime(value);
    }
    
    @SideEffectFree
    private static boolean isPrime(
            @Positive long value
    ) {
        // Only even prime
        if (value == 2) {
            return true;
        }
        
        // If can be ruled out as not being prime
        if ((value == 1) || (value % 2 == 0)) {
            return false;
        }
        
        long max = (long) Math.sqrt(value);
        
        for (long l = 3; l <= max; l += 2) {
            if (value % l == 0) {
                return false;
            }
        }
        //else
        return true;
    }
    
    /**
     * Like {@link #compareTo(AlgebraNumber)}, but specialized specifically for FiniteIntegers.
     *
     * @param that  the FiniteInteger to be compared
     *
     * @return  a negative integer, zero, or a positive integer
     *          as this is less than, equal to, or greater than {@code that}
     */
    @Pure
    public int compareTo(
            FiniteInteger that
    ) {
        return Long.compare(value, that.value);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Pure
    public int compareTo(
            AlgebraInteger that
    ) {
        return (that instanceof FiniteInteger thatPI)
                ? compareTo(thatPI)
                : super.compareTo(that);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Pure
    public boolean equiv(
            AlgebraInteger that
    ) {
        return (that instanceof FiniteInteger thatFR)
                ? value == thatFR.value
                : super.equiv(that);
    }
    
    /**
     * Divides this by {@code divisor}, with the caveat that the quotient is a whole number
     * ({@link AlgebraInteger}).
     * That is, the quotient is the highest value such divisor {@code divisor * quotient <= this}.
     * The second value is the remainder, which is {@code this - (divisor * quotient)}.
     *
     * @param   divisor the value to divide this by
     *
     * @return  an array with the quotient, followed by the remainder
     *
     * @throws  ArithmeticException if {@code divisor == 0}
     */
    @SideEffectFree
    public NumberRemainderPair<FiniteInteger, FiniteInteger> quotientZWithRemainder(
            FiniteInteger divisor
    ) {
        return new NumberRemainderPair<>(quotientZ(divisor), remainder(divisor));
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @SideEffectFree
    public NumberRemainderPair<FiniteInteger, FiniteInteger> quotientZWithRemainder(
            AlgebraInteger divisor
    ) {
        return new NumberRemainderPair<>(quotientZ(divisor), remainder(divisor));
    }
    
    /**
     * Divides this by {@code divisor}, with the caveat that the quotient is a whole number
     * ({@link AlgebraInteger}).
     * That is, the quotient is the highest value such divisor {@code divisor * quotient <= this}.
     *
     * @param   divisor the value to divide this by
     *
     * @return  the quotient of {@code this / divisor}, specifically using integer division
     *
     * @throws  ArithmeticException if {@code divisor == 0}
     */
    @SideEffectFree
    public FiniteInteger quotientZ(
            FiniteInteger divisor
    ) {
        return valueFactory(value / divisor.value);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @SideEffectFree
    public FiniteInteger quotientZ(
            AlgebraInteger divisor
    ) {
        return (divisor instanceof FiniteInteger divisorFR)
                ? quotientZ(divisorFR)
                : (FiniteInteger) super.quotientZ(divisor);
    }
    
    /**
     * Finds the remainder of this if it were divided by {@code divisor}, in line with {@code %}.
     * Specifically, this is the remainder and not the modulo, so it will not throw errors on negatives.
     *
     * @implNote    The remainder is as if from {@code this - (divisor * quotient)}, but there is no requirement
     *              the quotient be specifically calculated if it is not needed to find the result.
     *
     * @param   divisor    the value to pretend divide this by for the purpose of finding the remainder.
     *
     * @return  the remainder as if from {@code this % divisor}
     *
     * @throws  ArithmeticException if {@code divisor == 0}
     */
    @SideEffectFree
    public FiniteInteger remainder(
            FiniteInteger divisor
    ) {
        return valueFactory(value % divisor.value);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @SideEffectFree
    public FiniteInteger remainder(
            AlgebraInteger divisor
    ) {
        return (divisor instanceof FiniteInteger divisorFI)
                ? remainder(divisorFI)
                : (FiniteInteger) super.remainder(divisor);
    }
    
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
    public FiniteInteger modulo(
            FiniteInteger modulus
    ) {
        if (modulus.value <= 0) {
            throw new ArithmeticException("Non-positive modulus!");
        }
        long res = value % modulus.value;
        if (res < 0) {
            res += modulus.value;
        }
        return valueOfStrict(res);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @SideEffectFree
    public FiniteInteger modulo(
            AlgebraInteger modulus
    ) {
        return (modulus instanceof FiniteInteger modulusFI)
                ? modulo(modulusFI)
                : (FiniteInteger) super.modulo(modulus);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @SideEffectFree
    public NumberRemainderPair<FiniteInteger, FiniteInteger> rootZWithRemainder(
            int index
    ) {
        FiniteInteger floor = rootRoundZ(index, RoundingMode.DOWN);
        FiniteInteger reverseAns = (index < 0)
                ? floor
                : (FiniteInteger) floor.raisedZ(index);
        return new NumberRemainderPair<>(this, floor, reverseAns);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @SideEffectFree
    public FiniteInteger rootRoundZ(
            int index,
            @Nullable RoundingMode roundingMode
    ) {
        return (FiniteInteger) super.rootRoundZ(index, roundingMode);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @SideEffectFree
    public NumberRemainderPair<FiniteInteger, FiniteInteger> rootZWithRemainder(
            AlgebraInteger index
    ) {
        FiniteInteger floor = rootRoundZ(index, RoundingMode.DOWN);
        FiniteInteger reverseAns = index.isNegative()
                ? floor
                : (FiniteInteger) floor.raisedZ(index);
        return new NumberRemainderPair<>(this, floor, reverseAns);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @SideEffectFree
    public FiniteInteger rootRoundZ(
            AlgebraInteger index,
            @Nullable RoundingMode roundingMode
    ) {
        return (FiniteInteger) super.rootRoundZ(index, roundingMode);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @SideEffectFree
    public List<FiniteInteger> factors() {
        return isZero()
                ? new ArrayList<>(0)
                : primitiveFactorer(Math.abs(value));
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @SideEffectFree
    public List<FiniteInteger> primeFactorization() {
        ArrayList<FiniteInteger> factorList = new ArrayList<>();
        if (value > 0) {
            long currVal = value;
            long factor = 2;
            while (!isPrime(currVal)) {
                if (currVal % factor == 0) {
                    factorList.add(valueFactory(factor));
                    currVal /= factor;
                } else {
                    factor++;
                }
            }
            factorList.add(valueFactory(currVal));
        }
        return factorList;
    }
}
