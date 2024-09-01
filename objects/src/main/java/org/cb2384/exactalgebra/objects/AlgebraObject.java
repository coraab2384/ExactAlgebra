package org.cb2384.exactalgebra.objects;

import org.checkerframework.checker.nullness.qual.*;
import org.checkerframework.common.value.qual.*;
import org.checkerframework.dataflow.qual.*;

/**
 * <p>This interface represents an algebraic object. In this module, all algebraic objects can be asked about
 * sign, zero-ness, and one-ness, and can take a radix argument with their {@link #toString()} method.</p>
 *
 * @param <T>   the type of the implementing class,
 *              as that will be the class that results and other arguments are in
 *
 * @author  Corinne Buxton
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
     * Unlike {@link Object#equals equals}, this function returns {@code true} if different
     * over-arching class-types are used, with different hashes. It only checks value-equality.
     *
     * @implSpec    For objects with an order, this result shall be equivalent to {@link
     *              org.cb2384.exactalgebra.util.corutils.ternary.ComparableSwitchSignum#compareTo
     *              compareTo(}{@code that}{@link
     *              org.cb2384.exactalgebra.util.corutils.ternary.ComparableSwitchSignum#compareTo
     *              )&nbsp;}{@code == 0}.
     *
     * @param   that    the value to check against this
     *
     * @return  {@code true} if {@code this} and {@code that} have
     *          the same underlying value, otherwise {@code false}
     */
    @Pure
    boolean equiv(T that);
    
    /**
     * Yield this object in string form, with a default radix of 10
     *
     * @implSpec    This shall be equivalent to {@link #toString(int) toString(}{@code 10}{@link #toString(int) )}.
     *
     * @return  a string representing the value of this object
     */
    @Override
    @SideEffectFree
    String toString();
    
    /**
     * Represents this object as a string, for the given {@code radix}
     * 
     * @param radix the radix for the representation
     *
     * @return  a string representation of this
     *
     * @throws NumberFormatException    if {@code radix < }{@link Character#MIN_RADIX} or
     *                                  {@code radix > }{@link Character#MAX_RADIX}
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
     * @param radix     the radix for the representation
     * @param variables the variable/-s for this representation; ignored if there are
     *                  no variables used in this type
     *
     * @return  a string representation of this
     *
     * @throws NumberFormatException    if {@code radix < }{@link Character#MIN_RADIX} or
     *                                  {@code radix > }{@link Character#MAX_RADIX}
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
