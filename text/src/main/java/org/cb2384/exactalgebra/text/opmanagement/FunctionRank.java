package org.cb2384.exactalgebra.text.opmanagement;

import java.io.ObjectStreamException;
import java.io.Serial;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.IntStream;

import org.cb2384.exactalgebra.objects.relations.AlgebraFunction;
import org.cb2384.exactalgebra.objects.relations.polynomial.Polynomial;
import org.cb2384.exactalgebra.util.corutils.ternary.ComparableSwitchSignum;

import org.checkerframework.checker.index.qual.*;
import org.checkerframework.dataflow.qual.*;

/**
 * <p>A FunctionRank is composed of two parts. One is a {@link NumberRank}, and the other a
 * {@link FunctionalType}, which is like a NumberRank but for functions. Combining them together
 * yields a Rank that categorizes both the type of function as well as the complexity of their components
 * or coefficients. A Polynomial of Rational coefficients can never be relied upon to give an integer answer,
 * unlike one of integer coefficients, which is why both parts are needed to properly 'bound' their output</p>
 *
 * <p>While this class is not an {@link Enum} class, it is similar in that it only has constant members,
 * and a finite number of them. The difference is that these do not have declared names. </p>
 *
 * @author Corinne Buxton
 */
public final class FunctionRank
        implements Rank<Polynomial<?>, FunctionRank> {
    
    @Serial
    private static final long serialVersionUID = 0x5A27034607BC45DBL;
    
    /**
     * Constant types of functions, without considering their coefficient, only the general shape
     */
    public enum FunctionalType
            implements ComparableSwitchSignum<FunctionalType> {
        /**
         * The simplest function, any plain constant
         * {@link org.cb2384.exactalgebra.objects.numbers.AlgebraNumber}
         */
        CONSTANT(Polynomial.class, EnumSet.noneOf(NumberRank.class)),
        /**
         * A linear function
         */
        LINEAR(Polynomial.class, EnumSet.range(NumberRank.NATURAL_1, NumberRank.NATURAL_0)),
        /**
         * A polynomial function
         */
        POLYNOMIAL(Polynomial.class, EnumSet.range(NumberRank.NATURAL_1, NumberRank.NATURAL_0)),
        /**
         * A rational function
         */
        RATIONAL(AlgebraFunction.class, EnumSet.range(NumberRank.NATURAL_1, NumberRank.INTEGER)),
        /**
         * A radical function, or up-to-rational function to a rational power
         */
        RADICAL(AlgebraFunction.class, EnumSet.range(NumberRank.NATURAL_1, NumberRank.RATIONAL)),
        /**
         * Any other function
         */
        TRANSCENDENTAL(AlgebraFunction.class, EnumSet.range(NumberRank.NATURAL_1, NumberRank.REAL_RADICAL));
        
        private final Class<? extends AlgebraFunction<?, ?>> resultingClass;
        
        private final Set<NumberRank> compatibleRanks;
        
        @SideEffectFree @SuppressWarnings("unchecked")
        FunctionalType(
                Class<?> resultingClass,
                EnumSet<NumberRank> incompatibleRanks
        ) {
            this.resultingClass = (Class<? extends AlgebraFunction<?, ?>>) resultingClass;
            compatibleRanks = Collections.unmodifiableSet(EnumSet.complementOf(incompatibleRanks));
        }
        
        /**
         * Maps a function to its overarching type
         *
         * @param function  the function to type
         *
         * @return  the lowest type-rank which covers the 'shape' of {@code function}
         */
        @Pure
        public static FunctionalType typeOf(
                AlgebraFunction<?, ?> function
        ) {
            return switch (function) {
                default -> POLYNOMIAL;
            };
        }
        
        /**
         * Yields the larger, as in higher-ranked, of the two.
         *
         * @implNote    Simply {@link Enum#compareTo(Enum) compareTo(}{@code
         *              that}{@link Enum#compareTo(Enum) )&nbsp;}{@code < 0 ? that : this}.
         *
         * @param that  the value to compare against this
         *
         * @return  {@code that} if and only if it is 'higher-ranked', otherwise this
         */
        @Pure
        public FunctionalType ceiling(
                FunctionalType that
        ) {
            return (compareTo(that) < 0) ? that : this;
        }
    }
    
    private static final List<List<FunctionRank>> RANKS = Arrays.stream(FunctionalType.values())
                .map(FunctionRank::generateRanksFor)
                .toList();
    
    private static final List<FunctionRank> VALUES = RANKS.stream()
            .flatMap(List::stream)
            .distinct()
            .sorted()
            .toList();
    
    private final FunctionalType functionalType;
    
    private final NumberRank coefficientRank;
    
    @SideEffectFree
    private FunctionRank(
            FunctionalType functionalType,
            NumberRank coefficientRank
    ) {
        this.functionalType = functionalType;
        this.coefficientRank = coefficientRank;
    }
    
    @Pure
    static FunctionRank rankFrom(
            FunctionalType functionalType,
            NumberRank numberRank
    ) {
        return RANKS.get( functionalType.ordinal() ).get( numberRank.ordinal() );
    }
    
    /**
     * Maps a function to its rank
     *
     * @param value the function to get a rank for
     *
     * @return  the lowest rank of the appropriate type which covers {@code value}
     */
    @Pure
    public static FunctionRank rankOf(
            AlgebraFunction<?, ?> value
    ) {
        return rankFrom(FunctionalType.typeOf(value), NumberRank.rankOf( value.coefficientHighestRank()) );
    }
    
    @SideEffectFree
    private static List<FunctionRank> generateRanksFor(
            FunctionalType functionalType
    ) {
        NumberRank[] numberRanks = NumberRank.values();
        int floorIndex = functionalType.compatibleRanks.stream()
                .mapToInt(NumberRank::ordinal)
                .reduce(numberRanks.length, Integer::min);
        NumberRank floor = numberRanks[floorIndex];
        FunctionRank floorRank = new FunctionRank(functionalType, floor);
        
        return IntStream.range(0, numberRanks.length)
                .mapToObj(i -> (i > floorIndex) ? new FunctionRank(functionalType, numberRanks[i]) : floorRank)
                .toList();
    }
    
    /**
     * The FunctionalType component of this particular FunctionalRank
     */
    @Pure
    public FunctionalType functionalType() {
        return functionalType;
    }
    
    /**
     * The coefficient NumberRank component of this particular FunctionalRank
     */
    @Pure
    public NumberRank coefficientRank() {
        return coefficientRank;
    }
    
    /**
     * Yields the rank that is composed of the ceiling of each of the constituent ranks. This might
     * not be either this or {@code that}.
     *
     * @param that  the value to compare against this
     *
     * @return  the lowest Rank that has a higher {@link #functionalType()} and {@link #coefficientRank()}
     */
    @Override
    @Pure
    public FunctionRank ceiling(
            FunctionRank that
    ) {
        return rankFrom(functionalType.ceiling(that.functionalType),
                coefficientRank.ceiling(that.coefficientRank));
    }
    
    /**
     * Compares this enum with the specified object for order. The {@link #functionalType()} is the
     * primary comparison, with {@link #coefficientRank()} only being used for tiebreaking.
     *
     * @param o the object to be compared
     *
     * @return  a negative integer, zero, or a positive integer as this is less than, equal to,
     *          or greater than the specified object respectively
     */
    @Override
    @Pure
    public int compareTo(
            FunctionRank o
    ) {
        int compResult = functionalType.compareTo(o.functionalType);
        if (compResult == 0) {
            compResult = coefficientRank.compareTo(o.coefficientRank);
        }
        return compResult;
    }
    
    /**
     * {@inheritDoc}
     *
     * @implNote    Currently, only {@link Polynomial} is implemented, so only {@link Polynomial}
     *              can be returned
     */
    @Override
    @Pure @SuppressWarnings("unchecked")
    public Class<? extends Polynomial<?>> resultingClass() {
        return (Class<? extends Polynomial<?>>) Polynomial.class;
    }
    
    /**
     * Returns a hash code for this constant.
     *
     * @return  a hash code for this enum constant
     */
    @Override
    @Pure
    public int hashCode() {
        return functionalType.hashCode() ^ 31 * coefficientRank.hashCode();
    }
    
    /**
     * Yields a name of this constant, which formulated similar to a record's default
     * {@link Record#toString() toString()} with the two record components being
     * {@link #functionalType()} and {@link #coefficientRank()}.
     *
     * @return  a string representation of this constant
     */
    @Override
    @SideEffectFree
    public String toString() {
        return "FunctionRank[" + functionalType.toString() + ", " + coefficientRank + "]";
    }
    
    /**
     * Returns an array containing the constants of this type, in the order consistent with
     * {@link #compareTo(FunctionRank)}. This is similar in purpose to the {@code values()} methods
     * of {@link Enum} subclasses.
     *
     * @return  a sorted array containing the constants of this type
     */
    @SideEffectFree
    public static FunctionRank[] values() {
        return VALUES.toArray(FunctionRank[]::new);
    }
    
    /**
     * Similar to {@link Enum#ordinal()} in intent, though specifically in this case the ordinal is
     * the index of this constant when in the result returned from {@link #values()}
     *
     * @return  the ordinal value of this constant, as per {@link #values()}
     */
    @Pure
    public @NonNegative int ordinal() {
        return VALUES.indexOf(this);
    }
    
    @Pure
    static BiFunction<FunctionRank, ? extends Rank<?, ?>, FunctionRank> defaultCeilingMapWithFloor(
            NumberRank floor
    ) {
        return (left, right) -> FunctionRank.rankFrom(
                left.functionalType().ceiling( ((FunctionRank) right).functionalType() ),
                floor.ceiling(left.coefficientRank()).ceiling( ((FunctionRank) right).coefficientRank() )
        );
    }
    
    @Serial
    private Object readResolve()
            throws ObjectStreamException {
        return rankFrom(functionalType, coefficientRank);
    }
}
