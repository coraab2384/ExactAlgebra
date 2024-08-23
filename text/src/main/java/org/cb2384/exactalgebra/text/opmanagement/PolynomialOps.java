package org.cb2384.exactalgebra.text.opmanagement;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import org.cb2384.exactalgebra.objects.relations.polynomial.Polynomial;
import org.cb2384.exactalgebra.text.OpNames;
import org.cb2384.exactalgebra.text.opmanagement.FunctionRank.FunctionalType;
import org.cb2384.exactalgebra.text.opmanagement.PairRank.FunctionPairRank;
import org.cb2384.exactalgebra.text.opmanagement.PairRank.NumberPairRank;
import org.cb2384.exactalgebra.util.MiscUtils;
import org.cb2384.exactalgebra.util.corutils.StringUtils;

import org.checkerframework.checker.nullness.qual.*;
import org.checkerframework.dataflow.qual.*;

public enum PolynomialOps
        implements AlgebraOp<Polynomial<?>, FunctionRank> {
    NEGATED(
            EnumSet.of(OpFlag.UNARY, OpFlag.FUNCTIONAL, OpFlag.RELATIONAL)
    ) {
        @Override
        @Pure
        public final FunctionRank ceilingRank(
                FunctionRank left,
                @Nullable Rank<?, ?> right
        ) {
            return FunctionRank.rankFrom(
                    left.functionalType(),
                    NumberRank.INTEGER.ceiling(left.coefficientRank())
            );
        }
    },
    ABSOLUTE_VALUE(
            OpNames.MAGNITUDE,
            EnumSet.of(OpFlag.UNARY, OpFlag.UNARY_REVERSIBLE, OpFlag.FUNCTIONAL, OpFlag.RELATIONAL)
    ) {
        @Override
        @Pure
        public final FunctionRank ceilingRank(
                FunctionRank left,
                @Nullable Rank<?, ?> right
        ) {
            return FunctionRank.rankFrom(
                    left.functionalType(),
                    RealFieldOps.MAGNITUDE.ceilingRank(left.coefficientRank(), null)
            );
        }
    },
    SUM(
            EnumSet.of(OpFlag.FUNCTIONAL, OpFlag.RELATIONAL)
    ) {
        @Override
        @Pure
        public final FunctionRank ceilingRank(
                FunctionRank left,
                Rank<?, ?> right
        ) {
            return PolynomialOps.defaultCeilingWithFloor(left, right, NumberRank.INTEGER);
        }
    },
    DIFFERENCE(
            EnumSet.of(OpFlag.FUNCTIONAL, OpFlag.RELATIONAL)
    ) {
        @Override
        @Pure
        public final FunctionRank ceilingRank(
                FunctionRank left,
                Rank<?, ?> right
        ) {
            return PolynomialOps.defaultCeilingWithFloor(left, right, NumberRank.INTEGER);
        }
    },
    SCALE(
            EnumSet.of(OpFlag.SECOND_ARG_PRIM_OPTION, OpFlag.FUNCTIONAL, OpFlag.RELATIONAL)
    ) {
        @Override
        @Pure
        public final FunctionRank ceilingRank(
                FunctionRank left,
                Rank<?, ?> right
        ) {
            return FunctionRank.rankFrom(
                    left.functionalType(),
                    left.coefficientRank().ceiling((NumberRank) right)
            );
        }
    },
    PRODUCT(
            EnumSet.of(OpFlag.FUNCTIONAL, OpFlag.RELATIONAL)
    ) {
        @Override
        @Pure
        public final FunctionRank ceilingRank(
                FunctionRank left,
                Rank<?, ?> right
        ) {
            return left.ceiling((FunctionRank) right);
        }
    },
    QUOTIENT_Z(
            OpNames.QUOTIENT,
            EnumSet.of(OpFlag.TRUNCATING, OpFlag.OUTPUT_POLY, OpFlag.FUNCTIONAL, OpFlag.RELATIONAL)
    ) {
        @Override
        @Pure
        public final FunctionRank ceilingRank(
                FunctionRank left,
                Rank<?, ?> right
        ) {
            return PolynomialOps.defaultCeilingWithFloor(left, right, NumberRank.RATIONAL);
        }
    },
    REMAINDER(
            EnumSet.of(OpFlag.TRUNCATING, OpFlag.FUNCTIONAL, OpFlag.RELATIONAL)
    ) {
        @Override
        @Pure
        public final FunctionRank ceilingRank(
                FunctionRank left,
                Rank<?, ?> right
        ) {
            return PolynomialOps.defaultCeilingWithFloor(left, right, NumberRank.NATURAL_0);
        }
    },
    SQUARED(
            EnumSet.of(OpFlag.UNARY, OpFlag.UNARY_REVERSIBLE, OpFlag.FUNCTIONAL, OpFlag.RELATIONAL)
    ) {
        @Override
        @Pure
        public final FunctionRank ceilingRank(
                FunctionRank left,
                @Nullable Rank<?, ?> right
        ) {
            FunctionalType resultFT = (left.functionalType() != FunctionalType.LINEAR)
                    ? left.functionalType()
                    : FunctionalType.POLYNOMIAL;
            return FunctionRank.rankFrom(resultFT, left.coefficientRank());
        }
    },
    RAISED_Z(
            EnumSet.of(OpFlag.SECOND_ARG_PRIM, OpFlag.SECOND_ARG_PRIM_OPTION,
                    OpFlag.FUNCTIONAL, OpFlag.RELATIONAL)
    ) {
        @Override
        @Pure
        public final FunctionRank ceilingRank(
                FunctionRank left,
                @Nullable Rank<?, ?> right
        ) {
            FunctionalType resultFT = (left.functionalType() != FunctionalType.LINEAR)
                    ? left.functionalType()
                    : FunctionalType.POLYNOMIAL;
            return FunctionRank.rankFrom(resultFT, left.coefficientRank());
        }
    },
    MAX(
            EnumSet.of(OpFlag.FUNCTIONAL, OpFlag.RELATIONAL)
    ) {
        @Override
        @Pure
        public final FunctionRank ceilingRank(
                FunctionRank left,
                Rank<?, ?> right
        ) {
            return switch (right) {
                case NumberRank rightNR -> {
                    if (left.functionalType() != FunctionalType.CONSTANT) {
                        yield left;
                    }
                    yield FunctionRank.rankFrom(
                            FunctionalType.CONSTANT,
                            rightNR.ceiling(left.coefficientRank())
                    );
                }
                case FunctionRank rightFR -> left.ceiling(rightFR);
                case NumberPairRank rightNPR -> {
                    if (left.functionalType() != FunctionalType.CONSTANT) {
                        yield left;
                    }
                    yield FunctionRank.rankFrom(
                            FunctionalType.CONSTANT,
                            left.coefficientRank().ceiling(rightNPR.valueRank())
                    );
                }
                case FunctionPairRank rightFPR -> left.ceiling(rightFPR.valueRank());
            };
        }
    },
    MIN(
            EnumSet.of(OpFlag.FUNCTIONAL, OpFlag.RELATIONAL)
    ) {
        @Override
        @Pure
        public final FunctionRank ceilingRank(
                FunctionRank left,
                Rank<?, ?> right
        ) {
            return switch (right) {
                case NumberRank rightNR -> {
                    if (left.functionalType() != FunctionalType.CONSTANT) {
                        yield left;
                    }
                    yield FunctionRank.rankFrom(
                            FunctionalType.CONSTANT,
                            rightNR.ceiling(left.coefficientRank())
                    );
                }
                case FunctionRank rightFR -> left.ceiling(rightFR);
                case NumberPairRank rightNPR -> {
                    if (left.functionalType() != FunctionalType.CONSTANT) {
                        yield left;
                    }
                    yield FunctionRank.rankFrom(
                            FunctionalType.CONSTANT,
                            left.coefficientRank().ceiling(rightNPR.valueRank())
                    );
                }
                case FunctionPairRank rightFPR -> left.ceiling(rightFPR.valueRank());
            };
        }
    },
    QUOTIENT_Z_WITH_REMAINDER(
            OpNames.QUOTIENT_WITH_REMAINDER,
            EnumSet.of(OpFlag.OUTPUT_PAIR, OpFlag.OUTPUT_POLY, OpFlag.FUNCTIONAL, OpFlag.RELATIONAL)
    ) {
        @Override
        @Pure
        public final FunctionRank ceilingRank(
                FunctionRank left,
                Rank<?, ?> right
        ) {
            return PolynomialOps.defaultCeilingWithFloor(left, right, NumberRank.RATIONAL);
        }
    },
    FACTOR(
            EnumSet.of(OpFlag.UNARY, OpFlag.FUNCTIONAL, OpFlag.RELATIONAL, OpFlag.OUTPUT_PAIR)
    ) {
        @Override
        @Pure
        public final FunctionRank ceilingRank(
                FunctionRank left,
                Rank<?, ?> right
        ) {
            return PolynomialOps.defaultCeilingWithFloor(left, right, NumberRank.RATIONAL);
        }
    };
    
    private final String internalName = StringUtils.toCamelCase(name());
    
    private final OpNames externalNameKey;
    
    private final Set<OpFlag> flags;
    
    @SideEffectFree
    PolynomialOps(
            EnumSet<OpFlag> unfinishedFlags
    ) {
        externalNameKey = OpNames.valueOf(name());
        flags = Collections.unmodifiableSet(unfinishedFlags);
    }
    
    @SideEffectFree
    PolynomialOps(
            OpNames externalNameKey,
            EnumSet<OpFlag> unfinishedFlags
    ) {
        this.externalNameKey = externalNameKey;
        flags = Collections.unmodifiableSet(unfinishedFlags);
    }
    
    @Pure
    private static FunctionRank defaultCeilingWithFloor(
            FunctionRank left,
            Rank<?, ?> right,
            NumberRank floor
    ) {
        return FunctionRank.rankFrom(
                left.functionalType().ceiling( ((FunctionRank) right).functionalType() ),
                floor.ceiling(left.coefficientRank()).ceiling( ((FunctionRank) right).coefficientRank() )
        );
    }
    
    @Override
    @Pure
    public final Set<OpFlag> flags() {
        return flags;
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
}
