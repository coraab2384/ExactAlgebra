package org.cb2384.exactalgebra.objects.numbers.integral;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigInteger;

import org.cb2384.exactalgebra.objects.pair.NumberRemainderPair;

import org.checkerframework.dataflow.qual.*;

/**
 * <p>Essentially a wrapper around {@link BigInteger}, though with more functions and integration into
 * the rest of the {@link org.cb2384.exactalgebra.objects.numbers.AlgebraNumber AlgebraNumber} hierarchy.</p>
 *
 * <p>Throws:&ensp{@link NullPointerException} on any {@code null} argument, unless otherwise specified</p>
 *
 * @author  Corinne Buxton
 */
public final class ArbitraryInteger
        extends AbstractAlgebraInteger
        implements Serializable {
    
    /**
     * The cereal gods be assuaged
     */
    @Serial
    private static final long serialVersionUID = 0x83A0B98FB73A1A4L;
    
    /**
     * the actual value
     */
    private final BigInteger value;
    
    /**
     * Constructs an {@link ArbitraryInteger} from the given {@link BigInteger}.
     *
     * @param   value   the actual value that this will have
     */
    @SideEffectFree
    ArbitraryInteger(
            BigInteger value
    ) {
        this.value = value;
    }
    
    /**
     * Constructs an {@link ArbitraryInteger} from the given {@code long}, though why?
     * {@link FiniteInteger} exists after all.
     *
     * @param   value   the actual value that this will have
     */
    @SideEffectFree
    ArbitraryInteger(
            long value
    ) {
        this.value = BigInteger.valueOf(value);
    }
    
    public static ArbitraryInteger valueOfStrict(
            BigInteger value
    ) {
        return new ArbitraryInteger(value);
    }
    
    public static AlgebraInteger valueOf(
            BigInteger value
    ) {
        return IntegerFactory.fromBigInteger(value);
    }
    
    public static ArbitraryInteger valueOfStrict(
            long value
    ) {
        return new ArbitraryInteger(value);
    }
    
    public static AlgebraInteger valueOf(
            long value
    ) {
        return IntegerFactory.fromLong(value);
    }
    
    @Override
    @Pure
    public BigInteger toBigInteger() {
        return value;
    }
    
    @Override
    @SideEffectFree
    public NumberRemainderPair<AlgebraInteger, AlgebraInteger> sqrtZWithRemainder() {
        BigInteger[] ans = value.sqrtAndRemainder();
        return new NumberRemainderPair<>(IntegerFactory.fromBigInteger(ans[0]),
                IntegerFactory.fromBigInteger(ans[1]) );
    }
}
