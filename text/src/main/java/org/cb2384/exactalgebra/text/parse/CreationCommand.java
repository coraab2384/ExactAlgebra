package org.cb2384.exactalgebra.text.parse;

import java.util.Collections;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import org.cb2384.exactalgebra.objects.AlgebraObject;
import org.cb2384.exactalgebra.objects.numbers.AlgebraNumber;
import org.cb2384.exactalgebra.objects.numbers.integral.AlgebraInteger;
import org.cb2384.exactalgebra.objects.numbers.rational.Rational;
import org.cb2384.exactalgebra.objects.relations.polynomial.PolyRat;
import org.cb2384.exactalgebra.objects.relations.polynomial.Polynomial;
import org.cb2384.exactalgebra.text.opmanagement.FunctionRank;
import org.cb2384.exactalgebra.text.opmanagement.NumberRank;
import org.cb2384.exactalgebra.text.opmanagement.OpFlag;
import org.cb2384.exactalgebra.util.corutils.NullnessUtils;

import org.checkerframework.checker.nullness.qual.*;
import org.checkerframework.dataflow.qual.*;

abstract sealed class CreationCommand<O extends U, U extends AlgebraObject<U>>
        extends Command<O, U> {
    
    private final O value;
    
    @SideEffectFree
    private CreationCommand(
            String source,
            O value,
            Function<? super U, Set<OpFlag>> flagSupplier
    ) {
        super(source, flagSupplier.apply(value));
        this.value = value;
        assert source.equals(source.trim());
    }
    
    @Override
    @Pure
    public O get() {
        return value;
    }
    
    @SideEffectFree
    private static Set<OpFlag> numFlagsBuilder(
            AlgebraNumber value
    ) {
        return Collections.unmodifiableSet( SavedAO.numberFlagsModifiable(value) );
    }
    
    @SideEffectFree
    private static Polynomial<?> fromStrings(
            Stream<String> strings
    ) {
        return PolyRat.fromLittleEndianArray(strings
                .map(s -> (Rational) AlgebraNumber.valueOf(s))
                .toArray(Rational[]::new)
        );
    }
    
    @SideEffectFree
    private static Set<OpFlag> funcFlagsBuilder(
            Polynomial<?> value
    ) {
        return Collections.unmodifiableSet( SavedAO.functionFlagsModifiable(value) );
    }
    
    @SideEffectFree
    static CreationCommand<?, ?> numberCreationCommand(
            String source,
            Interfacer instance,
            String firstArg,
            @Nullable String secondArg,
            @Nullable Boolean overwrite
    ) {
        AlgebraNumber number;
        if (secondArg == null) {
            try {
                number = AlgebraNumber.valueOf(firstArg);
            } catch (NumberFormatException oldNFE) {
                CommandFormatException newCFE = CommandFormatException.forInputString(source);
                newCFE.initCause(oldNFE);
                throw newCFE;
            }
            return switch (number) {
                case AlgebraInteger integer -> new NumberCreationCommand<>(source, integer);
                case Rational rat -> new NumberCreationCommand<>(source, rat);
                default -> new NumberCreationCommand<>(source, number);
            };
        }
        
        boolean overwriteOk = NullnessUtils.nullToFalse(overwrite);
        String name;
        try {
            number = AlgebraNumber.valueOf(firstArg);
            Function<String, RuntimeException> excThrower = nullIfNameAvailable(secondArg, overwriteOk, instance);
            if (excThrower != null) {
                throw excThrower.apply(source);
            }
            name = secondArg;
        } catch (NumberFormatException oldNFE) {
            try {
                number = AlgebraNumber.valueOf(secondArg);
            } catch (NumberFormatException suppressedNFE) {
                CommandFormatException newCFE = CommandFormatException.forInputString(source);
                newCFE.initCause(oldNFE);
                newCFE.addSuppressed(suppressedNFE);
                throw newCFE;
            }
            Function<String, RuntimeException> excThrower = nullIfNameAvailable(firstArg, overwriteOk, instance);
            if (excThrower != null) {
                RuntimeException newRE = excThrower.apply(source);
                throw (RuntimeException) newRE.initCause(oldNFE);
            }
            name = firstArg;
        }
        
        return switch (number) {
            case AlgebraInteger integer -> new NamedNumberCreationCommand<>(source, integer,
                    name, overwriteOk, instance);
            case Rational rat -> new NamedNumberCreationCommand<>(source, rat, name, overwriteOk, instance);
            default -> new NamedNumberCreationCommand<>(source, number, name, overwriteOk, instance);
        };
    }
    
    static final class NumberCreationCommand<N extends AlgebraNumber>
            extends CreationCommand<N, AlgebraNumber> {
        
        @SideEffectFree
        NumberCreationCommand(
                String source,
                N number
        ) {
            super(source, number, CreationCommand::numFlagsBuilder);
        }
        
        @Override
        @Pure
        NumberRank getResultRank() {
            return NumberRank.rankOf(super.value);
        }
    }
    
    static final class NamedNumberCreationCommand<N extends AlgebraNumber>
            extends CreationCommand<N, AlgebraNumber> {
        
        private final String name;
        
        private final Interfacer instance;
        
        @SideEffectFree
        NamedNumberCreationCommand(
                String source,
                N number,
                String name,
                boolean overwrite,
                Interfacer instance
        ) {
            super(source, number, CreationCommand::numFlagsBuilder);
            Function<String, RuntimeException> okIfNull = nullIfNameAvailable(name, overwrite, instance);
            if (okIfNull != null) {
                throw okIfNull.apply(source);
            }
            this.name = name;
            this.instance = instance;
        }
        
        @Override
        @Pure
        NumberRank getResultRank() {
            return NumberRank.rankOf(super.value);
        }
        
        @Override
        @Pure
        public N get() {
            instance.add(name, new SavedAO.SavedNumber<N>(super.value, getResultRank()));
            return super.value;
        }
    }
    
    static final class FunctionCreationCommand<F extends Polynomial<N>, N extends AlgebraNumber>
            extends CreationCommand<F, Polynomial<?>> {
        
        @SideEffectFree
        FunctionCreationCommand(
                String source,
                Stream<String> coefficients
        ) {
            super(source, (F) fromStrings(coefficients), CreationCommand::funcFlagsBuilder);
        }
        
        @Override
        @Pure
        FunctionRank getResultRank() {
            return FunctionRank.rankOf(super.value);
        }
    }
    
    static final class NamedFunctionCreationCommand<F extends Polynomial<N>, N extends AlgebraNumber>
            extends CreationCommand<F, Polynomial<?>> {
        
        private final String name;
        
        private final Interfacer instance;
        
        @SideEffectFree
        NamedFunctionCreationCommand(
                String source,
                String name,
                Stream<String> coefficients,
                boolean overwrite,
                Interfacer instance
        ) {
            super(source, (F) fromStrings(coefficients), CreationCommand::funcFlagsBuilder);
            Function<String, RuntimeException> okIfNull = nullIfNameAvailable(name, overwrite, instance);
            if (okIfNull != null) {
                throw okIfNull.apply(source);
            }
            this.name = name;
            this.instance = instance;
        }
        
        @Override
        @Pure
        FunctionRank getResultRank() {
            return FunctionRank.rankOf(super.value);
        }
        
        @Override
        @Pure
        public F get() {
            instance.add(name, new SavedAO.SavedFunction<>(super.value, getResultRank()));
            return super.value;
        }
    }
}
