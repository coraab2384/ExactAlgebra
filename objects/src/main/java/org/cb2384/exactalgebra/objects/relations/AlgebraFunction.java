package org.cb2384.exactalgebra.objects.relations;

import java.util.function.Function;

import org.cb2384.exactalgebra.objects.numbers.AlgebraNumber;

import org.checkerframework.dataflow.qual.*;

/**
 * <p>An algebraic function, with an input and output.</p>
 *
 * <p>Throws:&ensp;{@link NullPointerException} &ndash; on any {@code null} argument,
 * unless otherwise specified</p>
 *
 * @param <I>   The input type
 * @param <O>   The output type
 *
 * @author  Corinne Buxton
 */
public interface AlgebraFunction<I extends AlgebraNumber, O extends AlgebraNumber>
        extends AlgebraicRelation<AlgebraNumber>, Function<I, O> {
    
    /**
     * {@inheritDoc}
     */
    @SideEffectFree
    AlgebraFunction<I, O> negated();
    
    /*
    @SideEffectFree
    AlgebraFunction<T> inverted();
    
    @SideEffectFree
    <S, R> AlgebraFunction<R> composed(AlgebraFunction<S> interior);
    */
    
    /**
     * Similar to {@link #apply}, but while that method has limits on it's input, this does not.
     * However, this also prevents bounds on its output beyond just AlgebraNumber.
     *
     * @param input the input to feed this function
     *
     * @return  the output
     */
    @SideEffectFree
    AlgebraNumber evaluate(AlgebraNumber input);
    
    /**
     * A way of asking the highest "Rank" that a coefficient or value within this function has.
     *
     * @return  the class of the highest coefficient (that is, the most super class)
     */
    @Pure
    Class<I> coefficientHighestRank();
}
