package org.cb2384.exactalgebra.objects.numbers.integral;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.function.LongToIntFunction;

import org.cb2384.exactalgebra.objects.exceptions.DisallowedNarrowingException;
import org.cb2384.exactalgebra.objects.internalaccess.CacheInteger;
import org.cb2384.exactalgebra.objects.pair.NumberRemainderPair;
import org.cb2384.exactalgebra.util.BigMathObjectUtils;
import org.cb2384.exactalgebra.util.PrimMathUtils;
import org.cb2384.exactalgebra.objects.Sigmagnum;
import org.cb2384.exactalgebra.util.corutils.NullnessUtils;
import org.cb2384.exactalgebra.util.corutils.ternary.Signum;

import org.checkerframework.checker.index.qual.*;
import org.checkerframework.checker.nullness.qual.*;
import org.checkerframework.common.returnsreceiver.qual.*;
import org.checkerframework.common.value.qual.*;
import org.checkerframework.dataflow.qual.*;

public sealed class FiniteInteger
        extends AbstractAlgebraInteger
        implements Serializable
        permits CacheInteger {
    @Serial
    private static final long serialVersionUID = 0xBE0A89B78FE0C0DAL;
    
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
     * @param   value   the value to represent
     *
     * @param   valueBI the value to represent, as a {@link BigInteger} for the cache
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
     * @param   value   the value to represent
     */
    @SideEffectFree
    private FiniteInteger(
            @IntRange(from = MIN_VALUE) long value
    ) {
        this.value = value;
    }
    
    /**
     * Creates a {@link FiniteInteger} to represent the given value, but first checks the Cache.
     *
     * @param   value   the {@code long} value to represent, though it cannot be {@link Long#MIN_VALUE}
     *
     * @return  {@code value} as a {@link FiniteInteger}
     *
     * @throws  IllegalArgumentException    if {@code value == }{@link Long#MIN_VALUE}
     */
    @SideEffectFree
    public static FiniteInteger valueOf(
            @IntRange(from = MIN_VALUE) long value
    ) {
        if (value == Long.MIN_VALUE) {
            throw new IllegalArgumentException(
                    "This {Long.MIN_VALUE} is the one unsupported long value, for the sake of symmetry");
        }
        return valueFactory(value);
    }
    
    protected static FiniteInteger valueFactory(
            @IntRange(from = MIN_VALUE) long value
    ) {
        if (Math.abs(value) <= CACHE_DEPTH) {
            return Cache.get((int) value);
        }
        return new FiniteInteger(value);
    }
    
    @Override
    @Pure
    public @This FiniteInteger roundQ() {
        return this;
    }
    
    @Override
    @SideEffectFree
    public FiniteInteger roundQ(
            @Nullable Integer precision,
            @Nullable RoundingMode roundingMode
    ) {
        if ((precision == null) || (precision >= Long.toString(Long.MAX_VALUE).length())) {
            return this;
        }
        return valueOf(toBigDecimal(precision, roundingMode).longValue());
    }
    
    @Override
    @SideEffectFree
    public FiniteInteger roundQ(
            @Nullable MathContext mathContext
    ) {
        if ((mathContext == null) || (mathContext.getPrecision() >= Long.toString(Long.MAX_VALUE).length())) {
            return this;
        }
        return valueOf(toBigDecimal(mathContext).longValue());
    }
    
    @Override
    @SideEffectFree
    public BigDecimal toBigDecimal() {
        return BigDecimal.valueOf(value);
    }
    
    @Override
    @SideEffectFree
    public BigDecimal toBigDecimal(
            @Nullable MathContext mathContext
    ) {
        BigDecimal res = BigDecimal.valueOf(value);
        return NullnessUtils.returnDefaultIfNull(mathContext, res::round, res);
    }
    
    @Override
    @SideEffectFree
    public String toString(int radix) {
        return Long.toString(value, radix);
    }
    
    @Override
    @Pure
    public BigInteger toBigInteger() {
        BigInteger valueBI = this.valueBI;
        return (valueBI == null)
                ? this.valueBI = BigInteger.valueOf(value)
                : valueBI;
    }
    
    @Override
    @Pure
    public long longValue() {
        return value;
    }
    
    @Override
    @Pure
    public int intValue() {
        return (int) value;
    }
    
    @Override
    @Pure
    public double doubleValue() {
        return value;
    }
    
    @Override
    @Pure
    public float floatValue() {
        return value;
    }
    
    @Override
    @Pure
    public long longValueExact() {
        return value;
    }
    
    @Pure
    private int getExactVal(
            LongToIntFunction narrowFunc,
            Class<?> targetType
    ) {
        try {
            return narrowFunc.applyAsInt(value);
        } catch (ArithmeticException oldAE) {
            DisallowedNarrowingException newDNE
                    = new DisallowedNarrowingException(AlgebraInteger.class, targetType);
            newDNE.initCause(oldAE);
            throw newDNE;
        }
    }
    
    @Override
    @Pure
    public int intValueExact() {
        return getExactVal(Math::toIntExact, int.class);
    }
    
    @Override
    @Pure
    public short shortValueExact() {
        LongToIntFunction narrowFunc = l -> {
            if ((Short.MIN_VALUE <= l) && (l <= Short.MAX_VALUE)) {
                return (int) l;
            }
            throw new ArithmeticException("Information lost in narrowing!");
        };
        return (short) getExactVal(narrowFunc, short.class);
    }
    
    @Override
    @Pure
    public byte byteValueExact() {
        LongToIntFunction narrowFunc = l -> {
            if ((Byte.MIN_VALUE <= l) && (l <= Byte.MAX_VALUE)) {
                return (int) l;
            }
            throw new ArithmeticException("Information lost in narrowing!");
        };
        return (byte) getExactVal(narrowFunc, byte.class);
    }
    
    @Override
    @Pure
    public char charValueExact() {
        LongToIntFunction narrowFunc = l -> {
            if ((Character.MIN_VALUE <= l) && (l <= Character.MAX_VALUE)) {
                return (int) l;
            }
            throw new ArithmeticException("Information lost in narrowing!");
        };
        return (char) getExactVal(narrowFunc, char.class);
    }
    
    @Override
    @Pure
    public boolean isZero() {
        return value == 0;
    }
    
    @Override
    @Pure
    public boolean isOne() {
        return value == 1;
    }
    
    @Override
    @Pure
    public boolean isNegative() {
        return value < 0;
    }
    
    @Override
    @Pure
    public Signum signum() {
        return Signum.valueOf(value);
    }
    
    @Override
    @Pure
    public Sigmagnum sigmagnum() {
        return Sigmagnum.valueOf(value);
    }
    
    @Override
    @SideEffectFree
    public FiniteInteger negated() {
        return valueFactory(-value);
    }
    
    @Override
    @SideEffectFree
    public FiniteInteger magnitude() {
        return (FiniteInteger) super.magnitude();
    }
    
    @Override
    @SideEffectFree
    public FiniteInteger max(
            long that
    ) {
        return (value < that)
                ? valueFactory(that)
                : this;
    }
    
    @Override
    @SideEffectFree
    public AlgebraInteger gcf(
            AlgebraInteger that
    ) {
        return (that instanceof FiniteInteger thatFI)
                ? gcf(thatFI.value)
                : super.gcf(that);
    }
    
    @Override
    @SideEffectFree
    public FiniteInteger gcf(
            long that
    ) {
        return valueFactory(PrimMathUtils.gcf(value, that));
    }
    
    @Override
    @SideEffectFree
    public AlgebraInteger lcm(
            AlgebraInteger that
    ) {
        return (that instanceof FiniteInteger thatFI)
                ? lcm(thatFI.value)
                : super.lcm(that);
    }
    
    @Override
    @SideEffectFree
    public AlgebraInteger lcm(
            long that
    ) {
        return IntegerFactory.fromBigInteger(BigMathObjectUtils.lcm(value, that));
    }
    
    @Override
    @Pure
    public boolean canDivideBy(
            AlgebraInteger divisor
    ) {
        return (divisor instanceof FiniteInteger divisorPI)
                ? canDivideBy(divisorPI.value)
                : super.canDivideBy(divisor);
    }
    
    @Override
    @Pure
    public boolean canDivideBy(
            long divisor
    ) {
        return value % divisor == 0;
    }
    
    @Override
    @Pure
    public boolean canDivideBy(
            BigInteger divisor
    ) {
        return BigMathObjectUtils.canBeLong(divisor, null)
                && canDivideBy(divisor.longValue());
    }
    
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
    
    @Pure
    public int compareTo(
            FiniteInteger that
    ) {
        return Long.compare(value, that.value);
    }
    
    @Override
    @Pure
    public int compareTo(
            AlgebraInteger that
    ) {
        return (that instanceof FiniteInteger thatPI)
                ? compareTo(thatPI)
                : super.compareTo(that);
    }
    
    @SideEffectFree
    public NumberRemainderPair<FiniteInteger, FiniteInteger> quotientZWithRemainder(
            FiniteInteger divisor
    ) {
        return new NumberRemainderPair<>(quotientZ(divisor), remainder(divisor));
    }
    
    @Override
    @SideEffectFree
    public NumberRemainderPair<FiniteInteger, FiniteInteger> quotientZWithRemainder(
            AlgebraInteger divisor
    ) {
        return new NumberRemainderPair<>(quotientZ(divisor), remainder(divisor));
    }
    
    @SideEffectFree
    public FiniteInteger quotientZ(
            FiniteInteger divisor
    ) {
        return valueFactory(value / divisor.value);
    }
    
    @Override
    @SideEffectFree
    public FiniteInteger quotientZ(
            AlgebraInteger divisor
    ) {
        return (divisor instanceof FiniteInteger divisorFR)
                ? quotientZ(divisorFR)
                : (FiniteInteger) super.quotientZ(divisor);
    }
    
    @SideEffectFree
    public FiniteInteger remainder(
            FiniteInteger divisor
    ) {
        return valueFactory(value % divisor.value);
    }
    
    @Override
    @SideEffectFree
    public FiniteInteger remainder(
            AlgebraInteger divisor
    ) {
        return (divisor instanceof FiniteInteger divisorFI)
                ? remainder(divisorFI)
                : (FiniteInteger) super.remainder(divisor);
    }
    
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
        return valueOf(res);
    }
    
    @Override
    @SideEffectFree
    public FiniteInteger modulo(
            AlgebraInteger modulus
    ) {
        return (modulus instanceof FiniteInteger modulusFI)
                ? modulo(modulusFI)
                : (FiniteInteger) super.modulo(modulus);
    }
    
    @Override
    @SideEffectFree
    public NumberRemainderPair<FiniteInteger, FiniteInteger> rootZWithRemainder(
            int index
    ) {
        FiniteInteger floor = rootRoundZ(index, RoundingMode.DOWN);
        return new NumberRemainderPair<>(floor, (FiniteInteger) difference(floor.raisedZ(index)));
    }
    
    @Override
    @SideEffectFree
    public FiniteInteger rootRoundZ(
            int index,
            @Nullable RoundingMode roundingMode
    ) {
        return (FiniteInteger) super.rootRoundZ(index, roundingMode);
    }
    
    @Override
    @SideEffectFree
    public NumberRemainderPair<FiniteInteger, FiniteInteger> rootZWithRemainder(
            AlgebraInteger index
    ) {
        FiniteInteger floor = rootRoundZ(index, RoundingMode.DOWN);
        return new NumberRemainderPair<>(floor, (FiniteInteger) difference(floor.raisedZ(index)));
    }
    
    @Override
    @SideEffectFree
    public FiniteInteger rootRoundZ(
            AlgebraInteger index,
            @Nullable RoundingMode roundingMode
    ) {
        return (FiniteInteger) super.rootRoundZ(index, roundingMode);
    }
    
    @Override
    @SideEffectFree
    public List<FiniteInteger> factors() {
        return isZero()
                ? new ArrayList<>(0)
                : primitiveFactorer(Math.abs(value));
    }
    
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
