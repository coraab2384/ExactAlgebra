package org.cb2384.exactalgebra.text;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.cb2384.exactalgebra.util.MiscUtils;
import org.cb2384.exactalgebra.util.corutils.StringUtils;

import org.checkerframework.dataflow.qual.*;

public enum OpNames
        implements Identifier<OpNames, String> {
    NEGATED("negate"),
    MAGNITUDE("absoluteValue", "absVal", "abs"),
    INVERTED("invert", "reciprocal"),
    SUM("add"),
    DIFFERENCE("diff", "subtract", "minus"),
    PRODUCT("prod", "multiply", "times"),
    QUOTIENT("div", "divide"),
    REMAINDER("remain"),
    SQUARED("^2", "square"),
    MAX("maximum"),
    MIN("minimum"),
    QUOTIENT_WITH_REMAINDER("quoWithRemain", "quotientWithRemain", "quoWithRemainder"),
    SQRT_WITH_REMAINDER("sqrtWithRemain", "squareRootWithRemainder", "squareRootWithRemain",
            "sqtWithRemainder", "sqtWithRemain"),
    ROOT_WITH_REMAINDER("rootWithRemain"),
    EXP_WITH_REMAINDER("expWithRemain"),
    POWER_WITH_REMAINDER("powerWithRemain", "raisedWithRemainder", "raisedWithRemain"),
    LN_WITH_REMAINDER("lnWithRemain", "natLogWithRemainder", "natLogWithRemain"),
    LOG_BASE_WITH_REMAINDER("logBaseWithRemain", "logWithRemainder", "logWithRemain",
            "logarithmWithRemainder", "logarithemWithRemain"),
    POWER("raised", "^"),
    POWER_REMAINDER("raisedRemainder", "powerRemain", "raisedRemain"),
    SQRT("squareRoot", "sqt"),
    SQRT_REMAINDER("squareRootRemainder", "sqrtRemain", "squareRootRemain",
            "sqtRemainder", "sqtRemain"),
    ROOT,
    ROOT_REMAINDER("rootRemain"),
    EXP("e^"),
    EXP_REMAINDER("expRemain"),
    LN("natLog"),
    LN_REMAINDER("lnRemain", "natLogRemainder", "natLogRemain"),
    LOG_BASE("log", "logarithm"),
    LOG_BASE_REMAINDER("logBaseRemain", "logRemain", "logRemainder",
            "logarithmRemainder", "logarithmRemain"),
    ROUND,
    SCALE,
    FACTOR;
    
    public static final Set<String> OP_NAMES
            = (Set<String>) Identifier.identifiersFor(EnumSet.allOf(OpNames.class));
    
    private static final Pattern COMPLETE_PATTERN
            = Identifier.groupedPatternCompiler( Arrays.asList(OpNames.values()) );
    
    private final Set<String> EXTERNAL_NAMES;
    
    private final Pattern pattern;
    
    @SideEffectFree
    OpNames() {
        EXTERNAL_NAMES = Set.of(StringUtils.toCamelCase(name()));
        pattern = Identifier.patternOrCompiler(EXTERNAL_NAMES);
    }
    
    @SideEffectFree
    OpNames(
            String secondName
    ) {
        EXTERNAL_NAMES = Set.of(StringUtils.toCamelCase(name()), secondName);
        pattern = Identifier.patternOrCompiler(EXTERNAL_NAMES);
    }
    
    @SideEffectFree
    OpNames(
            String... namesMinusOne
    ) {
        EXTERNAL_NAMES = MiscUtils.streamOf(StringUtils.toCamelCase(name()), namesMinusOne)
                .collect(Collectors.toUnmodifiableSet());
        pattern = Identifier.patternOrCompiler(EXTERNAL_NAMES);
    }
    
    @Override
    @Pure
    public final Set<String> identifiers() {
        return EXTERNAL_NAMES;
    }
    
    @Override
    @Pure
    public final Set<String> enumIdentifiers() {
        return OP_NAMES;
    }
    
    @Override
    @Pure
    public final Pattern asPattern() {
        return pattern;
    }
    
    @Override
    @Pure
    public final Pattern enumPattern() {
        return COMPLETE_PATTERN;
    }
}
