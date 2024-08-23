package org.cb2384.exactalgebra.objects.relations;

import java.util.function.Function;

import org.cb2384.exactalgebra.objects.numbers.AlgebraNumber;

import org.checkerframework.dataflow.qual.*;

public interface AlgebraFunction<I extends O, O extends AlgebraNumber>
        extends AlgebraicRelation<AlgebraNumber>, Function<I, O> {
    
    @SideEffectFree
    AlgebraFunction<I, O> negated();
    
    @SideEffectFree
    AlgebraFunction<I, O> absoluteValue();
    /*
    @SideEffectFree
    AlgebraFunction<T> inverted();
    
    @SideEffectFree
    <S, R> AlgebraFunction<R> composed(AlgebraFunction<S> interior);
    */
    
    @SideEffectFree
    AlgebraNumber evaluate(AlgebraNumber input);
    
    @Pure
    Class<I> coefficientHighestRank();
}
