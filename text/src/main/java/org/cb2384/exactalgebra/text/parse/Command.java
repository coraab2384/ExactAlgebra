package org.cb2384.exactalgebra.text.parse;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.cb2384.exactalgebra.objects.AlgebraObject;
import org.cb2384.exactalgebra.text.Identifier;
import org.cb2384.exactalgebra.text.opmanagement.OpFlag;
import org.cb2384.exactalgebra.text.opmanagement.Rank;
import org.cb2384.exactalgebra.util.corutils.StringUtils;

import org.checkerframework.checker.nullness.qual.*;
import org.checkerframework.checker.signedness.qual.*;
import org.checkerframework.common.value.qual.*;
import org.checkerframework.dataflow.qual.*;

/**
 * <p>An individual command is a node in the chain of commands that any input invariably results in.
 * Each command supplies a result to its parent, when called to do so, creating a possibly-bifrucating pipeline
 * that can be recursively queried.</p>
 *
 * <p>Broadly, commands can be categorized as either {@link CreationCommand}, {@link OperativeCommand},
 * or {@link UtilCommand}; however, this is only the beginning. Each of those have further subclasses, depending on,
 * for example, the number of arguments a command has.</p>
 *
 * @param <O>   The maximal type of the result; the type that {@link #get()} returns
 * @param <U>   The overarching type of {@link AlgebraObject}, such as
 *              {@link org.cb2384.exactalgebra.objects.numbers.AlgebraNumber}
 *
 * @author Corinne Buxton
 */
public sealed abstract class Command<O extends U, U extends AlgebraObject<U>>
        implements Supplier<O>
        permits CreationCommand, OperativeCommand, UtilCommand {
    
    /**
     * <p>These are names that are specially reserved for specific meanings. They are not commands, and do not
     * need the {@link org.cb2384.exactalgebra.text.ReservedSymbols#COMMAND_KEY} prepended. They are rather treated
     * as names that, instead of referring to a previously-saved object, have a specific meaning.</p>
     */
    public enum ReservedNames
            implements Identifier<ReservedNames, String> {
        /**
         * When taken as an argument for {@link org.cb2384.exactalgebra.text.Utils#PRINT} or
         * {@link org.cb2384.exactalgebra.text.Utils#SAVE}, this prints or saves everything. Its use
         * in any other context is a {@link CommandFormatException}.
         */
        ALL,
        /**
         * In any context, this refers to the answer from the previously-executed command. The previous command
         * must have been an {@link OperativeCommand} or one of the {@link UtilCommand}s that results in a returned,
         * value (so, for example, not {@link org.cb2384.exactalgebra.text.Utils#SAVE}).
         */
        ANS,
        /**
         * When determining a type of rounding, or a class of number, for relevant arguments, such as printing all
         * integers
         */
        INT,
        /**
         * When determining a type of rounding, or a class of number, for relevant arguments, such as printing all
         * rationals
         */
        RAT,
        /**
         * When determining a type of function, such as for printing all polynomials
         */
        POLY;
        
        /**
         * The set of all names reserved under this enum
         */
        public static final Set<String> RESERVED_NAMES
                = (Set<String>) Identifier.identifiersFor( EnumSet.allOf(ReservedNames.class) );
        
        private static final Pattern COMPLETE_PATTERN
                = Identifier.groupedPatternCompiler( Arrays.asList(ReservedNames.values()) );
        
        /**
         * The set of all names for this particular type (in this particular {@link Identifier} implementation,
         * each set is in fact a singleton)
         */
        public final Set<String> EXTERNAL_NAMES;
        
        private final Pattern pattern;
        
        @SideEffectFree
        ReservedNames() {
            EXTERNAL_NAMES = Set.of(StringUtils.toCamelCase(name()));
            pattern = Identifier.patternOrCompiler(EXTERNAL_NAMES);
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        @Pure
        public Set<String> identifiers() {
            return EXTERNAL_NAMES;
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        @Pure
        public Set<String> enumIdentifiers() {
            return RESERVED_NAMES;
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        @Pure
        public Pattern asPattern() {
            return pattern;
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        @Pure
        public Pattern enumPattern() {
            return COMPLETE_PATTERN;
        }
    }
    
    /**
     * Used specifically in static nested class {@link ExecutionResult}, but put here in the enclosing class
     * for easier access. This string in the execution result slot indicates that "quit"
     * or a synonym has been called, and signals the program to quit.
     */
    public static final String CLOSE_KEY_STRING = "\tCL0SE";
    
    static final String IO_EXC_MSG = "IO exception in saving or loading";
    
    static final MethodHandles.Lookup LOOKUP = MethodHandles.publicLookup();
    
    private static final Pattern DISALLOWED_NAMES
            = Identifier.patternOrCompiler(Identifier.allIdentifiers());
    
    final String source;
    
    final Set<OpFlag> flags;
    
    Command(
            String source,
            Set<OpFlag> flags
    ) {
        this.source = source;
        this.flags = flags;
    }
    
    abstract Rank<U, ? extends Rank<U, ?>> getResultRank();
    
    @Pure
    @Unsigned int height() {
        return 1;
    }
    
    @Pure
    static @Nullable Boolean parseBool(
            String toParse
    ) {
        return switch (toParse.toLowerCase()) {
            case "true", "yes", "y" -> Boolean.TRUE;
            case "false", "no", "n" -> Boolean.FALSE;
            default -> null;
        };
    }
    
    @Pure
    static boolean parseBoolPrim(
            String toParse,
            Supplier<? extends RuntimeException> ifCannot
    ) {
        Boolean bool = parseBool(toParse);
        if (bool == null) {
            throw ifCannot.get();
        }
        return bool;
    }
    
    static @IntRange(from = Character.MIN_RADIX, to = Character.MAX_RADIX) int getRadix(
            String arg,
            Supplier<? extends RuntimeException> ifCannot
    ) {
        int radix;
        try {
            radix = Integer.parseInt(arg);
        } catch (NumberFormatException oldNFE) {
            RuntimeException newExc = ifCannot.get();
            throw (RuntimeException) newExc.initCause(oldNFE);
        }
        if ((Character.MIN_RADIX <= radix) && (radix <= Character.MAX_RADIX)) {
            return radix;
        }
        throw ifCannot.get();
    }
    
    static @Nullable Function<String, RuntimeException> nullIfNameAvailable(
            String name,
            boolean overwrite,
            Interfacer instance
    ) {
        Matcher matcher = DISALLOWED_NAMES.matcher(name);
        if (matcher.find() && (matcher.start() == 0)) {
            return string -> CommandFormatException.forInputStringWithFocus(string, name);
        }
        if (!overwrite && instance.namePresent(name)) {
            return string -> CommandStateException.nameUnavailable(name);
        }
        return null;
    }
    
    @SideEffectFree
    CommandFormatException excFact() {
        return CommandFormatException.forInputString(source);
    }
    
    static final class ExecutionResult
            implements AlgebraObject<ExecutionResult> {
        
        static final ExecutionResult CLOSE = new ExecutionResult(CLOSE_KEY_STRING);
        
        static final ExecutionResult SUCCESS = new ExecutionResult(null);
        
        private static final ExecutionResult IO_FAILURE = new ExecutionResult(IO_EXC_MSG);
        
        private final @Nullable String errorMsg;
        
        @SideEffectFree
        private ExecutionResult(
                @Nullable String errorMsg
        ) {
            this.errorMsg = errorMsg;
        }
        
        @SideEffectFree
        static ExecutionResult fromMsg(
                @Nullable String errorMsg
        ) {
            if (errorMsg == null) {
                return SUCCESS;
            }
            if (IO_EXC_MSG.equals(errorMsg)) {
                return IO_FAILURE;
            }
            if (CLOSE_KEY_STRING.equals(errorMsg)) {
                return CLOSE;
            }
            return new ExecutionResult(errorMsg);
        }
        
        @Override
        @Pure
        public boolean isZero() {
            return errorMsg == null;
        }
        
        @Override
        @Pure
        public boolean isOne() {
            return IO_EXC_MSG.equals(errorMsg);
        }
        
        @Override
        @Pure
        public boolean isNegative() {
            return (errorMsg != null) && !errorMsg.equals(IO_EXC_MSG);
        }
        
        @Override
        @Pure
        public boolean equiv(
                ExecutionResult that
        ) {
            return (isZero() == that.isZero()) && (isOne() == that.isOne());
        }
        
        @Override
        @Pure
        public boolean equals(
                Object obj
        ) {
            return (this == obj) || (
                    (obj instanceof ExecutionResult erObj) && Objects.equals(errorMsg, erObj.errorMsg) );
        }
        
        @Override
        @Pure
        public int hashCode() {
            return Objects.hashCode(errorMsg);
        }
        
        @Override
        @Pure
        public String toString() {
            return "Execution Result: " + Objects.requireNonNullElse(errorMsg, "Success!");
        }
        
        @Override
        @Pure
        public String toString(
                @IntRange(from = Character.MIN_RADIX, to = Character.MAX_RADIX) int radix
        ) {
            return toString();
        }
    }
}
