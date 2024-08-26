package org.cb2384.exactalgebra.util.corutils.ternary;

import java.util.function.Function;
import java.util.function.Predicate;

import org.checkerframework.checker.nullness.qual.*;
import org.checkerframework.dataflow.qual.*;

/**
 * A {@link BooleanValuedContext} is an object that defines a context for boolean-yielding operations
 * performed an a {@link ThreeValued} type. It is up to the implementation whether to accept {@code null}
 * inputs for the operations or not, but whichever way it is, it should be <i>consistent</i>
 * and <i>documented</i>.
 *
 * @param   <T> the {@link ThreeValued} implementation that this context is compatible with
 *
 * @author  Corinne Buxton
 */
public interface BooleanValuedContext<T extends ThreeValued> {
    
    /**
     * Returns the boolean value of the given {@link ThreeValued} argument, according to this context.
     *
     * @param   value   the Three-Valued object to get the {@code boolean} value of
     *
     * @return  the {@code boolean} value of {@code value}
     */
    @Pure
    boolean booleanValue(T value);
    
    /**
     * Performs a logical not ({@code !} or {@code ~}) on {@code value}.
     *
     * @param   value   the {@link ThreeValued} member to 'not'
     *
     * @return  the logical inverse of {@code value}
     */
    @Pure
    boolean not(T value);
    
    /**
     * Performs a logical and ({@code &} or {@code &&}) on {@code left} and {@code right}.
     *
     * @param   left    the left-hand argument
     *
     * @param   right   the right-hand argument
     *
     * @return  the {@code boolean} and result
     */
    @Pure
    boolean and(T left, T right);
    
    /**
     * <p>Performs a logical nand ({@code &} or {@code &&} followed by a not) on {@code left} and {@code right}.</p>
     *
     * <p>Implementation Note:  The default implementation calls {@code !}{@link #and and(}{@code
     *                          left, right}{@link #and )};
     *                          it should be overridden should different behavior be desired.</p>
     *
     *
     * @param   left    the left-hand argument
     *
     * @param   right   the right-hand argument
     *
     * @return  the {@code boolean} nand result
     */
    @Pure
    default boolean nand(
            T left,
            T right
    ) {
        return !and(left, right);
    }
    
    /**
     * Performs a logical or ({@code |} or {@code ||}) on {@code left} and {@code right}.
     *
     * @param   left    the left-hand argument
     *
     * @param   right   the right-hand argument
     *
     * @return  the {@code boolean} or result
     */
    @Pure
    boolean or(T left, T right);
    
    /**
     * <p>Performs a logical nor ({@code |} or {@code ||} followed by a not) on {@code left} and {@code right}.</p>
     *
     * <p>Implementation Note:  The default implementation calls {@code !}{@link #or or(}{@code
     *                          left, right}{@link #or )};
     *                          it should be overridden should different behavior be desired.
     *
     * @param   left    the left-hand argument
     *
     * @param   right   the right-hand argument
     *
     * @return  the {@code boolean} nor result
     */
    @Pure
    default boolean nor(
            T left,
            T right
    ) {
        return !or(left, right);
    }
    
    /**
     * Performs a logical xor ({@code ^}) on {@code left} and {@code right}.
     *
     * @param   left    the left-hand argument
     *
     * @param   right   the right-hand argument
     *
     * @return  the {@code boolean} xor result
     */
    @Pure
    boolean xor(T left, T right);
    
    /**
     * Returns the boolean truth value of the logical statement {@code precondition -> postcondition},
     * an if-{@code precondition}&ndash;then-{@code postcondition} statement.
     *
     * @param   precondition    the left argument, the one before the implies arrow
     *
     * @param   postcondition   the right argument, the one after the implies arrow
     *
     * @return  the {@code boolean} implies or if-then result
     */
    @Pure
    boolean implies(T precondition, T postcondition);
    
    /**
     * Performs a logcial iff operation (also an xnor operation for a {@code boolean} context, like this)
     * on {@code left} and {@code right}.
     *
     * @param   left    the left-hand argument
     *
     * @param   right   the right-hand argument
     *
     * @return  the {@code boolean} iff or xnor result
     */
    @Pure
    boolean iff(T left, T right);
    
    /**
     * Transforms a function which takes in some object and yields a {@link T} into a {@link Predicate},
     * for use with {@link java.util.stream.Stream} methods which call specifically for a predicate for example.
     *
     * @param   ternaryFunction the {@link Function} that is to be transformed into a {@link Predicate}
     *
     * @return  the {@link Predicate} that captures the logic of the input {@link Function}, with default/unknown
     *          values being pushed according this context
     *
     * @throws  NullPointerException    if {@code ternaryFunction == null}
     */
    @SideEffectFree
    @NonNull Predicate<@PolyNull T> threeValuedFunctionToPredicate(
            @NonNull Function<? super T, @PolyNull ? extends T> ternaryFunction
    );
}
