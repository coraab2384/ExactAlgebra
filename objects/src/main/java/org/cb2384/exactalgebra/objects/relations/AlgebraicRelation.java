package org.cb2384.exactalgebra.objects.relations;

import org.cb2384.exactalgebra.objects.AlgebraObject;

import org.checkerframework.dataflow.qual.*;

public interface AlgebraicRelation<T extends AlgebraObject<T>> {
    
    @SideEffectFree
    AlgebraicRelation<T> negated();
    
    @SideEffectFree
    AlgebraicRelation<T> absoluteValue();
}
