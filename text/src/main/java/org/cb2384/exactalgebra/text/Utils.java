package org.cb2384.exactalgebra.text;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.cb2384.exactalgebra.util.MiscUtils;
import org.cb2384.exactalgebra.util.corutils.StringUtils;

import org.checkerframework.dataflow.qual.*;

/**
 * <p>These constants enumerate the strings that indicate special types of commands. Specifically,
 * these are utility commands, defined as being a command that does not actually do any math.</p>
 *
 * @author Corinne Buxton
 */
public enum Utils
        implements Identifier<Utils, String> {
    CREATE("new"),
    WRITE("cache", "store"),
    GET("retrieve"),
    LOAD,
    SAVE,
    PRINT("show"),
    DELETE,
    EXIT("quit", "close");
    
    public static final Set<String> UTIL_NAMES
            = (Set<String>) Identifier.identifiersFor(EnumSet.allOf(Utils.class));
    
    private static final Pattern COMPLETE_PATTERN
            = Identifier.groupedPatternCompiler( Arrays.asList(Utils.values()) );
    
    public final Set<String> EXTERNAL_NAMES;
    
    private final Pattern pattern;
    
    @SideEffectFree
    Utils() {
        EXTERNAL_NAMES = Set.of(StringUtils.toCamelCase(name()));
        pattern = Identifier.patternOrCompiler(EXTERNAL_NAMES);
    }
    
    @SideEffectFree
    Utils(
            String secondName
    ) {
        EXTERNAL_NAMES = Set.of(StringUtils.toCamelCase(name()).toLowerCase(), secondName);
        pattern = Identifier.patternOrCompiler(EXTERNAL_NAMES);
    }
    
    @SideEffectFree
    Utils(
            String... namesMinusOne
    ) {
        EXTERNAL_NAMES = MiscUtils.streamOf(StringUtils.toCamelCase(name()), namesMinusOne)
                .collect(Collectors.toUnmodifiableSet());
        pattern = Identifier.patternOrCompiler(EXTERNAL_NAMES);
    }
    
    @Override
    @Pure
    public Set<String> identifiers() {
        return EXTERNAL_NAMES;
    }
    
    @Override
    @Pure
    public Set<String> enumIdentifiers() {
        return UTIL_NAMES;
    }
    
    @Override
    @Pure
    public Pattern asPattern() {
        return pattern;
    }
    
    @Override
    @Pure
    public Pattern enumPattern() {
        return COMPLETE_PATTERN;
    }
}
