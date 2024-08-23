package org.cb2384.exactalgebra.text.opmanagement;

import org.cb2384.exactalgebra.objects.AlgebraObject;
import org.cb2384.exactalgebra.objects.numbers.AlgebraNumber;
import org.cb2384.exactalgebra.objects.numbers.integral.AlgebraInteger;
import org.cb2384.exactalgebra.objects.numbers.rational.Rational;
import org.cb2384.exactalgebra.objects.pair.RemainderPair;
import org.cb2384.exactalgebra.objects.relations.AlgebraFunction;
import org.cb2384.exactalgebra.objects.relations.AlgebraicRelation;
import org.cb2384.exactalgebra.objects.relations.polynomial.Polynomial;

import org.checkerframework.checker.nullness.qual.*;
import org.checkerframework.dataflow.qual.*;

public enum OpFlag {
    UNARY(1),
    UNARY_REVERSIBLE,
    SECOND_ARG_PRIM,
    SECOND_ARG_PRIM_OPTION,
    TRINARY(3),
    LAST_ARG_OPTIONAL,
    SECOND_LAST_ARG_OPTIONAL,
    QUATERNARY(4),
    TRUNCATING,
    ROUNDING,
    OUTPUT_INTEGER(AlgebraInteger.class),
    OUTPUT_RAT_NUM(Rational.class),
    OUTPUT_RAD_NUM(AlgebraNumber.class),
    OUTPUT_REAL_NUM(AlgebraNumber.class),
    OUTPUT_PAIR(RemainderPair.class),
    OUTPUT_POLY(Polynomial.class),
    OUTPUT_RATIONAL(AlgebraFunction.class),
    OUTPUT_RADICAL(AlgebraFunction.class),
    OUTPUT_REAL(AlgebraFunction.class),
    FUNCTIONAL(AlgebraFunction.class),
    RELATIONAL(AlgebraObject.class),
    UTILITY;
    
    private final Object supplement;
    
    OpFlag(
            Object supplement
    ) {
        this.supplement = supplement;
    }
    
    OpFlag() {
        supplement = null;
    }
    
    @Pure
    public final <T> @Nullable T supplemental() {
        return (T) supplement;
    }
    
    @Pure
    public static OpFlag outputFlagOf(
            AlgebraNumber value
    ) {
        return switch (value) {
            case AlgebraInteger ignored -> OpFlag.OUTPUT_INTEGER;
            case Rational ignored -> OpFlag.OUTPUT_RAT_NUM;
            default -> OpFlag.OUTPUT_REAL_NUM;
        };
    }
    
    @Pure
    public static OpFlag outputFlagOf(
            AlgebraicRelation<?> relation
    ) {
        return switch (relation) {
            case Polynomial<?> ignored -> OpFlag.OUTPUT_POLY;
            default -> OpFlag.OUTPUT_REAL;
        };
    }
    
    @Pure
    public static OpFlag coefficientFlagOf(
            AlgebraFunction<?, ?> function
    ) {
        Class<?> coeffClass = function.coefficientHighestRank();
        if (AlgebraInteger.class.isAssignableFrom(coeffClass)) {
            return OpFlag.OUTPUT_INTEGER;
        }
        if (Rational.class.isAssignableFrom(coeffClass)) {
            return OpFlag.OUTPUT_RAT_NUM;
        }
        return OpFlag.OUTPUT_REAL_NUM;
    }
}
