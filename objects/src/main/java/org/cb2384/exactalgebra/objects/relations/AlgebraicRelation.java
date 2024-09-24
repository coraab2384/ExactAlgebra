package org.cb2384.exactalgebra.objects.relations;

import org.cb2384.exactalgebra.objects.AlgebraObject;

import org.checkerframework.dataflow.qual.*;

/**
 * Relation between some number of things
 *
 * @param <T>   The overarching type of the things being related
 *
 * @author  Corinne Buxton
 */
public interface AlgebraicRelation<T extends AlgebraObject<T>> {
    
    /**
     * Negates this
     *
     * @return  this, but with the opposite sign with respect to addition; negated
     */
    @SideEffectFree
    AlgebraicRelation<T> negated();
}
