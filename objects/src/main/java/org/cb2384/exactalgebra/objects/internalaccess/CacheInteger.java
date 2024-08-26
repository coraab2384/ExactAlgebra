package org.cb2384.exactalgebra.objects.internalaccess;

import java.io.ObjectStreamException;
import java.io.Serial;
import java.math.BigInteger;

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
    
    @Serial
    private static final long serialVersionUID = 0x83A0B98FB73A1AEL;
    
    /**
     * Passes the given value up, both as itself and as a {@link BigInteger} for pre-caching
     *
     * @param   value   the value to represent
     */
    @SideEffectFree
    public CacheInteger(
            @IntRange(from = -AlgebraNumber.CACHE_DEPTH, to = AlgebraNumber.CACHE_DEPTH) int value
    ) {
        super(value, BigInteger.valueOf(value));
    }
    
    /**
     * Simply passes the values up
     *
     * @param   value   the value to represent
     *
     * @param   valueBI the value to represent, as a {@link BigInteger} for the cache
     */
    @SideEffectFree
    public CacheInteger(
            @IntRange(from = MIN_VALUE) long value,
            BigInteger valueBI
    ) {
        super(value, valueBI);
    }
    
    /**
     * An alternate version of {@link #valueOf(long)} which pre-caches the {@link BigInteger} value as well.
     *
     * @param   value   the {@code long} value to represent, though it cannot be {@link Long#MIN_VALUE}
     *
     * @param   valueBI the {@link BigInteger} representation of {@code value}, to be handed to the cache field
     *
     * @return  A {@link FiniteInteger} representing {@code value} and {@code valueBI}
     */
    @SideEffectFree
    public static FiniteInteger valueOf(
            @IntRange(from = MIN_VALUE) long value,
            BigInteger valueBI
    ) {
        if (Math.abs(value) <= CACHE_DEPTH) {
            return Cache.get((int) value);
        }
        return new CacheInteger(value, valueBI);
    }
    
    @Override
    @Pure
    public BigInteger toBigInteger() {
        assert valueBI != null;
        return valueBI;
    }
    
    @Serial
    private Object readResolve() throws ObjectStreamException {
        if (Math.abs(value) <= CACHE_DEPTH) {
            return Cache.get((int) value);
        }
        valueBI = BigInteger.valueOf(value);
        return this;
    }
}
