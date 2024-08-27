package org.cb2384.exactalgebra.objects.relations.polynomial;

import java.util.stream.Stream;

import org.cb2384.exactalgebra.objects.AlgebraObject;
import org.cb2384.exactalgebra.objects.AlgebraicRing;
import org.cb2384.exactalgebra.objects.numbers.AlgebraNumber;
import org.cb2384.exactalgebra.objects.pair.FunctionRemainderPair;
import org.cb2384.exactalgebra.objects.relations.AlgebraFunction;
import org.cb2384.exactalgebra.util.corutils.ternary.ComparableSwitchSignum;
import org.cb2384.exactalgebra.util.corutils.ternary.Signum;

import org.checkerframework.checker.index.qual.*;
import org.checkerframework.checker.nullness.qual.*;
import org.checkerframework.checker.signedness.qual.*;
import org.checkerframework.common.value.qual.*;
import org.checkerframework.dataflow.qual.*;

public interface Polynomial<N extends AlgebraNumber>
        extends AlgebraFunction<N, N>, AlgebraicRing<Polynomial<?>, Polynomial<?>>,
        AlgebraObject<Polynomial<?>>, ComparableSwitchSignum<Polynomial<?>>, Iterable<N> {
    
    @Pure
    @Positive int length();
    
    @Pure
    @NonNegative int degree();
    
    @SideEffectFree
    N constant();
    
    @SideEffectFree
    N leadCoefficient();
    
    @SideEffectFree
    N coefficientUS(@Unsigned short degree);
    
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
    
    @SideEffectFree
    String toString(
            String variable,
            @IntRange(from = Character.MIN_RADIX, to = Character.MAX_RADIX) int radix
    );
    
    @Pure
    default boolean isConstant() {
        return length() == 1;
    }
    
    @Pure
    boolean isMonomial();
    
    @Override
    @Pure
    default boolean equiv(
            Polynomial<?> that
    ) {
        return compareTo(that) == 0;
    }
    
    @Pure
    Signum compareDeg(Polynomial<?> that);
    
    @Override
    @Pure
    default Polynomial<?> max(
            Polynomial<?> that
    ) {
        return (compareTo(that) < 0)
                ? that
                : this;
    }
    
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
     * Multiply this by a constant.
     * @param scalar the constant to multiply this by
     * @return the product
     */
    @SideEffectFree
    Polynomial<? extends AlgebraNumber> scaled(AlgebraNumber scalar);
    
    @Override
    @SideEffectFree
    default FunctionRemainderPair<? extends Polynomial<?>, ? extends Polynomial<?>> quotientZWithRemainder(
            Polynomial<?> divisor
    ) {
        return new FunctionRemainderPair<>(quotientZ(divisor), remainder(divisor));
    }
    
    @SideEffectFree
    default Polynomial<N> squared() {
        return (Polynomial<N>) product(this);
    }
    
    /**
     * Raises this to the power of the given exponent.
     * @param exponent the UNSIGNED short exponent to raise this to
     * @return this to the power of exponent
     * @throws ArithmeticException if value would be too big
     */
    @SideEffectFree
    Polynomial<N> raisedUS(@Unsigned short exponent);
    
    @SideEffectFree
    Stream<N> stream();
    
    @SideEffectFree
    Stream<N> stream(@Nullable Integer startInclusive, @Nullable Integer endExclusive);
    
    @SideEffectFree
    Stream<N> parallelStream();
    
    @SideEffectFree
    Stream<N> parallelStream(@Nullable Integer startInclusive, @Nullable Integer endExclusive);
}
