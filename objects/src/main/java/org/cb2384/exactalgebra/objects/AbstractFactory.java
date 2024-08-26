package org.cb2384.exactalgebra.objects;

import org.cb2384.exactalgebra.objects.numbers.integral.IntegerFactory;

/**
 * <p>Abstract parent class of all factories in this module.</p>
 *
 * @param <R>   The result type
 * @param <S>   The type that the result is a subtype of, which actually implements
 *              the {@link AlgebraObject} interface
 *
 * @author  Corinne Buxton
 */
public abstract sealed class AbstractFactory<R extends S, S extends AlgebraObject<S>>
        permits IntegerFactory {
    
    /**
     * standardized exception message, so as to not have to retype it
     */
    protected static final String EMPTY_STATE_EXC = "No Parameters given; cannot build!";
    
    /**
     * Builds an {@link AlgebraObject} type using the parameters entered so far.
     *
     * @return  the built {@link AlgebraObject} (or rather implementer)
     *
     * @throws  IllegalStateException   if no parameters have been given so far
     */
    public abstract R build();
    
    /**
     * Clears all Parameters in this factory
     */
    public abstract void clear();
}
