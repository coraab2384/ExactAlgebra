package org.cb2384.exactalgebra.util.corutils.ternary;

import java.util.function.Function;
import java.util.function.Predicate;

import org.cb2384.corutils.ternary.KnownDefaultBooleanContext;
import org.cb2384.corutils.ternary.ThreeValued;
import org.cb2384.corutils.ternary.ThreeValuedContext;

import org.checkerframework.checker.nullness.qual.*;
import org.checkerframework.dataflow.qual.*;

/**
 * <p>Imagine a public-facing function that takes a {@code boolean} argument, but the argument isn't strictly
 * necessary, because there's a default perhaps, but that default might change based on something that isn't
 * known until runtime. Well, {@link Boolean}, being nullable, could work for that, if it isn't
 * being passed around too much. Can't use any concurrent Collections though, unless you wrap it in an
 * {@link java.util.Optional} perhaps? But that's more work than this need to be.</p>
 *
 * <p>Behold, instead, the {@link Ternary}. Similar to a boolean, but with a third value to represent
 * some not-yet-specified default value, or an unknown-truthiness value. This places it, conceptually,
 * in-between {@code true} and {@code false}. A Ternary can be converted to a
 * Boolean object using {@link #booleanValue()}, and if the default value is known, it can be converted
 * to a {@code boolean} using {@link #booleanValue(boolean)}. This enables a boolean-ish value with
 * an unknown or default-but-not-yet-specified value to be passed around without dealing with
 * {@code null} and with better switch-support than a possibly-empty
 * {@link java.util.Optional Optional&lt;Boolean&gt;}.</p>
 *
 * <p>When actually-three-valued logic is desired, a {@link ThreeValuedContext} can be created for the
 * Three-Valued logic paradigm desired. {@link org.cb2384.corutils.ternary.KnownDefaultBooleanContext} is provided, but this class
 * already has similar functions for just the case of the third value being default.</p>
 *
 * @author  Corinne Buxton
 */
public enum Ternary
        implements ThreeValued {
    
    /**
     * Corresponding to {@link Boolean#FALSE} and {@code false} value
     */
    FALSE(Boolean.FALSE),
    
    /**
     * Corresponding to a {@code null} {@link Boolean} value
     */
    DEFAULT(null),
    
    /**
     * Corresponding to a {@link Boolean#TRUE} and {@code true} value
     */
    TRUE(Boolean.TRUE);
    
    /**
     * Corresponding to a {@code null} {@link Boolean} value
     */
    public static final @NonNull Ternary NULL = DEFAULT;
    
    /**
     * Corresponding to a {@code null} {@link Boolean} value
     */
    public static final @NonNull Ternary UNKNOWN = DEFAULT;
    
    /**
     * the corresponding boolean
     */
    private final @Nullable Boolean value;
    
    /**
     * maps boolean to ternary
     *
     * @param   value the corresponding boolean
     */
    Ternary(
            @Nullable Boolean value
    ) {
        this.value = value;
    }
    
    /**
     * Transforms a {@link Boolean} into a {@link Ternary}; essentially the inverse of
     * {@link #booleanValue()}.
     *
     * @param   value   the {@code boolean} or {@code null} to map to a {@link Ternary}
     *
     * @return  {@link #TRUE} for {@link Boolean#TRUE},
     *          {@link #FALSE} for {@link Boolean#FALSE}, and
     *          {@link #DEFAULT} for {@code null}
     */
    @Pure
    public static @NonNull Ternary valueOf(
            @Nullable Boolean value
    ) {
        if (value == null) {
            return DEFAULT;
        }
        if (value) {
            return TRUE;
        }
        return FALSE;
    }
    
    /**
     * For internal logic using 0-false and 2-true, and becomes a min and or becomes a max. This function
     * is to turn that result back into a Ternary returned value.
     *
     * @param   ordinal ordinal of the returned ternary
     *
     * @return  the ternary of the corresponding ordinal
     *
     * @throws  IllegalArgumentException if the ordinal isn't valid
     */
    @Pure
    private static @NonNull Ternary valueOf(
            int ordinal
    ) {
        switch (ordinal) {
            case 0:
                return FALSE;
                
            case 1:
                return DEFAULT;
                
            case 2:
                return TRUE;
        }
        throw new IllegalArgumentException("Not a valid ordinal‽");
    }
    
    /**
     * Essentially equivalent to {@link #valueOf(Boolean) valueOf(}{@code that}{@link #valueOf(Boolean) )}{@link
     * #not .not()}, but as one function.
     *
     * @param   that    the {@link Boolean} value to map the boolean negation of
     *
     * @return  the negation of the {@link Ternary} corresponding to {@code that}
     */
    @Pure
    public static @NonNull Ternary not(
            @Nullable Boolean that
    ) {
        if (that == null) {
            return DEFAULT;
        }
        if (that) {
            return FALSE;
        }
        return TRUE;
    }
    
    /**
     * Gives the {@link Boolean} value of this {@link Ternary}
     *
     * @return  {@link Boolean#TRUE} for {@link #TRUE}, {@code null} for {@link #DEFAULT},
     *          and {@link Boolean#FALSE} for {@link #FALSE}
     */
    @Override
    @Pure
    public @Nullable Boolean booleanValue() {
        return value;
    }
    
    /**
     * Gives the {@code boolean} value of this {@link Ternary}, with the middle default/unknown value being pushed to
     * the given default argument.
     *
     * @param   defaultValue    the value to map {@link #DEFAULT} to
     *
     * @return  the {@code boolean} value represented by this {@link Ternary}, with {@link #DEFAULT}
     *          mapping to {@code defaultValue}
     */
    @Pure
    public boolean booleanValue(
            boolean defaultValue
    ) {
        Boolean bool = booleanValue();
        return (bool != null) ? bool : defaultValue;
    }
    
    /**
     * Takes the logical negation ({@code !} or {@code ~}) of this ternary value.
     *
     * @return  the {@link Ternary} value 'opposite' this one
     */
    @Pure
    public @NonNull Ternary not() {
        return valueOf(2 - ordinal());
    }
    
    /**
     * Performs a logical and ({@code &} or {@code &&}) of this ternary value and the value that
     * {@code that} would represent, per {@link #valueOf(Boolean) Ternary.valueOf}.
     *
     * @param   that    the other {@link Boolean} to 'and' with this
     *
     * @return  the and output
     */
    @Pure
    public @NonNull Ternary and(
            @Nullable Boolean that
    ) {
        return and(valueOf(that));
    }
    
    /**
     * Performs a logical and ({@code &} or {@code &&}) of this ternary value and {@code that}.
     *
     * @param   that    the other {@link Ternary} to 'and' with this; {@code null} defaults to {@link #DEFAULT}
     *
     * @return  the and output
     */
    @Pure
    public @NonNull Ternary and(
            @Nullable Ternary that
    ) {
        return valueOf(Math.min(ordinal(that), ordinal()));
    }
    
    /**
     * Performs a logical or ({@code |} or {@code ||}) of this ternary value and the value that
     * {@code that} would represent, per {@link #valueOf(Boolean) Ternary.valueOf}.
     *
     * @param   that    the other {@link Boolean} to 'or' with this
     *
     * @return  the or output
     */
    @Pure
    public @NonNull Ternary or(
            @Nullable Boolean that
    ) {
        return or(valueOf(that));
    }
    
    /**
     * Performs a logical or ({@code |} or {@code ||}) of this ternary value and {@code that}.
     *
     * @param   that    the other {@link Ternary} to 'or' with this; {@code null} defaults to {@link #DEFAULT}
     *
     * @return  the or output
     */
    @Pure
    public @NonNull Ternary or(
            @Nullable Ternary that
    ) {
        return valueOf(Math.max(ordinal(that), ordinal()));
    }
    
    /**
     * Performs a logical xor ({@code ^}) of this ternary value and the value that {@code that}
     * would represent. For both inputs, {@code null} is interpreted as {@link #DEFAULT}.
     * In the case that both are/map to {@link #DEFAULT}, {@code defaultWithDefaultAns} is used.
     *
     * @param   that    the other {@link Boolean} to 'xor' with this
     *
     * @param   defaultWithDefaultAns   the answer to use in the specific case that both this
     *                                  and {@code that} are/map to {@link #DEFAULT}
     *
     * @return  the xor output
     */
    @Pure
    public @NonNull Ternary xor(
            @Nullable Boolean that,
            @Nullable Boolean defaultWithDefaultAns
    ) {
        return xor(valueOf(that), valueOf(defaultWithDefaultAns));
    }
    
    /**
     * Performs a logical xor ({@code ^}) of this ternary value and {@code that}.
     * In the case that both are {@link #DEFAULT}, wherein a specific {@link ThreeValuedContext} would
     * normally be required, {@code defaultWithDefaultAns} is used in this implementation.
     * For both inputs, {@code null} is interpreted as {@link #DEFAULT}.
     *
     * @param   that    the other {@link Ternary} to 'xor' with this
     *
     * @param   defaultWithDefaultAns   the answer to use in the specific case that both this
     *                                  and {@code that} are {@link #DEFAULT}
     *
     * @return  the xor output
     */
    @Pure
    public @NonNull Ternary xor(
            @Nullable Ternary that,
            @Nullable Ternary defaultWithDefaultAns
    ) {
        int thatOrd = ordinal(that);
        switch (this) {
            case TRUE:
                return valueOf(2 - thatOrd);
                
            case DEFAULT:
                return (thatOrd != 1)
                        ? TRUE
                        : valueOf(ordinal(defaultWithDefaultAns));
                
            case FALSE:
                return valueOf(thatOrd);
        }
        throw new IllegalArgumentException("Enum somehow not an enum‽");
    }
    
    /**
     * This function is like {@link #ordinal()} but maps null to DEFAULT's ordinal, rather than throwing up.
     *
     * @param   that    the Ternary to get the ordinal value of
     *
     * @return  the ordinal value of {@code that}
     */
    @Pure
    private static int ordinal(
            @Nullable Ternary that
    ) {
        return (that != null) ? that.ordinal() : 1;
    }
    
    /**
     * Transforms a function which takes in some object and yields a {@link Ternary} into a {@link Predicate},
     * for use with {@link java.util.stream.Stream} methods which call specifically for a predicate for example.
     * To collapse the default/unknown value to either {@code true} or {@code false}, a default value argument
     * is required. This can be thought of as being {@code ternaryFunction}{@link
     * Function#andThen .andThen(}{@code t -> t}{@link #booleanValue(boolean) .booleanValue(}{@code
     * defaultValue}{@link #booleanValue(boolean) )}{@link Function#andThen )},
     * though it is in actuality somewhat more streamlined.
     *
     * @param   defaultValue    the value to map {@link #DEFAULT} to
     *
     * @param   ternaryFunction the {@link Function} that is to be transformed into a {@link Predicate}
     *
     * @return  the {@link Predicate} that captures the logic of the input {@link Function}, with default/unknown
     *          values being pushed according to {@code defaultValue}
     *
     * @throws  NullPointerException    if {@code ternaryFunction == null}
     *
     * @param   <T> The input type of the function, which is also the input type of
     *              the returned predicate
     */
    @SideEffectFree
    public static <T> @NonNull Predicate<@PolyNull T> ternaryFunctionToPredicate(
            boolean defaultValue,
            @NonNull Function<? super T, @PolyNull Ternary> ternaryFunction
    ) {
        return t -> ternaryFunction.apply(t).booleanValue(defaultValue);
    }
    
    /**
     * Creates a {@link org.cb2384.corutils.ternary.KnownDefaultBooleanContext} specific to {@link Ternary}.
     *
     * @param   defaultValue    the value to use for {@link #DEFAULT} when collapsing a {@link Ternary}
     *                          to a {@code boolean}
     *
     * @return  a concrete {@link org.cb2384.corutils.ternary.KnownDefaultBooleanContext} specialized for {@link Ternary} and with
     *          the given mapping for {@link #DEFAULT}
     */
    @SideEffectFree
    public static @NonNull org.cb2384.corutils.ternary.KnownDefaultBooleanContext<Ternary> booleanFromTernary(
            boolean defaultValue
    ) {
        return new KnownDefaultBooleanContext<Ternary>() {
            /**
             * Returns the boolean value of the given {@link Ternary} argument, according to this context.
             *
             * @param   value   the {@link Ternary} object to get the {@code boolean} value of
             *
             * @return  the {@code boolean} value of {@code value}
             */
            @Override
            @Pure
            public boolean booleanValue(
                    @Nullable Ternary value
            ) {
                return (value != null)
                        ? value.booleanValue(defaultValue)
                        : defaultValue;
            }
        };
    }
}
