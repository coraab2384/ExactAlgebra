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

/**
 * <p>A Rank is an ({@link Enum}esque) object that indicates the 'complexity' of a result, or, that
 * creates a hierarchy of results and indicates a highest possible output. Different implementing classes
 * subdivide different fields or types of algebraic objects.</p>
 *
 * <p>The most-used two are {@link NumberRank}, followed by {@link FunctionRank}. {@link PairRank} also exists,
 * which itself contains two implementations for number-based and function-based pairs.</p>
 *
 * @param <T>   The overarching type of algebraic object, such as {@link AlgebraNumber} for {@link NumberRank}
 * @param <R>   The implementing rank class
 *
 * @author Corinne Buxton
 */
public sealed interface Rank<T extends AlgebraObject<T>, R extends Rank<T, R>>
        extends ComparableSwitchSignum<R>, Serializable
        permits FunctionRank, NumberRank, PairRank {
    
    /**
     * Yields the larger, as in higher-ranked, of the two.
     *
     * @implNote    The default implementation is simply {@link #compareTo(Object) compareTo(}{@code
     *              that}{@link #compareTo(Object) )&nbsp;}{@code < 0 ? that : this}.
     *
     * @param that  the value to compare against this
     *
     * @return  {@code that} if and only if it is 'higher-ranked', otherwise this
     */
    @Pure
    default R ceiling(
            R that
    ) {
        return (compareTo(that) < 0) ? that : (R) this;
    }
    
    /**
     * Yields the least-nested type interface that can cover the type of this rank. The type could cover
     * things that are from higher ranks as well (the relationship is not 1-to-1).
     *
     * @return  The class object of the type that is guaranteed to cover this rank
     */
    @Pure
    Class<?> resultingClass();
    
    /**
     * Maps a value to its rank
     *
     * @implNote    Each implementer or sub-interface carries its own {@code rankOf(T)} static method;
     *              this one simply redirects to the appropriate one.
     *
     * @param value the {@link AlgebraObject} to get a rank for
     *
     * @return  the lowest rank of the appropriate type which covers {@code value}
     *
     * @param <T>   The specific overarching AlgebraObject type
     */
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
    
    /**
     * Creates a BiFunction object which, regardless of inputs, always returns this specific rank.
     * The output could be written as a
     *
     * @return  effectively a function object for the lambda {@code (x, y) -> this}
     */
    @SideEffectFree
    default BiFunction<@Nullable R, @Nullable R, R> constantReturningThis() {
        R thisR = (R) this;
        return (left, right) -> thisR;
    }
    
    /**
     * Generates a BiFunction which compares two Ranks and produces a result. The way the comparison
     * is handled depends on the arguments here.
     *
     * @param useSecondArgCeiling       determines whether the ceiling of both inputs will be the subject
     *                                  for the rest of the function, or if only the first will be used
     * @param compResultForFirstOption  determines whether the value determined by the previous
     *                                  parameter should be compared as {@code <, >} or {@code =}
     * @param firstOption               the value to be returned if the comparison indicated in the
     *                                  above parameter evaluates as {@code true}; if it is {@code null},
     *                                  then the result that was compared to this will be used in its place
     * @param secondOption              the value to be returned if the comparison indicated in the
     *                                  above parameter evaluates as {@code false}; if it is {@code null},
     *                                  then the result that was compared to this will be used in its place
     *
     * @return  The BiFunction object that performs the indicated comparisons
     *
     * @throws NullPointerException if both {@code firstOption} and {@code secondOption} are {@code null}
     */
    @SideEffectFree
    default BiFunction<R, @PolyNull R, R> compareToThisAndThen(
            boolean useSecondArgCeiling,
            Signum compResultForFirstOption,
            @Nullable R firstOption,
            @Nullable R secondOption
    ) {
        if (firstOption == null) {
            if (secondOption == null) {
                throw new NullPointerException("Both options cannot be null!");
            }
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
    
    /**
     * Generates a BiFunction which compares two Ranks and produces a result. The way the comparison
     * is handled depends on the arguments here.
     *
     * @param isUnary   determines if the returned object first compares its two inputs, or if
     *                  it simply takes the left and discards the right
     * @param floorRank if present, this is the minimum rank that the answer must have; if {@code null},
     *                  no floor check is made
     *
     * @return  The BiFunction object that performs the indicated comparisons
     *
     * @param <R>   The specific Rank implementation in use
     */
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
