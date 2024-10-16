package org.cb2384.exactalgebra.text.opmanagement;

import java.util.function.BiFunction;

import org.cb2384.exactalgebra.objects.AlgebraObject;
import org.cb2384.exactalgebra.objects.numbers.AlgebraNumber;
import org.cb2384.exactalgebra.objects.numbers.integral.AlgebraInteger;
import org.cb2384.exactalgebra.objects.numbers.rational.Rational;

import org.checkerframework.checker.nullness.qual.*;
import org.checkerframework.dataflow.qual.*;

/**
 * <p>How complicated a number is, or how for removed from simple counting it is</p>
 *
 * @author Corinne Buxton
 */
public enum NumberRank
        implements Rank<AlgebraNumber, NumberRank> {
    /**
     * Natural numbers not including 0
     */
    NATURAL_1(AlgebraInteger.class),
    /**
     * Natural numbers including 0
     */
    NATURAL_0(AlgebraInteger.class),
    /**
     * Positive and negative integers
     */
    INTEGER(AlgebraInteger.class),
    /**
     * Rational numbers
     */
    RATIONAL(Rational.class),
    /**
     * Rational numbers to the power of rational exponents (the result of which may not themselves
     * be rational)
     */
    REAL_RADICAL(AlgebraNumber.class),
    /**
     * All real numbers
     */
    REAL_TRANSCENDENTAL(AlgebraNumber.class),
    /**
     * Complex numbers whose real and imaginary parts are both integral
     */
    COMPLEX_INTEGRAL(AlgebraNumber.class),
    /**
     * Complex numbers whose real and imaginary parts are both rational
     */
    COMPLEX_RATIONAL(AlgebraNumber.class),
    /**
     * Complex numbers whose real and imaginary parts are both rationals to rational powers, or radical
     */
    COMPLEX_RADICAL(AlgebraNumber.class),
    /**
     * Complex numbers whose real and imaginary parts are simply real
     */
    COMPLEX_TRANSCENDENTAL(AlgebraNumber.class),
    /**
     * Miscellaneous for outputs that would transcend complex, such as perhaps quaternions (not implemented)
     */
    COMPLEX_COMPLEX(AlgebraNumber.class);
    
    static final BiFunction<@Nullable NumberRank, @Nullable NumberRank, NumberRank> Q_VALUED_RANK_MAP
            = NumberRank.RATIONAL.constantReturningThis();
    
    static final BiFunction<NumberRank, NumberRank, NumberRank> POWER_Z_REM_MAP = (left, right) -> switch (right) {
        case NATURAL_1, NATURAL_0 -> left;
        case INTEGER -> left.ceiling(RATIONAL);
        case RATIONAL -> left.ceiling(REAL_RADICAL);
        case REAL_RADICAL, REAL_TRANSCENDENTAL -> left.ceiling(REAL_TRANSCENDENTAL);
        case COMPLEX_INTEGRAL, COMPLEX_RATIONAL -> left.ceiling(COMPLEX_RADICAL);
        case COMPLEX_RADICAL, COMPLEX_TRANSCENDENTAL -> left.ceiling(COMPLEX_TRANSCENDENTAL);
        case COMPLEX_COMPLEX -> COMPLEX_COMPLEX;
    };
    
    static final BiFunction<NumberRank, NumberRank, NumberRank> POWER_Q_REM_MAP = (left, right) -> switch (right) {
        default -> left.ceiling(RATIONAL);
        case RATIONAL -> left.ceiling(REAL_RADICAL);
        case REAL_RADICAL, REAL_TRANSCENDENTAL -> left.ceiling(REAL_TRANSCENDENTAL);
        case COMPLEX_INTEGRAL, COMPLEX_RATIONAL -> left.ceiling(COMPLEX_RADICAL);
        case COMPLEX_RADICAL, COMPLEX_TRANSCENDENTAL -> left.ceiling(COMPLEX_TRANSCENDENTAL);
        case COMPLEX_COMPLEX -> COMPLEX_COMPLEX;
    };
    
    private final Class<? extends AlgebraNumber> resultingClass;
    
    @SideEffectFree
    NumberRank(
            Class<? extends AlgebraNumber> resultingClass
    ) {
        this.resultingClass = resultingClass;
    }
    
    /**
     * Maps a value to its rank
     *
     * @param value the number to get a rank for
     *
     * @return  the lowest rank of the appropriate type which covers {@code value}
     */
    @Pure
    public static NumberRank rankOf(
            AlgebraNumber value
    ) {
        return switch (value) {
            case AlgebraInteger ignored -> switch (value.signum()) {
                case POSITIVE -> NATURAL_1;
                case ZERO -> NATURAL_0;
                case NEGATIVE -> INTEGER;
            };
            case Rational ignored -> RATIONAL;
            default -> throw new IllegalArgumentException("not yet implemented");
        };
    }
    
    @Pure
    static NumberRank rankOf(
            Class<? extends AlgebraNumber> clazz
    ) {
        if (AlgebraInteger.class.isAssignableFrom(clazz)) {
            return INTEGER;
        }
        if (Rational.class.isAssignableFrom(clazz)) {
            return RATIONAL;
        }
        throw new IllegalArgumentException("not yet implemented");
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Pure
    public Class<? extends AlgebraNumber> resultingClass() {
        return resultingClass;
    }
}
