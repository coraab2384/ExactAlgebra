package org.cb2384.exactalgebra.objects.relations.polynomial;

import java.util.stream.Stream;

import org.cb2384.exactalgebra.objects.AlgebraObject;
import org.cb2384.exactalgebra.objects.AlgebraicRing;
import org.cb2384.exactalgebra.objects.numbers.AlgebraNumber;
import org.cb2384.exactalgebra.objects.numbers.integral.AlgebraInteger;
import org.cb2384.exactalgebra.objects.pair.FunctionRemainderPair;
import org.cb2384.exactalgebra.objects.relations.AlgebraFunction;
import org.cb2384.exactalgebra.util.corutils.ternary.ComparableSwitchSignum;
import org.cb2384.exactalgebra.util.corutils.ternary.Signum;

import org.checkerframework.checker.index.qual.*;
import org.checkerframework.checker.nullness.qual.*;
import org.checkerframework.checker.signedness.qual.*;
import org.checkerframework.common.value.qual.*;
import org.checkerframework.dataflow.qual.*;

/**
 * <p>Polynomials with coefficients of AlgebraNumbers</p>
 *
 * <p>Throws:&ensp;{@link NullPointerException} &ndash; on any {@code null} argument,
 * unless otherwise specified</p>
 *
 * @param <N>   the type of the coefficients, which also determines the type of the output if the input
 *              is also of this type
 *
 * @author  Corinne Buxton
 */
public interface Polynomial<N extends AlgebraNumber>
        extends AlgebraFunction<N, N>, AlgebraicRing<Polynomial<?>, Polynomial<?>>,
        AlgebraObject<Polynomial<?>>, ComparableSwitchSignum<Polynomial<?>>, Iterable<N> {
    
    /**
     * Yields the length of this Polynomial. This length does include non-leading 0-terms!
     *
     * @return  the length of this Polynomial, which is the number of terms if coefficients of 0 are still written out
     */
    @Pure
    @Positive int length();
    
    /**
     * Yields the highest degree of this Polynomial; the degree of the leading term
     *
     * @return  the degree of the leading term of this Polynomial
     */
    @Pure
    @NonNegative int degree();
    
    /**
     * Returns the constant term, the term of degree 0.
     *
     * @implNote    The default implementation simply calls {@link #coefficient(int) coefficient(}{@code
     *              0}{@link #coefficient(int) )}.
     *
     * @return  the constant term, which is the final term when written in normal descending order
     */
    @SideEffectFree
    default N constant() {
        return coefficient(0);
    }
    
    /**
     * Returns the leading term's coefficient, the coefficient from the highest-degree term.
     *
     * @implNote    The default implementation simply calls {@link #coefficient(int) coefficient(}{@link
     *              #degree()}{@link #coefficient(int) )}.
     *
     * @return  the leading coefficient, which is the first term when written in normal descending order
     */
    @SideEffectFree
    default N leadCoefficient() {
        return coefficient(degree());
    }
    
    /**
     * Like {@link #coefficient(int)}, but accepting an <em>unsigned</em> short instead. Some implementations limit
     * the amount of terms to within the bounds of an unsigned short, and using them means there is no possibility
     * of negative index errors. However, since most would not, this is a default method.
     *
     * @implNote    The default implementation simply calls {@link #coefficient(int) coefficient(}{@link
     *              Short#toUnsignedInt(short) Short.toUnsignedInt(}{@code degree}{@link Short#toUnsignedInt(short)
     *              )}{@link #coefficient(int) )}.
     *
     * @param degree    the degree of the coefficient to return
     *
     * @return  the coefficient from the term with degree {@code degree}
     *
     * @throws IllegalArgumentException if {@code degree < 0}
     */
    @SideEffectFree
    default N coefficientUS(
            @Unsigned short degree
    ) {
        return coefficient(Short.toUnsignedInt(degree));
    }
    
    /**
     * Gets the coefficient for the term with the indicated degree
     *
     * @param degree    the degree of the coefficient to return
     *
     * @return  the coefficient from the term with degree {@code degree}
     *
     * @throws IllegalArgumentException if {@code degree < 0}
     */
    @SideEffectFree
    N coefficient(@NonNegative int degree);
    
    /**
     * {@inheritDoc}
     *
     * @implNote    This default implementation calls {@link #toString(String, int) toString(}{@code
     *              variables[0], radix}{@link #toString(String, int) )} if there is at least one variable
     *              given for {@code variables}, and otherwise calls {@link #toString(int) toString(}{@code
     *              radix}{@link #toString(int) )}. Any extra arguments in {@code variables} after the first
     *              are ignored.
     *
     * @throws NumberFormatException    if {@code radix < }{@link Character#MIN_RADIX} or {@code radix > }{@link
     *                                  Character#MAX_RADIX}
     */
    @Override
    @SideEffectFree
    default String toString(
            @IntRange(from = Character.MIN_RADIX, to = Character.MAX_RADIX) int radix,
            String@Nullable... variables
    ) {
        return ((variables == null) || (variables.length == 0))
                ? toString(radix)
                : toString(variables[0], radix);
    }
    
    /**
     * Represents this Polynomial as a string, with the given {@code radix} and
     * {@code variable}
     *
     * @param variable  the variable for this representation
     * @param radix     the radix for the representation
     *
     * @return  a string representation of this
     *
     * @throws NumberFormatException    if {@code radix < }{@link Character#MIN_RADIX} or
     *                                  {@code radix > }{@link Character#MAX_RADIX}
     */
    @SideEffectFree
    String toString(
            String variable,
            @IntRange(from = Character.MIN_RADIX, to = Character.MAX_RADIX) int radix
    );
    
    /**
     * Checks if this Polynomial is constant, and it does not contain a variable.
     *
     * @implNote    The default implementation simply checks {@link #length()}<code> == 1</code>.
     *
     * @return  {@code true} if this is constant, otherwise {@code false}
     */
    @Pure
    default boolean isConstant() {
        return length() == 1;
    }
    
    /**
     * Checks if this Polynomial is a monomial, one which only has one non-zero term.
     *
     * @return  {@code true} if this is constant, otherwise {@code false}
     */
    @Pure
    boolean isMonomial();
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Pure
    default boolean equiv(
            Polynomial<?> that
    ) {
        return compareTo(that) == 0;
    }
    
    /**
     * Compares only the degrees of this and {@code that}.
     *
     * @param that  the polynomial to compare against this
     *
     * @return  {@link Signum#POSITIVE} if this has a higher degree,
     *          {@link Signum#ZERO} if these have the same degree,
     *          or {@link Signum#NEGATIVE} if {@code that} has a higher degree
     */
    @Pure
    Signum compareDeg(Polynomial<?> that);
    
    /**
     * Compares this and another Polynomial; two Polynomials are compared first by their lengths
     * (see {@link #compareDeg(Polynomial)}), and if that is equal, are then compared coefficient-for-coefficient
     * starting from the leading term.
     *
     * @param o the function to be compared
     *
     * @return  a negative integer, zero, or a positive integer as this is less than, equal to,
     *          or greater than {@code o}
     */
    @Override
    @Pure
    int compareTo(Polynomial<?> o);
    
    /**
     * Yields the larger of these values, as if {@link Math#max max(}{@code this, that}{@link Math#max )}.
     * The comparison is done using the default comparator ({@link #compareTo}).
     *
     * @implNote    The default implementation simply returns {@code (}{@link #compareTo compareTo(}{@code
     *              that}{@link #compareTo )&nbsp}{@code < 0) ? that : this}.
     *
     * @param that  the value to check against this
     *
     * @return  the larger of this and {@code that}
     */
    @Override
    @Pure
    default Polynomial<?> max(
            Polynomial<?> that
    ) {
        return (compareTo(that) < 0)
                ? that
                : this;
    }
    
    /**
     * Yields the smaller of these values, as if {@link Math#min min(}{@code this, that}{@link Math#min )}.
     * The comparison is done using the default comparator ({@link #compareTo}).
     *
     * @implNote    The default implementation simply calls {@code (}{@link #compareTo compareTo(}{@code
     *              that}{@link #compareTo )&nbsp;}{@code > 0) ? that : this}.
     *
     * @param that  the value to check against this
     *
     * @return  the smaller of this and {@code that}
     */
    @Override
    @Pure
    default Polynomial<?> min(
            Polynomial<?> that
    ) {
        return (compareTo(that) < 0)
                ? this
                : that;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @SideEffectFree
    Polynomial<N> negated();
    
    /**
     * Take the absolute value of this polynomial, turning all coefficients positive.
     *
     * @return  a new Polynomial with all coefficients being the absolute value of their former values
     */
    @SideEffectFree
    Polynomial<N> absoluteValue();
    
    /**
     * Multiply this by a constant.
     *
     * @param scalar    the constant to multiply this by
     *
     * @return the product
     */
    @SideEffectFree
    Polynomial<? extends AlgebraNumber> scaled(AlgebraNumber scalar);
    
    /**
     * {@inheritDoc}
     */
    @Override
    @SideEffectFree
    Polynomial<?> sum(Polynomial<?> augend);
    
    /**
     * {@inheritDoc}
     */
    @Override
    @SideEffectFree
    Polynomial<?> difference(Polynomial<?> subtrahend);
    
    /**
     * {@inheritDoc}
     *
     * @throws ArithmeticException  possibly, if the result would be too big
     */
    @Override
    @SideEffectFree
    Polynomial<?> product(Polynomial<?> multiplicand);
    
    /**
     * {@inheritDoc}
     *
     * @implNote    The default implementation calls both {@link #quotientZ(Polynomial)} and
     *              {@link #remainder(Polynomial)} and sticks them in the pair object. However, some implementations
     *              use this method to find the remainder; they must override this current one.
     *
     * @throws ArithmeticException  if {@code divisor} is a constant 0 Polynomial
     */
    @Override
    @SideEffectFree
    default FunctionRemainderPair<? extends Polynomial<?>, ? extends Polynomial<?>> quotientZWithRemainder(
            Polynomial<?> divisor
    ) {
        return new FunctionRemainderPair<>(quotientZ(divisor), remainder(divisor));
    }
    
    /**
     * {@inheritDoc}
     *
     * @throws ArithmeticException  if {@code divisor} is a constant 0 Polynomial
     */
    @Override
    @SideEffectFree
    Polynomial<?> quotientZ(Polynomial<?> divisor);
    
    /**
     * {@inheritDoc}
     *
     * @throws ArithmeticException  if {@code divisor} is a constant 0 Polynomial
     */
    @Override
    @SideEffectFree
    Polynomial<?> remainder(Polynomial<?> divisor);
    
    /**
     * {@inheritDoc}
     */
    @SideEffectFree
    default Polynomial<N> squared() {
        return (Polynomial<N>) product(this);
    }
    
    /**
     * Raises this to the power of the given exponent.
     *
     * @param exponent  the <em>unsigned</em> {@code short} exponent to raise this to
     *
     * @return  this to the power of {@code exponent}
     *
     * @throws ArithmeticException  possibly, if the result would be too big
     */
    @SideEffectFree
    Polynomial<?> raisedUS(@Unsigned short exponent);
    
    /**
     * {@inheritDoc}
     */
    @Override
    @SideEffectFree
    Polynomial<?> raisedZ(int exponent);
    
    /**
     * {@inheritDoc}
     */
    @Override
    @SideEffectFree
    Polynomial<?> raisedZ(AlgebraInteger exponent);
    
    /**
     * Builds a stream from the coefficients of this Polynomial, in big-endian order.
     *
     * @return  a sequential stream of the coefficients of this Polynomial, from constant term to lead term
     */
    @SideEffectFree
    Stream<N> stream();
    
    /**
     * Builds a stream from the coefficients of this Polynomial, in big-endian order, starting from
     * the term with degree {@code startInclusive} and going up until the term with degree {@code endExclusive}.
     * While the first argument must be positive, the second can be negative; in such a case,
     * {@code endExclusive = -n} is taken to mean that {@code endExclusive = }{@link #length()}<code> - n</code>.
     *
     * @param startInclusive    the starting point, inclusive, which defaults to 0 if {@code null}
     * @param endExclusive      the ending point, exclusive, which defaults to the length of the polynomial if
     *                          {@code null}
     *
     * @return  a sequential stream of the coefficients of this Polynomial, from constant term to lead term
     *
     * @throws IllegalArgumentException if {@code startInclusive < 0} or {@code startInclusive > endExclusive},
     *                                  or if {@code endExclusive < 0} while {@code startInclusive > }{@link
     *                                  #length()}<code> + endExclusive</code>
     */
    @SideEffectFree
    Stream<N> stream(@Nullable Integer startInclusive, @Nullable Integer endExclusive);
    
    /**
     * Builds a stream from the coefficients of this Polynomial, in big-endian order. The stream
     * is parallel, though, so the order shouldn't need to be too important.
     *
     * @return  a parallel stream of the coefficients of this Polynomial, from constant term to lead term
     */
    @SideEffectFree
    Stream<N> parallelStream();
    
    /**
     * Builds a stream from the coefficients of this Polynomial, in big-endian order, starting from
     * the term with degree {@code startInclusive} and going up until the term with degree {@code endExclusive}.
     * The stream is parallel, though, so the order shouldn't need to be too important. While the first argument
     * must be positive, the second can be negative; in such a case, {@code endExclusive = -n} is taken to mean
     * that {@code endExclusive = }{@link #length()}<code> - n</code>.
     *
     * @param startInclusive    the starting point, inclusive, which defaults to 0 if {@code null}
     * @param endExclusive      the ending point, exclusive, which defaults to the length of the polynomial if
     *                          {@code null}
     *
     * @return  a parallel stream of the coefficients of this Polynomial, from constant term to lead term
     *
     * @throws IllegalArgumentException if {@code startInclusive < 0} or {@code startInclusive > endExclusive},
     *                                  or if {@code endExclusive < 0} while {@code startInclusive > }{@link
     *                                  #length()}<code> + endExclusive</code>
     */
    @SideEffectFree
    Stream<N> parallelStream(@Nullable Integer startInclusive, @Nullable Integer endExclusive);
}
