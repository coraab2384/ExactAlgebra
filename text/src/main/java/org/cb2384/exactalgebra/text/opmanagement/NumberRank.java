package org.cb2384.exactalgebra.text.opmanagement;

import java.util.function.BiFunction;

import org.cb2384.exactalgebra.objects.numbers.AlgebraNumber;
import org.cb2384.exactalgebra.objects.numbers.integral.AlgebraInteger;
import org.cb2384.exactalgebra.objects.numbers.rational.Rational;

import org.checkerframework.checker.nullness.qual.*;
import org.checkerframework.dataflow.qual.*;

public enum NumberRank
        implements Rank<AlgebraNumber, NumberRank> {
    NATURAL_1(AlgebraInteger.class),
    NATURAL_0(AlgebraInteger.class),
    INTEGER(AlgebraInteger.class),
    RATIONAL(Rational.class),
    REAL_RADICAL(AlgebraNumber.class),
    REAL_TRANSCENDENTAL(AlgebraNumber.class),
    COMPLEX_INTEGRAL(AlgebraNumber.class),
    COMPLEX_RATIONAL(AlgebraNumber.class),
    COMPLEX_RADICAL(AlgebraNumber.class),
    COMPLEX_TRANSCENDENTAL(AlgebraNumber.class),
    COMPLEX_COMPLEX(AlgebraNumber.class);
    
    private final Class<? extends AlgebraNumber> resultingClass;
    
    @SideEffectFree
    NumberRank(
            Class<? extends AlgebraNumber> resultingClass
    ) {
        this.resultingClass = resultingClass;
    }
    
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
    
    @Override
    @Pure
    public Class<? extends AlgebraNumber> resultingClass() {
        return resultingClass;
    }
}
