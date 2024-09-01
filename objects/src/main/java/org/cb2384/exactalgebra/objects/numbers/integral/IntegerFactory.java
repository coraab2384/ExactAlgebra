package org.cb2384.exactalgebra.objects.numbers.integral;

import static org.cb2384.exactalgebra.util.PrimMathUtils.IntegralBoundaryTypes.SHORTENED;

import java.math.BigInteger;

import org.cb2384.exactalgebra.objects.AbstractFactory;
import org.cb2384.exactalgebra.objects.internalaccess.factory.IntValuedParameter;
import org.cb2384.exactalgebra.objects.internalaccess.factory.Parameter;
import org.cb2384.exactalgebra.objects.numbers.AlgebraNumber;
import org.cb2384.exactalgebra.objects.numbers.rational.RationalFactory;
import org.cb2384.exactalgebra.util.BigMathObjectUtils;

import org.checkerframework.checker.nullness.qual.*;
import org.checkerframework.common.returnsreceiver.qual.*;
import org.checkerframework.dataflow.qual.*;

public sealed abstract class IntegerFactory<N extends AlgebraNumber>
        extends AbstractFactory<N, AlgebraNumber>
        permits IntegerFactory.IntegerFabricator, RationalFactory {
    
    /**
     * The basic parameter common to all whole
     */
    protected @Nullable IntValuedParameter whole;
    
    protected IntegerFactory() {}
    
    @SideEffectFree
    public static IntegerFactory<AlgebraInteger> newAlgebraInteger() {
        return new IntegerFabricator();
    }
    
    @SideEffectFree
    public static AlgebraInteger fromBigInteger(
            BigInteger value
    ) {
        return BigMathObjectUtils.canBeLong(value, SHORTENED)
                ? FiniteInteger.valueOfStrict(value.longValue())
                : new ArbitraryInteger(value);
    }
    
    @SideEffectFree
    public static AlgebraInteger fromLong(
            long value
    ) {
        return (value != Long.MIN_VALUE)
                ? FiniteInteger.valueOfStrict(value)
                : new ArbitraryInteger(value);
    }
    
    /**
     * Takes the parameter for this factory in the form of a pre-existing {@link AlgebraInteger}.
     *
     * @param   value   an {@link AlgebraInteger} representation of the value number to use; this same exact object
     *                  may be returned by this the factory, though it will attempt narrowing if possible
     *
     * @return  this same factory, to allow chaining to {@link #build()}
     */
    @Deterministic
    public abstract @This IntegerFactory<N> whole(AlgebraInteger value);
    
    /**
     * Takes the parameter for this factory in the form of a {@link BigInteger}.
     *
     * @param   value   a {@link BigInteger} representation of the value number to use
     *
     * @return  this same factory, to allow chaining to {@link #build()}
     */
    @Deterministic
    public @This IntegerFactory<N> whole(
            BigInteger value
    ) {
        whole = new IntValuedParameter(value);
        return this;
    }
    
    /**
     * Takes the parameter for this factory in the form of a {@code long}.
     *
     * @param   value   a {@code long} representation of the value number to use
     *
     * @return  this same factory, to allow chaining to {@link #build()}
     */
    @Deterministic
    public @This IntegerFactory<N> whole(
            long value
    ) {
        whole = new IntValuedParameter(value);
        return this;
    }
    
    static final class IntegerFabricator
            extends IntegerFactory<AlgebraInteger> {
        
        private @Nullable AlgebraInteger wholeAI;
        
        private IntegerFabricator() {}
        
        /**
         * Takes the parameter for this factory in the form of a pre-existing {@link AlgebraInteger}.
         *
         * @param   value   an {@link AlgebraInteger} representation of the value number to use; this same exact object
         *                  may be returned by this the factory, though it will attempt narrowing if possible
         *
         * @return  this same factory, to allow chaining to {@link #build()}
         */
        @Override
        @Deterministic
        public @This IntegerFactory<AlgebraInteger> whole(
                AlgebraInteger value
        ) {
            wholeAI = Parameter.confirmNonNull(value);
            return this;
        }
        
        @Override
        @Deterministic
        public @This IntegerFactory<AlgebraInteger> whole(
                BigInteger value
        ) {
            wholeAI = null;
            whole = new IntValuedParameter(value);
            return this;
        }
        
        @Override
        @Deterministic
        public @This IntegerFactory<AlgebraInteger> whole(
                long value
        ) {
            wholeAI = null;
            whole = new IntValuedParameter(value);
            return this;
        }
        
        @Override
        public void clear() {
            wholeAI = null;
            whole = null;
        }
        
        /**
         * Returns the simplest or smallest {@link AlgebraInteger} representation of the single parameter
         * that this factory has. In the unique case of an {@link AlgebraInteger} that was given as a parameter in
         * {@link #whole(AlgebraInteger)}, that value itself might be returned if a smaller {@link AlgebraInteger}
         * representation is not known.
         *
         * @return  an {@link AlgebraInteger} representation of the value of the single integral parameter that this
         *          factory accepts
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
