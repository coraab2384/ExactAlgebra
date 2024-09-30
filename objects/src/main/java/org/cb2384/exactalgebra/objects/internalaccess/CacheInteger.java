package org.cb2384.exactalgebra.objects.internalaccess;

import java.io.ObjectStreamException;
import java.io.Serial;
import java.math.BigInteger;
import java.util.List;
import java.util.stream.IntStream;

import org.cb2384.exactalgebra.objects.numbers.AlgebraNumber;
import org.cb2384.exactalgebra.objects.numbers.integral.FiniteInteger;

import org.checkerframework.common.value.qual.*;
import org.checkerframework.dataflow.qual.*;

/**
 * Exists solely for module-private but package-public constructor access;
 * the constructor is otherwise disallowed as it has no input validation.
 *
 * @author  Corinne Buxton
 */
public final class CacheInteger
        extends FiniteInteger {
    
    /**
     * serialVersionUID
     */
    @Serial
    private static final long serialVersionUID = 0x83A0B98FB73A1AEL;
    
    /**
     * The Cache. It stores all values from negative to positive {@link #CACHE_DEPTH}.
     * As it is immutable, and this class not exported, it is ok to be public
     */
    public static final List<List<CacheInteger>> CACHE = List.of(
            buildRow(true),
            List.of(new CacheInteger(0)),
            buildRow(false)
    );
    
    /**
     * Passes the given value up, both as itself and as a {@link BigInteger} for pre-caching
     *
     * @param value the value to represent
     */
    @SideEffectFree
    private CacheInteger(
            @IntRange(from = -AlgebraNumber.CACHE_DEPTH, to = AlgebraNumber.CACHE_DEPTH) int value
    ) {
        super(value, BigInteger.valueOf(value));
    }
    
    /**
     * Simply passes the values up to {@link FiniteInteger#FiniteInteger(long, BigInteger)}.
     *
     * @param value     the value to represent
     * @param valueBI   the value to represent, as a {@link BigInteger} for the cache
     */
    @SideEffectFree
    public CacheInteger(
            @IntRange(from = MIN_VALUE) long value,
            BigInteger valueBI
    ) {
        super(value, valueBI);
        assert (valueBI.longValueExact() == value) && (value != Long.MIN_VALUE);
    }
    
    /**
     * An alternate version of {@link #valueOf(long)} which pre-caches the {@link BigInteger} value as well.
     *
     * @param value     the {@code long} value to represent, though it cannot be {@link Long#MIN_VALUE}
     * @param valueBI   the {@link BigInteger} representation of {@code value}, to be handed to the cache field
     *
     * @return  A {@link FiniteInteger} representing {@code value} and {@code valueBI}
     */
    @SideEffectFree
    public static FiniteInteger valueOf(
            @IntRange(from = MIN_VALUE) long value,
            BigInteger valueBI
    ) {
        assert (valueBI.longValueExact() == value) && (value != Long.MIN_VALUE);
        if (Math.abs(value) <= CACHE_DEPTH) {
            return getFromCache((int) value);
        }
        return new CacheInteger(value, valueBI);
    }
    
    /**
     * Used to build a row of the Cache
     *
     * @param negate    whether this is the negative or positive row
     *
     * @return  the built row (from {@code 1} to {@link #CACHE_DEPTH} or
     *          {@code -}{@link #CACHE_DEPTH} to {@code -1}, inclusive)
     */
    @SideEffectFree
    private static List<CacheInteger> buildRow(
            boolean negate
    ) {
        IntStream intStream = IntStream.rangeClosed(1, CACHE_DEPTH);
        if (negate) {
            intStream = intStream.map(i -> -i);
        }
        return intStream.mapToObj(CacheInteger::new).toList();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Pure
    public BigInteger toBigInteger() {
        assert valueBI != null;
        return valueBI;
    }
    
    /**
     * Finisher for deserialization. If the value is in {@link #CACHE} then the pre-cached value is returned
     * instead. Otherwise, {@link #valueBI} is regenerated, as it is transient.
     *
     * @return  either this value, or the cached copy of this value
     *
     * @throws ObjectStreamException    If something goes wrong with deserialization, though I don't actually
     *                                  think it is feasible to be thrown in this particular implementation
     */
    @Serial
    @SideEffectFree
    private Object readResolve() throws ObjectStreamException {
        if (Math.abs(value) <= CACHE_DEPTH) {
            return getFromCache((int) value);
        }
        valueBI = BigInteger.valueOf(value);
        return this;
    }
}
