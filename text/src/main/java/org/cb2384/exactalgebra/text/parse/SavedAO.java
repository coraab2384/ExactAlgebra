package org.cb2384.exactalgebra.text.parse;

import java.io.Serial;
import java.io.Serializable;
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

/**
 * <p>A holder for an {@link AlgebraObject} which also holds important information about it. Specifically,
 * the holder (a concrete subclass of this class) will contain information about the number such as
 * what {@link Rank} it has.</p>
 *
 * @param <R>   the {@link Rank} for the contained type
 * @param <S>   the specific type that this value is stored as
 * @param <T>   the overarching {@link AlgebraObject} type
 *
 * @author Corinne Buxton
 */
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
        this.flags = Set.copyOf(flags);
    }
    
    /**
     * Constructs a concrete SavedAO for the given input. The input must be an {@link AlgebraNumber}
     * or {@link AlgebraFunction}, or a {@link NumberRemainderPair} or {@link FunctionRemainderPair}.
     * The class to use will always be the class from calling the appropriate {@link
     * Rank#rankOf(AlgebraObject) rankOf} function on the input, and perhaps not the actual
     * type that the answer had.
     *
     * @param input the value to be saved
     *
     * @return  the constructed SavedAO box for {@code input}
     *
     * @throws CommandStateException    if {@code input} is not one of the supported AlgebraObject types
     */
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
    
    /**
     * return the actual stored value for this object
     *
     * @return  the value that this SavedAO saves
     */
    @Pure
    public abstract S value();
    
    /**
     * return the rank of this object
     *
     * @return  the specific rank type of this SavedAO
     */
    @Pure
    public R type() {
        return type;
    }
    
    /**
     * Yields an (immutable) set of flags regarding the value
     *
     * @return  relevant flags about this SavedAO
     */
    @Pure
    public Set<OpFlag> flags() {
        return flags;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Pure
    public boolean isZero() {
        return value().isZero();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Pure
    public boolean isOne() {
        return value().isOne();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Pure
    public boolean isNegative() {
        return value().isNegative();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @SideEffectFree
    public String toString() {
        return toString(10, (String[]) null);
    }
    
    /**
     * {@inheritDoc}
     *
     * @throws NumberFormatException    {@inheritDoc}
     */
    @Override
    @SideEffectFree
    public String toString(
            @IntRange(from = Character.MIN_RADIX, to = Character.MAX_RADIX) int radix
    ) {
        return toString(radix, (String[]) null);
    }
    
    /**
     * {@inheritDoc}
     *
     * @throws NumberFormatException    {@inheritDoc}
     */
    @Override
    public abstract String toString(
            @IntRange(from = Character.MIN_RADIX, to = Character.MAX_RADIX) int radix,
            String@Nullable... variables
    );
    
    /**
     * Prints this value in a format more amenable to printing the values as saved than normal
     * {@link #toString}.
     *
     * @param name          the name to give this value
     * @param separation    the amount of spaces between the name and the printed value
     * @param radix         the radix for representation
     * @param variable      for objects that use a variable (like functions), the variable to use
     *
     * @return  the formatted string
     *
     * @throws NumberFormatException    if {@code radix < }{@link Character#MIN_RADIX} or
     *                                  {@code radix > }{@link Character#MAX_RADIX}
     */
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
    
    sealed static abstract class SavedRP<V extends W, W extends T, T extends AlgebraObject<T>,
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
        
        /**
         * Yields the actual, direct RemainderPair object
         *
         * @return  the actual RemainderPair
         */
        @SideEffectFree
        public abstract P remainderPair();
        
        /**
         * Since this is a Saved pair type, rather than the whole value, the value part of the
         * {@link RemainderPair} type is returned here.
         *
         * @return  the value of the pair that this object saves
         */
        @Override
        @Pure
        public V value() {
            return (V) remainderPair().value();
        }
        
        /**
         * Since this is a Saved pair type, the remainder part of the
         * {@link RemainderPair} type is returned here.
         *
         * @return  the remainder of the pair that this object saves
         */
        @Pure
        public W remainder() {
            return (W) remainderPair().remainder();
        }
        
        @Pure
        public abstract R valueType();
        
        @Pure
        public abstract R remainderType();
        
        /**
         * {@inheritDoc}
         */
        @Override
        @Pure
        public boolean equiv(
                SavedAO<PairRank<T, ?, ?, ?>, ?, T> that
        ) {
            return remainderPair.equiv(((SavedRP<?, ?, T, ?, P>) that).remainderPair);
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        @Pure
        public int hashCode() {
            return type.hashCode() + 7 * super.flags.hashCode() + 31 * remainderPair.hashCode();
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        @Pure
        public boolean equals(
                @Nullable Object obj
        ) {
            return (obj instanceof SavedRP<?, ?, ?, ?, ?> objSRP) && type.equals(objSRP.type)
                    && remainderPair.equals(objSRP.remainderPair) && super.flags.equals(objSRP.flags());
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
        
        /**
         * {@inheritDoc}
         */
        @Override
        @Pure
        public N value() {
            return value;
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        @Pure
        public boolean equiv(
                SavedAO<NumberRank, ?, AlgebraNumber> that
        ) {
            return value.equiv((AlgebraNumber) that.value());
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        @Pure
        public int hashCode() {
            return type.hashCode() + 7 * super.flags.hashCode() + 31 * value.hashCode() + 1;
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        @Pure
        public boolean equals(
                @Nullable Object obj
        ) {
            return (obj instanceof SavedNumber<?> objSN) && type.equals(objSN.type)
                    && value.equals(objSN.value) && super.flags.equals(objSN.flags());
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        @SideEffectFree
        public String toString(
                @IntRange(from = Character.MIN_RADIX, to = Character.MAX_RADIX) int radix,
                String@Nullable... variables
        ) {
            return "Saved Number: " + value + " with type " + type;
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
        
        /**
         * {@inheritDoc}
         */
        @Override
        @Pure
        public NumberRemainderPair<Q, R> remainderPair() {
            return (NumberRemainderPair<Q, R>) super.remainderPair;
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        @Pure
        public NumberRank valueType() {
            return (NumberRank) type.valueRank();
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        @Pure
        public NumberRank remainderType() {
            return (NumberRank) type.remainderRank();
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        @Pure
        public int hashCode() {
            return super.hashCode() + 3;
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        @SideEffectFree
        public String toString(
                @IntRange(from = Character.MIN_RADIX, to = Character.MAX_RADIX) int radix,
                String @Nullable... variables
        ) {
            return "Saved Pair: " + super.remainderPair.toString(radix, variables)
                    + " with type " + type;
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
        
        /**
         * {@inheritDoc}
         */
        @Override
        @Pure
        public F value() {
            return value;
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        @Pure
        public boolean equiv(
                SavedAO<FunctionRank, ?, Polynomial<?>> that
        ) {
            return value.equiv((Polynomial<?>) that.value());
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        @Pure
        public int hashCode() {
            return type.hashCode() + 7 * super.flags.hashCode() + 31 * value.hashCode() + 2;
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        @Pure
        public boolean equals(
                @Nullable Object obj
        ) {
            return (obj instanceof SavedFunction<?> objSF) && type.equals(objSF.type)
                    && value.equals(objSF.value) && super.flags.equals(objSF.flags());
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        @SideEffectFree
        public String toString(
                @IntRange(from = Character.MIN_RADIX, to = Character.MAX_RADIX) int radix,
                String @Nullable... variables
        ) {
            return "Saved Function: " + value + " with type " + type;
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
        
        /**
         * {@inheritDoc}
         */
        @Override
        @Pure
        public FunctionRemainderPair<Q, R> remainderPair() {
            return (FunctionRemainderPair<Q, R>) super.remainderPair;
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        @Pure
        public FunctionRank valueType() {
            return (FunctionRank) type.valueRank();
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        @Pure
        public FunctionRank remainderType() {
            return (FunctionRank) type.remainderRank();
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        @Pure
        public int hashCode() {
            return super.hashCode() + 4;
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        @SideEffectFree
        public String toString(
                @IntRange(from = Character.MIN_RADIX, to = Character.MAX_RADIX) int radix,
                String @Nullable... variables
        ) {
            return "Saved Pair: " + super.remainderPair.toString(radix, variables)
                    + " with type " + type;
        }
    }
}
