package org.cb2384.exactalgebra.objects.internalaccess.factory;

import java.math.BigInteger;

import org.cb2384.exactalgebra.objects.internalaccess.CacheInteger;
import org.cb2384.exactalgebra.objects.numbers.AlgebraNumber;
import org.cb2384.exactalgebra.objects.numbers.integral.AbstractAlgebraInteger;
import org.cb2384.exactalgebra.objects.numbers.integral.AlgebraInteger;
import org.cb2384.exactalgebra.objects.numbers.integral.ArbitraryInteger;
import org.cb2384.exactalgebra.objects.numbers.integral.FiniteInteger;
import org.cb2384.exactalgebra.util.BigMathObjectUtils;
import org.cb2384.exactalgebra.util.corutils.ternary.ComparableSwitchSignum;
import org.cb2384.exactalgebra.util.corutils.ternary.Ternary;

import org.checkerframework.checker.nullness.qual.*;
import org.checkerframework.common.value.qual.*;
import org.checkerframework.dataflow.qual.*;

/**
 * <p>A parameter for a factory; a mutable class built around {@link BigInteger}, though which also tracks
 * a {@code long} primitive value if the value can in fact be represented as a long.</p>
 *
 * <p>Throws:&ensp;{@link NullPointerException} &ndash; on any {@code null} input</p>
 *
 * @author  Corinne Buxton
 */
public final class IntValuedParameter
        implements ComparableSwitchSignum<IntValuedParameter>,
                Parameter<AlgebraInteger, AlgebraNumber, BigInteger> {
    
    /**
     * Indicates that primValue is a meaningful value, not just a default 0
     * NULL means that the value hasn't been measured yet
     */
    private Ternary isPrim = Ternary.NULL;
    
    /**
     * The primitive value, if this value can be primitive
     */
    private long primValue;
    
    /**
     * The main BigInteger value
     */
    private BigInteger value;
    
    /**
     * Constructs a Parameter using the given {@code value}.
     *
     * @param   value   the {@link BigInteger} whose value this will represent
     */
    @SideEffectFree
    public IntValuedParameter(
            BigInteger value
    ) {
        this.value = Parameter.confirmNonNull(value);
    }
    
    /**
     * Constructs a Parameter using the given {@code value}.
     *
     * @param   value   the {@code long} whose value this will represent
     */
    @SideEffectFree
    public IntValuedParameter(
            long value
    ) {
        reset(value);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Pure
    public boolean isZero() {
        return BigMathObjectUtils.isZero(value);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Pure
    public boolean isOne() {
        return BigMathObjectUtils.isOne(value);
    }
    
    /**
     * Checks if the value of this parameter is negative
     *
     * @return  {@code true} if this is negative, otherwise {@code false}
     */
    @Override
    @Pure
    public boolean isNegative() {
        return value.signum() == -1;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equiv(
            Parameter<?, AlgebraNumber, BigInteger> that
    ) {
        return value.equals(that.value());
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Pure
    public int compareTo(
            IntValuedParameter o
    ) {
        return value.compareTo(o.value);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Pure
    public boolean equals(
            @Nullable Object obj
    ) {
        return (obj instanceof IntValuedParameter objIVP) && value.equals(objIVP.value);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Pure
    public int hashCode() {
        return ~value.hashCode();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @SideEffectFree
    public String toString() {
        return toString(10);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @SideEffectFree
    public String toString(
            @IntRange(from = Character.MIN_RADIX, to = Character.MAX_RADIX) int radix
    ) {
        return "Integer-Valued Parameter: " + value.toString(radix);
    }
    
    /**
     * Determines if the value of this Parameter can be represented by a primitive {@code long},
     * and thus that {@link #valPrim} is safe to call, if that is not yet known; or,
     * or the known value to that question if already determined.
     *
     * @return  {@code true} if this value can be represented as a primitive {@code long}
     *          (and therefore {@link #valPrim()} is safe to call)
     */
    @Deterministic
    public boolean isPrim() {
        if (isPrim == Ternary.DEFAULT) {
            if (BigMathObjectUtils.canBeLong(value, null)) {
                isPrim = Ternary.TRUE;
                primValue = value.longValue();
            } else {
                isPrim = Ternary.FALSE;
            }
        }
        assert isPrim.booleanValue() != null;
        return isPrim.booleanValue();
    }
    
    /**
     * Returns the value of this Parameter as a primitive {@code long}, assuming that that can be done.
     * Throws an error if it cannot.
     *
     * @return  the value of this represented as a {@code long}
     *
     * @throws  IllegalStateException   if the state of this parameter is such that the value
     *                                  cannot be represented as a {@code long}; namely, the value
     *                                  is too big
     */
    @Pure
    public long primValue() {
        if (isPrim()) {
            return primValue;
        }
        throw new IllegalStateException("This Parameter has no primitive-representable value!");
    }
    
    /**
     * Yields the {@link BigInteger} representation of this Parameter's value.
     *
     * @return  the value of this represented as a {@link BigInteger}
     */
    @Override
    @Pure
    public BigInteger value() {
        return value;
    }
    
    /**
     * Completely resets this Parameter, as if overwriting it with
     * {@link #Parameter(BigInteger) new Parameter(}{@code value}{@link #Parameter(BigInteger) )}.
     *
     * @param   value   the {@link BigInteger} whose value this will now represent
     */
    @Override
    public void reset(
            BigInteger value
    ) {
        this.value = Parameter.confirmNonNull(value);
        isPrim = Ternary.NULL;
    }
    
    /**
     * Completely resets this Parameter, as if overwriting it with
     * {@link #Parameter(long) new Parameter(}{@code value}{@link #Parameter(long) )}.
     *
     * @param   value   the {@code long} whose value this will now represent
     */
    public void reset(
            long value
    ) {
        isPrim = Ternary.TRUE;
        primValue = value;
        this.value = BigInteger.valueOf(value);
    }
    
    /**
     * <p>Measures the size of the value of this parameter, and compares it with the given size
     * per {@link NarrowTo#comp}, in effect being equivalent to {@code currentDepth}{@link NarrowTo#comp
     * .comp(}{@link NarrowTo#getPrimNarrow(BigInteger) NarrowTo.getPrimNarrow(}{@link #valBI()}{@link
     * NarrowTo#getPrimNarrow(BigInteger) )}{@link NarrowTo#comp )} but while also updating the internal logic
     * regarding {@link #isPrim()} and {@link #valPrim()} in accordance with the measuring results.</p>
     *
     * <p>Is exactly equivalent to {@code currentDepth}{@link NarrowTo#getAndCompPrimNarrow(NumberFactory.Parameter)
     * .getAndCompPrimNarrow(}{@code this}{@link NarrowTo#getAndCompPrimNarrow(NumberFactory.Parameter) )}.</p>
     *
     * @param   currentDepth    the current depth of this factory, which is then compared via
     *                          {@link NarrowTo#comp comp} (which uses logic in line with the standard
     *                          {@link Enum#compareTo})
     *
     * @return  the result of the comparison between the given {@link NarrowTo NarrowTo} and that derived
     *          from processing this parameter (that is, the larger of the two)
     */
    public NarrowTo process(
            NarrowTo currentDepth
    ) {
        return isPrim()
                ? currentDepth.getAndCompPrimNarrow(primValue)
                : NarrowTo.ARBITRARY;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void negate() {
        value = value.negate();
        switch (isPrim) {
            case TRUE -> primValue = -primValue;
            case DEFAULT -> isPrim();
        }
    }
    
    /**
     * Constructs an {@link AlgebraInteger} out of the value of this Parameter;
     * specifically, it will be a {@link FiniteInteger} if {@link #isPrim()} and otherwise
     * an {@link ArbitraryInteger}, both of which are subclasses of {@link AbstractAlgebraInteger}.
     *
     * @return  an {@link AlgebraInteger} implementation (and {@link AbstractAlgebraInteger} subclass)
     *          equivalent in value to this parameter
     */
    @Override
    @SideEffectFree
    public AlgebraInteger asAlgebraObject() {
        assert value != null;
        return (isPrim() && (primValue != Long.MIN_VALUE))
                ? CacheInteger.valueOf(primValue, value)
                : ArbitraryInteger.valueOfStrict(value);
    }
}
