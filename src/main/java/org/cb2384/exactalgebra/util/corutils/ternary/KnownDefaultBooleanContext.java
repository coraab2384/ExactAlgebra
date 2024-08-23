package org.cb2384.exactalgebra.util.corutils.ternary;

import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

import org.cb2384.exactalgebra.util.corutils.NullnessUtils;

import org.checkerframework.checker.nullness.qual.*;
import org.checkerframework.dataflow.qual.*;

/**
 * <p>A type of {@link org.cb2384.corutils.ternary.BooleanValuedContext} wherenin the third value in the {@link org.cb2384.corutils.ternary.ThreeValued} system is mapped
 * to a specific {@code boolean} value known by this context. This allows for defining the relationship
 * between the {@code boolean} and {@link T} outside of the class {@link T}, enabling more flexibility.</p>
 *
 * <p>The sole missing abstract method here is {@link #booleanValue}; all of the other (final) methods
 * here are implemented in terms of that one method, which determines the actual translation between
 * {@link org.cb2384.corutils.ternary.ThreeValued} and {@code boolean}. Of course, this is not required, as the provided static factory
 * {@link #buildContext} implements the final function according to its supplied arguments.</p>
 *
 * <p>This {@link org.cb2384.corutils.ternary.BooleanValuedContext} implementation allows {@code null} in all methods
 * (except parts of the static factory method); {@code null} is treated as the third default/unknown value.</p>
 *
 * @param   <T> the {@link org.cb2384.corutils.ternary.ThreeValued} implementation that this context is compatible with
 *
 * @author  Corinne Buxton
 */
public abstract class KnownDefaultBooleanContext<T extends ThreeValued>
        implements BooleanValuedContext<T> {
    
    /**
     * The javadoc compiler doesn't like it if I don't explicitly write this out and put a comment here;
     * however, this function doesn't do anything beyond implicitly calling the {@link Object#Object()}
     * constructor that all constructors directly or indirectly call :/
     */
    protected KnownDefaultBooleanContext() {}
    
    /**
     * Creates a {@link KnownDefaultBooleanContext} using the given values.
     *
     * @param   trueTernary the value representing {@code true}
     *
     * @param   unknownTernary  the value representing the default or unknown value; that value that would
     *                          be mapped to a {@code null }{@link Boolean}. As such, unlike the argument
     *                          before and after, this one can be {@code null}.
     *
     * @param   falseTernary    the value representing {@code false}
     *
     * @param   booleanDefault  the default value for this {@link KnownDefaultBooleanContext}
     *
     * @param   checkByEquals   a {@code null}able parameter regarding whether checking the value
     *                          of an input in one of the {@link BooleanValuedContext}-specified operations
     *                          should be compared against the values here using {@code ==} or
     *                          {@link Object#equals}; {@code null} and {@code false} use {@code ==}
     *
     * @return  a concrete {@link KnownDefaultBooleanContext} as defined by the given arguments
     *
     * @throws  IllegalArgumentException    if {@code trueTernary}{@link org.cb2384.corutils.ternary.ThreeValued#booleanValue()
     *                                      .booleanValue()}{@code != true} or {@code falseTernary}{@link
     *                                      org.cb2384.corutils.ternary.ThreeValued#booleanValue() .booleanValue()}{@code != false} or if
     *                                      any of the first three arguments are equal to each other
     *
     * @throws  NullPointerException    if {@code trueTernary == null} or {@code falseTernary == null}
     *
     * @param   <T> the {@link org.cb2384.corutils.ternary.ThreeValued} implementation that the returned context will be compatible with
     */
    public static <T extends ThreeValued> @NonNull KnownDefaultBooleanContext<T> buildContext(
            @NonNull T trueTernary,
            @Nullable T unknownTernary,
            @NonNull T falseTernary,
            boolean booleanDefault,
            @Nullable Boolean checkByEquals
    ) {
        BiPredicate<T, T> eqChecker = NullnessUtils.nullToFalse(checkByEquals)
                ? Object::equals
                : (x, y) -> x == y;
        
        if (falseTernary.booleanValue() || !trueTernary.booleanValue()
                || eqChecker.test(trueTernary, unknownTernary)
                || eqChecker.test(falseTernary, unknownTernary)
                || eqChecker.test(trueTernary, falseTernary)) {
            throw new IllegalArgumentException("Given true, false, or unknown/default values are inconsistent!");
        }
        
        return new KnownDefaultBooleanContext<T>() {
            /**
             * Returns the boolean value of the given {@link org.cb2384.corutils.ternary.ThreeValued} argument, according to this context.
             *
             * @param   value   the Three-Valued object to get the {@code boolean} value of
             *
             * @return  the {@code boolean} value of {@code value}
             */
            @Override
            @Pure
            public boolean booleanValue(
                    @Nullable T value
            ) {
                if ((value == null) || eqChecker.test(value, unknownTernary)) {
                    return booleanDefault;
                }
                if (eqChecker.test(value, trueTernary)) {
                    return true;
                }
                if (eqChecker.test(value, falseTernary)) {
                    return false;
                }
                return value.booleanValue();
            }
        };
    }
    
    /**
     * {@inheritDoc}
     *
     * <p>Implementation Requirements: This method should line up with {@link ThreeValued#booleanValue()}
     *                                  in the way that {@link java.util.Objects#hashCode(Object)
     *                                  Objects.hashCode} lines up with {@link Object#hashCode()};
     *                                  that is, as if it were allowing a {@code null} receiver.</p>
     */
    @Override
    @Pure
    public abstract boolean booleanValue(@Nullable T value);
    
    @Override
    @Pure
    public final boolean not(
            @Nullable T value
    ) {
        return !booleanValue(value);
    }
    
    @Override
    @Pure
    public final boolean and(
            @Nullable T left,
            @Nullable T right
    ) {
        return booleanValue(left) && booleanValue(right);
    }
    
    @Override
    @Pure
    public final boolean or(
            @Nullable T left,
            @Nullable T right
    ) {
        return booleanValue(left) || booleanValue(right);
    }
    
    @Override
    @Pure
    public final boolean xor(
            @Nullable T left,
            @Nullable T right
    ) {
        return booleanValue(left) ^ booleanValue(right);
    }
    
    @Override
    @Pure
    public final boolean implies(
            @Nullable T precondition,
            @Nullable T postcondition
    ) {
        return !booleanValue(postcondition) || booleanValue(postcondition);
    }
    
    @Override
    @Pure
    public final boolean iff(
            @Nullable T left,
            @Nullable T right
    ) {
        return !xor(left, right);
    }
    
    @Override
    @SideEffectFree
    public final @NonNull Predicate<@PolyNull T> threeValuedFunctionToPredicate(
            @NonNull Function<? super T, @PolyNull ? extends T> ternaryFunction
    ) {
        return t -> booleanValue(ternaryFunction.apply(t));
    }
}
