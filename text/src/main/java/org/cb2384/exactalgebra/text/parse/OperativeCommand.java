package org.cb2384.exactalgebra.text.parse;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.function.Function;

import org.cb2384.exactalgebra.objects.AlgebraObject;
import org.cb2384.exactalgebra.objects.numbers.AlgebraNumber;
import org.cb2384.exactalgebra.objects.pair.FunctionRemainderPair;
import org.cb2384.exactalgebra.objects.pair.NumberRemainderPair;
import org.cb2384.exactalgebra.objects.relations.polynomial.Polynomial;
import org.cb2384.exactalgebra.text.opmanagement.AlgebraOp;
import org.cb2384.exactalgebra.text.opmanagement.FunctionRank;
import org.cb2384.exactalgebra.text.opmanagement.NumberRank;
import org.cb2384.exactalgebra.text.opmanagement.OpFlag;
import org.cb2384.exactalgebra.text.opmanagement.PairRank.FunctionPairRank;
import org.cb2384.exactalgebra.text.opmanagement.PairRank.NumberPairRank;
import org.cb2384.exactalgebra.text.opmanagement.PolynomialOps;
import org.cb2384.exactalgebra.text.opmanagement.Rank;
import org.cb2384.exactalgebra.text.opmanagement.RealFieldOps;

import org.checkerframework.checker.nullness.qual.*;
import org.checkerframework.checker.signedness.qual.*;
import org.checkerframework.dataflow.qual.*;

abstract sealed class OperativeCommand<A extends U, O extends U, U extends AlgebraObject<U>, R extends Rank<U, R>>
        extends Command<O, U> {
    
    private final AlgebraOp<U, R> op;
    
    private final Command<A, U> receiver;
    
    private final R resultRankCeiling;
    
    private final MethodHandle opHandle;
    
    @SideEffectFree
    private OperativeCommand(
            String source,
            AlgebraOp<U, R> op,
            Command<A, U> receiver,
            Function<Class<?>, MethodType> typeGetter
    ) {
        this(source, op, receiver,
                getResultRank(op, (R) receiver.getResultRank(), null), typeGetter);
    }
    
    @SideEffectFree
    private OperativeCommand(
            String source,
            AlgebraOp<U, R> op,
            Command<A, U> receiver,
            R resultRankCeiling,
            Function<Class<?>, MethodType> typeGetter
    ) {
        super(source, op.flags());
        this.op = op;
        this.receiver = receiver;
        this.resultRankCeiling = resultRankCeiling;
        try {
            opHandle = getHandle(typeGetter);
        } catch (IllegalAccessException | NoSuchMethodException passed) {
            // This is an unrecoverable occurrence, and should not happen with proper testing
            throw new RuntimeException(passed.getMessage(), passed);
        }
    }
    
    @Pure
    private static <U extends AlgebraObject<U>, R extends Rank<U, R>> R getResultRank(
            AlgebraOp<U, R> op,
            R receiverRank,
            @Nullable Rank<?, ?> secondaryRank
    ) {
        return (R) switch (op) {
            case RealFieldOps rfop -> rfop.ceilingRank((NumberRank) receiverRank, secondaryRank);
            case PolynomialOps pop -> pop.ceilingRank((FunctionRank) receiverRank, secondaryRank);
        };
    }
    
    @SideEffectFree
    MethodHandle getHandle(
            Function<Class<?>, MethodType> typeGetter
    ) throws IllegalAccessException, NoSuchMethodException {
        Class<?> recieverClass = receiver.getResultRank().resultingClass();
        Class<?> resultType;
        if (flags.contains(OpFlag.OUTPUT_PAIR)) {
            resultType = switch (resultRankCeiling) {
                case NumberRank ignored -> NumberRemainderPair.class;
                case FunctionRank ignored -> FunctionRemainderPair.class;
                case NumberPairRank ignored -> NumberRemainderPair.class;
                case FunctionPairRank ignored -> FunctionRemainderPair.class;
            };
        } else {
            resultType = resultRankCeiling.resultingClass();
        }
        
        return LOOKUP.findVirtual(recieverClass, op.internalName(), typeGetter.apply(resultType));
    }
    
    @Override
    @Pure
    R getResultRank() {
        return resultRankCeiling;
    }
    
    @Override
    @Pure
    @Unsigned int height() {
        return receiver.height() + 1;
    }
    
    @Override
    public O get() {
        try {
            Object[] args = getArgs();
            AlgebraObject<?> result = (AlgebraObject<?>) switch (args.length) {
                case 1 -> opHandle.invoke(args[0]);
                case 2 -> opHandle.invoke(args[0], args[1]);
                case 3 -> opHandle.invoke(args[0], args[1], args[2]);
                default -> throw new RuntimeException("Number of arguments exceeds max; add new switch branches");
            };
            return (O) result;
        } catch (Throwable old) {
            CommandFormatException newCFE = excFact();
            newCFE.initCause(old);
            throw newCFE;
        }
    }
    
    abstract Object[] getArgs();
    
    @SideEffectFree
    private static MethodType methodTypeBiGetter(
            Class<?> resultType,
            Command<?, ?> secondary
    ) {
        return MethodType.methodType(resultType, secondary.getResultRank().resultingClass());
    }
    
    static final class UnaryCommand<A extends U, O extends U, U extends AlgebraObject<U>, R extends Rank<U, R>>
            extends OperativeCommand<A, O, U, R> {
        
        @SideEffectFree
        UnaryCommand(
                String source,
                AlgebraOp<U, R> op,
                Command<A, U> receiver
        ) {
            super(source, op, receiver, MethodType::methodType);
        }
        
        @Override
        Object[] getArgs() {
            return new Object[]{super.receiver.get()};
        }
    }
    
    static final class BinaryStandardCommand<A extends U, E extends U, O extends U,
                    U extends AlgebraObject<U>, R extends Rank<U, R>>
            extends OperativeCommand<A, O, U, R> {
        
        private final Command<E, U> secondary;
        
        @SideEffectFree
        BinaryStandardCommand(
                String source,
                AlgebraOp<U, R> op,
                Command<A, U> receiver,
                Command<E, U> secondary
        ) {
            super(source,
                    op,
                    receiver,
                    OperativeCommand.getResultRank(op, (R) receiver.getResultRank(), secondary.getResultRank()),
                    c -> OperativeCommand.methodTypeBiGetter(c, secondary)
            );
            this.secondary = secondary;
        }
        
        @Override
        @Pure
        @Unsigned int height() {
            return super.height() + secondary.height();
        }
        
        @Override
        Object[] getArgs() {
            return new Object[]{super.receiver.get(), secondary.get()};
        }
    }
    
    static final class BinaryPrimCommand<A extends U, O extends U,
                    U extends AlgebraObject<U>, R extends Rank<U, R>>
            extends OperativeCommand<A, O, U, R> {
        
        static final int MAX_INT_STRING_LENGTH = Integer.toString(Integer.MAX_VALUE).length();
        
        static final int MAX_LONG_STRING_LENGTH = Long.toString(Long.MAX_VALUE).length();
        
        private final @Nullable Object secondary;
        
        @SideEffectFree
        BinaryPrimCommand(
                String source,
                AlgebraOp<U, R> op,
                Command<A, U> receiver,
                @Nullable Object secondary,
                Class<?> secondaryClass
        ) {
            super(source, op, receiver, c -> MethodType.methodType(c, secondaryClass));
            assert secondaryClass.isInstance(secondary);
            this.secondary = secondary;
        }
        
        @Override
        Object[] getArgs() {
            return new Object[]{super.receiver.get(), secondary};
        }
    }
    
    static final class BinaryFunctionAndNumberCommand<I extends Polynomial<?>, N extends AlgebraNumber,
                    O extends Polynomial<?>>
            extends OperativeCommand<I, O, Polynomial<?>, FunctionRank> {
        
        private final Command<N, AlgebraNumber> secondary;
        
        @SideEffectFree
        BinaryFunctionAndNumberCommand(
                String source,
                AlgebraOp<Polynomial<?>, FunctionRank> op,
                Command<I, Polynomial<?>> receiver,
                Command<N, AlgebraNumber> secondary
        ) {
            super(source,
                    op,
                    receiver,
                    OperativeCommand.getResultRank(op, (FunctionRank) receiver.getResultRank(),
                            secondary.getResultRank() ),
                    c -> OperativeCommand.methodTypeBiGetter(c, secondary)
            );
            this.secondary = secondary;
        }
        
        @Override
        @Pure
        @Unsigned int height() {
            return super.height() + secondary.height();
        }
        
        @Override
        Object[] getArgs() {
            return new Object[]{super.receiver.get(), secondary.get()};
        }
    }
    
    static final class TrinaryCommand<A extends U, E extends U, O extends U,
            U extends AlgebraObject<U>, R extends Rank<U, R>>
            extends OperativeCommand<A, O, U, R> {
        
        private final Command<E, U> secondary;
        
        private final @Nullable Object third;
        
        @SideEffectFree
        TrinaryCommand(
                String source,
                AlgebraOp<U, R> op,
                Command<A, U> receiver,
                Command<E, U> secondary,
                @Nullable Object third,
                Class<?> thirdClass
        ) {
            super(source,
                    op,
                    receiver,
                    OperativeCommand.getResultRank(op, (R) receiver.getResultRank(), secondary.getResultRank()),
                    c -> MethodType.methodType(c, secondary.getResultRank().resultingClass(), thirdClass)
            );
            this.secondary = secondary;
            this.third = third;
        }
        
        @Override
        @Pure
        @Unsigned int height() {
            return super.height() + secondary.height();
        }
        
        @Override
        Object[] getArgs() {
            return new Object[]{super.receiver.get(), secondary.get(), third};
        }
    }
    
    static final class TrinaryDoublePrimCommand<A extends AlgebraNumber, O extends AlgebraNumber>
            extends OperativeCommand<A, O, AlgebraNumber, NumberRank> {
        
        private final @Nullable Object secondary;
        
        private final @Nullable Object third;
        
        @SideEffectFree
        TrinaryDoublePrimCommand(
                String source,
                AlgebraOp<AlgebraNumber, NumberRank> op,
                Command<A, AlgebraNumber> receiver,
                @Nullable Object secondary,
                Class<?> secondaryClass,
                @Nullable Object third,
                Class<?> thirdClass
        ) {
            super(source, op, receiver, c -> MethodType.methodType(c, secondaryClass, thirdClass));
            this.secondary = secondary;
            this.third = third;
        }
        
        @Override
        Object[] getArgs() {
            return new Object[]{super.receiver.get(), secondary, third};
        }
    }
}
