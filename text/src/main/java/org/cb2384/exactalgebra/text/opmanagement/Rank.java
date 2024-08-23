package org.cb2384.exactalgebra.text.opmanagement;

import java.io.Serializable;
import java.util.function.BiFunction;

import org.cb2384.exactalgebra.objects.AlgebraObject;
import org.cb2384.exactalgebra.objects.numbers.AlgebraNumber;
import org.cb2384.exactalgebra.objects.pair.RemainderPair;
import org.cb2384.exactalgebra.objects.relations.AlgebraFunction;
import org.cb2384.exactalgebra.util.corutils.ternary.ComparableSwitchSignum;
import org.cb2384.exactalgebra.util.corutils.ternary.Signum;

import org.checkerframework.checker.nullness.qual.*;
import org.checkerframework.dataflow.qual.*;

public sealed interface Rank<T extends AlgebraObject<T>, R extends Rank<T, R>>
        extends ComparableSwitchSignum<R>, Serializable
        permits FunctionRank, NumberRank, PairRank {
    
    @Pure
    default R ceiling(
            R that
    ) {
        return (compareTo(that) > 0) ? (R) this : that;
    }
    
    @Pure
    Class<?> resultingClass();
    
    @SideEffectFree
    static <T extends AlgebraObject<T>> Rank<T, ?> rankOf(
            T value
    ) {
        return switch (value) {
            case AlgebraNumber valAN -> (Rank<T, ?>) NumberRank.rankOf(valAN);
            case AlgebraFunction<?, ?> valAF -> (Rank<T, ?>) FunctionRank.rankOf(valAF);
            //case AlgebraicRelation<?> valAR -> RelationRank.rankOf(valAR);
            case RemainderPair<?, ?, ?, ?> valRP -> (Rank<T, ?>) PairRank.rankOf(valRP);
            default -> throw new IllegalArgumentException("not yet implemented");
        };
    }
    
    @SideEffectFree
    default BiFunction<@Nullable R, @Nullable R, R> constantReturningThis() {
        R thisR = (R) this;
        return (left, right) -> thisR;
    }
    
    @SideEffectFree
    default BiFunction<R, @PolyNull R, R> compareToThisAndThen(
            boolean useSecondArgCeiling,
            Signum compResultForFirstOption,
            @Nullable R firstOption,
            @Nullable R secondOption
    ) {
        if (firstOption == null) {
            assert secondOption != null;
            return switch (compResultForFirstOption) {
                case POSITIVE:
                    if (useSecondArgCeiling) {
                        yield (left, right) -> {
                            R ceiling = left.ceiling(right);
                            return (compareTo(ceiling) < 0) ? ceiling : secondOption;
                        };
                    }
                    yield (R left, @Nullable R right) -> (compareTo(left) < 0) ? left : secondOption;
                    
                case ZERO:
                    if (useSecondArgCeiling) {
                        yield (left, right) -> {
                            R ceiling = left.ceiling(right);
                            return (compareTo(ceiling) == 0) ? ceiling : secondOption;
                        };
                    }
                    yield (R left, @Nullable R right) -> (compareTo(left) == 0) ? left : secondOption;
                    
                case NEGATIVE:
                    if (useSecondArgCeiling) {
                        yield (left, right) -> {
                            R ceiling = left.ceiling(right);
                            return (compareTo(ceiling) > 0) ? ceiling : secondOption;
                        };
                    }
                    yield (R left, @Nullable R right) -> (compareTo(left) > 0) ? left : secondOption;
            };
        }
        
        if (secondOption == null) {
            return switch (compResultForFirstOption) {
                case POSITIVE:
                    if (useSecondArgCeiling) {
                        yield (left, right) -> {
                            R ceiling = left.ceiling(right);
                            return (compareTo(ceiling) < 0) ? firstOption : ceiling;
                        };
                    }
                    yield (R left, @Nullable R right) -> (compareTo(left) < 0) ? firstOption : left;
                
                case ZERO:
                    if (useSecondArgCeiling) {
                        yield (left, right) -> {
                            R ceiling = left.ceiling(right);
                            return (compareTo(ceiling) == 0) ? firstOption : ceiling;
                        };
                    }
                    yield (R left, @Nullable R right) -> (compareTo(left) == 0) ? firstOption : left;
                
                case NEGATIVE:
                    if (useSecondArgCeiling) {
                        yield (left, right) -> {
                            R ceiling = left.ceiling(right);
                            return (compareTo(ceiling) > 0) ? firstOption : ceiling;
                        };
                    }
                    yield (R left, @Nullable R right) -> (compareTo(left) > 0) ? firstOption : left;
            };
        }
        
        return switch (compResultForFirstOption) {
            case POSITIVE -> (useSecondArgCeiling)
                    ? (left, right) -> (compareTo( left.ceiling(right) ) < 0) ? firstOption : secondOption
                    : (R left, @Nullable R right) -> (compareTo(left) < 0) ? firstOption : secondOption;
            case ZERO -> useSecondArgCeiling
                    ? (left, right) -> (compareTo( left.ceiling(right) ) == 0) ? firstOption : secondOption
                    : (R left, @Nullable R right) -> (compareTo(left) == 0) ? firstOption : secondOption;
            case NEGATIVE -> useSecondArgCeiling
                    ? (left, right) -> (compareTo( left.ceiling(right) ) > 0) ? firstOption : secondOption
                    : (R left, @Nullable R right) -> (compareTo(left) > 0) ? firstOption : secondOption;
        };
    }
    
    @SideEffectFree
    static <R extends Rank<?, R>> BiFunction<R, @PolyNull R, R> simpleCeilingMap(
            boolean isUnary,
            @Nullable R floorRank
    ) {
        if (isUnary) {
            return (floorRank != null)
                    ? (left, right) -> floorRank.ceiling(left)
                    : (left, right) -> left;
        } else {
            BiFunction<R, @PolyNull R, R> firstStep = R::ceiling;
            return (floorRank != null)
                    ? firstStep.andThen(floorRank::ceiling)
                    : firstStep;
        }
    }
}
