package org.cb2384.exactalgebra.objects;

import org.checkerframework.checker.nullness.qual.*;
import org.checkerframework.common.value.qual.*;
import org.checkerframework.dataflow.qual.*;

/**
 * This interface represents an AlgebraInteger Object, considered to be an AlgebraInteger Ring with addition and multiplication
 * The ring is closed under all operations except, possibly, division.
 *
 * @param   <T> the type of the implementing class,
 *          as that will be the class that results and other arguments are in
 */
public interface AlgebraObject<T> {
    
    /**
     * Check if this object is {@code 0}, the additive identity.
     *
     * @return  {@code true} if this is {@code 0}, otherwise {@code false}
     */
    @Pure
    boolean isZero();
    
    /**
     * Check if this object is {@code 1}, the multiplicative identity
     *
     * @return  {@code true} if this is {@code 1}, otherwise {@code false}
     */
    @Pure
    boolean isOne();
    
    /**
     * Check if this object is negative; that is, less than {@code 0}.
     *
     * @return  {@code true} if this is strictly less than {@code 0}, otherwise {@code false}
     */
    @Pure
    boolean isNegative();
    
    /**
     * Checks if this is equivalent in value to {@code that}.
     * Unlike {@link Object#equals}, this function returns {@code true} if different
     * over-arching class-types are used, with different hashes. It only checks value-equality.
     * For objects with an order, this is always equivalent to {@link
     * org.cb2384.corutils.ternary.ComparableSwitchSignum#compareTo compareTo(}{@code that}{@link
     * org.cb2384.corutils.ternary.ComparableSwitchSignum#compareTo )&nbsp;}{@code == 0}.
     *
     * @param   that    the value to check against this
     *
     * @return  {@code true} if {@code this} and {@code that} have
     *          the same underlying value, otherwise {@code false}
     */
    @Pure
    boolean equiv(T that);
    
    @Override
    @SideEffectFree
    String toString();
    
    /**
     * Represents this AlgebraObject as a string, for the given {@code radix}
     * 
     * @param   radix   the radix for the representation 
     *
     * @return  a string representation of this
     * 
     * @see Character#forDigit 
     */
    @SideEffectFree
    String toString(@IntRange(from = Character.MIN_RADIX, to = Character.MAX_RADIX) int radix);
    
    /**
     * Represents this AlgebraObject as a string, with the given {@code radix} and
     * {@code variables} (to be used if this a functional or relational type of AlgebraObject).
     *
     * @implNote    The default implementation ignores the {@code variables} and simply calls {@link
     *              #toString(int) toString(}{@code radix}{@link #toString(int) )}; and should be overwritten
     *              if this type is actually to be represented with variables.
     *
     * @param   radix   the radix for the representation
     *
     * @param   variables   the variable/-s for this representation; ignored if there are
     *                      no variables used in this type
     *
     * @return  a string representation of this
     *
     * @see Character#forDigit
     */
    @SideEffectFree
    default String toString(
            @IntRange(from = Character.MIN_RADIX, to = Character.MAX_RADIX) int radix,
            String@Nullable... variables
    ) {
        return toString(radix);
    }
}
