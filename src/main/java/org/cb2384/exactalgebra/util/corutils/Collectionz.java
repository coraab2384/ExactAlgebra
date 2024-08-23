package org.cb2384.exactalgebra.util.corutils;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import org.cb2384.corutils.StringUtils;

import org.checkerframework.checker.nullness.qual.*;
import org.checkerframework.dataflow.qual.*;

/**
 * <p>Simple static utilities to augment basic {@link Collection} utilities. Like an add-on to
 * {@link java.util.Collections}. When used in some situations, a user might like to receive the original
 * collection back after adding something, rather than the customary {@code boolean} that {@link Collection#add}
 * normally gives, such as appending something to the end of a List before handing it off.</p>
 * These functions fill that gap. There are self-returning (self being the Collection or Map argument, not the
 * receiver, as these are static) methods for adding a value to a collection, an entry to a map, or a collection
 * or map to another collection or map, respectively.</p>
 *
 * @author  Corinne Buxton
 */
public class Collectionz {
    
    /**
     * Shouldn't ever be called
     *
     * @throws IllegalAccessException   always
     */
    private Collectionz() throws IllegalAccessException {
        throw new IllegalAccessException("This should never be called" + StringUtils.INTERROBANG);
    }
    
    /**
     * Performs the lambda <code>(collection, element) -&gt; {collection</code>{@link Collection#add(Object)
     * .add(}{@code element}{@link Collection#add(Object) )}<code>; return collection;}</code>,
     * but written here for the ability to use a direct method reference.
     *
     * @param collection    the collection {@code element} will be added to
     * @param element       the element to add to {@code collection}
     *
     * @return  {@code collection}, now containing {@code element}
     *
     * @param <C>   the specific type of {@code collection}
     * @param <E>   the specific type of {@code collection}'s elements, which should be a superclass/interface
     *              of {@code element}'s actual type
     */
    @Deterministic
    public static <C extends Collection<@PolyNull E>, E> @NonNull C add(
            @NonNull C collection,
            @PolyNull E element
    ) {
        collection.add(element);
        return collection;
    }
    
    /**
     * Performs the lambda <code>(receiverCollection, addedCollection) -&gt; {receiverCollection</code>{@link
     * Collection#addAll(Collection) .addAll(}{@code addedCollection}{@link Collection#addAll(Collection)
     * )}<code>; return receiverCollection;}</code>, but written here for the ability to use a direct method reference.
     *
     * @param receiverCollection    the collection {@code addedCollection} will be added to
     * @param addedCollection       the collection to add to {@code receiverCollection}
     *
     * @return  {@code receiverCollection}, now containing all of {@code addedCollection}
     *
     * @param <C>   the specific type of {@code receiverCollection}
     * @param <E>   the specific type of {@code receiverCollection}'s elements, which should be a superclass/interface
     *              of {@code addedCollection}'s elements
     */
    @Deterministic
    public static <C extends Collection<@PolyNull E>, E> @NonNull C addAllOrdered(
            @NonNull C receiverCollection,
            @NonNull Collection<@PolyNull ? extends E> addedCollection
    ) {
        receiverCollection.addAll(addedCollection);
        return receiverCollection;
    }
    
    /**
     * Similar to {@link #addAllOrdered}, but with the difference that it is not
     * pre-defined which Collection will be added to the other; whichever is smaller will be added,
     * defaulting to the first ({@code leftCollection}) still.
     *
     * @param leftCollection    the first Map to combine
     * @param rightCollection   the second Map to combine
     *
     * @return  the larger of the two input Collections, now containing the other
     *
     * @param <C>   the specific type of the input Collections
     * @param <E>   the specific type of {@code C}'s elements
     *
     * @see #addAllOrdered(Collection, Collection)
     */
    @Deterministic
    public static <C extends Collection<E>, E> @NonNull C addAllUnordered(
            @NonNull C leftCollection,
            @NonNull C rightCollection
    ) {
        if (leftCollection.size() < rightCollection.size()) {
            rightCollection.addAll(leftCollection);
            return rightCollection;
        }
        leftCollection.addAll(rightCollection);
        return leftCollection;
    }
    
    /**
     * Performs the lambda <code>(map, key, value) -&gt; {map</code>{@link Map#put(Object, Object)
     * .put(}{@code key, value}{@link Map#put(Object, Object) )}<code>; return map;}</code>,
     * but written here for the ability to use a direct method reference.
     *
     * @param map   the map {@code key} and {@code value} will be added to
     * @param key   the key to add to {@code map}
     * @param value the value to add for {@code key}
     *
     *
     * @return  {@code map}, now containing {@code key} with the mapping {@code value}
     *
     * @param <M>   the specific type of {@code map}
     * @param <K>   the specific type of {@code maps}'s keys, which should be a superclass/interface
     *              of {@code key}'s actual type
     * @param <V>   the specific type of {@code maps}'s values, which should be a superclass/interface
     *              of {@code value}'s actual type
     */
    @Deterministic
    public static <M extends Map<@PolyNull K, @PolyNull V>, K, V> @NonNull M put(
            @NonNull M map,
            @PolyNull K key,
            @PolyNull V value
    ) {
        map.put(key, value);
        return map;
    }
    
    /**
     * Performs the lambda <code>(map, entry) -&gt; {map</code>{@link Map#put(Object, Object)
     * .put(}{@code entry}{@link Entry#getKey() .getKey()}{@code , entry}{@link Entry#getValue()
     * .getValue()}{@link Map#put(Object, Object) )}<code>; return map;}</code>,
     * but written here for the ability to use a direct method reference.
     *
     * @param map   the map {@code entry} will be added to
     * @param entry the entry to add to {@code map}
     *
     *
     * @return  {@code map}, now containing the {@code entry}
     *
     * @param <M>   the specific type of {@code map}
     * @param <K>   the specific type of {@code maps}'s keys, which should be a superclass/interface
     *              of {@code entry}'s key type
     * @param <V>   the specific type of {@code maps}'s values, which should be a superclass/interface
     *              of {@code entry}'s value type
     */
    @Deterministic
    public static <M extends Map<@PolyNull K, @PolyNull V>, K, V> @NonNull M put(
            @NonNull M map,
            @NonNull Entry<@PolyNull ? extends K, @PolyNull ? extends V> entry
    ) {
        map.put(entry.getKey(), entry.getValue());
        return map;
    }
    
    /**
     * Performs the lambda <code>(receiverMap, addedMap) -&gt; {receiverMap</code>{@link
     * Map#putAll(Map) .putAll(}{@code addedMap}{@link Map#putAll(Map) )}<code>; return receiverMap;}</code>,
     * but written here for the ability to use a direct method reference.
     *
     * @param receiverMap   the map {@code addedMap} will be added to
     * @param addedMap      the map to add to {@code receiverMap}
     *
     * @return  {@code receiverMap}, now containing all of {@code addedMap}
     *
     * @param <M>   the specific type of {@code receiverMap}
     * @param <K>   the specific type of {@code receiverMap}'s keys, which should be a superclass/interface
     *              of {@code addedMap}'s key type
     * @param <V>   the specific type of {@code receiverMap}'s values, which should be a superclass/interface
     *              of {@code addedMap}'s value type
     */
    @Deterministic
    public static <M extends Map<@PolyNull K, @PolyNull V>, K, V> @NonNull M putAllOrdered(
            @NonNull M receiverMap,
            @NonNull Map<@PolyNull ? extends K, @PolyNull ? extends V> addedMap
    ) {
        receiverMap.putAll(addedMap);
        return receiverMap;
    }
    
    /**
     * Similar to {@link #putAllOrdered}, but with the difference that it is not pre-defined which Map
     * will be added to the other; whichever is smaller will be added,
     * defaulting to the first ({@code leftMap}) still.
     *
     * @param leftMap   the first Map to combine
     * @param rightMap  the second Map to combine
     *
     * @return  the larger of the two input Maps, now containing the other
     *
     * @param <M>   the specific type of the input Maps
     * @param <K>   the specific type of {@code M}'s keys
     * @param <V>   the specific type of {@code M}'s values
     *
     * @see #putAllOrdered(Map, Map)
     */
    @Deterministic
    public static <M extends Map<K, V>, K, V> @NonNull M putAllUnordered(
            @NonNull M leftMap,
            @NonNull M rightMap
    ) {
        if (leftMap.size() < rightMap.size()) {
            rightMap.putAll(leftMap);
            return rightMap;
        }
        leftMap.putAll(rightMap);
        return leftMap;
    }
}
