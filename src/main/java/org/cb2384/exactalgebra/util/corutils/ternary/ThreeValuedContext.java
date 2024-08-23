package org.cb2384.exactalgebra.util.corutils.ternary;

import org.cb2384.corutils.ternary.Ternary;
import org.cb2384.corutils.ternary.ThreeValued;

import org.checkerframework.checker.nullness.qual.*;
import org.checkerframework.dataflow.qual.*;

/**
 * An interface designating a context for a {@link org.cb2384.corutils.ternary.ThreeValued} logic structure.
 * In Three-Valued Logic (3VL or TVL) there exists an additional value between the normal
 * {@link Boolean} true and false. However, what this value represents depends on the context&mdash;
 * that, is, dependent upon the implementation of this interface. It is up to the implementation
 * whether to accept {@code null} inputs for the operations or not, but whichever way it is,
 * it should be <i>consistent</i> and <i>documented</i>.
 *
 * @param   <T> the actual three-valued class; {@link Ternary} is provided as a primary Three-Valued implementation
 *
 * @author  Corinne Buxton
 */
public interface ThreeValuedContext<T extends org.cb2384.corutils.ternary.ThreeValued> {
    
    /**
     * A logical not or negation within in this context.
     *
     * @param   value   the {@link org.cb2384.corutils.ternary.ThreeValued} value to negate
     *
     * @return  the 3VL equivalent of {@code !value} for this context
     */
    @Pure
    @NonNull T not(T value);
    
    /**
     * A logical and within the rules of this context.
     *
     * @param   left    the left-hand {@link org.cb2384.corutils.ternary.ThreeValued} value
     *
     * @param   right   the right-hand {@link org.cb2384.corutils.ternary.ThreeValued} value
     *
     * @return  the 3VL equivalent of {@code left & right}
     */
    @Pure
    @NonNull T and(T left, T right);
    
    /**
     * <p>A logical nand within the rules of this context.</p>
     *
     * <p>Implementation Note:  The default implementation calls {@link #not not(}{@link #and and(}{@code
     *                          left, right}{@link #and )}{@link #not )}; it should be overridden should
     *                          different behaviour be desired.</p>
     *
     * @param   left    the left-hand {@link org.cb2384.corutils.ternary.ThreeValued} value
     *
     * @param   right   the right-hand {@link org.cb2384.corutils.ternary.ThreeValued} value
     *
     * @return  the 3VL equivalent of {@code !(left & right)}
     */
    @Pure
    default @NonNull T nand(
            T left,
            T right
    ) {
        return not(and(left, right));
    }
    
    /**
     * A logical or within the rules of this context.
     *
     * @param   left    the left-hand {@link org.cb2384.corutils.ternary.ThreeValued} value
     *
     * @param   right   the right-hand {@link org.cb2384.corutils.ternary.ThreeValued} value
     *
     * @return  the 3VL equivalent of {@code left | right}
     */
    @Pure
    @NonNull T or(T left, T right);
    
    /**
     * <p>A logical nor within the rules of this context.</p>
     *
     * <p>Implementation Note:  The default implementation calls {@link #not not(}{@link #or or(}{@code
     *                          left, right}{@link #or )}{@link #not )}; it should be overridden
     *                          should different behaviour be desired.</p>
     *
     * @param   left    the left-hand {@link org.cb2384.corutils.ternary.ThreeValued} value
     *
     * @param   right   the right-hand {@link org.cb2384.corutils.ternary.ThreeValued} value
     *
     * @return  the 3VL equivalent of {@code !(left | right)}
     */
    @Pure
    default @NonNull T nor(
            T left,
            T right
    ) {
        return not(or(left, right));
    }
    
    /**
     * A logical xor within the rules of this context.
     *
     * @param   left    the left-hand {@link org.cb2384.corutils.ternary.ThreeValued} value
     *
     * @param   right   the right-hand {@link org.cb2384.corutils.ternary.ThreeValued} value
     *
     * @return  the 3VL equivalent of {@code left ^ right}
     */
    @Pure
    @NonNull T xor(T left, T right);
    
    /**
     * <p>A logical xnor within the rules of this context.</p>
     *
     * <p>Implementation Note:  The default implementation calls {@link #not not(}{@link #xor xor(}{@code
     *                          left, right}{@link #xor )}{@link #not )};
     *                          it should be overridden should different behaviour be desired.</p>
     *
     * @param   left    the left-hand {@link org.cb2384.corutils.ternary.ThreeValued} value
     *
     * @param   right   the right-hand {@link org.cb2384.corutils.ternary.ThreeValued} value
     *
     * @return  the 3VL equivalent of {@code !(left ^ right)}
     */
    @Pure
    default @NonNull T xnor(
            T left,
            T right
    ) {
        return not(xor(left, right));
    }
    
    /**
     * A logical implication operation within the rules of this context.
     *
     * @param   precondition    the left-hand {@link org.cb2384.corutils.ternary.ThreeValued} value
     *
     * @param   postcondition   the right-hand {@link org.cb2384.corutils.ternary.ThreeValued} value
     *
     * @return  the 3VL equivalent of {@code precondition -> postcondition}, where {@code ->}
     *          is a mathematical implication operation (if-then)
     */
    @Pure
    @NonNull T implies(T precondition, T postcondition);
    
    /**
     * A logical prevention ({@code P -> !Q}) within the rules of this context.
     *
     * @param   precondition    the left-hand {@link org.cb2384.corutils.ternary.ThreeValued} value
     *
     * @param   postcondition   the right-hand {@link org.cb2384.corutils.ternary.ThreeValued} value
     *
     * @return  the 3VL equivalent of {@code precondition -> !postcondition}, where {@code ->}
     *          is a mathematical implication operation (if-then)
     */
    @Pure
    @NonNull T prevents(T precondition, T postcondition);
    
    /**
     * A logical iff ({@code <->}) within the rules of this context. In standard {@code boolean} logic,
     * this is equivalent to {@link #xnor}, but that is not necessarily the case in 3VL.
     *
     * @param   left    the left-hand {@link org.cb2384.corutils.ternary.ThreeValued} value
     *
     * @param   right   the right-hand {@link ThreeValued} value
     *
     * @return  the 3VL equivalent of {@code (left <-> right)}
     */
    @Pure
    @NonNull T iff(T left, T right);
}
