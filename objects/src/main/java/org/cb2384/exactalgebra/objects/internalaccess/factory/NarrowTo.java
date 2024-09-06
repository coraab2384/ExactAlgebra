package org.cb2384.exactalgebra.objects.internalaccess.factory;

import java.math.BigInteger;

import org.cb2384.exactalgebra.util.BigMathObjectUtils;
import org.cb2384.exactalgebra.util.PrimMathUtils.IntegralBoundaryTypes;
import org.cb2384.exactalgebra.util.corutils.ternary.ComparableSwitchSignum;

import org.checkerframework.dataflow.qual.*;

/**
 * <p>An {@link Enum} used to measure the size of parameters, to determine which implementation
 * the factory should ultimately employ.</p>
 *
 * <p>Throws:&ensp;{@link NullPointerException} &ndash; on any {@code null} input</p>
 *
 * @author  Corinne Buxton
 */
public enum NarrowTo
        implements ComparableSwitchSignum<NarrowTo> {
    /**
     * Large enough to only fit in a {@link BigInteger}
     */
    ARBITRARY,
    /**
     * Large enough to only fit in a {@code long} or larger
     */
    LONG,
    /**
     * Large enough to only fit in an {@code int} or larger
     */
    INTEGER,
    /**
     * 'Large' enough to 'only' fit in a {@code short} or larger
     */
    SHORT,
    /**
     * Small enough to fit inside a {@code byte}
     */
    BYTE,
    /**
     * Signifies an as-yet-unmeasured size, fulfilling a role similar to{@code null}
     * but without causing {@link NullPointerException}s
     */
    NULL;
    
    /**
     * Gets the {@link NarrowTo NarrowTo} constant that indicates the actual length of {@code parameter}.
     * Has the side effect of {@linkplain IntValuedParameter#process process}ing the parameter,
     * through the process by which the parameter's internal value is accessed.
     *
     * @param   parameter   the Parameter whose actual size is to measured
     *
     * @return  the corresponding {@code enum} constant for the length of the actual numerical
     *          value represented by {@code parameter}
     *
     * @see IntValuedParameter#process
     */
    @Deterministic
    public static NarrowTo getPrimNarrow(
            IntValuedParameter parameter
    ) {
        return parameter.isPrim()
                ? getPrimNarrow(parameter.primValue())
                : NarrowTo.ARBITRARY;
    }
    
    /**
     * Gets the {@link NarrowTo NarrowTo} constant that indicates the actual length of {@code value}.
     *
     * @param   value   the {@link BigInteger} whose actual size is to measured
     *
     * @return  the corresponding {@code enum} constant for the length of the actual numerical
     *          value represented by {@code value}
     */
    @Pure
    public static NarrowTo getPrimNarrow(
            BigInteger value
    ) {
        return BigMathObjectUtils.canBeLong(value, IntegralBoundaryTypes.DEFAULT)
                ? getPrimNarrow(value.longValue())
                : NarrowTo.ARBITRARY;
    }
    
    /**
     * Gets the {@link NarrowTo NarrowTo} constant that indicates the actual length of {@code value}.
     *
     * @param   value   the {@code long} whose actual size is to measured
     *
     * @return  the corresponding {@code enum} constant for the length of the actual numerical
     *          value represented by {@code value}
     */
    @Pure
    public static NarrowTo getPrimNarrow(
            long value
    ) {
        long valNeg = -Math.abs(value);
        if (valNeg < Integer.MIN_VALUE) {
            return NarrowTo.LONG;
        }
        
        if (valNeg < Short.MIN_VALUE) {
            return NarrowTo.INTEGER;
        }
        
        if (valNeg < Byte.MIN_VALUE) {
            return NarrowTo.SHORT;
        }
        
        return NarrowTo.BYTE;
    }
    
    /**
     * <p>Gets the {@link NarrowTo NarrowTo} constant that indicates the {@link #comp} result
     * of this and the {@link Enum} that indicates the actual length of {@code parameter}.
     * Has the side effect of processing the parameter, through the process by which the parameter's
     * internal value is accessed.</p>
     *
     * <p>Is exactly equivalent to {@code parameter}{@link IntValuedParameter#process(NarrowTo) .process(}{@code
     * this}{@link IntValuedParameter#process(NarrowTo) )}.</p>
     *
     * @param   parameter   the {@link Parameter} whose actual size is to measured
     *
     * @return  {@link #comp comp(}{@link #getPrimNarrow(IntValuedParameter) getPrimNarrow(}{@code
     *          parameter}{@link #getPrimNarrow(IntValuedParameter) )}{@link #comp )}
     */
    @Deterministic
    public NarrowTo getAndCompPrimNarrow(
            IntValuedParameter parameter
    ) {
        return parameter.process(this);
    }
    
    /**
     * Gets the {@link NarrowTo NarrowTo} constant that indicates the {@link #comp} result
     * of this and the {@link Enum} that indicates the actual length of {@code parameter}.
     *
     * @param   value   the {@link BigInteger} whose actual size is to measured
     *
     * @return  {@link #comp comp(}{@link #getPrimNarrow(BigInteger) getPrimNarrow(}{@code
     *          value}{@link #getPrimNarrow(BigInteger) )}{@link #comp )}
     */
    @Pure
    public NarrowTo getAndCompPrimNarrow(
            BigInteger value
    ) {
        return (BigMathObjectUtils .canBeLong(value, IntegralBoundaryTypes.DEFAULT))
                ? getAndCompPrimNarrow(value.longValue())
                : NarrowTo.ARBITRARY;
    }
    
    /**
     * Gets the {@link NarrowTo NarrowTo} constant that indicates the {@link #comp} result
     * of this and the {@link Enum} that indicates the actual length of {@code parameter}.
     *
     * @param   value   the {@code long} whose actual size is to measured
     *
     * @return  {@link #comp comp(}{@link #getPrimNarrow(long) getPrimNarrow(}{@code
     *          value}{@link #getPrimNarrow(long) )}{@link #comp )}
     */
    @Pure
    public NarrowTo getAndCompPrimNarrow(
            long value
    ) {
        return comp(getPrimNarrow(value));
    }
    
    /**
     * Essentially like {@link Math#min} using the ordinal comparison; or, like {@link #compareTo} but
     * returning the larger of the two rather than a comparison result.
     *
     * @param   that    the {@link NarrowTo NarrowTo} constant to compare with this one
     *
     * @return  the larger of the two sizes, or, the {@code enum} of this and {@code that} with
     *          the larger {@link #ordinal()}
     */
    @Pure
    public NarrowTo comp(
            NarrowTo that
    ) {
        return (compareTo(that) < 0)
                ? that
                : this;
    }
}
