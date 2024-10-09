package org.cb2384.exactalgebra.text.opmanagement;

import java.io.ObjectStreamException;
import java.io.Serial;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.IntStream;

import org.cb2384.exactalgebra.objects.relations.AlgebraFunction;
import org.cb2384.exactalgebra.objects.relations.polynomial.Polynomial;

import org.checkerframework.dataflow.qual.*;

/**
 * <p>A FunctionRank is composed of two parts. One is a {@link NumberRank}, and the other a
 * {@link FunctionalType}, which is like a NumberRank but for functions. Combining them together
 * yields a Rank that categorizes both the type of function as well as the complexity of their components
 * or coefficients. A Polynomial of Rational coefficients can never be relied upon to give an integer answer,
 * unlike one of integer coefficients, which is why both parts are needed to properly 'bound' their output</p>
 *
 * @author Corinne Buxton
 */
public final class FunctionRank
        implements Rank<Polynomial<?>, FunctionRank> {
    
    @Serial
    private static final long serialVersionUID = 0x5A27034607BC45DBL;
    
    public enum FunctionalType {
        CONSTANT(Polynomial.class, EnumSet.noneOf(NumberRank.class)),
        LINEAR(Polynomial.class, EnumSet.range(NumberRank.NATURAL_1, NumberRank.NATURAL_0)),
        POLYNOMIAL(Polynomial.class, EnumSet.range(NumberRank.NATURAL_1, NumberRank.NATURAL_0)),
        RATIONAL(AlgebraFunction.class, EnumSet.range(NumberRank.NATURAL_1, NumberRank.INTEGER)),
        RADICAL(AlgebraFunction.class, EnumSet.range(NumberRank.NATURAL_1, NumberRank.RATIONAL)),
        TRANSCENDENTAL(AlgebraFunction.class, EnumSet.range(NumberRank.NATURAL_1, NumberRank.REAL_RADICAL));
        
        private final Class<? extends AlgebraFunction<?, ?>> resultingClass;
        
        private final Set<NumberRank> compatibleRanks;
        
        @SideEffectFree
        FunctionalType(
                Class<?> resultingClass,
                EnumSet<NumberRank> incompatibleRanks
        ) {
            this.resultingClass = (Class<? extends AlgebraFunction<?, ?>>) resultingClass;
            compatibleRanks = EnumSet.complementOf(incompatibleRanks);
        }
        
        @Pure
        public static FunctionalType typeOf(
                AlgebraFunction<?, ?> function
        ) {
            return switch (function) {
                default -> POLYNOMIAL;
            };
        }
        
        @Pure
        public FunctionalType ceiling(
                FunctionalType that
        ) {
            return that.ordinal() < ordinal()
                    ? this
                    : that;
        }
    }
    
    private static final List<List<FunctionRank>> RANKS = Arrays.stream(FunctionalType.values())
                .map(FunctionRank::generateRanksFor)
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
                .mapToObj(i -> generateRank(functionalType, numberRanks, i, floorIndex, floorRank))
                .toList();
    }
    
    @SideEffectFree
    private static FunctionRank generateRank(
            FunctionalType functionalType,
            NumberRank[] numberRanks,
            int index,
            int floorIndex,
            FunctionRank floorRank
    ) {
        return (index > floorIndex)
                ? new FunctionRank(functionalType, numberRanks[index])
                : floorRank;
    }
    
    @Pure
    public FunctionalType functionalType() {
        return functionalType;
    }
    
    @Pure
    public NumberRank coefficientRank() {
        return coefficientRank;
    }
    
    @Override
    @Pure
    public FunctionRank ceiling(
            FunctionRank that
    ) {
        return rankFrom(functionalType.ceiling(that.functionalType),
                coefficientRank.ceiling(that.coefficientRank));
    }
    
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
    
    @Override
    @Pure
    public Class<? extends Polynomial<?>> resultingClass() {
        return (Class<? extends Polynomial<?>>) functionalType.resultingClass;
    }
    
    @Override
    @Pure
    public int hashCode() {
        return functionalType.hashCode() ^ 31 * coefficientRank.hashCode();
    }
    
    @Override
    @SideEffectFree
    public String toString() {
        return "FunctionRank[" + functionalType + ", " + coefficientRank + ']';
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
    private Object readResolve() throws ObjectStreamException {
        return rankFrom(functionalType, coefficientRank);
    }
}
