package org.cb2384.exactalgebra.text.parse;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import org.cb2384.exactalgebra.objects.AlgebraObject;
import org.cb2384.exactalgebra.text.opmanagement.OpFlag;
import org.cb2384.exactalgebra.text.opmanagement.Rank;
import org.cb2384.exactalgebra.util.corutils.NullnessUtils;

import org.checkerframework.checker.nullness.qual.*;
import org.checkerframework.common.value.qual.*;
import org.checkerframework.dataflow.qual.*;

public sealed abstract class UtilCommand<O extends U, U extends AlgebraObject<U>>
        extends Command<O, U> {
    
    private static final Set<OpFlag> FLAGS = Set.of(OpFlag.UTILITY);
    
    private final Interfacer instance;
    
    UtilCommand(
            String source,
            Interfacer instance
    ) {
        super(source, FLAGS);
        this.instance = instance;
    }
    
    @Override
    @Pure
    Rank<U, ? extends Rank<U, ?>> getResultRank() {
        throw excFact();
    }
    
    static final class PrintCommand
            extends UtilCommand<ExecutionResult, ExecutionResult> {
        
        private final @IntRange(from = Character.MIN_RADIX, to = Character.MAX_RADIX) int radix;
        
        private final Command<?, ?> toPrintResult;
        
        PrintCommand(
                String source,
                @IntRange(from = Character.MIN_RADIX, to = Character.MAX_RADIX) int radix,
                Command<?, ?> toPrintResult,
                Interfacer instance
        ) {
            super(source, instance);
            this.radix = radix;
            this.toPrintResult = toPrintResult;
        }
        
        @Override
        public ExecutionResult get() {
            return super.instance.printExternal(toPrintResult.get(), radix);
        }
    }
    
    static final class PrintRetrievedCommand
            extends UtilCommand<ExecutionResult, ExecutionResult> {
        
        private final Supplier<ExecutionResult> printer;
        
        PrintRetrievedCommand(
                String source,
                Interfacer instance,
                String@ArrayLenRange(from = 1, to = 3)... args
        ) {
            super(source, instance);
            printer = instance.getPrinter(args, source);
        }
        
        @Override
        public ExecutionResult get() {
            return printer.get();
        }
    }
    
    static final class WriteCommand
            extends UtilCommand<ExecutionResult, ExecutionResult> {
        
        private final String name;
        
        private final Command<?, ?> toWrite;
        
        WriteCommand(
                String source,
                String name,
                @Nullable Command<?, ?> toWrite,
                @Nullable Boolean overwrite,
                Interfacer instance
        ) {
            super(source, instance);
            Function<String, RuntimeException> okIfNull
                    = nullIfNameAvailable(name, NullnessUtils.nullToFalse(overwrite), instance);
            if (okIfNull != null) {
                throw okIfNull.apply(source);
            }
            this.name = name;
            if (toWrite == null) {
                toWrite = instance.getLastCommand();
                if (toWrite == null) {
                    throw excFact();
                }
            }
            this.toWrite = toWrite;
        }
        
        @Override
        public ExecutionResult get() {
            super.instance.add(name, SavedAO.asSavedAO(toWrite.get()));
            return ExecutionResult.SUCCESS;
        }
    }
    
    static final class SaveCommand
            extends UtilCommand<ExecutionResult, ExecutionResult> {
        
        private final Supplier<ExecutionResult> saver;
        
        @SideEffectFree
        SaveCommand(
                String source,
                Interfacer instance,
                String@ArrayLen({1, 2, 3})... args
        ) {
            super(source, instance);
            saver = instance.getSaver(args, source);
        }
        
        @Override
        @SideEffectFree
        public ExecutionResult get() {
            return saver.get();
        }
    }
    
    static final class SaveWriteCommand
            extends UtilCommand<ExecutionResult, ExecutionResult> {
        
        private final WriteCommand writer;
        
        private final SaveCommand saver;
        
        SaveWriteCommand(
                String source,
                String name,
                @Nullable Command<?, ?> toWrite,
                Interfacer instance,
                String@ArrayLen({1, 2, 3})... args
        ) {
            super(source, instance);
            @Nullable Boolean overwrite = (args.length >= 2)
                    ? parseBoolPrim(args[1], this::excFact)
                    : null;
            writer = new WriteCommand(source, name, toWrite, overwrite, instance);
            saver = new SaveCommand(source, instance, args);
        }
        
        @Override
        public ExecutionResult get() {
            ExecutionResult firstResult = writer.get();
            return firstResult.isZero()
                    ? saver.get()
                    : firstResult;
        }
    }
    
    static final class LoadCommand
            extends UtilCommand<ExecutionResult, ExecutionResult> {
        
        private final Path loadPath;
        
        LoadCommand(
                String source,
                Interfacer instance,
                String@ArrayLen({1, 2})[] args
        ) {
            super(source, instance);
            loadPath = instance.parsePath(source, args);
        }
        
        @Override
        public ExecutionResult get() {
            Exception exc = super.instance.load(loadPath);
            return (exc == null)
                    ? ExecutionResult.SUCCESS
                    : ExecutionResult.fromMsg(exc.getLocalizedMessage());
        }
    }
    
    static final class RetrieveCommand<O extends U, U extends AlgebraObject<U>, R extends Rank<U, R>>
            extends UtilCommand<O, U> {
        
        private final String target;
        
        private @MonotonicNonNull SavedAO<R, O, U> retrieved;
        
        @SideEffectFree
        RetrieveCommand(
                String source,
                String target,
                Interfacer instance
        ) {
            super(source, instance);
            if (!instance.namePresent(target)) {
                throw excFact();
            }
            this.target = target;
        }
        
        @Override
        R getResultRank() {
            SavedAO<R, O, U> retrieved = this.retrieved;
            if (retrieved != null) {
                return retrieved.type();
            }
            retrieved = (this.retrieved = (SavedAO<R, O, U>) super.instance.retrieve(target));
            if (retrieved != null) {
                return retrieved.type();
            }
            throw excFact();
        }
        
        @Override
        public O get() {
            if (retrieved == null) {
                retrieved = (SavedAO<R, O, U>) super.instance.retrieve(target);
                if (retrieved == null) {
                    throw excFact();
                }
            }
            return retrieved.value();
        }
    }
    
    static final class LoadAndRetrieveCommand<O extends U, U extends AlgebraObject<U>>
            extends UtilCommand<O, U> {
        
        private final Path loadPath;
        
        private @MonotonicNonNull SavedAO<?, O, U> loaded;
        
        LoadAndRetrieveCommand(
                String source,
                Interfacer instance,
                String@ArrayLen({1, 2})[] args
        ) {
            super(source, instance);
            loadPath = instance.parsePath(source, args);
        }
        
        @Override
        @SideEffectFree
        Rank<U, ? extends Rank<U, ?>> getResultRank() {
            SavedAO<?, O, U> retrieved = loaded;
            if (retrieved != null) {
                return (Rank<U, ? extends Rank<U,?>>) retrieved.type();
            }
            try {
                SavedAO<?, ?, ?> loaded = super.instance.directLoad(loadPath);
                this.loaded = (SavedAO<?, O, U>) loaded;
                return (Rank<U, ? extends Rank<U,?>>) loaded.type();
            } catch (Exception oldE) {
                CommandStateException newCSE = new CommandStateException("Cannot load type!");
                throw (CommandStateException) newCSE.initCause(oldE);
            }
        }
        
        @Override
        public O get() {
            try {
                return (loaded != null)
                        ? loaded.value()
                        : (O) super.instance.directLoad(loadPath).value();
            } catch (IOException oldIOE) {
                return (O) ExecutionResult.fromMsg(oldIOE.getLocalizedMessage());
            }
        }
    }
    
    static final class DeletionCommand
            extends UtilCommand<ExecutionResult, ExecutionResult> {
        
        private final @Nullable String target;
        
        DeletionCommand(
                String source,
                String target,
                Interfacer instance
        ) {
            super(source, instance);
            if (ReservedNames.ALL.reserves(target)) {
                this.target = null;
            } else if (!instance.namePresent(target)) {
                throw excFact();
            } else {
                this.target = target;
            }
        }
        
        @Override
        public ExecutionResult get() {
            return super.instance.delete(target);
        }
    }
    
    static final class Close
            extends UtilCommand<ExecutionResult, ExecutionResult> {
        
        Close(
                String source,
                Interfacer instance
        ) {
            super(source, instance);
        }
        
        @Override
        public ExecutionResult get() {
            return ExecutionResult.CLOSE;
        }
    }
}
