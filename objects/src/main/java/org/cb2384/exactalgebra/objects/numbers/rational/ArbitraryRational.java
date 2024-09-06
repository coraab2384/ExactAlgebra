package org.cb2384.exactalgebra.objects.numbers.rational;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigInteger;

import org.cb2384.exactalgebra.objects.numbers.integral.IntegerFactory;
import org.cb2384.exactalgebra.util.BigMathObjectUtils;
import org.cb2384.exactalgebra.util.PrimMathUtils.IntegralBoundaryTypes;

import org.checkerframework.dataflow.qual.*;

/**
 * <p>Rationals with an arbitrary length (technically bounded by {@link #MAX_PRECISION} <em>digits</em>,
 * as that is the max size of one of the {@link BigInteger} numerator or denominators, but that's huge).
 * Each one is essential a BigInteger numerator and denominator.</p>
 *
 * <p>Throws:&ensp;{@link NullPointerException} &ndash; on any {@code null} input unless otherwise noted</p>
 *
 * @author  Corinne Buxton
 */
public final class ArbitraryRational
        extends AbstractRational
        implements Serializable {
    
    /**
     * serialVersionUID
     */
    @Serial
    private static final long serialVersionUID = 0x5E6EF3718CB341E4L;
    
    /**
     * the numerator
     */
    private final BigInteger numerator;
    
    /**
     * the denominator
     */
    private final BigInteger denominator;
    
    /**
     * Simply places the numerator and denominator in the appropriate fields.
     *
     * @param numerator     the numerator
     * @param denominator   the denominator
     */
    @SideEffectFree
    ArbitraryRational(
            BigInteger numerator,
            BigInteger denominator
    ) {
        assert denominator.signum() == 1;
        this.numerator = numerator;
        this.denominator = denominator;
    }
    
    /**
     * Constructs a {@link Rational} from the given numerator and denominator. This value
     * will be automatically simplified, and that may mean that the runtime returned time is in fact a
     * {@link FiniteRational} or even some flavor of {@link
     * org.cb2384.exactalgebra.objects.numbers.integral.AlgebraInteger}.
     *
     * @param numerator     the numerator to make the Rational from
     * @param denominator   the denominator to make the Rational from
     *
     * @return  a Rational representing a value equal to {@code numerator} over {@code denominator}
     */
    @SideEffectFree
    public static Rational valueOf(
            BigInteger numerator,
            BigInteger denominator
    ) {
        if (BigMathObjectUtils.isZero(denominator)) {
            throw new ArithmeticException(DIV_0_EXC_MSG);
        }
        
        BigInteger gcf = numerator.gcd(denominator);
        numerator = numerator.divide(gcf);
        denominator = denominator.divide(gcf);
        
        if (BigMathObjectUtils.isOne(denominator)) {
            return IntegerFactory.fromBigInteger(numerator);
        }
        if (denominator.negate().equals(BigInteger.ONE)) {
            return IntegerFactory.fromBigInteger(numerator.negate());
        }
        
        if (BigMathObjectUtils.canBeInt(denominator, IntegralBoundaryTypes.EXTENDED)
                && BigMathObjectUtils.canBeInt(numerator.abs(), IntegralBoundaryTypes.UNSIGNED)) {
            return BigMathObjectUtils.isNegative(numerator)
                    ? new FiniteRational(-numerator.longValue(), -denominator.longValue())
                    : new FiniteRational(numerator.longValue(), denominator.longValue());
        }
        
        if (BigMathObjectUtils.isNegative(denominator)) {
            numerator = numerator.negate();
            denominator = denominator.negate();
        }
        return new ArbitraryRational(numerator, denominator);
    }
    
    /**
     * Constructs an ArbitraryRational from the given numerator and denominator. This value
     * will be simplified, but not to the point that it might be a different Rational implementation.
     *
     * @param numerator     the numerator to make the ArbitraryRational from
     * @param denominator   the denominator to make the ArbitraryRational from
     *
     * @return  an ArbitraryRational representing a value equal to {@code numerator} over {@code denominator}
     */
    @SideEffectFree
    public static ArbitraryRational valueOfStrict(
            BigInteger numerator,
            BigInteger denominator
    ) {
        switch (denominator.signum()) {
            case 0:
                throw new ArithmeticException(DIV_0_EXC_MSG);
            
            case -1:
                numerator = numerator.negate();
                denominator = denominator.negate();
        }
        
        BigInteger gcf = numerator.gcd(denominator);
        numerator = numerator.divide(gcf);
        denominator = denominator.divide(gcf);
        
        return new ArbitraryRational(numerator, denominator);
    }
    
    /**
     * Constructs a {@link Rational} from the given numerator and denominator. This value
     * will be automatically simplified, and that may mean that the runtime returned time is in fact a
     * {@link FiniteRational} or even some flavor of {@link
     * org.cb2384.exactalgebra.objects.numbers.integral.AlgebraInteger}.
     *
     * @param numerator     the numerator to make the Rational from
     * @param denominator   the denominator to make the Rational from
     *
     * @return  a Rational representing a value equal to {@code numerator} over {@code denominator}
     */
    @SideEffectFree
    public static Rational valueOf(
            long numerator,
            long denominator
    ) {
        return FiniteRational.valueOf(numerator, denominator);
    }
    
    /**
     * Constructs an ArbitraryRational from the given numerator and denominator. This value
     * will be simplified, but not to the point that it might be a different Rational implementation.
     *
     * @param numerator     the numerator to make the ArbitraryRational from
     * @param denominator   the denominator to make the ArbitraryRational from
     *
     * @return  an ArbitraryRational representing a value equal to {@code numerator} over {@code denominator}
     */
    @SideEffectFree
    public static ArbitraryRational valueOfStrict(
            long numerator,
            long denominator
    ) {
        return valueOfStrict(BigInteger.valueOf(numerator), BigInteger.valueOf(denominator));
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Pure
    public BigInteger numeratorBI() {
        return numerator;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Pure
    public BigInteger denominatorBI() {
        return denominator;
    }
}
