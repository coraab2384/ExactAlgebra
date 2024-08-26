package org.cb2384.exactalgebra.util.corutils.ternary;

import java.io.Serializable;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

import org.cb2384.exactalgebra.util.corutils.NullnessUtils;

import org.checkerframework.checker.nullness.qual.*;
import org.checkerframework.dataflow.qual.*;

/**
 * <p>A type of {@link BooleanValuedContext} wherenin the third value in the {@link ThreeValued} system is mapped
 * to a specific {@code boolean} value known by this context. This allows for defining the relationship
 * between the {@code boolean} and {@link T} outside of the class {@link T}, enabling more flexibility.</p>
 *
 * <p>The sole missing abstract method here is {@link #booleanValue}; all of the other (final) methods
 * here are implemented in terms of that one method, which determines the actual translation between
 * {@link ThreeValued} and {@code boolean}. Of course, this is not required, as the provided static factory
 * {@link #buildContext} implements the final function according to its supplied arguments.</p>
 *
 * <p>This {@link BooleanValuedContext} implementation allows {@code null} in all methods
 * (except parts of the static factory method); {@code null} is treated as the third default/unknown value.</p>
 *
 * @param   <T> the {@link ThreeValued} implementation that this context is compatible with
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
     * Creates a {@link KnownDefaultBooleanContext} using the given values. The returned instance
     * is {@link Serializable} if all input arguments are serializable. That the return value is of the specific
     * type&mdash;{@link KnownDefaultBooleanDefaultContext}&mdash; should be considered an implementation detail
     * provided in case one wants to verify source or serializability.
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
     * @throws  IllegalArgumentException    if {@code trueTernary}{@link ThreeValued#booleanValue()
     *                                      .booleanValue()}{@code != true} or {@code falseTernary}{@link
     *                                      ThreeValued#booleanValue() .booleanValue()}{@code != false} or if
     *                                      any of the first three arguments are equal to each other
     *
     * @param   <T> the {@link ThreeValued} implementation that the returned context will be compatible with
     */
    public static <T extends ThreeValued> @NonNull KnownDefaultBooleanDefaultContext<T> buildContext(
            @NonNull T trueTernary,
            @Nullable T unknownTernary,
            @NonNull T falseTernary,
            boolean booleanDefault,
            @Nullable Boolean checkByEquals
    ) {
        return new KnownDefaultBooleanDefaultContext<>(trueTernary, unknownTernary,
                falseTernary, booleanDefault, checkByEquals);
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
        return (Predicate<T> & Serializable) t -> booleanValue(ternaryFunction.apply(t));
    }
    
    /**
     * An implementation of {@link KnownDefaultBooleanContext}. It is implemented this way to not clutter the
     * internals of other implementations.
     *
     * @param   <T> the {@link ThreeValued} implementation that the returned context will be compatible with
     */
    public static final class KnownDefaultBooleanDefaultContext<T extends ThreeValued>
            extends KnownDefaultBooleanContext<T>
            implements Serializable {
        
        private static final long serialVersionUID = 0x90134C87562DB130L;
        
        /**
         * either {@code ==} or {@link Object#equals}
         */
        private final BiPredicate<T, T> equalityChecker;
        
        /**
         * 3Val mapping to true
         */
        private final T trueTernary;
        
        /**
         * 3Val mapping to the default value
         */
        private final @Nullable T unknownTernary;
        
        /**
         * 3Val mapping to false
         */
        private final T falseTernary;
        
        /**
         * default boolean value, for when the default or null 3Val is given
         */
        private final boolean booleanDefault;
        
        /**
         * @see #buildContext(ThreeValued, ThreeValued, ThreeValued, boolean, Boolean)
         */
        private KnownDefaultBooleanDefaultContext(
                @NonNull T trueTernary,
                @Nullable T unknownTernary,
                @NonNull T falseTernary,
                boolean booleanDefault,
                @Nullable Boolean checkByEquals
        ) {
            equalityChecker = NullnessUtils.nullToFalse(checkByEquals)
                    ? (BiPredicate<T, T> & Serializable) Object::equals
                    : (BiPredicate<T, T> & Serializable) (x, y) -> x == y;
            
            if ( !((unknownTernary == null) || (unknownTernary.booleanValue() == null))
                    || NullnessUtils.nullToTrue(falseTernary.booleanValue())
                    || !NullnessUtils.nullToFalse(trueTernary.booleanValue())
                    || equalityChecker.test(trueTernary, unknownTernary)
                    || equalityChecker.test(falseTernary, unknownTernary)
                    || equalityChecker.test(trueTernary, falseTernary)) {
                throw new IllegalArgumentException("Given true, false, or unknown/default values are inconsistent!");
            }
            
            this.trueTernary = trueTernary;
            this.unknownTernary = unknownTernary;
            this.falseTernary = falseTernary;
            this.booleanDefault = booleanDefault;
        }
        
        /**
         * Returns the boolean value of the given {@link ThreeValued} argument, according to this context.
         *
         * @param   value   the Three-Valued object to get the {@code boolean} value of
         *
         * @return  the {@code boolean} value of {@code value}
         *
         * @throws  IllegalStateException   if {@code value}{@link ThreeValued#booleanValue() .booleanValue()}
         *                                  would return {@code null} and {@code value} was not properly
         *                                  associated with the null/default/unknown ThreeValued-type at
         *                                  object creation&mdash;see {@link #buildContext}
         */
        @Override
        @Pure
        public boolean booleanValue(
                @Nullable T value
        ) {
            if ((value == null) || equalityChecker.test(value, unknownTernary)) {
                return booleanDefault;
            }
            if (equalityChecker.test(value, trueTernary)) {
                return true;
            }
            if (equalityChecker.test(value, falseTernary)) {
                return false;
            }
            Boolean result = value.booleanValue();
            if (result == null) {
                throw new IllegalStateException("value " + value + " is incompatible with the initial parameter"
                        + " arguments given at Context creation");
            }
            return result;
        }
    }
}
