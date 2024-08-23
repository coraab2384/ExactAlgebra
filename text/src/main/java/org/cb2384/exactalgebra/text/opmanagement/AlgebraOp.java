package org.cb2384.exactalgebra.text.opmanagement;

import java.util.Set;

import org.cb2384.exactalgebra.objects.AlgebraObject;

import org.checkerframework.checker.nullness.qual.*;
import org.checkerframework.dataflow.qual.*;

public sealed interface AlgebraOp<T extends AlgebraObject<T>, R extends Rank<T, R>>
        permits PolynomialOps, RealFieldOps {
        
    @Pure
    R ceilingRank(R left, @Nullable Rank<?, ?> right);
    
    @SideEffectFree
    Set<OpFlag> flags();
    
    @SideEffectFree
    String internalName();
    
    @SideEffectFree
    Set<String> externalNames();
    
    @Pure
    boolean matches(String name);
}
