package org.cb2384.exactalgebra.text.opmanagement;

import java.io.ObjectStreamException;
import java.io.Serial;
import java.util.Arrays;
import java.util.List;

import org.cb2384.exactalgebra.objects.AlgebraObject;
import org.cb2384.exactalgebra.objects.numbers.AlgebraNumber;
import org.cb2384.exactalgebra.objects.pair.FunctionRemainderPair;
import org.cb2384.exactalgebra.objects.pair.NumberRemainderPair;
import org.cb2384.exactalgebra.objects.pair.RemainderPair;
import org.cb2384.exactalgebra.objects.relations.polynomial.Polynomial;
import org.cb2384.exactalgebra.util.corutils.StringUtils;

import org.checkerframework.dataflow.qual.*;

public sealed interface PairRank<T extends AlgebraObject<T>, R extends Rank<T, R>,
                E extends PairRank<T, R, E, P>, P extends RemainderPair<?, ?, T, P>>
        extends Rank<P, E> {
    
    @SideEffectFree
    static PairRank<?, ?, ?, ?> rankOf(
            RemainderPair<?, ?, ?, ?> pair
    ) {
        return switch (pair) {
            case NumberRemainderPair<?, ?> numPair -> NumberPairRank.rankOf(numPair);
            case FunctionRemainderPair<?, ?> funcPair -> FunctionPairRank.rankOf(funcPair);
            default -> throw new IllegalArgumentException("unrecognized "
                    + StringUtils.getIdealName(RemainderPair.class) + " implementation");
        };
    }
    
    R valueRank();
    
    R remainderRank();
    
    final class NumberPairRank
            implements PairRank<AlgebraNumber, NumberRank, NumberPairRank, NumberRemainderPair<?, ?>> {
        
        @Serial
        private static final long serialVersionUID = 0x39683AB98723A7D9L;
        
        private static final List<List<NumberPairRank>> RANKS = Arrays.stream(NumberRank.values())
                .map(NumberPairRank::generateRanksFor)
                .toList();
        
        private final NumberRank valueRank;
        
        private final NumberRank remainderRank;
        
        @SideEffectFree
        private NumberPairRank(
                NumberRank valueRank,
                NumberRank remainderRank
        ) {
            this.valueRank = valueRank;
            this.remainderRank = remainderRank;
        }
        
        @SideEffectFree
        private static List<NumberPairRank> generateRanksFor(
                NumberRank valueRank
        ) {
            return Arrays.stream(NumberRank.values())
                    .map(r -> new NumberPairRank(valueRank, r))
                    .toList();
        }
        
        @Pure
        static NumberPairRank rankFrom(
                NumberRank valueRank,
                NumberRank remainderRank
        ) {
            return RANKS.get( valueRank.ordinal() ).get( remainderRank.ordinal() );
        }
        
        @Pure
        public static NumberPairRank rankOf(
                NumberRemainderPair<?, ?> pair
        ) {
            return rankFrom(NumberRank.rankOf(pair.value()), NumberRank.rankOf( pair.remainder()) );
        }
        
        @Override
        @Pure
        public NumberRank valueRank() {
            return valueRank;
        }
        
        @Override
        @Pure
        public NumberRank remainderRank() {
            return remainderRank;
        }
        
        @Override
        @Pure
        public int compareTo(
                NumberPairRank o
        ) {
            int compResult = valueRank.compareTo(o.valueRank);
            if (compResult == 0) {
                compResult = remainderRank.compareTo(o.remainderRank);
            }
            return compResult;
        }
        
        @Override
        @Pure
        public NumberPairRank ceiling(
                NumberPairRank that
        ) {
            return rankFrom(valueRank.ceiling(that.valueRank),
                    remainderRank.ceiling(that.remainderRank));
        }
        
        @Override
        @Pure
        public Class<?> resultingClass() {
            return NumberRemainderPair.class;
        }
        
        @Override
        @Pure
        public int hashCode() {
            return valueRank.hashCode() * 31 ^ remainderRank.hashCode();
        }
        
        @Override
        @SideEffectFree
        public String toString() {
            return "NumberPairRank[" + valueRank + ", remainder " + remainderRank + "]";
        }
        
        @Serial
        private Object readResolve() throws ObjectStreamException {
            return rankFrom(valueRank, remainderRank);
        }
    }
    
    record FunctionPairRank(FunctionRank valueRank, FunctionRank remainderRank)
            implements PairRank<Polynomial<?>, FunctionRank, FunctionPairRank, FunctionRemainderPair<?, ?>> {
        
        @SideEffectFree
        public static FunctionPairRank rankOf(
                FunctionRemainderPair<?, ?> pair
        ) {
            return new FunctionPairRank(
                    FunctionRank.rankOf(pair.value()),
                    FunctionRank.rankOf(pair.remainder())
            );
        }
        
        @Override
        @Pure
        public int compareTo(
                FunctionPairRank o
        ) {
            int compResult = valueRank.functionalType().compareTo(o.valueRank.functionalType());
            if (compResult == 0) {
                compResult = remainderRank.functionalType().compareTo(o.remainderRank.functionalType());
            }
            if (compResult == 0) {
                compResult = valueRank.coefficientRank().compareTo(o.valueRank.coefficientRank());
            }
            if (compResult == 0) {
                compResult = remainderRank.coefficientRank().compareTo(o.remainderRank.coefficientRank());
            }
            return compResult;
        }
        
        @Override
        @Pure
        public FunctionPairRank ceiling(
                FunctionPairRank that
        ) {
            FunctionRank newValueRank = valueRank.ceiling(that.valueRank);
            FunctionRank newRemainderRank = remainderRank.ceiling(that.remainderRank);
            if ((newValueRank.equals(valueRank)) && (newRemainderRank.equals(remainderRank))) {
                return this;
            }
            if ((newValueRank.equals(that.valueRank)) && (newRemainderRank.equals(that.remainderRank))) {
                return that;
            }
            return new FunctionPairRank(newValueRank, newRemainderRank);
        }
        
        @Override
        @Pure
        public Class<?> resultingClass() {
            return FunctionRemainderPair.class;
        }
        
        @Override
        @SideEffectFree
        public String toString() {
            return "FunctionPairRank[" + valueRank + ", remainder " + remainderRank + "]";
        }
    }
}
