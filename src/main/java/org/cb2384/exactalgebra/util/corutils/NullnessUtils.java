package org.cb2384.exactalgebra.util.corutils;

import static java.util.Map.Entry;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.checkerframework.checker.nullness.qual.*;
import org.checkerframework.dataflow.qual.*;

/**
 * Some alternatives to {@link Optional} for dealing with {@code null} pointers.
 * In versions after Java 8, {@link Objects} has some functions for these utilities, but there
 * are several more of them here.
 * The apply/generate/return functions are essentially a single {@code ? :} statement
 * or {@code if}({@code /else}) block.
 * This class also contains functions that determine of certain containers or data structures contain nulls.
 *
 * @author  Corinne Buxton
 */
public class NullnessUtils {
    
    /**
     * A consumer that simply eats whatever it is given
     */
    public static final Consumer<@Nullable Object> NULL_CONSUMER = x -> {};
    
    /**
     * Predicate for use with checking for deep nulls through streaming
     */
    private static final Predicate<Object> DEEP_NULL_STREAM_PRED
            = e -> (e == null) || containerHasNulls(e, Boolean.TRUE);
    
    /**
     * Shouldn't ever be called
     *
     * @throws  IllegalAccessException  always
     */
    private NullnessUtils() throws IllegalAccessException {
        throw new IllegalAccessException("This should never be called" + StringUtils.INTERROBANG);
    }
    
    /**
     * Returns a consumer; if the input is null, the consumer is {@link #NULL_CONSUMER} (possibly recast).
     *
     * @param   consumer    the consumer that might be {@code null}
     *
     * @return  the given consumer, or a dummy consumer if {@code null}
     *
     * @param   <T> The type of element that the consumer consumes
     */
    @Pure @SuppressWarnings("unchecked")
    public static <T> @NonNull Consumer<@Nullable T> nullConsumerIfNull(
            @Nullable Consumer<T> consumer
    ) {
        return (consumer != null) ? consumer : (Consumer<@Nullable T>) NULL_CONSUMER;
    }
    
    /**
     * Returns a supplier; if the supplier is {@code null}, then a supplier that produces {@code null}
     * values will be returned.
     *
     * @param   supplier    the supplier that might be {@code null}
     *
     * @return  the given supplier, or a {@code null}-producing supplier if {@code null}
     *
     * @param   <T> the type of element produced by the supplier
     */
    @SideEffectFree
    public static <T> @NonNull Supplier<@Nullable T> nullSupplierIfNull(
            @Nullable Supplier<T> supplier
    ) {
        return (supplier != null) ? supplier : () -> null;
    }
    
    /**
     * Collapses a {@link Boolean} wrapper to a primitive {@code boolean},
     * with {@code null} defaulting to {@code false}.
     *
     * @param   input   the {@code Boolean} which might be {@code null}
     *
     * @return  a {@code boolean} primitive with the value of {@code input},
     *          or {@code false} if the input was {@code null}
     */
    @Pure
    public static boolean nullToFalse(
            @Nullable Boolean input
    ) {
        return (input != null) && input;
    }
    
    /**
     * Collapses a {@link Boolean} wrapper to a primitive {@code boolean},
     * with {@code null} defaulting to {@code true}.
     *
     * @param   input   the {@code Boolean} which might be {@code null}
     *
     * @return  a {@code boolean} primitive with the value of {@code input},
     *          or {@code true} if the input was {@code null}
     */
    @Pure
    public static boolean nullToTrue(
            @Nullable Boolean input
    ) {
        return (input == null) || input;
    }
    
    /**
     * Returns the first input if it is nonnull, otherwise the second.
     * Equivalent in effect to {@link Optional#ofNullable(Object)
     * Optional.ofNullable(}{@code input}{@link Optional#ofNullable(Object) )}{@link
     * Optional#orElse(Object) .orElse(}{@code defaultOutput}{@link Optional#orElse(Object) )}.
     * Is equivalent to {@link #returnDefaultIfNull(Object, Function, Object)
     * applyDefaultIfNull(}{@code input, }{@link Function#identity()}{@code , defaultOutput}{@link
     * #returnDefaultIfNull(Object, Function, Object) )}.
     *
     * @deprecated  in favor of the new {@code Objects.requireNonNullElse(obj, defaultObj)} from Java 9
     *
     * @param   input   the input that might be {@code null}
     *
     * @param   defaultOutput   the output to give if the input is null
     *
     * @return  {@code (input != null) ? input : defaultOutput}
     *
     * @param   <T> the type of the input and output
     */
    @Deprecated @Pure
    public static <T> @PolyNull T returnDefaultIfNull(
            @Nullable T input,
            @PolyNull T defaultOutput
    ) {
        return (input != null) ? input : defaultOutput;
    }
    
    /**
     * Returns the output of the function on the first input if it is nonnull,
     * otherwise returns the second. If the third argument is to be {@code null},
     * consider {@link #applyIfNonNull(Object, Function)}.
     * The other difference between these two methods is that here the function is encouraged to be
     * side-effect-free, unlike the other wherein side-effects are expected.
     * Equivalent in effect to {@link Optional#ofNullable(Object) Optional.ofNullable(}{@code input}{@link
     * Optional#ofNullable(Object) )}{@link Optional#map(Function) .map(}{@code toGetOutputIfNonNull}{@link
     * Optional#map(Function) )}{@link Optional#orElse(Object) .orElse(}{@code defaultOutput}{@link
     * Optional#orElse(Object) )}.
     *
     * @param   input   the input that might be {@code null}
     *
     * @param   toGetOutputIfNonNull    the function to apply to input if it is nonnull
     *
     * @param   defaultOutput   the output to give if the input is null
     *
     * @return  the output of the function upon {@code input}, or {@code defaultOutput} if {@code input == null}
     *
     * @param   <I> the type of the input
     *
     * @param   <O> the type of the output
     *
     * @throws  NullPointerException    if {@code toGetOutputIfNonNull == null}
     */
    public static <I, O> @PolyNull O returnDefaultIfNull(
            @Nullable I input,
            @NonNull Function<? super I, @PolyNull ? extends O> toGetOutputIfNonNull,
            @PolyNull O defaultOutput
    ) {
        return (input != null) ? toGetOutputIfNonNull.apply(input) : defaultOutput;
    }
    
    /**
     * Returns the first input; if it is nonnull, runs the generator to get the second.
     * Equivalent in effect to {@link Optional#ofNullable(Object) Optional.ofNullable(}{@code input}{@link
     * Optional#ofNullable(Object) )}{@link Optional#orElseGet(Supplier) .orElseGet(}{@code defaultGenerator}{@link
     * Optional#orElseGet(Supplier) )}.
     * Is equivalent to {@link #returnDefaultIfNull(Object, Function, Object) applyDefaultIfNull(}{@code input, }{@link
     * Function#identity()}{@code , defaultGenerator}{@link #returnDefaultIfNull(Object, Function, Object) )}.
     *
     * @deprecated  in favor of the new {@code Objects.requireNonNullElseGet(obj, supplier)} from Java 9
     *
     * @param   input   the input that might be {@code null}
     *
     * @param   defaultGenerator    a supplier to generate a default result in the case of a null input
     *
     * @return  {@code (input != null) ? input : defaultGenerator.get()}
     *
     * @param   <T> the type of the input and output
     *
     * @throws  NullPointerException    if {@code defaultGenerator == null}
     */
    @Deprecated
    public static <T> @PolyNull T generateDefaultIfNull(
            @Nullable T input,
            @NonNull Supplier<@PolyNull ? extends T> defaultGenerator
    ) {
        return (input != null) ? input : defaultGenerator.get();
    }
    
    /**
     * Returns the output of the function on the first input if it is nonnull,
     * otherwise generates a result using the supplier.
     * Equivalent in effect to {@link Optional#ofNullable(Object) Optional.ofNullable(}{@code input}{@link
     * Optional#ofNullable(Object) )}{@link Optional#map(Function) .map(}{@code toGetOutputIfNonNull}{@link
     * Optional#map(Function) )}{@link Optional#orElseGet(Supplier) .orElseGet(}{@code defaultSupplier}{@link
     * Optional#orElseGet(Supplier) )}.
     *
     * @param   input   the input that might be {@code null}
     *
     * @param   toGetOutputIfNonNull    the function to apply to input if it is nonnull
     *
     * @param   defaultGenerator    a supplier to generate a default result in the case of a null input
     *
     * @return  the output of the function upon {@code input}, or the value obtained from the supplier
     *          in the case that {@code input == null}
     *
     * @param   <I> the type of the input
     *
     * @param   <O> the type of the output
     *
     * @throws  NullPointerException    if {@code toGetOutputIfNonNull == null} or {@code defaultGenerator == null}
     */
    public static <I, O> @PolyNull O generateDefaultIfNull(
            @Nullable I input,
            @NonNull Function<? super I, @PolyNull ? extends O> toGetOutputIfNonNull,
            @NonNull Supplier<@PolyNull ? extends O> defaultGenerator
    ) {
        return (input != null) ? toGetOutputIfNonNull.apply(input) : defaultGenerator.get();
    }
    
    /**
     * Hands the value to the consumer if it is not {@code null}
     * Equivalent in effect to {@link Optional#ofNullable(Object) Optional.ofNullable(}{@code input}{@link
     * Optional#ofNullable(Object) )}{@link Optional#ifPresent .ifPresent(}{@code consumer}{@link
     * Optional#ifPresent(Consumer) )}.
     *
     * @param   consumer    the consumer to give the input to, when it is nonnull
     *
     * @param   input   the input that might be {@code null}
     *
     * @param   <T> the type of the input
     *
     * @throws  NullPointerException    if {@code consumer == null}
     */
    public static <T> void applyIfNonNull(
            @NonNull Consumer<? super T> consumer,
            @Nullable T input
    ) {
        if (input != null) {
            consumer.accept(input);
        }
    }
    
    /**
     * <p>A consumer-like method, but for functions that operate through
     * side-effects while still returning something, such as {@link Map#put(Object, Object) java.util.Map.put}.
     * This method is created with functions that operate through side-effects in mind.</p>
     *
     * <p>Equivalent in effect to {@link Optional#ofNullable(Object) Optional.ofNullable(}{@code input}{@link
     * Optional#ofNullable(Object) )}{@link Optional#map(Function) .map(}{@code sideEffectFunction}{@link
     * Optional#map(Function) )}{@link Optional#orElse(Object) .orElse(null)}. Would be equivalent to
     * {@link #returnDefaultIfNull(Object, Function, Object) returnDefaultIfNull(}{@code
     * input, sideEffectFunction, }{@link #returnDefaultIfNull(Object, Function, Object) null)},
     * except that the {@code defaultOutput} argument of that method is not advised to be {@code null}.</p>
     *
     * @param   input   the input that might be {@code null}
     *
     * @param   sideEffectFunction  the function that consumes the input, but unlike a {@link Consumer},
     *                              also has a desired return value
     *
     * @return  {@code (input != null) ? sideEffectFunction.apply(input) : null}
     *
     * @param   <I> the type of the input
     *
     * @param   <O> the type of the output
     *
     * @throws  NullPointerException    if {@code sideEffectFunction == null}
     */
    public static <I, O> @PolyNull O applyIfNonNull(
            @PolyNull I input,
            @NonNull Function<? super I, @PolyNull ? extends O> sideEffectFunction
    ) {
        return (input != null) ? sideEffectFunction.apply(input) : null;
    }
    
    /**
     * <p>This function wraps the given {@code value} into an {@link Optional}, unless
     * that value is {@code null} or is already an {@link Optional} of type {@code T}.
     * The intent is to create something similar to a varargs, but for {@link Optional} instead.
     * To specify an empty {@link Optional}, {@link Optional#empty()} should be passed.
     * This allows for {@code null} to be used to indicate a lack of value, and an {@link Optional}
     * that is empty to indicate a value that is {@code null}.</p>
     *
     * <p>Note that if {@code value} was intended to be an {@link Optional}, which is then to be itself
     * wrapped in an {@link Optional}, this function will not do so. (That is, if {@code T} is itself an {@link
     * Optional}).  This shouldn't be much of an issue, as the intention here is that this function is
     * used on inputs, and {@link Optional} usually shouldn't be an input,
     * but if in fact that is desired, use {@link #nullableOptionalOf(Optional, Class)}.</p>
     *
     * @param   value   This should be either of type {@code T} or an {@link Optional} with parameter
     *                  {@code T} or {@code ? extends T}
     *
     * @return  an {@link Optional} that may or may not have a value, or {@code null}
     *
     * @param   <T> the type of the {@link Optional}
     *
     * @throws  ClassCastException  if {@code value} is neither an instance or sub-instance of {@code T}
     *                              nor an {@link Optional} of a type that does not extend {@code T}
     *
     * @throws  IllegalArgumentException    if {@code T} is itself intended to be an {@link Optional},
     *                                      or if {@code value} is an {@link Optional} containing an {@link Optional}
     */
    @SideEffectFree @SuppressWarnings("unchecked")
    public static <T> @PolyNull Optional<T> nullableOptionalOf(
            @PolyNull Object value
    ) {
        if (value instanceof Optional<?>) {
            Optional<?> test = (Optional<?>) value;
            if (test.isPresent() && (test.get() instanceof Optional)) {
                throw new IllegalArgumentException("Optionals of Optionals are not supported!");
            }
            return (Optional<T>) value;
        }
        return applyIfNonNull((T) value, (Function<T, Optional<T>>) Optional::of);
    }
    
    /**
     * <p>This function wraps the given {@code optional} into another {@link Optional}, unless
     * that value is {@code null} or is already an {@link Optional} of an {@link Optional} of type {@code T}.
     * The intent is to create something similar to a varargs, but for {@link Optional} instead.
     * To specify an empty {@link Optional}, {@link Optional#of Optional.of(}{@link Optional#empty()}{@link
     * Optional#of )} should be passed. This allows for {@code null} to be used to indicate a lack of value,
     * and an {@link Optional} containing an empty {@link Optional} or
     * that is empty to indicate a value that is {@code null}.</p>
     *
     * <p>Note that if {@code optional} is itself meant to contain an {@link Optional}&mdash;that is, if {@code T}
     * is itself an {@link Optional}&mdash; this function will throw an exception. This shouldn't be much of
     * an issue, as the intention here is that this function is used on inputs, and {@link Optional} usually
     * shouldn't be an input in the first place, and if it is it definitely shouldn't be an {@link Optional}
     * of an {@link Optional}.</p>
     *
     * @param   optional    Either the output, if it is to be empty (in which case this should be an empty {@link
     *                      Optional} or the {@link Optional} to be itself wrapped in an {@link Optional}
     *
     * @param   type    A {@link Class} token for the type to be contained within the {@link Optional} that is
     *                  contained by the output; needed for type checking due to generic erasure
     *
     * @return  an {@link Optional} that may or may not have an {@link Optional}, or {@code null}
     *
     * @param   <T> the type of the {@link Optional} contained within the outer {@link Optional}
     *
     * @throws  ClassCastException  if {@code optional} is neither an {@link Optional} of type {@code T}
     *                              nor contains an {@link Optional} of type {@code T}
     *
     * @throws  IllegalArgumentException    if {@code T} is itself intended to be an {@link Optional}
     */
    @SideEffectFree @SuppressWarnings("unchecked")
    public static <T> @PolyNull Optional<Optional<? extends T>> nullableOptionalOf(
            @PolyNull Optional<?> optional,
            @NonNull Class<T> type
    ) {
        if (type.equals(Optional.class)) {
            throw new IllegalArgumentException("Can't differentiate between pre-made Optional "
                    + "and Optional to be put into an Optional of an Optional!");
        }
        if (optional == null) {
            return null;
        }
        if (optional.isPresent()) {
            Object test = optional.get();
            if (test instanceof Optional<?>) {
                Optional<?> testOpt = (Optional<?>) test;
                if (!testOpt.isPresent() || type.isInstance(testOpt.get())) {
                    return (Optional<Optional<? extends T>>) optional;
                }
            } else if (type.isInstance(test)) {
                return Optional.of((Optional<? extends T>) optional);
            }
            throw new ClassCastException("Contents of optional " + optional + " are not of type T or " +
                    "an Optional of type T: " + StringUtils.getIdealName(type));
        }
        return (Optional<Optional<? extends T>>) optional;
    }
    
    /**
     * Throws exception with message generated by this function
     *
     * @param   containerName   name of container for error message
     *
     * @throws  NullPointerException    always
     */
    private static void throwNPE(
            @Nullable String containerName
    ) {
        throw new NullPointerException("At least one element of this "
                + returnDefaultIfNull(containerName, "container") + " is null!");
    }
    
    /**
     * Gets a name for the collection about which an error is going to be thrown, and then gives it
     * to the message generator to throw with the exception.
     *
     * @param   input   the collection to name
     *
     * @throws  NullPointerException    always
     */
    private static void throwCollecError(
            @Nullable Collection<@Nullable ?> input
    ) {
        String containerName;
        if (input != null) {
            Class<?> clazz = input.getClass();
            containerName = clazz.getSimpleName();
            if (containerName.isEmpty()) {
                if (List.class.isAssignableFrom(clazz)) {
                    containerName = "List";
                } else if (Queue.class.isAssignableFrom(clazz)) {
                    containerName = "Queue";
                } else if (Set.class.isAssignableFrom(clazz)) {
                    containerName = "Set";
                } else {
                    containerName = "Collection";
                }
            }
        } else {
            containerName = null;
        }
        throwNPE(containerName);
    }
    
    /**
     * Gets a name for the array about which an error is going to be thrown, and then gives it
     * to the message generator to throw with the exception.
     *
     * @param   input   the array to name
     *
     * @throws  NullPointerException    always
     */
    private static void throwArrayError(
            @Nullable Object@Nullable[] input
    ) {
        String arrayName;
        if (input != null) {
            String component = input.getClass().getComponentType().getSimpleName();
            arrayName = component.isEmpty() ? "Array" : component + "[]";
        } else {
            arrayName = null;
        }
        throwNPE(arrayName);
    }
    
    /**
     * Checks if {@code input} contains another object that is {@code null}.
     * Supports reference arrays (arrays that are or are subclasses of {@code Object[]})
     * and implementers of {@link Iterable}, {@link Map}, and {@link Entry}, as well as {@link Optional}.
     * If the {@code recurse == true}, then this function
     * (or a more specialized one when relevant) is called recursively on each contained object.
     * If {@code recurse == null}, it defaults to false;
     *
     * @param   input   the object to check for contained null pointers
     *
     * @param   recurse determines whether to do a 'deep check' or just a shallow check. If {@code true},
     *                  then this function or another from this class is called to check
     *                  every element  found. If {@code null} or {@code false}, it does not.
     *
     * @return  {@code true} upon finding a {@code null}, {@code false} if it cannot find any.
     *          Note that a false result might not guarantee there being no nulls,
     *          if the input container type is not recognized by this function.
     *
     * @throws  NullPointerException    if {@code input == null}
     */
    @Pure
    public static boolean containerHasNulls(
            @NonNull Object input,
            @Nullable Boolean recurse
    ) {
        final boolean goDeep = nullToFalse(recurse);
        
        if (input.getClass().isArray()) {
            Object[] inputArray = (Object[]) input;
            return goDeep ?
                    arrayHasDeepNulls(inputArray) :
                    arrayHasNulls(inputArray);
        }
        
        if (input instanceof Iterable) {
            return iterableHasNulls((Iterable<?>) input, recurse);
        }
        
        if (input instanceof Map) {
            return mapHasNulls((Map<?, ?>) input, recurse);
        }
        
        if (input instanceof Optional) {
            Optional<?> inputOpt = (Optional<?>) input;
            // Return true if not present, or if is present, but is recursive and a lower level is true
            return !inputOpt.isPresent() || (goDeep && containerHasNulls(inputOpt.get(), Boolean.TRUE));
        }
        
        if (input instanceof Entry) {
            return mapEntryHasNulls((Entry<?, ?>) input, goDeep);
        }
        
        return false;
    }
    
    /**
     * Checks if this {@link Iterable} contains any {@code null} elements that would be iterated through.
     * If {@code recurse == true}, then {@link #containerHasNulls(Object, Boolean)
     * containerHasNulls(}{@code X, }{@link Boolean#TRUE}{@link #containerHasNulls(Object, Boolean) )}
     * is called on each encountered object {@code X}.
     *
     * @param   input   something implementing {@link Iterable} that might contain nulls
     *
     * @param   recurse if {@code true}, checks each element of this iterable for nulls recursively as well;
     *                  otherwise not
     *
     * @return  {@code true} if any nulls are encountered, otherwise {@code false}
     *
     * @throws  NullPointerException    if {@code input == null}
     */
    @Pure
    public static boolean iterableHasNulls(
            @NonNull Iterable<@Nullable ?> input,
            @Nullable Boolean recurse
    ) {
       boolean goDeep = nullToFalse(recurse);
        for (Object o : input) {
            if (o == null) {
                return true;
            }
            if (goDeep && containerHasNulls(o, Boolean.TRUE)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Checks if this {@link Map} contains any {@code null} keys or values.
     * If {@code recurse == true}, then each entry, both key and value, is also checked to see if any
     * of these also contain nulls.
     *
     * @param   input   something implementing {@link Map} that might contain nulls
     *
     * @param   recurse if {@code true}, checks each entry of this map for nulls recursively as well;
     *                  otherwise not
     *
     * @return  {@code true} if any nulls are encountered, otherwise {@code false}
     *
     * @throws  NullPointerException    if {@code input == null}
     */
    @Pure
    public static boolean mapHasNulls(
            @NonNull Map<@Nullable ?, @Nullable ?> input,
            @Nullable Boolean recurse
    ) {
        boolean goDeep = nullToFalse(recurse);
        return input.entrySet().parallelStream().anyMatch(e -> mapEntryHasNulls(e, goDeep));
    }
    
    /**
     * Checks if this {@link Entry} contains a {@code null} key or value.
     * If {@code recurse == true}, then key and value are also checked to see if any
     * are themselves containers with nulls.
     *
     * @param   entry   a map entry that might contain nulls
     *
     * @param   recurse if {@code true}, checks each element of this map entry for nulls recursively as well;
     *                  otherwise not
     *
     * @return  {@code true} if any nulls are encountered, otherwise {@code false}
     *
     * @throws  NullPointerException    if {@code entry == null}
     */
    @Pure
    private static boolean mapEntryHasNulls(
            @NonNull Entry<@Nullable ?, @Nullable ?> entry,
            boolean recurse
    ) {
        Object key = entry.getKey();
        Object val = entry.getValue();
        return (key == null || val == null) || (recurse
                && (containerHasNulls(key, Boolean.TRUE) || containerHasNulls(val, Boolean.TRUE)));
    }
    
    /**
     * Checks if this {@link Collection} has any {@code null} elements.
     * This function only checks one layer deep.
     *
     * @param   input   the {@link Collection} to check
     *
     * @return  {@code true} if there are any {@code null} elements in this collection, otherwise {@code false}
     *
     * @throws  NullPointerException    if {@code input == null}
     */
    @Pure
    public static boolean collectionHasNulls(
            @NonNull Collection<@Nullable ?> input
    ) {
        return input.parallelStream().anyMatch(Objects::isNull);
    }
    
    /**
     * Checks if this {@link Collection} has any {@code null} elements, and throws an exception if so
     *
     * @param   input   the {@link Collection} to check
     *
     * @throws  NullPointerException    if {@code input == null} or any elements in the collection are {@code null}
     */
    @Pure
    public static void checkNullCollec(
            @NonNull Collection<@Nullable ?> input
    ) {
        if (collectionHasNulls(input)) {
            throwCollecError(input);
        }
    }
    
    /**
     * Checks if this {@link Collection} has any {@code null} elements, and if any of the elements themselves
     * are containers containing {@code null} elements.
     *
     * @param   input   the {@link Collection} to check
     *
     * @return  {@code true} if there are any {@code null} elements in this collection
     *          or any elements are containers with nulls, otherwise {@code false}
     *
     * @throws  NullPointerException    if {@code input == null}
     */
    @Pure
    public static boolean collectionHasDeepNulls(
            @NonNull Collection<@Nullable ?> input
    ) {
        return input.parallelStream().anyMatch(DEEP_NULL_STREAM_PRED);
    }
    
    /**
     * Checks if this {@link Collection} has any {@code null} elements, and if any of the elements themselves
     * are containers containing {@code null} elements. An error is thrown if so.
     *
     * @param   input   the {@link Collection} to check
     *
     * @throws  NullPointerException    if {@code input == null}, if any elements in the collection
     *                                  are {@code null}, or if any elements in the collection are themselves
     *                                  containers that contain nulls
     */
    @Pure
    public static void checkDeepNullCollec(
            @NonNull Collection<@Nullable ?> input
    ) {
        if (collectionHasDeepNulls(input)) {
            throwCollecError(input);
        }
    }
    
    /**
     * Checks if this array has any {@code null} elements.
     * This function only checks one layer deep, and is only for reference arrays ({@code Object[]}
     * or a subclass thereof), since primitives can be neither {@code null} nor containers anyway.
     *
     * @param   input   the array to check
     *
     * @return  {@code true} if there are any {@code null} elements in this array, otherwise {@code false}
     *
     * @throws  NullPointerException    if {@code input == null}
     */
    @Pure
    public static boolean arrayHasNulls(
            @Nullable Object@NonNull[] input
    ) {
        
        return Arrays.stream(input).parallel().anyMatch(Objects::isNull);
    }
    
    /**
     * Checks if this {@link Collection} has any {@code null} elements, and throws an exception if so.
     * This function is only checks one layer deep, and is only for reference arrays ({@code Object[]}
     * or a subclass thereof), since primitives can be neither {@code null} nor containers anyway.
     *
     * @param   input   the array to check
     *
     * @throws  NullPointerException    if {@code input == null} or any elements in the array are {@code null}
     */
    @Pure
    public static void checkNullArray(
            @Nullable Object@NonNull[] input
    ) {
        if (arrayHasNulls(input)) {
            throwArrayError(input);
        }
    }
    
    /**
     * Checks if this array has any {@code null} elements, and if any of the elements themselves
     * are containers containing {@code null} elements.
     * This function is only for reference arrays ({@code Object[]} or a subclass thereof), since primitives
     * can be neither {@code null} nor containers anyway.
     *
     * @param   input   the array to check
     *
     * @return  {@code true} if there are any {@code null} elements in this collection
     *          or any elements are containers with nulls, otherwise {@code false}
     *
     * @throws  NullPointerException    if {@code input == null}
     */
    @Pure
    public static boolean arrayHasDeepNulls(
            @Nullable Object@NonNull[] input
    ) {
        return Arrays.stream(input).parallel().anyMatch(DEEP_NULL_STREAM_PRED);
    }
    
    /**
     * Checks if this array has any {@code null} elements, and if any of the elements themselves
     * are containers containing {@code null} elements. An error is thrown if so.
     * This function is only for reference arrays ({@code Object[]} or a subclass thereof), since primitives
     * can be neither {@code null} nor containers anyway.
     *
     * @param   input   the array to check
     *
     * @throws  NullPointerException    if {@code input == null}, if any elements of the array
     *                                  are {@code null}, or if any elements of the array are themselves
     *                                  containers that contain nulls
     */
    @Pure
    public static void checkDeepNullArray(
            @Nullable Object@NonNull[] input
    ) {
        if (arrayHasDeepNulls(input)) {
            throwArrayError(input);
        }
    }
}
