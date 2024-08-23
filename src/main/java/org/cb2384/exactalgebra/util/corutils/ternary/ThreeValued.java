package org.cb2384.exactalgebra.util.corutils.ternary;

import org.cb2384.corutils.ternary.ThreeValuedContext;

import org.checkerframework.checker.nullness.qual.*;
import org.checkerframework.dataflow.qual.*;

/**
 * Indicates that this is a Three-Valued Logic class, one with a value for {@code true}, {@code false}, and
 * a third value (usually a default value or an unknown-truth value). While this could, theoretically,
 * be used as a functional interface, that it not the intention; an interface specifically labelled as
 * {@link FunctionalInterface} would likely serve better.
 *
 * @see ThreeValuedContext
 *
 * @author  Corinne Buxton
 */
public interface ThreeValued {
    
    /**
     * <p>This is the single specified operation of this interface. It determines the mapping between
     * a nullable {@link Boolean} instance and the Three-Valued implementation. It would seem advisable,
     * though is not required, that the implementing class be an {@link Enum} with three constants.</p>
     *
     * <p>Implementation Requirements:  It should return {@code true} for the value that represents
     *                                  {@code true}, {@code false} for the value that represents
     *                                  {@code false}, and {@code null} for the third value.</p>
     *
     * @return  {@link Boolean#TRUE}, {@link Boolean#FALSE}, or {@code null} based on the value of this
     *          Three-Valued class.
     */
    @Pure
    @Nullable Boolean booleanValue();
}
