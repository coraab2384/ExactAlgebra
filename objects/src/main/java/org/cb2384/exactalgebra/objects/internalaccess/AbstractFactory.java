package org.cb2384.exactalgebra.objects.internalaccess;

import java.math.BigInteger;
import java.util.Objects;

import org.cb2384.exactalgebra.objects.AlgebraObject;
import org.cb2384.exactalgebra.objects.numbers.AlgebraNumber;
import org.cb2384.exactalgebra.objects.numbers.integral.AbstractAlgebraInteger;
import org.cb2384.exactalgebra.objects.numbers.integral.AlgebraInteger;
import org.cb2384.exactalgebra.objects.numbers.integral.ArbitraryInteger;
import org.cb2384.exactalgebra.objects.numbers.integral.FiniteInteger;
import org.cb2384.exactalgebra.objects.numbers.integral.IntegerFactory;
import org.cb2384.exactalgebra.util.BigMathObjectUtils;
import org.cb2384.exactalgebra.util.PrimMathUtils.IntegralBoundaryTypes;
import org.cb2384.exactalgebra.util.corutils.ternary.ComparableSwitchSignum;
import org.cb2384.exactalgebra.util.corutils.ternary.Ternary;

import org.checkerframework.checker.nullness.qual.*;
import org.checkerframework.common.returnsreceiver.qual.*;
import org.checkerframework.dataflow.qual.*;

public sealed abstract class AbstractFactory<R extends S, S extends AlgebraObject<S>>
        permits IntegerFactory {
    
    /**
     * standardized exception message, so as to not have to retype it
     */
    protected static final String IllStateExc = "No Parameters given; cannot build";
    
    /**
     * <p>An {@link Enum} used to measure the size of parameters, to determine which implementation
     * the factory should ultimately employ.</p>
     *
     * <p>Throws: {@link NullPointerException} &ndash; on any {@code null} input</p>
     */
    protected enum NarrowTo
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
         * Has the side effect of {@linkplain Parameter#process process}ing the parameter,
         * through the process by which the parameter's internal value is accessed.
         *
         * @param   parameter   the {@link Parameter} whose actual size is to measured
         *
         * @return  the corresponding {@code enum} constant for the length of the actual numerical
         *          value represented by {@code parameter}
         *
         * @see Parameter#process
         */
        @Deterministic
        public static NarrowTo getPrimNarrow(
                IntegralParameter parameter
        ) {
            return parameter.isPrim()
                    ? getPrimNarrow(parameter.primValue)
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
         * <p>Is exactly equivalent to {@code parameter}{@link Parameter#process .process(}{@code
         * this}{@link Parameter#process )}.</p>
         *
         * @param   parameter   the {@link Parameter} whose actual size is to measured
         *
         * @return  {@link #comp comp(}{@link #getPrimNarrow(Parameter) getPrimNarrow(}{@code
         *          parameter}{@link #getPrimNarrow(Parameter) )}{@link #comp )}
         */
        @Deterministic
        public NarrowTo getAndCompPrimNarrow(
                IntegralParameter parameter
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
    
    protected NarrowTo currentDepth;
    
    /**
     * Give a parameter for a value-number value
     *
     * @param   value   an {@link AlgebraInteger} representation of the value number to use
     *
     * @return  this same factory, to allow chaining for possibly further parameters
     */
    @Deterministic
    public abstract @This AbstractFactory<R, S> whole(@Nullable AlgebraInteger value);
    
    /**
     * Give a parameter for a value-number value
     *
     * @param   value   a {@link BigInteger} representation of the value number to use
     *
     * @return  this same factory, to allow chaining for possibly further parameters
     */
    @Deterministic
    public abstract @This AbstractFactory<R, S> whole(@Nullable BigInteger value);
    
    /**
     * Give a parameter for a value-number value
     *
     * @param   value   a {@code long} (or other automatically-widened integral primitive)
     *                  representation of the value number to use
     *
     * @return  this same factory, to allow chaining for possibly further parameters
     */
    @Deterministic
    public abstract @This AbstractFactory<R, S> whole(long value);
    
    /**
     * Builds an {@link AlgebraObject} type using the parameters entered so far.
     *
     * @return  the built {@link AlgebraObject} (or rather implementer)
     *
     * @throws  IllegalStateException   if no parameters have been given so far
     */
    public abstract R build();
    
    /**
     * Essentially a modified {@link Objects#requireNonNull(Object, String)} but with
     * a constant string rather than having to re-enter it each time.
     *
     * @param   input   the input that shouldn't be {@code null}
     *
     * @return  {@code input}
     */
    @Pure
    protected static <T> T confirmNonNull(
            T input
    ) {
        return Objects.requireNonNull(input, "Null input not allowed in Parameter!");
    }
    
    protected interface Parameter<R extends S, S extends AlgebraObject<S>, T> {
        
        T value();
        
        void reset(T value);
        
        boolean isNegative();
        
        void negate();
        
        R asAlgebraObject();
    }
    
    /**
     * <p>A parameter for a factory; a mutable class built around {@link BigInteger}, though which also tracks
     * a {@code long} primitive value if the value can in fact be represented as a long.</p>
     *
     * <p>Throws: {@link NullPointerException} &ndash; on any {@code null} input</p>
     */
    protected static final class IntegralParameter
            implements Parameter<AlgebraInteger, AlgebraNumber, BigInteger> {
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
        public IntegralParameter(
                BigInteger value
        ) {
            this.value = confirmNonNull(value);
        }
        
        /**
         * Constructs a Parameter using the given {@code value}.
         *
         * @param   value   the {@code long} whose value this will represent
         */
        @SideEffectFree
        public IntegralParameter(
                long value
        ) {
            reset(value);
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
                if (BigMathObjectUtils.canBeLong(value, IntegralBoundaryTypes.DEFAULT)) {
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
            this.value = confirmNonNull(value);
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
         * <p>Is exactly equivalent to {@code currentDepth}{@link NarrowTo#getAndCompPrimNarrow(Parameter)
         * .getAndCompPrimNarrow(}{@code this}{@link NarrowTo#getAndCompPrimNarrow(Parameter) )}.</p>
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
         * Negates the value of this parameter
         */
        @Override
        public void negate() {
            value = value.negate();
            if (isPrim()) {
                if (primValue != Long.MIN_VALUE) {
                    primValue = -primValue;
                } else {
                    isPrim = Ternary.FALSE;
                }
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
                    ? FiniteIntegerFabricator.valueOf(primValue, value)
                    : new ArbitraryInteger(value);
        }
    }
}
