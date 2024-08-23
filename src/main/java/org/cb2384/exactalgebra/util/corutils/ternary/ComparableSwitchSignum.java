package org.cb2384.exactalgebra.util.corutils.ternary;

import org.cb2384.corutils.ternary.Signum;
import org.cb2384.corutils.ternary.ThreeValued;

import org.checkerframework.checker.nullness.qual.*;
import org.checkerframework.dataflow.qual.*;

/**
 * <p>Extends {@link Comparable} with two default functions that return the comparison result in different forms.
 * The most important one returns a {@link Signum} (a {@link ThreeValued} type), which is useful
 * for switch statements based on the comparison result.</p>
 *
 * <p>Implementing this interface instead of {@code Comparable} gains these two functions,
 * but also still grants the normal benefits of {@code Comparable}.</p>
 *
 * @param   <T> the implementing class itself, just like {@link Comparable}.
 *
 * @author  Corinne Buxton
 */
public interface ComparableSwitchSignum<T extends ComparableSwitchSignum<?>>
        extends Comparable<T> {
    
    /**
     * Returns a {@code byte} representing the comparison result. Unlike {@link #compareTo},
     * this value <i>MUST</i> be one of {@code -1}, {@code 0}, or {@code 1}. This <i>was</i>, and to an extent
     * still is, useful for switching; however, the {@code enum}-based {@link #compare} function, incorporating
     * {@link ThreeValued} and associated features, is even better in this regard. This function will
     * stick around too though.
     *
     * @param   that    the object of comparison with {@code this}
     *
     * @return  {@code -1}, {@code 0}, or {@code 1}, based on the {@link #compareTo(Object) compareTo} result
     *
     * @throws  NullPointerException    if {@code that == null} and the original {@link #compareTo(Object) compareTo}
     *                                  implementation does not support nulls (which most don't)
     *
     * @throws  ClassCastException  if the type of {@code that} prevents it from being compared to this
     */
    @Pure
    default byte compareTob(
            @NonNull T that
    ) {
        return (byte) Integer.signum(compareTo(that));
    }
    
    /**
     * Returns a {@link Signum} representing the comparison result. This is useful for switching,
     * as then no {@link Integer#signum(int) signum} and default block are required.
     *
     * @param   that    the object of comparison with {@code this}
     *
     * @return  a {@code Signum}, which represents {@code -1}, {@code 0}, or {@code 1},
     *          according to the {@link #compareTo} result
     *
     * @throws  NullPointerException    if {@code that == null} and the original {@link #compareTo}
     *                                  implementation does not support nulls (which most don't)
     *
     * @throws  ClassCastException  if the type of {@code that} prevents it from being compared to this
     */
    @Pure
    default @NonNull Signum compare(
            @NonNull T that
    ) {
        return Signum.valueOf(compareTo(that));
    }
}
