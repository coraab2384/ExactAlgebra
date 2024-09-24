package org.cb2384.exactalgebra.objects;

/**
 * <p>Abstract parent class of all factories in this module. Primarily enforces the requirement
 * of the {@link #build()} method.</p>
 *
 * <p>All factory top-level classes that are provided will also be abstract,
 * and will each be implemented concretely in a private static nested class. This is
 * to enable the inheritence of factories of simpler types by factories of more complex types. {@link
 * org.cb2384.exactalgebra.objects.numbers.integral.AlgebraInteger} has a whole value, but {@link
 * org.cb2384.exactalgebra.objects.numbers.rational.Rational}s can also have a whole number, if built in
 * mixed number format. Similarly, a radical type can also have a denominator, so that would inherit from
 * the rational factory. However, this is inverse to the actual mathematical hierarchy of numbers,
 * so the return type of {@link #build()} must be further indicated in a non-extendable extension of
 * the main factory class.</p>
 *
 * @param <R>   The result type
 * @param <S>   The type that the result is a subtype of, which actually implements
 *              the {@link AlgebraObject} interface
 *
 * @author  Corinne Buxton
 */
public interface Factory<R extends S, S extends AlgebraObject<S>> {
    
    /**
     * Builds an {@link AlgebraObject} type using the parameters entered so far.
     *
     * @return  the built {@link AlgebraObject} (or rather implementer)
     *
     * @throws IllegalStateException    if no parameters have been given so far
     */
    R build();
    
    /**
     * Builds an {@link AlgebraObject} type using the parameters entered so far. Unlike {@link #build()},
     * this method does not allow narrowing to a more simply type.
     *
     * @implNote    The default implementation just returns the value from {@link #build()}; only if this
     *              would be different must this be overridden.
     *
     * @return  the built {@link AlgebraObject} (or rather implementer)
     *
     * @throws IllegalStateException    if no parameters have been given so far
     */
    default R buildStrict() {
        return build();
    }
    
    /**
     * Clears all Parameters in this factory
     */
    void clear();
}
