package org.cb2384.exactalgebra.objects.numbers.integral;

import static org.cb2384.exactalgebra.util.PrimMathUtils.IntegralBoundaryTypes.SHORTENED;

import java.math.BigInteger;

import org.cb2384.exactalgebra.objects.Factory;
import org.cb2384.exactalgebra.objects.internalaccess.CacheInteger;
import org.cb2384.exactalgebra.objects.internalaccess.factory.IntValuedParameter;
import org.cb2384.exactalgebra.objects.internalaccess.factory.Parameter;
import org.cb2384.exactalgebra.objects.numbers.AlgebraNumber;
import org.cb2384.exactalgebra.objects.numbers.rational.RationalFactory;
import org.cb2384.exactalgebra.util.BigMathObjectUtils;

import org.checkerframework.checker.nullness.qual.*;
import org.checkerframework.common.returnsreceiver.qual.*;
import org.checkerframework.dataflow.qual.*;

/**
 * <p>Factory for {@link AlgebraInteger}s. This specific class is not instantiated directly (this is done for
 * reasons relating to inheritance and code reuse) but a new instance of it is instead obtained through
 * {@link #newAlgebraInteger()}. The value can be added, or overwritten, using {@link #whole(BigInteger)},
 * {@link #whole(long)}, or {@link #whole(AlgebraInteger)}. One of these must be called before {@link #build()}
 * can be called. This class works closely with the internals of multiple {@link AlgebraInteger} types,
 * and is therefore sealed.</p>
 *
 * <p>This class also provides some static methods that collapse the multiple calls into one. For
 * the case of an integer type, they actually make more sense to use in many situations.</p>
 *
 * <p>Throws:&ensp;{@link NullPointerException} &ndash; on any {@code null} input, unless otherwise noted</p>
 *
 * @param <N>   The type of the number returned by {@link #build()}; for the instances returned by
 *              {@link #newAlgebraInteger()}, this is always reified to {@link AlgebraInteger}.
 *
 * @author  Corinne Buxton
 */
public sealed abstract class IntegerFactory<N extends AlgebraNumber>
        implements Factory<N, AlgebraNumber>
        permits IntegerFactory.IntegerFabricator, RationalFactory {
    
    /**
     * The basic parameter common to all whole
     */
    protected @Nullable IntValuedParameter whole;
    
    /**
     * constructor for inheritance
     */
    @SideEffectFree
    protected IntegerFactory() {}
    
    /**
     * Grabs an instance of a factory implementation that is reified to always produce an {@link AlgebraInteger}.
     *
     * @return  an instance of a factory implementation specifically for {@link AlgebraInteger}s
     */
    @SideEffectFree
    public static IntegerFactory<AlgebraInteger> newAlgebraInteger() {
        return new IntegerFabricator();
    }
    
    /**
     * Builds an AlgebraInteger from the given {@code value}. This is equivalent in effect to calling
     * {@link #newAlgebraInteger()}{@link #whole(BigInteger) .whole(}{@code value}{@link #whole(BigInteger)
     * )}{@link #build() .build()}, but is actually faster.
     *
     * @param value the value that the returned AlgebraInteger will have
     *
     * @return  the constructed AlgebraInteger with a value of {@code value}
     */
    @SideEffectFree
    public static AlgebraInteger fromBigInteger(
            BigInteger value
    ) {
        return BigMathObjectUtils.canBeLong(value, SHORTENED)
                ? CacheInteger.valueOf(value.longValue(), value)
                : new ArbitraryInteger(value);
    }
    
    /**
     * Builds an AlgebraInteger from the given {@code value}. This is equivalent in effect to calling
     * {@link #newAlgebraInteger()}{@link #whole(long) .whole(}{@code value}{@link #whole(long)
     * )}{@link #build() .build()}, but is actually faster.
     *
     * @param value the value that the returned AlgebraInteger will have
     *
     * @return  the constructed AlgebraInteger with a value of {@code value}
     */
    @SideEffectFree
    public static AlgebraInteger fromLong(
            long value
    ) {
        return (value != Long.MIN_VALUE)
                ? FiniteInteger.valueOfStrict(value)
                : new ArbitraryInteger(value);
    }
    
    /**
     * Takes the parameter for this factory in the form of a pre-existing {@link AlgebraInteger}. If
     * there already is a whole parameter given, this will overwrite it.
     *
     * @param value the value number to use; this same exact object may be returned by this factory through,
     *              {@link #build()}, though it will attempt narrowing if possible.
     *
     * @return  this same factory, to allow chaining to {@link #build()}
     */
    @Deterministic
    public abstract @This IntegerFactory<N> whole(AlgebraInteger value);
    
    /**
     * Takes the parameter for this factory in the form of a {@link BigInteger}. If
     * there already is a whole parameter given, this will overwrite it.
     *
     * @param value the value number to use
     *
     * @return  this same factory, to allow chaining to {@link #build()}
     */
    @Deterministic
    public abstract @This IntegerFactory<N> whole(BigInteger value);
    
    /**
     * Takes the parameter for this factory in the form of a {@code long}. If
     * there already is a whole parameter given, this will overwrite it.
     *
     * @param value the value number to use
     *
     * @return  this same factory, to allow chaining to {@link #build()}
     */
    @Deterministic
    public abstract @This IntegerFactory<N> whole(long value);
    
    /**
     * Returns the simplest or smallest {@link AlgebraInteger} representation of the single parameter
     * that this factory has. In the unique case of an AlgebraInteger that was given as a parameter in
     * {@link #whole(AlgebraInteger)}, that value itself might be returned if a smaller AlgebraInteger
     * representation is not known.
     *
     * @return  an {@link AlgebraInteger} representation of the value of the single integral parameter that this
     *          factory accepts
     *
     * @throws  IllegalStateException   if no parameters have been given yet
     */
    @Override
    public abstract N build();
    
    /**
     * Returns the simplest or smallest {@link AlgebraInteger} representation of the single parameter
     * that this factory has. In the unique case of an AlgebraInteger that was given as a parameter in
     * {@link #whole(AlgebraInteger)}, that value itself might be returned if a smaller AlgebraInteger
     * representation is not known.
     *
     * @implNote    This is precisely the same as {@link #build()} for {@link AlgebraInteger}s
     *
     * @return  an {@link AlgebraInteger} representation of the value of the single integral parameter that this
     *          factory accepts
     *
     * @throws  IllegalStateException   if no parameters have been given yet
     */
    @Override
    public N buildStrict() {
        return build();
    }
    
    /**
     * Actual implementation of parts that aren't meant to be inherited.
     */
    static final class IntegerFabricator
            extends IntegerFactory<AlgebraInteger> {
        
        /**
         * whole stored raw in this case, so as to return it later if not narrowable
         */
        private @Nullable AlgebraInteger wholeAI;
        
        /**
         * basic constructor
         */
        private IntegerFabricator() {}
        
        /**
         * {@inheritDoc}
         */
        @Override
        @Deterministic
        public @This IntegerFactory<AlgebraInteger> whole(
                AlgebraInteger value
        ) {
            wholeAI = Parameter.confirmNonNull(value);
            return this;
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        @Deterministic
        public @This IntegerFactory<AlgebraInteger> whole(
                BigInteger value
        ) {
            wholeAI = null;
            whole = new IntValuedParameter(value);
            return this;
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        @Deterministic
        public @This IntegerFactory<AlgebraInteger> whole(
                long value
        ) {
            wholeAI = null;
            whole = new IntValuedParameter(value);
            return this;
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public void clear() {
            wholeAI = null;
            whole = null;
        }
        
        /**
         * {@inheritDoc}
         *
         * @throws  IllegalStateException   if no parameters have been given yet
         */
        @Override
        public AlgebraInteger build() {
            if (wholeAI != null) {
                if (wholeAI instanceof FiniteInteger wholeFI) {
                    return wholeFI;
                }
                BigInteger wholeBI = wholeAI.toBigInteger();
                if (!BigMathObjectUtils.canBeLong(wholeBI, SHORTENED)
                        && (wholeAI instanceof ArbitraryInteger wholeAAI)) {
                    return wholeAAI;
                }
                whole = new IntValuedParameter(wholeBI);
            }
            
            if (whole == null) {
                throw new IllegalStateException(Parameter.EMPTY_STATE_EXC);
            }
            return whole.asAlgebraObject();
        }
    }
}
