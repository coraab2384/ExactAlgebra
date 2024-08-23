package org.cb2384.exactalgebra.util.corutils;

import java.lang.reflect.Array;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.IntStream;

import org.checkerframework.checker.index.qual.*;
import org.checkerframework.checker.nullness.qual.*;
import org.checkerframework.dataflow.qual.*;

/**
 * <p>Additional array-based utility methods. This includes methods built upon {@link Array#newInstance(Class, int)}
 * as well as array mappers (non-stream versions that are essentially equivalent
 * {@link java.util.Arrays#stream(Object[]) Arrays.stream(I[])}{@link java.util.stream.Stream#map(Function)
 * .map(Function&lt;I, O&gt;)}{@link java.util.stream.Stream#toArray .toArray} with varying versions of
 * {@link java.util.stream.Stream#toArray toArray} depending on which particular function. (Though
 * unlike {@link java.util.stream.Stream Stream}s, none are late-binding.)</p>
 *
 * <p>Finally, there is array verifying, which verifies that an array has the correct length and returns
 * an array of the same type but correct length if not, and an <i>UNCHECKED</i> array generator (only safe for
 * arrays that will only see internal use, e.g., that will not be returned by a public and/or exported method).</p>
 *
 * <p>Note that in this class, any parameter named {@code seedArray} is not actually modified, merely used
 * for to get the actual runtime array class. Thus, arrays that are used for other purposes are safe to used
 * for any argument named {@code seedArray} without fear of mutation.</p>
 *
 * @author  Corinne Buxton
 */
public class Arrayz {
    
    /**
     * Shouldn't ever be called
     *
     * @throws  IllegalAccessException  always
     */
    private Arrayz() throws IllegalAccessException {
        throw new IllegalAccessException("This should never be called" + StringUtils.INTERROBANG);
    }
    
    /**
     * <p>Creates an array of generic {@code Object}, but then casts it to the indicated class.
     * This is not safe if the array is ever recast,
     * or exposed in such a way that it could be recast.
     * It is an <i>UNCHECKED</i> array, whose re-casting could generate {@link ArrayStoreException}.</p>
     *
     * <p>This is marked as deprecated, because it cannot be marked as being externally unchecked.
     * At least this way there is still a compiler warning</p>
     *
     * @param   length  the length of the array
     *
     * @return  an {@code Object[]} that is <i>cast</i> to {@code A[]}
     *
     * @param   <A> the class that the array will be cast as when returned
     *
     * @throws  NegativeArraySizeException  if {@code length < 0}
     */
    @Deprecated @SideEffectFree @SuppressWarnings("unchecked")
    public static <A> @Nullable A@NonNull[] unCheckedArrayGenerator(
            @NonNegative int length
    ) {
        return (A[]) new Object[length];
    }
    
    /**
     * Essentially calls {@link Array#newInstance(Class, int)
     * Array.newInstance(}{@code componentClass, length}{@link Array#newInstance(Class, int) )}
     * but also casts the result as an array of the given class.
     * Note that class {@code A} is implemented as a raw class,
     * so re-casting will be required again if the class {@code A} has type parameters.
     *
     * @param   length  the length of the array
     *
     * @param   componentClass  the class of the elements that are to be contained within the array
     *
     * @return   an array of the given component type and length
     *
     * @param   <A> the class (raw) of the resulting array's components
     *
     * @throws  NullPointerException    if {@code componentClass == null}
     *
     * @throws  IllegalArgumentException    if {@code componentClass == }{@link Void#TYPE}
     *
     * @throws  NegativeArraySizeException  if {@code length < 0}
     */
    @SideEffectFree @SuppressWarnings("unchecked")
    public static <A> @Nullable A@NonNull[] checkedArrayGenerator(
            @NonNegative int length,
            @NonNull Class<A> componentClass
    ) {
        return (A[]) Array.newInstance(componentClass, length);
    }
    
    /**
     * Essentially calls {@link Array#newInstance(Class, int)
     * Array.newInstance(}{@code arrayClass}{@link Class#getComponentType() .getComponentType()}{@code
     * , length}{@link Array#newInstance(Class, int) )}
     * but also casts the result as an array of the given class.
     * Note that class {@code A} is implemented as a raw class,
     * so re-casting will be required again if the class {@code A} has type parameters.
     *
     * @param   arrayClass  the class for the resulting array
     *
     * @param   length  the length of the array
     *
     * @return  an array of the given type and length
     *
     * @param   <A> the class (raw) of the resulting array's components
     *
     * @throws  NullPointerException    if {@code arrayClass == null}
     *
     * @throws  NegativeArraySizeException  if {@code length < 0}
     */
    @SideEffectFree @SuppressWarnings("unchecked")
    public static <A> @Nullable A@NonNull[] checkedArrayGenerator(
            @NonNull Class<A[]> arrayClass,
            @NonNegative int length
    ) {
        return (A[]) Array.newInstance(arrayClass.getComponentType(), length);
    }
    
    /**
     * Essentially calls {@link Array#newInstance(Class, int)
     * Array.newInstance(}{@code seedArray}{@link Class#getClass() .getClass()}{@link
     * Class#getComponentType() .getComponentType()}{@code , length}{@link Array#newInstance(Class, int) )}
     * but also casts the result as an array of the given class.
     * Note that class {@code A} is implemented as a raw class,
     * so re-casting will be required again if the class {@code A} has type parameters.
     *
     * @param   length  the length of the array
     *
     * @param   seedArray   an array of the same class to copy
     *
     * @return  an array of the given type and length
     *
     * @param   <A> the class (raw) of the resulting array's components
     *
     * @throws  NullPointerException    if {@code arrayClass == null}
     *
     * @throws  NegativeArraySizeException  if {@code length < 0}
     */
    @SideEffectFree @SuppressWarnings("unchecked")
    public static <A> @Nullable A@NonNull[] checkedArrayGenerator(
            @NonNegative int length,
            @Nullable A@NonNull[] seedArray
    ) {
        return (A[]) Array.newInstance(seedArray.getClass().getComponentType(), length);
    }
    
    /**
     * Maps the elements of the first argument to a new {@code Object[]} using the function
     * of the second argument. Essentially {@link java.util.Arrays#stream(Object[])
     * Arrays.stream(}{@code source}{@link
     * java.util.Arrays#stream(Object[]) )}{@link java.util.stream.Stream#map(Function) .map(}{@code
     * mapper}{@link java.util.stream.Stream#map(Function) )}{@link java.util.stream.Stream#toArray() .toArray()},
     * though not actually utilizing streams, nor late-binding.
     *
     * @param   source  the array with the source elements for the function
     *
     * @param   mapper  the mapping function
     *
     * @return  an {@code Object[]} containing the results of the original array when the
     *          given function is applied
     *
     * @param   <A> the type of the input array
     *
     * @throws  NullPointerException    if either argument is null
     */
    public static <A> @PolyNull Object@NonNull[] mapArray(
            A@NonNull[] source,
            @NonNull Function<? super A, @PolyNull ?> mapper
    ) {
        int length = source.length;
        Object[] res = new Object[length];
        mapArray(source, res, mapper, 0, length, 0);
        return res;
    }
    
    /**
     * Maps the elements of the first argument, from 0 to {@code length}, to a new {@code Object[]}
     * using the function of the second argument. Essentially {@link java.util.Arrays#stream(Object[], int, int)
     * Arrays.stream(}{@code source, 0, }{@link Math#min(int, int) Math.min(}{@code length, source.length}{@link
     * Math#min(int, int) )}{@link java.util.Arrays#stream(Object[], int, int) )}{@link
     * java.util.stream.Stream#map(Function) .map(}{@code mapper}{@link
     * java.util.stream.Stream#map(Function) )}{@link java.util.stream.Stream#toArray() .toArray()},
     * though not actually utilizing streams, nor late-binding.
     *
     * @param   source  the array with the source elements for the function
     *
     * @param   mapper  the mapping function
     *
     * @param   length  the length of the array to be returned; this can be longer than
     *                  the length of {@code source}, in which case the leftover entries will be {@code null}
     *
     * @return  an {@code Object[]} containing the results of the original array when the
     *          given function is applied, plus possibly extra nulls if {@code length > source.length}
     *
     * @param   <A> the type of the input array
     *
     * @throws  NullPointerException    if either of the first two arguments is null
     *
     * @throws  NegativeArraySizeException  if {@code length < 0}
     */
    public static <A> @Nullable Object@NonNull[] mapArray(
            @PolyNull A@NonNull[] source,
            @NonNull Function<@PolyNull ? super A, ?> mapper,
            @NonNegative int length
    ) {
        int copyLength = Math.min(length, source.length);
        Object[] res = new Object[length];
        mapArray(source, res, mapper, 0, copyLength, 0);
        return res;
    }
    
    /**
     * Taking the range of the first array from {@code sourceFromIndex} to {@code sourceToIndex}, these
     * values are mapped using {@code mapper} to a new {@code Object[]} between {@code resultFromIndex}
     * and {@code resultToIndex}. In both cases, the first index is inclusive, and the second is exclusive.
     * The returned array's length is indicated by {@code resultLength}. If these values are inconsistent,
     * an exception will be thrown.
     *
     * @param   source  the array with the source elements for the function
     *
     * @param   sourceFromIndex the index to start taking elements from out of {@code source}
     *
     * @param   sourceToIndex   the index of the first element from {@code source} not to include
     *
     * @param   resultFromIndex the index to start storing elements at in the returned array
     *
     * @param   mapper  the mapping function
     *
     * @param   resultLength    the length of the returned array
     *
     * @return  an {@code Object[]} of the indicated length, storing the mapping results in
     *          the indicated locations
     *
     * @param   <A> the type of the input array
     *
     * @throws  NullPointerException    if {@code source == null} or {@code mapper == null}
     *
     * @throws  IllegalArgumentException    if {@code sourceToIndex - sourceFromIndex
     *                                      > resultLength - resultFromIndex}
     *
     * @throws  ArrayIndexOutOfBoundsException  if either of the source indexes are out of the
     *                                          bounds of {@code source}, or if {@code resultFromIndex < 0}
     *
     * @throws  NegativeArraySizeException  if {@code resultLength < 0}
     */
    public static <A> @PolyNull Object@NonNull[] mapArray(
            A@NonNull[] source,
            @NonNegative @LTLengthOf("source") int sourceFromIndex,
            @NonNegative @LTEqLengthOf("source") int sourceToIndex,
            @NonNegative int resultFromIndex,
            @NonNull Function<? super A, @PolyNull ?> mapper,
            @NonNegative int resultLength
    ) {
        Object[] res = new Object[resultLength];
        mapArray(source, res, sourceFromIndex, sourceToIndex, mapper, resultFromIndex);
        return res;
    }
    
    /**
     * Maps the elements of the first argument to an array created using the
     * given generator function in the second argument and the length of the fourth, using the mapping function
     * of the third argument. Essentially {@link java.util.Arrays#stream(Object[])
     * Arrays.stream(}{@code source}{@link java.util.Arrays#stream(Object[]) )}{@link
     * java.util.stream.Stream#map(Function) .map(}{@code
     * mapper}{@link java.util.stream.Stream#map(Function) )}{@link
     * java.util.stream.Stream#toArray(IntFunction) .toArray(}{@code generator}{@link
     * java.util.stream.Stream#toArray(IntFunction) )},
     * though not actually utilizing streams, nor late-binding.
     *
     * @param   source  the array with the source elements for the function
     *
     * @param   generator   the function to use to generate the result array
     *
     * @param   mapper  the mapping function
     *
     * @return  an array containing the results of the original array when the
     *          given function is applied
     *
     * @param   <I> the type of the input array
     *
     * @param   <O> the type of the output array
     *
     * @throws  NullPointerException    if any of the arguments is null
     */
    public static <I, O> @PolyNull O@NonNull[] mapArray(
            I@NonNull[] source,
            @NonNull IntFunction<@Nullable O@NonNull[]> generator,
            @NonNull Function<? super I, @PolyNull ? extends O> mapper
    ) {
        int length = source.length;
        O[] res = generator.apply(length);
        mapArray(source, res, mapper, 0, length, 0);
        return res;
    }
    
    /**
     * Maps the elements of the first argument to an array created using the
     * given generator function in the second argument, using the mapping function
     * of the third argument. Essentially {@link java.util.Arrays#stream(Object[], int, int)
     * Arrays.stream(}{@code source, 0, }{@link Math#min(int, int) Math.min(}{@code length,
     * source.length}{@link Math#min(int, int) )}{@link
     * java.util.Arrays#stream(Object[], int, int) )}{@link java.util.stream.Stream#map(Function) .map(}{@code
     * mapper}{@link java.util.stream.Stream#map(Function) )}{@link
     * java.util.stream.Stream#toArray(IntFunction) .toArray(}{@code generator}{@link
     * java.util.stream.Stream#toArray(IntFunction) )},
     * though not actually utilizing streams, nor late-binding.
     *
     * @param   source  the array with the source elements for the function
     *
     * @param   generator   the function to use to generate the result array
     *
     * @param   mapper  the mapping function
     *
     * @param   length  the length of the array to be returned; this can be longer than
     *                  the length of {@code source}, in which case the leftover entries will be {@code null}
     *
     * @return  an array containing the results of the original array when the
     *          given function is applied
     *
     * @param   <I> the type of the input array
     *
     * @param   <O> the type of the output array
     *
     * @throws  NullPointerException    if any of the arguments is null
     *
     * @throws  NegativeArraySizeException  if {@code length < 0}
     */
    public static <I, O> @Nullable O@NonNull[] mapArray(
            @PolyNull I@NonNull[] source,
            @NonNull IntFunction<@Nullable O@NonNull[]> generator,
            @NonNull Function<@PolyNull ? super I, ? extends O> mapper,
            @NonNegative int length
    ) {
        int copyLength = Math.min(length, source.length);
        O[] res = generator.apply(length);
        mapArray(source, res, mapper, 0, copyLength, 0);
        return res;
    }
    
    /**
     * Taking the range of the first array from {@code sourceFromIndex} to {@code sourceToIndex}, these
     * values are mapped using {@code mapper} to a new array generated by {@code generator}{@link
     * IntFunction#apply(int) .apply(}{@code resultLength}{@link IntFunction#apply(int) )}
     * starting from {@code resultFromIndex}. In both cases, the first index is inclusive,
     * and the second is exclusive. The returned array's length is indicated by {@code resultLength}.
     * If these indexes are inconsistent, an exception will be thrown.
     *
     * @param   source  the array with the source elements for the function
     *
     * @param   sourceFromIndex the index to start taking elements from out of {@code source}
     *
     * @param   sourceToIndex   the index of the first element from {@code source} not to include
     *
     * @param   resultFromIndex the index to start storing elements at in the returned array
     *
     * @param   generator   the function to use to generate the result array
     *
     * @param   mapper  the mapping function
     *
     * @param   resultLength    the length of the returned array
     *
     * @return  an array of the indicated length and type, storing the mapping results in
     *          the indicated locations
     *
     * @param   <I> the type of the input array
     *
     * @param   <O> the type of the output array
     *
     * @throws  NullPointerException    if {@code source}, {@code generator}, or {@code mapper} are {@code null}
     *
     * @throws  IllegalArgumentException    if {@code sourceToIndex - sourceFromIndex
     *                                      > resultLength - resultFromIndex}
     *
     * @throws  ArrayIndexOutOfBoundsException  if either of the source indexes are out of the
     *                                          bounds of {@code source}, or if {@code resultFromIndex < 0}
     *
     * @throws  NegativeArraySizeException  if {@code resultLength < 0}
     */
    public static <I, O> @PolyNull O@NonNull[] mapArray(
            I@NonNull[] source,
            @NonNegative @LTLengthOf("source") int  sourceFromIndex,
            @NonNegative @LTEqLengthOf("source") int sourceToIndex,
            @NonNull IntFunction<@Nullable O@NonNull[]> generator,
            @NonNegative int resultFromIndex,
            @NonNull Function<? super I, @PolyNull ? extends O> mapper,
            @NonNegative int resultLength
    ) {
        O[] res = generator.apply(resultLength);
        mapArray(source, res, sourceFromIndex, sourceToIndex, mapper, resultFromIndex);
        return res;
    }
    
    /**
     * <p>Maps the elements of the first argument to an array created using the
     * class of the third argument, using the mapping function of the
     * second argument. Essentially {@link java.util.Arrays#stream(Object[]) Arrays.stream(}{@code source}{@link
     * java.util.Arrays#stream(Object[]) )}{@link java.util.stream.Stream#map(Function) .map(}{@code
     * mapper}{@link java.util.stream.Stream#map(Function) )}{@link
     * java.util.stream.Stream#collect(java.util.stream.Collector) .collect(}{@link
     * java.util.stream.Collectors#toList Collectors.toList()}{@link
     * java.util.stream.Stream#collect(java.util.stream.Collector) )}{@link java.util.List#toArray(Object[])
     * .toArray(}{@code seedArray}{@link java.util.List#toArray(Object[]) )}, though
     * without any intermediate {@code List} actually being constructed.</p>
     *
     * <p>Note that if {@code seedArray} is {@code null}, the array that is returned will be an array
     * of type {@code Object[]}, but possibly cast. Thus it is <i>NOT</i> typesafe; the array is <i>UNCHECKED</i>.
     * This, this is only advisable if {@code O} is itself {@link Object} or if this array will never be
     * exposed publicly. To avoid this, {@code seedArray} should not be {@code null}.</p>
     *
     * @param   source  the array with the source elements for the function
     *
     * @param   mapper  the mapping function
     *
     * @param   seedArray   an array to use to generate the returned array; the
     *                      contents of this array are not used, only its class, nor is it mutated
     *
     * @return  an array containing the results of the original array when the
     *          given function is applied
     *
     * @param   <I> the type of the input array
     *
     * @param   <O> the type of the output array
     *
     * @throws  NullPointerException    if either of the first two arguments are null
     *
     * @see #unCheckedArrayGenerator(int)
     */
    public static <I, O> @PolyNull O@NonNull[] mapArray(
            I@NonNull[] source,
            @NonNull Function<? super I, @PolyNull ? extends O> mapper,
            @Nullable O@Nullable[] seedArray
    ) {
        int length = source.length;
        O[] res = NullnessUtils.generateDefaultIfNull(
                seedArray,
                a -> checkedArrayGenerator(length, a),
                () -> unCheckedArrayGenerator(length)
        );
        mapArray(source, res, mapper, 0, length, 0);
        
        return res;
    }
    
    /**
     * <p>Maps the elements of the first argument to an array created using the
     * class of the fourth argument and length in the third, using the mapping function of the second argument.
     * Essentially {@link java.util.Arrays#stream(Object[], int, int) Arrays.stream(}{@code source, 0, }{@link
     * Math#min(int, int) Math.min(}{@code length, source.length}{@link Math#min(int, int) )}{@link
     * java.util.Arrays#stream(Object[], int, int) )}{@link java.util.stream.Stream#map(Function) .map(}{@code
     * mapper}{@link java.util.stream.Stream#map(Function) )}{@link
     * java.util.stream.Stream#collect(java.util.stream.Collector) .collect(}{@link
     * java.util.stream.Collectors#toList Collectors.toList()}{@link
     * java.util.stream.Stream#collect(java.util.stream.Collector) )}{@link java.util.List#toArray(Object[])
     * .toArray(}{@code seedArray}{@link java.util.List#toArray(Object[]) )}, though
     * without any intermediate {@code List} actually being constructed.</p>
     *
     * <p>Note that if {@code seedArray} is {@code null}, the array that is returned will be an array
     * of type {@code Object[]}, but possibly cast. Thus it is <i>NOT</i> typesafe; the array is <i>UNCHECKED</i>.
     * This, this is only advisable if {@code O} is itself {@link Object} or if this array will never be
     * exposed publicly. To avoid this, {@code seedArray} should not be {@code null}.</p>
     *
     * @param   source  the array with the source elements for the function
     *
     * @param   mapper  the mapping function
     *
     * @param   length  the length of the array to be returned; this can be longer than
     *                  the length of {@code source}, in which case the leftover entries will be {@code null}
     *
     * @param   seedArray   an array to use to generate the returned array; the
     *                      contents of this array are not used, only its class, nor is it mutated
     *
     * @return  an array containing the results of the original array when the
     *          given function is applied
     *
     * @param   <I> the type of the input array
     *
     * @param   <O> the type of the output array
     *
     * @throws  NullPointerException    if either of the first two arguments are null
     *
     * @throws  NegativeArraySizeException  if {@code length < 0}
     *
     * @see #unCheckedArrayGenerator(int)
     */
    public static <I, O> @Nullable O@NonNull[] mapArray(
            @PolyNull I@NonNull[] source,
            @NonNull Function<@PolyNull ? super I, ? extends O> mapper,
            @NonNegative int length,
            @Nullable O@Nullable[] seedArray
    ) {
        int copyLength = Math.min(length, source.length);
        O[] res = NullnessUtils.generateDefaultIfNull(
                seedArray,
                a -> checkedArrayGenerator(length, a),
                () -> unCheckedArrayGenerator(length)
        );
        mapArray(source, res, mapper, 0, copyLength, 0);
        return res;
    }
    
    /**
     * <p>Taking the range of the first array from {@code sourceFromIndex} to {@code sourceToIndex}, these
     * values are mapped using {@code mapper} to a new array between {@code resultFromIndex}
     * and {@code resultToIndex}. In both cases, the first index is inclusive, and the second is exclusive.
     * The returned array's length is indicated by {@code resultLength}. If these values are inconsistent,
     * an exception will be thrown.</p>
     *
     * <p>If {@code seedArray} is {@code null}, the array that is returned will be an array
     * of type {@code Object[]}, but possibly cast. Thus it is <i>NOT</i> typesafe; the array is <i>UNCHECKED</i>.
     * This, this is only advisable if {@code O} is itself {@link Object} or if this array will never be
     * exposed publicly. To avoid this, {@code seedArray} should not be {@code null}.</p>
     *
     * @param   source  the array with the source elements for the function
     *
     * @param   sourceFromIndex the index to start taking elements from out of {@code source}
     *
     * @param   sourceToIndex   the index of the first element from {@code source} not to include
     *
     * @param   resultFromIndex the index to start storing elements at in the returned array
     *
     * @param   mapper  the mapping function
     *
     * @param   resultLength    the length of the returned array
     *
     * @param   seedArray   an array to use to generate the returned array; the
     *                      contents of this array are not used, only its class, nor is it mutated
     *
     * @return  an array of the indicated length and type, storing the mapping results in
     *          the indicated locations
     *
     * @param   <I> the type of the input array
     *
     * @param   <O> the type of the output array
     *
     * @throws  NullPointerException    if {@code source == null} or {@code mapper == null}
     *
     * @throws  IllegalArgumentException    if {@code sourceToIndex - sourceFromIndex
     *                                      > resultLength - resultFromIndex}
     *
     * @throws  ArrayIndexOutOfBoundsException  if either of the source indexes are out of the
     *                                          bounds of {@code source}, or if {@code resultFromIndex < 0}
     *
     * @throws  NegativeArraySizeException  if {@code resultLength < 0}
     *
     * @see #unCheckedArrayGenerator(int)
     */
    public static <I, O> @PolyNull O@NonNull[] mapArray(
            I@NonNull[] source,
            @NonNegative @LTLengthOf("source") int sourceFromIndex,
            @NonNegative @LTEqLengthOf("source") int sourceToIndex,
            @NonNegative int resultFromIndex,
            @NonNull Function<? super I, @PolyNull ? extends O> mapper,
            @NonNegative int resultLength,
            @Nullable O@Nullable[] seedArray
    ) {
        O[] res = NullnessUtils.generateDefaultIfNull(
                seedArray,
                a -> checkedArrayGenerator(resultLength, a),
                () -> unCheckedArrayGenerator(resultLength)
        );
        mapArray(source, res, sourceFromIndex, sourceToIndex, mapper, resultFromIndex);
        return res;
    }
    
    /**
     * <p>Maps the elements of the first argument into the second, after they are passed through
     * the mapping function of the third argument. If {@code toStoreResults} is a size other than
     * that of {@code source}, a new array is allocated, as if through {@link
     * #mapArray(Object[], Function, Object[]) mapArray(}{@code source, mapper, toStoreResults}{@link
     * #mapArray(Object[], Function, Object[]) )}, though without the unchecked problems resulting from a {@code null}
     * array input.</p>
     *
     * <p>Like that method, this one is also essentially {@link java.util.Arrays#stream(Object[])
     * Arrays.stream(}{@code source}{@link java.util.Arrays#stream(Object[]) )}{@link
     * java.util.stream.Stream#map(Function) .map(}{@code mapper}{@link java.util.stream.Stream#map(Function)
     * )}{@link java.util.stream.Stream#collect(java.util.stream.Collector) .collect(}{@link
     * java.util.stream.Collectors#toList Collectors.toList()}{@link
     * java.util.stream.Stream#collect(java.util.stream.Collector) )}{@link java.util.List#toArray(Object[])
     * .toArray(}{@code seedArray}{@link java.util.List#toArray(Object[]) )}, though
     * without any intermediate {@code List} actually being constructed.</p>
     *
     * @param   source  the array with the source elements for the function
     *
     * @param   toStoreResults  the array within which to store the results,
     *                          assuming it is the correct length; if not, a new array is allocated of
     *                          the same type
     *
     * @param   mapper  the mapping function
     *
     * @return  an array of the class of the second argument containing the results of the mapping function
     *          when applied to {@code source} &mdash;
     *          {@code toStoreResults}, if it has the correct length
     *
     * @param   <I> the type of the input array
     *
     * @param   <O> the type of the output array
     *
     * @throws  NullPointerException    if any of the arguments is null
     */
    public static <I, O> @PolyNull O@NonNull[] mapArray(
            I@NonNull[] source,
            @Nullable O@NonNull[] toStoreResults,
            @NonNull Function<? super I, @PolyNull ? extends O> mapper
    ) {
        int length = source.length;
        O[] res = verifyArrayChecked(toStoreResults, length);
        mapArray(source, res, mapper, 0, length, 0);
        return res;
    }
    
    /**
     * <p>Maps the elements of the first argument into the second, after they are passed through
     * the mapping function of the third argument. If {@code toStoreResults} is a size other than
     * that of {@code source}, a new array is allocated, as if through {@link
     * #mapArray(Object[], Function, int, Object[]) mapArray(}{@code source, mapper, length, toStoreResults}{@link
     * #mapArray(Object[], Function, int, Object[]) )}, though without the unchecked problems resulting
     * from a {@code null} array input.</p>
     *
     * <p>Like that method, this one is also essentially {@link java.util.Arrays#stream(Object[], int, int)
     * Arrays.stream(}{@code source, 0, }{@link Math#min(int, int) Math.min(}{@code length,
     * source.length}{@link Math#min(int, int) )}{@link java.util.Arrays#stream(Object[], int, int) )}{@link
     * java.util.stream.Stream#map(Function) .map(}{@code mapper}{@link java.util.stream.Stream#map(Function)
     * )}{@link java.util.stream.Stream#collect(java.util.stream.Collector) .collect(}{@link
     * java.util.stream.Collectors#toList Collectors.toList()}{@link
     * java.util.stream.Stream#collect(java.util.stream.Collector) )}{@link java.util.List#toArray(Object[])
     * .toArray(}{@code seedArray}{@link java.util.List#toArray(Object[]) )}, though
     * without any intermediate {@code List} actually being constructed.</p>
     *
     * @param   source  the array with the source elements for the function
     *
     * @param   toStoreResults  the array within which to store the results,
     *                          assuming it is the correct length; if not, a new array is allocated of
     *                          the same type
     *
     * @param   mapper  the mapping function
     *
     * @param   length  the length of the array to be returned; this can be longer than
     *                  the length of {@code source}, in which case the leftover entries will be {@code null}
     *
     * @return  an array of the class of the second argument containing the results of the mapping function
     *          when applied to {@code source} &mdash;
     *          {@code toStoreResults}, if it has the correct length
     *
     * @param   <I> the type of the input array
     *
     * @param   <O> the type of the output array
     *
     * @throws  NullPointerException    if any of the arguments is null
     *
     * @throws  NegativeArraySizeException  if {@code length < 0}
     */
    public static <I, O> @Nullable O@NonNull[] mapArray(
            @PolyNull I@NonNull[] source,
            @Nullable O@NonNull[] toStoreResults,
            @NonNull Function<@PolyNull ? super I, ? extends O> mapper,
            @NonNegative int length
    ) {
        int copyLength = Math.min(length, source.length);
        O[] res = verifyArrayChecked(toStoreResults, length);
        mapArray(source, res, mapper, 0, copyLength, 0);
        return res;
    }
    
    /**
     * Taking the range of the first array from {@code sourceFromIndex} to {@code sourceToIndex}, these
     * values are mapped using {@code mapper} to {@code toStoreResults}, or a new array generated of the same
     * class, starting from {@code resultFromIndex}. In both cases, the first index is inclusive,
     * and the second is exclusive. The returned array's length is indicated by {@code resultLength}; if
     * the length of {@code toStoreResults} does not match this, a copy of the proper length is allocated instead.
     * If the indexes are inconsistent, an exception will be thrown.
     *
     * @param   source  the array with the source elements for the function
     *
     * @param   sourceFromIndex the index to start taking elements from out of {@code source}
     *
     * @param   sourceToIndex   the index of the first element from {@code source} not to include
     *
     * @param   resultFromIndex the index to start storing elements at in the returned array
     *
     * @param   toStoreResults  the array within which to store the results,
     *                          assuming it is the correct length; if not, a new array is allocated of
     *                          the same type
     *
     * @param   mapper  the mapping function
     *
     * @param   resultLength    the length of the returned array
     *
     * @return  an array of the indicated length and type, storing the mapping results in
     *          the indicated locations
     *
     * @param   <I> the type of the input array
     *
     * @param   <O> the type of the output array
     *
     * @throws  NullPointerException    if {@code source}, {@code toStoreResults},
     *                                  or {@code mapper} are {@code null}
     *
     * @throws  IllegalArgumentException    if {@code sourceToIndex - sourceFromIndex
     *                                      > resultLength - resultFromIndex}
     *
     * @throws  ArrayIndexOutOfBoundsException  if either of the source indexes are out of the
     *                                          bounds of {@code source}, or if {@code resultFromIndex < 0}
     *
     * @throws  NegativeArraySizeException  if {@code resultLength < 0}
     */
    public static <I, O> @PolyNull O@NonNull[] mapArray(
            I@NonNull[] source,
            @NonNegative @LTLengthOf("source") int sourceFromIndex,
            @NonNegative @LTEqLengthOf("source") int sourceToIndex,
            @Nullable O@NonNull[] toStoreResults,
            @NonNegative @LTLengthOf("toStoreResults") int resultFromIndex,
            @NonNull Function<? super I, @PolyNull ? extends O> mapper,
            @NonNegative int resultLength
    ) {
        O[] res = verifyArrayChecked(toStoreResults, resultLength);
        mapArray(source, res, sourceFromIndex, sourceToIndex, mapper, resultFromIndex);
        return res;
    }
    
    /**
     * Puts the output of the function on the source array's elements into the given array, and
     * then returns that array ({@code toStore}).
     *
     * @param   source  the source of function inputs
     *
     * @param   toStore the location to place function outputs. This is a muted argument!
     *
     * @param   sourceFromIndex the index to start in the source array
     *
     * @param   sourceToIndex   the index to end in the source array
     *
     * @param   mapper  the function
     *
     * @param   resultFromIndex the index to start in the result array
     *
     * @param   <I> the input array and functional argument type
     *
     * @param   <O> the output array and functional result type
     *
     * @throws  IllegalArgumentException    if {@code sourceToIndex - sourceFromIndex
     *                                      > toStore.length - resultFromIndex} (failfast for the mutable argument)
     */
    private static <I, O> void mapArray(
            @PolyNull I@NonNull[] source,
            @Nullable O@NonNull[] toStore,
            @NonNegative @LTLengthOf("source") int sourceFromIndex,
            @NonNegative @LTEqLengthOf("source") int sourceToIndex,
            @NonNull Function<@PolyNull ? super I, ? extends O> mapper,
            @NonNegative @LTLengthOf("toStore") int resultFromIndex
    ) {
        int offset = resultFromIndex - sourceFromIndex;
        if (sourceToIndex + offset > toStore.length) {
            throw new IllegalArgumentException("Inconsistent index arguments!");
        }
        int copyLength = sourceToIndex - sourceFromIndex;
        mapArray(source, toStore, mapper, sourceFromIndex, copyLength, offset);
    }
    
    /**
     * Puts the output of the function on the source array's elements into the given array.
     *
     * @param   source  the source of function inputs
     *
     * @param   toStore the location to place function outputs. This is a mutated argument!
     *
     * @param   mapper  the function
     *
     * @param   start   the start location in the source array
     *
     * @param   length  the length to copy
     *
     * @param   resultOffset    the offset from source starting index to result starting index
     *
     * @param   <I> the input array and functional argument type
     *
     * @param   <O> the output array and functional result type
     */
    private static <I, O> void mapArray(
            @PolyNull I@NonNull[] source,
            @Nullable O@NonNull[] toStore,
            @NonNull Function<@PolyNull ? super I, ? extends O> mapper,
            @NonNegative @LTLengthOf("source") int start,
            @NonNegative @LTEqLengthOf({"source", "toStore"}) int length,
            @LTLengthOf("toStore") int resultOffset
    ) {
        IntStream.range(start, length).parallel()
                .forEach(i -> toStore[i + resultOffset] = mapper.apply(source[i]));
    }
    
    /**
     * <p>Verifies that the given array has the given length,
     * and generates an empty copy with the proper {@code length} if not.</p>
     *
     * <p>If the given array is null, the copy that is returned
     * is simply a recast {@code Object[]}.
     * As such, it is an <i>UNCHECKED</i> array, and if recast or exposed in such as way that it could be recast,
     * there are no guarantees from {@link ArrayStoreException} popping up later on.</p>
     *
     * @param   length  the length of the returned array
     *
     * @param   toVerify    the array whose size is to be verified, and possibly to be replaced
     *
     * @return  an array with the same class as the given array and length of {@code length} &mdash;
     *          {@code toVerify}, if it has the correct length
     *
     * @param   <A> the component class of the array
     *
     * @throws  NegativeArraySizeException  if {@code length < 0}
     */
    @SideEffectFree
    public static <A> @PolyNull A@NonNull[] verifyArrayUnChecked(
            @NonNegative int length,
            @PolyNull A@Nullable[] toVerify
    ) {
        return NullnessUtils.generateDefaultIfNull(
                toVerify,
                a -> verifyArrayChecked(a, length),
                () -> unCheckedArrayGenerator(length)
        );
    }
    
    /**
     * Verifies that the given array has the given length,
     * and generates an empty copy with the proper {@code length} if not.
     *
     * @param   toVerify    the array whose size is to be verified, and possibly to be replaced
     *
     * @param   length  the length of the returned array
     *
     * @return  an array with the same class as the given array and length of {@code length} &mdash;
     *          {@code toVerify}, if it has the correct length
     *
     * @param   <A> the component class of the array
     *
     * @throws  NullPointerException    if {@code toVerify == null}
     *
     * @throws  NegativeArraySizeException  if {@code length < 0}
     */
    @SideEffectFree
    public static <A> @PolyNull A@NonNull[] verifyArrayChecked(
            @PolyNull A@NonNull[] toVerify,
            @NonNegative int length
    ) {
        return (toVerify.length == length)
                ? toVerify
                : checkedArrayGenerator(length, toVerify);
    }
}
