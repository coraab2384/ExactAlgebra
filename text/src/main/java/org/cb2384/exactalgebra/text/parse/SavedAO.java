package org.cb2384.exactalgebra.text.parse;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import org.cb2384.exactalgebra.objects.AlgebraObject;
import org.cb2384.exactalgebra.objects.numbers.AlgebraNumber;
import org.cb2384.exactalgebra.objects.pair.FunctionRemainderPair;
import org.cb2384.exactalgebra.objects.pair.NumberRemainderPair;
import org.cb2384.exactalgebra.objects.pair.RemainderPair;
import org.cb2384.exactalgebra.objects.relations.AlgebraFunction;
import org.cb2384.exactalgebra.objects.relations.polynomial.Polynomial;
import org.cb2384.exactalgebra.text.opmanagement.FunctionRank;
import org.cb2384.exactalgebra.text.opmanagement.NumberRank;
import org.cb2384.exactalgebra.text.opmanagement.OpFlag;
import org.cb2384.exactalgebra.text.opmanagement.PairRank;
import org.cb2384.exactalgebra.text.opmanagement.PairRank.FunctionPairRank;
import org.cb2384.exactalgebra.text.opmanagement.PairRank.NumberPairRank;
import org.cb2384.exactalgebra.text.opmanagement.Rank;
import org.cb2384.exactalgebra.util.corutils.Collectionz;

import org.checkerframework.checker.index.qual.*;
import org.checkerframework.checker.nullness.qual.*;
import org.checkerframework.common.value.qual.*;
import org.checkerframework.dataflow.qual.*;

public sealed abstract class SavedAO<R extends Rank<?, ?>, S extends AlgebraObject<T>,
                T extends AlgebraObject<T>>
        implements AlgebraObject<SavedAO<R, ?, T>>, Serializable {
    
    final R type;
    
    private final Set<OpFlag> flags;
    
    private SavedAO(
            R type,
            Set<OpFlag> flags
    ) {
        this.type = type;
        this.flags = Collections.unmodifiableSet(flags);
    }
    
    public static SavedAO<?, ?, ?> asSavedAO(
            AlgebraObject<?> input
    ) {
        return switch (input) {
            case AlgebraNumber number -> new SavedNumber<>(number);
            case NumberRemainderPair<?, ?> numberRP -> SavedNumbRP.constructor(numberRP);
            case AlgebraFunction<?, ?> function -> new SavedFunction<>((Polynomial<?>) function);
            case FunctionRemainderPair<?, ?> functionRP -> SavedFuncRP.constructor(functionRP);
            default -> throw new CommandStateException("Cannot process input: " + input);
        };
    }
    
    @Pure
    public abstract S value();
    
    @Pure
    public R type() {
        return type;
    }
    
    @Pure
    public Set<OpFlag> flags() {
        return flags;
    }
    
    @Override
    @Pure
    public boolean isZero() {
        return value().isZero();
    }
    
    @Override
    @Pure
    public boolean isOne() {
        return value().isOne();
    }
    
    @Override
    @Pure
    public boolean isNegative() {
        return value().isNegative();
    }
    
    @Override
    @SideEffectFree
    public String toString() {
        return toString(10, (String[]) null);
    }
    
    @Override
    @SideEffectFree
    public String toString(
            @IntRange(from = Character.MIN_RADIX, to = Character.MAX_RADIX) int radix
    ) {
        return toString(radix, (String[]) null);
    }
    
    @Override
    public abstract String toString(
            @IntRange(from = Character.MIN_RADIX, to = Character.MAX_RADIX) int radix,
            String@Nullable... variables
    );
    
    @SideEffectFree
    public String print(
            String name,
            @Positive int separation,
            @IntRange(from = Character.MIN_RADIX, to = Character.MAX_RADIX) int radix,
            String@Nullable... variable
    ) {
        return name + " ".repeat(separation - name.length()) + toString(radix, variable);
    }
    
    @SideEffectFree
    static Set<OpFlag> numberFlagsModifiable(
            AlgebraNumber value
    ) {
        Set<OpFlag> result = EnumSet.of(OpFlag.UTILITY);
        result.add(OpFlag.outputFlagOf(value));
        return result;
    }
    
    @SideEffectFree
    static Set<OpFlag> functionFlagsModifiable(
            Polynomial<?> function
    ) {
        Set<OpFlag> result = EnumSet.of(OpFlag.FUNCTIONAL, OpFlag.UTILITY);
        result.add(OpFlag.outputFlagOf(function));
        result.add(OpFlag.coefficientFlagOf(function));
        return result;
    }
    
    private sealed static abstract class SavedRP<V extends W, W extends T, T extends AlgebraObject<T>,
                    R extends Rank<T, R>, P extends RemainderPair<?, ?, T, P>>
            extends SavedAO<PairRank<T, ?, ?, ?>, V, T> {
        
        private final P remainderPair;
        
        SavedRP(
                P remainderPair,
                PairRank<T, ?, ?, ?> pairRank,
                Set<OpFlag> flagsModifiable
        ) {
            super(pairRank, Collectionz.add(flagsModifiable, OpFlag.OUTPUT_PAIR));
            this.remainderPair = remainderPair;
        }
        
        @SideEffectFree
        public abstract P remainderPair();
        
        @Override
        @Pure
        public V value() {
            return (V) remainderPair().value();
        }
        
        @Pure
        public W remainder() {
            return (W) remainderPair().remainder();
        }
        
        @Pure
        public abstract R valueType();
        
        @Pure
        public abstract R remainderType();
        
        @Override
        @Pure
        public boolean equiv(
                SavedAO<PairRank<T, ?, ?, ?>, ?, T> that
        ) {
            return remainderPair.equiv(((SavedRP<?, ?, T, ?, P>) that).remainderPair);
        }
    }
    
    static final class SavedNumber<N extends AlgebraNumber>
            extends SavedAO<NumberRank, N, AlgebraNumber> {
        
        @Serial
        private static final long serialVersionUID = 0x84A4C7BD4E15F3C9L;
        
        private final N value;
        
        @SideEffectFree
        public SavedNumber(
                N value,
                NumberRank type
        ) {
            super(type, numberFlagsModifiable(value));
            this.value = value;
        }
        
        @SideEffectFree
        public SavedNumber(
                N value
        ) {
            this(value, NumberRank.rankOf(value));
        }
        
        @Override
        @Pure
        public N value() {
            return value;
        }
        
        @Override
        @Pure
        public boolean equiv(
                SavedAO<NumberRank, ?, AlgebraNumber> that
        ) {
            return value.equiv((AlgebraNumber) that.value());
        }
        
        @Override
        @SideEffectFree
        public String toString(
                @IntRange(from = Character.MIN_RADIX, to = Character.MAX_RADIX) int radix,
                String @Nullable... variables
        ) {
            return "Saved Number: " + value + " with type " + super.type;
        }
    }
    
    static final class SavedNumbRP<Q extends R, R extends AlgebraNumber>
            extends SavedRP<Q, R, AlgebraNumber, NumberRank, NumberRemainderPair<?, ?>> {
        
        @Serial
        private static final long serialVersionUID = 0xF6D489C234567BF8L;
        
        @SideEffectFree
        public SavedNumbRP(
                NumberRemainderPair<Q, R> remainderPair,
                NumberPairRank type
        ) {
            super(remainderPair, type,
                    Collectionz.addAllUnordered(
                            numberFlagsModifiable(remainderPair.value()),
                            numberFlagsModifiable(remainderPair.remainder())
                    ));
        }
        
        @SideEffectFree
        public SavedNumbRP(
                NumberRemainderPair<Q, R> remainderPair
        ) {
            this(remainderPair, NumberPairRank.rankOf(remainderPair));
        }
        
        @SideEffectFree
        public SavedNumbRP(
                Q value,
                R remainder
        ) {
            this(new NumberRemainderPair<>(value, remainder), value, remainder);
        }
        
        @SideEffectFree
        private SavedNumbRP(
                NumberRemainderPair<Q, R> remainderPair,
                Q value,
                R remainder
        ) {
            super(remainderPair, NumberPairRank.rankOf(remainderPair),
                    Collectionz.addAllUnordered(
                            numberFlagsModifiable(value),
                            numberFlagsModifiable(remainder)
                    ));
        }
        
        @SideEffectFree
        private static <Q extends R, R extends AlgebraNumber> SavedNumbRP<Q, R> constructor(
                NumberRemainderPair<?, ?> input
        ) {
            return new SavedNumbRP<>((NumberRemainderPair<Q, R>) input);
        }
        
        @Override
        @Pure
        public NumberRemainderPair<Q, R> remainderPair() {
            return (NumberRemainderPair<Q, R>) super.remainderPair;
        }
        
        @Override
        @Pure
        public NumberRank valueType() {
            return (NumberRank) super.type.valueRank();
        }
        
        @Override
        @Pure
        public NumberRank remainderType() {
            return (NumberRank) super.type.remainderRank();
        }
        
        @Override
        @SideEffectFree
        public String toString(
                @IntRange(from = Character.MIN_RADIX, to = Character.MAX_RADIX) int radix,
                String @Nullable... variables
        ) {
            return "Saved Pair: " + super.remainderPair.toString(radix, variables)
                    + " with type " + super.type;
        }
    }
    
    static final class SavedFunction<F extends Polynomial<?>>
            extends SavedAO<FunctionRank, F, Polynomial<?>> {
        
        @Serial
        private static final long serialVersionUID = 0xFF783C5E90A6B4AL;
        
        private final F value;
        
        @SideEffectFree
        public SavedFunction(
                F value,
                FunctionRank type
        ) {
            super(type, functionFlagsModifiable(value));
            this.value = value;
        }
        
        @SideEffectFree
        public SavedFunction(
                F value
        ) {
            this(value, FunctionRank.rankOf(value));
        }
        
        @Override
        @Pure
        public F value() {
            return value;
        }
        
        @Override
        @Pure
        public boolean equiv(
                SavedAO<FunctionRank, ?, Polynomial<?>> that
        ) {
            return value.equiv((Polynomial<?>) that.value());
        }
        
        @Override
        @SideEffectFree
        public String toString(
                @IntRange(from = Character.MIN_RADIX, to = Character.MAX_RADIX) int radix,
                String @Nullable... variables
        ) {
            return "Saved Function: " + value + " with type " + super.type;
        }
    }
    
    static final class SavedFuncRP<Q extends R, R extends Polynomial<?>>
            extends SavedRP<Q, R, Polynomial<?>, FunctionRank, FunctionRemainderPair<?, ?>> {
        
        @Serial
        private static final long serialVersionUID = 0x73F5E93DC5BAA574L;
        
        @SideEffectFree
        public SavedFuncRP(
                FunctionRemainderPair<Q, R> remainderPair,
                FunctionPairRank type
        ) {
            super(remainderPair, type,
                    Collectionz.addAllUnordered(
                            functionFlagsModifiable(remainderPair.value()),
                            functionFlagsModifiable(remainderPair.remainder())
                    ));
        }
        
        @SideEffectFree
        public SavedFuncRP(
                FunctionRemainderPair<Q, R> remainderPair
        ) {
            this(remainderPair, FunctionPairRank.rankOf(remainderPair));
        }
        
        @SideEffectFree
        public SavedFuncRP(
                Q value,
                R remainder
        ) {
            this(new FunctionRemainderPair<>(value, remainder), value, remainder);
        }
        
        @SideEffectFree
        private SavedFuncRP(
                FunctionRemainderPair<Q, R> remainderPair,
                Q value,
                R remainder
        ) {
            super(
                    remainderPair,
                    new FunctionPairRank(FunctionRank.rankOf(value), FunctionRank.rankOf(remainder)),
                    Collectionz.addAllUnordered(
                            functionFlagsModifiable(value),
                            functionFlagsModifiable(remainder)
                    )
            );
        }
        
        @SideEffectFree
        private static <Q extends R, R extends Polynomial<?>> SavedFuncRP<Q, R> constructor(
                FunctionRemainderPair<?, ?> input
        ) {
            return new SavedFuncRP<>((FunctionRemainderPair<Q, R>) input);
        }
        
        @Override
        @Pure
        public FunctionRemainderPair<Q, R> remainderPair() {
            return (FunctionRemainderPair<Q, R>) super.remainderPair;
        }
        
        @Override
        @Pure
        public FunctionRank valueType() {
            return (FunctionRank) super.type.valueRank();
        }
        
        @Override
        @Pure
        public FunctionRank remainderType() {
            return (FunctionRank) super.type.remainderRank();
        }
        
        @Override
        @SideEffectFree
        public String toString(
                @IntRange(from = Character.MIN_RADIX, to = Character.MAX_RADIX) int radix,
                String @Nullable... variables
        ) {
            return "Saved Pair: " + super.remainderPair.toString(radix, variables)
                    + " with type " + super.type;
        }
    }
}
