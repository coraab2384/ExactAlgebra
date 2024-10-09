package org.cb2384.exactalgebra.text.opmanagement;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;

import org.cb2384.exactalgebra.objects.numbers.AlgebraNumber;
import org.cb2384.exactalgebra.text.OpNames;
import org.cb2384.exactalgebra.util.corutils.StringUtils;
import org.cb2384.exactalgebra.util.corutils.ternary.Signum;

import org.checkerframework.checker.nullness.qual.*;
import org.checkerframework.dataflow.qual.*;

public enum RealFieldOps
        implements AlgebraOp<AlgebraNumber, NumberRank> {
    // Integer Ring ops
    NEGATED(
            EnumSet.of(OpFlag.UNARY),
            Rank.simpleCeilingMap(false, NumberRank.INTEGER)
    ),
    MAGNITUDE(
            EnumSet.of(OpFlag.UNARY, OpFlag.UNARY_REVERSIBLE),
            (NumberRank r, @Nullable NumberRank ignored) -> switch (r) {
                case INTEGER -> NumberRank.NATURAL_0;
                case COMPLEX_INTEGRAL, COMPLEX_RATIONAL, COMPLEX_RADICAL -> NumberRank.REAL_RADICAL;
                case COMPLEX_TRANSCENDENTAL, COMPLEX_COMPLEX -> NumberRank.REAL_TRANSCENDENTAL;
                default -> r;
            }
    ),
    SUM(NumberRank.INTEGER),
    DIFFERENCE(NumberRank.INTEGER),
    PRODUCT(null),
    QUOTIENT_Z(
            OpNames.QUOTIENT,
            EnumSet.of(OpFlag.TRUNCATING, OpFlag.OUTPUT_INTEGER),
            NumberRank.INTEGER.compareToThisAndThen(true, Signum.NEGATIVE,
                    null, NumberRank.INTEGER)
    ),
    REMAINDER(NumberRank.NATURAL_0),
    SQUARED(true, null),
    
    // Integer misc ops
    RAISED_Z(
            OpNames.POWER,
            EnumSet.of(OpFlag.SECOND_ARG_PRIM, OpFlag.SECOND_ARG_PRIM_OPTION),
            Rank.simpleCeilingMap(false, null)
    ),
    MAX(
            EnumSet.of(OpFlag.SECOND_ARG_PRIM, OpFlag.SECOND_ARG_PRIM_OPTION),
            Rank.simpleCeilingMap(false, null)
    ),
    MIN(
            EnumSet.of(OpFlag.SECOND_ARG_PRIM, OpFlag.SECOND_ARG_PRIM_OPTION),
            Rank.simpleCeilingMap(false, null)
    ),
    
    //Integer pair ops
    QUOTIENT_Z_WITH_REMAINDER(
            OpNames.QUOTIENT_WITH_REMAINDER,
            EnumSet.of(OpFlag.OUTPUT_INTEGER, OpFlag.OUTPUT_PAIR),
            NumberRank.INTEGER.compareToThisAndThen(true, Signum.NEGATIVE,
                    null, NumberRank.INTEGER)
    ),
    SQRT_Z_WITH_REMAINDER(
            OpNames.SQRT_WITH_REMAINDER,
            EnumSet.of(OpFlag.UNARY, OpFlag.UNARY_REVERSIBLE, OpFlag.OUTPUT_INTEGER, OpFlag.OUTPUT_PAIR),
            Rank.simpleCeilingMap(true, NumberRank.NATURAL_0)
    ),
    ROOT_Z_WITH_REMAINDER(
            OpNames.ROOT_WITH_REMAINDER,
            EnumSet.of(OpFlag.SECOND_ARG_PRIM, OpFlag.SECOND_ARG_PRIM_OPTION,
                    OpFlag.OUTPUT_INTEGER, OpFlag.OUTPUT_PAIR),
            Rank.simpleCeilingMap(true, NumberRank.NATURAL_0)
    ),
    EXP_Z_WITH_REMAINDER(
            OpNames.EXP_WITH_REMAINDER,
            EnumSet.of(OpFlag.UNARY, OpFlag.UNARY_REVERSIBLE, OpFlag.OUTPUT_INTEGER, OpFlag.OUTPUT_PAIR),
            Rank.simpleCeilingMap(true, NumberRank.REAL_TRANSCENDENTAL)
    ),
    POWER_Z_WITH_REMAINDER(
            OpNames.POWER_WITH_REMAINDER,
            EnumSet.of(OpFlag.OUTPUT_INTEGER, OpFlag.OUTPUT_PAIR),
            NumberRank.POWER_Z_REM_MAP
    ),
    LN_Z_WITH_REMAINDER(
            OpNames.LN_WITH_REMAINDER,
            EnumSet.of(OpFlag.UNARY, OpFlag.UNARY_REVERSIBLE, OpFlag.OUTPUT_INTEGER, OpFlag.OUTPUT_PAIR),
            Rank.simpleCeilingMap(true, NumberRank.REAL_TRANSCENDENTAL)
    ),
    LOG_BASE_Z_WITH_REMAINDER(
            OpNames.LOG_BASE_WITH_REMAINDER,
            EnumSet.of(OpFlag.OUTPUT_INTEGER, OpFlag.OUTPUT_PAIR),
            Rank.simpleCeilingMap(false, NumberRank.REAL_TRANSCENDENTAL)
    ),
    
    //Integer Rounding ops
    ROUND_Z(
            OpNames.ROUND,
            EnumSet.of(OpFlag.SECOND_ARG_PRIM, OpFlag.LAST_ARG_OPTIONAL, OpFlag.ROUNDING, OpFlag.OUTPUT_INTEGER),
            NumberRank.INTEGER.compareToThisAndThen(false, Signum.NEGATIVE,
                    null, NumberRank.INTEGER)
    ),
    QUOTIENT_ROUND_Z(
            OpNames.QUOTIENT,
            EnumSet.of(OpFlag.TRINARY, OpFlag.LAST_ARG_OPTIONAL, OpFlag.ROUNDING, OpFlag.OUTPUT_INTEGER),
            NumberRank.INTEGER.compareToThisAndThen(true, Signum.NEGATIVE,
                    null, NumberRank.INTEGER)
    ),
    SQRT_ROUND_Z(
            OpNames.SQRT,
            EnumSet.of(OpFlag.SECOND_ARG_PRIM, OpFlag.LAST_ARG_OPTIONAL, OpFlag.ROUNDING, OpFlag.OUTPUT_INTEGER),
            NumberRank.NATURAL_1.compareToThisAndThen(false, Signum.ZERO,
                    NumberRank.NATURAL_1, NumberRank.NATURAL_0)
    ),
    SQRT_Z_REMAINDER(
            OpNames.SQRT_REMAINDER,
            EnumSet.of(OpFlag.UNARY, OpFlag.UNARY_REVERSIBLE, OpFlag.TRUNCATING),
            Rank.simpleCeilingMap(true, NumberRank.NATURAL_0)
    ),
    ROOT_ROUND_Z(
            OpNames.ROOT,
            EnumSet.of(OpFlag.SECOND_ARG_PRIM, OpFlag.SECOND_ARG_PRIM_OPTION, OpFlag.TRINARY,
                    OpFlag.LAST_ARG_OPTIONAL, OpFlag.ROUNDING, OpFlag.OUTPUT_INTEGER),
            NumberRank.NATURAL_1.compareToThisAndThen(false, Signum.ZERO,
                    NumberRank.NATURAL_1, NumberRank.NATURAL_0)
    ),
    ROOT_Z_REMAINDER(
            OpNames.ROOT_REMAINDER,
            EnumSet.of(OpFlag.SECOND_ARG_PRIM, OpFlag.SECOND_ARG_PRIM_OPTION, OpFlag.TRUNCATING),
            Rank.simpleCeilingMap(true, NumberRank.NATURAL_0)
    ),
    EXP_ROUND_Z(
            OpNames.EXP,
            EnumSet.of(OpFlag.SECOND_ARG_PRIM, OpFlag.LAST_ARG_OPTIONAL, OpFlag.ROUNDING, OpFlag.OUTPUT_INTEGER),
            NumberRank.NATURAL_0.compareToThisAndThen(false, Signum.POSITIVE,
                    NumberRank.NATURAL_0, NumberRank.NATURAL_1)
    ),
    EXP_Z_REMAINDER(
            OpNames.EXP_REMAINDER,
            EnumSet.of(OpFlag.UNARY, OpFlag.UNARY_REVERSIBLE, OpFlag.TRUNCATING),
            Rank.simpleCeilingMap(true, NumberRank.REAL_TRANSCENDENTAL)
    ),
    POWER_ROUND_Z(
            OpNames.POWER,
            EnumSet.of(OpFlag.TRINARY, OpFlag.LAST_ARG_OPTIONAL, OpFlag.ROUNDING, OpFlag.OUTPUT_INTEGER),
            NumberRank.NATURAL_0.compareToThisAndThen(false, Signum.POSITIVE,
                    NumberRank.INTEGER, null)
    ),
    POWER_Z_REMAINDER(
            OpNames.POWER_REMAINDER,
            EnumSet.of(OpFlag.TRUNCATING),
            NumberRank.POWER_Z_REM_MAP
    ),
    LN_ROUND_Z(
            OpNames.LN,
            EnumSet.of(OpFlag.SECOND_ARG_PRIM, OpFlag.LAST_ARG_OPTIONAL, OpFlag.ROUNDING, OpFlag.OUTPUT_INTEGER),
            NumberRank.NATURAL_0.compareToThisAndThen(false, Signum.POSITIVE,
                    NumberRank.INTEGER, null)
    ),
    LN_Z_REMAINDER(
            OpNames.LN_REMAINDER,
            EnumSet.of(OpFlag.UNARY, OpFlag.UNARY_REVERSIBLE, OpFlag.TRUNCATING),
            Rank.simpleCeilingMap(true, NumberRank.REAL_TRANSCENDENTAL)
    ),
    LOG_BASE_ROUND_Z(
            OpNames.LOG_BASE,
            EnumSet.of(OpFlag.TRINARY, OpFlag.LAST_ARG_OPTIONAL, OpFlag.ROUNDING, OpFlag.OUTPUT_INTEGER),
            NumberRank.NATURAL_0.compareToThisAndThen(false, Signum.POSITIVE,
                    NumberRank.INTEGER, null)
    ),
    LOG_BASE_Z_REMAINDER(
            OpNames.LOG_BASE_REMAINDER,
            EnumSet.of(OpFlag.TRUNCATING),
            Rank.simpleCeilingMap(false, NumberRank.REAL_TRANSCENDENTAL)
    ),
    
    //Rational Field ops
    RAISED(
            OpNames.POWER,
            EnumSet.of(OpFlag.SECOND_ARG_PRIM, OpFlag.SECOND_ARG_PRIM_OPTION),
            (base, exponent) -> (exponent == NumberRank.INTEGER)
                    ? base.ceiling(NumberRank.RATIONAL)
                    : base
    ),
    INVERTED(true, NumberRank.RATIONAL),
    QUOTIENT(NumberRank.RATIONAL),
    
    //Rational pair ops
    SQRT_Q_WITH_REMAINDER(
            OpNames.SQRT_WITH_REMAINDER,
            EnumSet.of(OpFlag.SECOND_ARG_PRIM, OpFlag.LAST_ARG_OPTIONAL,
                    OpFlag.OUTPUT_RAT_NUM, OpFlag.OUTPUT_PAIR),
            Rank.simpleCeilingMap(true, NumberRank.RATIONAL)
    ),
    ROOT_Q_WITH_REMAINDER(
            OpNames.ROOT_WITH_REMAINDER,
            EnumSet.of(OpFlag.TRINARY, OpFlag.LAST_ARG_OPTIONAL, OpFlag.OUTPUT_RAT_NUM, OpFlag.OUTPUT_PAIR),
            Rank.simpleCeilingMap(true, NumberRank.RATIONAL)
    ),
    EXP_Q_WITH_REMAINDER(
            OpNames.EXP_WITH_REMAINDER,
            EnumSet.of(OpFlag.SECOND_ARG_PRIM, OpFlag.LAST_ARG_OPTIONAL,
                    OpFlag.OUTPUT_RAT_NUM, OpFlag.OUTPUT_PAIR),
            Rank.simpleCeilingMap(true, NumberRank.REAL_TRANSCENDENTAL)
    ),
    POWER_Q_WITH_REMAINDER(
            OpNames.POWER_WITH_REMAINDER,
            EnumSet.of(OpFlag.TRINARY, OpFlag.LAST_ARG_OPTIONAL,
                    OpFlag.OUTPUT_RAT_NUM, OpFlag.OUTPUT_PAIR),
            NumberRank.POWER_Q_REM_MAP
    ),
    LN_Q_WITH_REMAINDER(
            OpNames.LN_WITH_REMAINDER,
            EnumSet.of(OpFlag.SECOND_ARG_PRIM, OpFlag.LAST_ARG_OPTIONAL,
                    OpFlag.OUTPUT_RAT_NUM, OpFlag.OUTPUT_PAIR),
            Rank.simpleCeilingMap(true, NumberRank.REAL_TRANSCENDENTAL)
    ),
    LOG_BASE_Q_WITH_REMAINDER(
            OpNames.LOG_BASE_WITH_REMAINDER,
            EnumSet.of(OpFlag.OUTPUT_RAT_NUM, OpFlag.OUTPUT_PAIR),
            Rank.simpleCeilingMap(false, NumberRank.REAL_TRANSCENDENTAL)
    ),
    
    //Rational Rounding ops
    ROUND_Q(
            OpNames.ROUND,
            EnumSet.of(OpFlag.SECOND_ARG_PRIM, OpFlag.TRINARY, OpFlag.LAST_ARG_OPTIONAL,
                    OpFlag.SECOND_LAST_ARG_OPTIONAL, OpFlag.ROUNDING, OpFlag.OUTPUT_RADICAL),
            NumberRank.Q_VALUED_RANK_MAP
    ),
    SQRT_ROUND_Q(
            OpNames.SQRT,
            EnumSet.of(OpFlag.SECOND_ARG_PRIM, OpFlag.LAST_ARG_OPTIONAL,
                    OpFlag.ROUNDING, OpFlag.OUTPUT_RAT_NUM),
            NumberRank.Q_VALUED_RANK_MAP
    ),
    SQRT_Q_REMAINDER(
            OpNames.SQRT_REMAINDER,
            EnumSet.of(OpFlag.SECOND_ARG_PRIM, OpFlag.LAST_ARG_OPTIONAL, OpFlag.TRUNCATING),
            Rank.simpleCeilingMap(true, NumberRank.RATIONAL)
    ),
    ROOT_ROUND_Q(
            OpNames.ROOT,
            EnumSet.of(OpFlag.SECOND_ARG_PRIM, OpFlag.SECOND_ARG_PRIM_OPTION, OpFlag.TRINARY,
                    OpFlag.LAST_ARG_OPTIONAL, OpFlag.ROUNDING, OpFlag.OUTPUT_RAT_NUM),
            NumberRank.Q_VALUED_RANK_MAP
    ),
    ROOT_Q_REMAINDER(
            OpNames.ROOT_REMAINDER,
            EnumSet.of(OpFlag.SECOND_ARG_PRIM, OpFlag.SECOND_ARG_PRIM_OPTION, OpFlag.TRUNCATING),
            Rank.simpleCeilingMap(true, NumberRank.RATIONAL)
    ),
    EXP_ROUND_Q(
            OpNames.EXP,
            EnumSet.of(OpFlag.SECOND_ARG_PRIM, OpFlag.LAST_ARG_OPTIONAL,
                    OpFlag.ROUNDING, OpFlag.OUTPUT_RAT_NUM),
            NumberRank.Q_VALUED_RANK_MAP
    ),
    EXP_Q_REMAINDER(
            OpNames.EXP_REMAINDER,
            EnumSet.of(OpFlag.SECOND_ARG_PRIM, OpFlag.LAST_ARG_OPTIONAL, OpFlag.TRUNCATING),
            Rank.simpleCeilingMap(true, NumberRank.REAL_TRANSCENDENTAL)
    ),
    POWER_ROUND_Q(
            OpNames.POWER,
            EnumSet.of(OpFlag.TRINARY, OpFlag.LAST_ARG_OPTIONAL, OpFlag.ROUNDING, OpFlag.OUTPUT_RAT_NUM),
            NumberRank.Q_VALUED_RANK_MAP
    ),
    POWER_Q_REMAINDER(
            OpNames.POWER_REMAINDER,
            EnumSet.of(OpFlag.TRINARY, OpFlag.LAST_ARG_OPTIONAL, OpFlag.TRUNCATING),
            NumberRank.POWER_Q_REM_MAP
    ),
    LN_ROUND_Q(
            OpNames.LN,
            EnumSet.of(OpFlag.SECOND_ARG_PRIM, OpFlag.LAST_ARG_OPTIONAL,
                    OpFlag.ROUNDING, OpFlag.OUTPUT_RAT_NUM),
            NumberRank.Q_VALUED_RANK_MAP
    ),
    LN_Q_REMAINDER(
            OpNames.LN_REMAINDER,
            EnumSet.of(OpFlag.SECOND_ARG_PRIM, OpFlag.LAST_ARG_OPTIONAL, OpFlag.TRUNCATING),
            Rank.simpleCeilingMap(true, NumberRank.REAL_TRANSCENDENTAL)
    ),
    LOG_BASE_ROUND_Q(
            OpNames.LOG_BASE,
            EnumSet.of(OpFlag.TRINARY, OpFlag.LAST_ARG_OPTIONAL, OpFlag.ROUNDING, OpFlag.OUTPUT_RAT_NUM),
            NumberRank.Q_VALUED_RANK_MAP
    ),
    LOG_BASE_Q_REMAINDER(
            OpNames.LOG_BASE_REMAINDER,
            EnumSet.of(OpFlag.TRINARY, OpFlag.LAST_ARG_OPTIONAL, OpFlag.TRUNCATING),
            Rank.simpleCeilingMap(false, NumberRank.REAL_TRANSCENDENTAL)
    ),
    
    //Real ops
    SQRT(true, NumberRank.REAL_RADICAL),
    ROOT(NumberRank.REAL_RADICAL),
    EXP(true, NumberRank.REAL_TRANSCENDENTAL),
    POWER(NumberRank.REAL_TRANSCENDENTAL),
    LN(true, NumberRank.REAL_TRANSCENDENTAL),
    LOG_BASE(NumberRank.REAL_TRANSCENDENTAL);
    
    private final String internalName = StringUtils.toCamelCase(name());
    
    private final OpNames externalNameKey;
    
    private final Set<OpFlag> flags;
    
    private final BiFunction<NumberRank, @PolyNull NumberRank, NumberRank> rankMap;
    
    @SideEffectFree
    RealFieldOps(
            @Nullable NumberRank floorRank
    ) {
        this(null, Set.of(), Rank.simpleCeilingMap(false, floorRank));
    }
    
    @SideEffectFree
    RealFieldOps(
            boolean isReversible,
            @Nullable NumberRank floorRank
    ) {
        this(null, unaryOpFlagBuilder(isReversible), Rank.simpleCeilingMap(true, floorRank));
    }
    
    @SideEffectFree
    RealFieldOps(
            EnumSet<OpFlag> unfinishedFlags,
            BiFunction<NumberRank, @PolyNull NumberRank, NumberRank> rankMap
    ) {
        this(null, Collections.unmodifiableSet(unfinishedFlags), rankMap);
    }
    
    @SideEffectFree
    RealFieldOps(
            @Nullable OpNames externalNameKey,
            EnumSet<OpFlag> unfinishedFlags,
            BiFunction<NumberRank, @PolyNull NumberRank, NumberRank> rankMap
    ) {
        this(externalNameKey, Collections.unmodifiableSet(unfinishedFlags), rankMap);
    }
    
    @SideEffectFree
    RealFieldOps(
            @Nullable OpNames externalNameKey,
            Set<OpFlag> flags,
            BiFunction<NumberRank, @PolyNull NumberRank, NumberRank> rankMap
    ) {
        assert !(flags instanceof EnumSet<?>);
        this.externalNameKey = Objects.requireNonNullElseGet(externalNameKey, () -> OpNames.valueOf(name()));
        this.flags = flags;
        this.rankMap = rankMap;
    }
    
    private static final Set<OpFlag> UNARY_REVERSIBLE_BARE_UNMODIFIABLE = Collections.unmodifiableSet(
            EnumSet.of(OpFlag.UNARY, OpFlag.UNARY_REVERSIBLE)
    );
    
    @SideEffectFree
    private static Set<OpFlag> unaryOpFlagBuilder(
            boolean isReversible
    ) {
        return isReversible
                ? UNARY_REVERSIBLE_BARE_UNMODIFIABLE
                : Collections.unmodifiableSet( EnumSet.of(OpFlag.UNARY) );
    }
    
    @Override
    @Pure
    public final String internalName() {
        return internalName;
    }
    
    @Override
    @Pure
    public final Set<String> externalNames() {
        return externalNameKey.identifiers();
    }
    
    @Override
    @Pure
    public final boolean matches(
            String name
    ) {
        return externalNameKey.asMatcher(name).matches();
    }
    
    @Override
    @Pure
    public final Set<OpFlag> flags() {
        return flags;
    }
    
    @Override
    @Pure
    public final NumberRank ceilingRank(
            NumberRank left,
            @Nullable Rank<?, ?> right
    ) {
        return rankMap.apply(left, (NumberRank) right);
    }
}
