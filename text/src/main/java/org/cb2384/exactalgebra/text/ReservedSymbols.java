package org.cb2384.exactalgebra.text;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.checkerframework.checker.nullness.qual.*;
import org.checkerframework.dataflow.qual.*;

public enum ReservedSymbols
        implements Identifier<ReservedSymbols, String> {
    COMMAND_KEY("\\"),
    OBJECT_GROUP("(") {
        
        private static final Set<String> secondarySymbols = Set.of(")");
        
        private static final Pattern secondaryPattern = Identifier.patternOrCompiler(secondarySymbols);
        
        @Override
        @Pure
        public final boolean reservedSecondary(
                char toTest
        ) {
            return secondaryPattern.matcher(Character.toString(toTest)).matches();
        }
        
        @Override
        @Pure
        public final boolean reservedSecondary(
                int toTest
        ) {
            return secondaryPattern.matcher(Character.toString(toTest)).matches();
        }
        
        @Override
        @Pure
        public final boolean reservedSecondary(
                CharSequence toTest
        ) {
            return secondaryPattern.matcher(toTest).matches();
        }
        
        @Override
        @Pure
        public final Set<String> secondaryIdentifier() {
            return secondarySymbols;
        }
        
        @Override
        @SideEffectFree
        public final Pattern secondaryPattern() {
            return secondaryPattern;
        }
    },
    LIST_GROUP("[") {
        
        private static final Set<String> secondarySymbols = Set.of("]");
        
        private static final Pattern secondaryPattern = Identifier.patternOrCompiler(secondarySymbols);
        
        @Override
        @Pure
        public final boolean reservedSecondary(
                char toTest
        ) {
            return secondaryPattern.matcher(Character.toString(toTest)).matches();
        }
        
        @Override
        @Pure
        public final boolean reservedSecondary(
                int toTest
        ) {
            return secondaryPattern.matcher(Character.toString(toTest)).matches();
        }
        
        @Override
        @Pure
        public final boolean reservedSecondary(
                CharSequence toTest
        ) {
            return secondaryPattern.matcher(toTest).matches();
        }
        
        @Override
        @Pure
        public final Set<String> secondaryIdentifier() {
            return secondarySymbols;
        }
        
        @Override
        @SideEffectFree
        public final Pattern secondaryPattern() {
            return secondaryPattern;
        }
    },
    ARG_GROUP("{") {
        
        private static final Set<String> secondarySymbols = Set.of("}");
        
        private static final Pattern secondaryPattern = Identifier.patternOrCompiler(secondarySymbols);
        
        @Override
        @Pure
        public final boolean reservedSecondary(
                char toTest
        ) {
            return secondaryPattern.matcher(Character.toString(toTest)).matches();
        }
        
        @Override
        @Pure
        public final boolean reservedSecondary(
                int toTest
        ) {
            return secondaryPattern.matcher(Character.toString(toTest)).matches();
        }
        
        @Override
        @Pure
        public final boolean reservedSecondary(
                CharSequence toTest
        ) {
            return secondaryPattern.matcher(toTest).matches();
        }
        
        @Override
        @Pure
        public final Set<String> secondaryIdentifier() {
            return secondarySymbols;
        }
        
        @Override
        @SideEffectFree
        public final Pattern secondaryPattern() {
            return secondaryPattern;
        }
    },
    ARG_SEP(","),
    SPACE(" ");
    
    public static final Set<ReservedSymbols> GROUPERS = Collections.unmodifiableSet(
            EnumSet.range(OBJECT_GROUP, ARG_GROUP)
    );
    
    public static final Set<String> RESERVED_SYMBOLS
            = (Set<String>) Identifier.identifiersFor(EnumSet.allOf(ReservedSymbols.class));
    
    private static final Pattern COMPLETE_PATTERN
            = Identifier.groupedPatternCompiler( Arrays.asList(ReservedSymbols.values()) );
    
    private final Set<String> primarySymbols;
    
    private final Pattern primaryPattern;
    
    @SideEffectFree
    ReservedSymbols(
            String... openers
    ) {
        primarySymbols = Set.of(openers);
        primaryPattern = Identifier.patternOrCompiler(primarySymbols);
    }
    
    @Pure
    public boolean reservedSecondary(
            char toTest
    ) {
        return false;
    }
    
    @Pure
    public boolean reservedSecondary(
            int toTest
    ) {
        return false;
    }
    
    @Pure
    public boolean reservedSecondary(
            CharSequence toTest
    ) {
        return false;
    }
    
    @Override
    @Pure
    public final Set<String> identifiers() {
        return primarySymbols;
    }
    
    @Pure
    public Set<@Nullable String> secondaryIdentifier() {
        return Set.of();
    }
    
    @Override
    @Pure
    public final Set<String> enumIdentifiers() {
        return RESERVED_SYMBOLS;
    }
    
    @Override
    @Pure
    public final Pattern asPattern() {
        return primaryPattern;
    }
    
    @SideEffectFree
    public @PolyNull Pattern secondaryPattern() {
        return null;
    }
    
    @Override
    @Pure
    public final Pattern enumPattern() {
        return COMPLETE_PATTERN;
    }
    
    @SideEffectFree
    public static Matcher completeMatcher(
            CharSequence toTest
    ) {
        return COMPLETE_PATTERN.matcher(toTest);
    }
}
