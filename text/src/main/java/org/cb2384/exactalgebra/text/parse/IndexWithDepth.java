package org.cb2384.exactalgebra.text.parse;

import org.cb2384.exactalgebra.text.ReservedSymbols;
import org.cb2384.exactalgebra.util.corutils.ternary.ComparableSwitchSignum;

import org.checkerframework.checker.index.qual.*;
import org.checkerframework.common.value.qual.*;
import org.checkerframework.dataflow.qual.*;

sealed interface IndexWithDepth
        extends ComparableSwitchSignum<IndexWithDepth> {
    
    @Pure
    @NonNegative int startInclusive();
    
    @Pure
    ReservedSymbols symbolType();
    
    @Pure
    @GTENegativeOne int depth();
    
    @Override
    @Pure
    default int compareTo(
            IndexWithDepth o
    ) {
        return Integer.compare(startInclusive(), o.startInclusive());
    }
    
    @SideEffectFree
    static IndexWithDepth dummyIndex(
            int index
    ) {
        return new StartWithDepth(index, null, 0);
    }
    
    record StartWithDepth(
            @NonNegative int startInclusive,
            ReservedSymbols symbolType,
            @NonNegative int depth
    ) implements IndexWithDepth {
        
        @SideEffectFree
        StartWithDepth(
                @GTENegativeOne int@ArrayLen(5)[] startEndDepthArray,
                ReservedSymbols startType
        ) {
            this(startEndDepthArray[0], startType, startEndDepthArray[4]);
        }
    }
    
    record RangeWithDepth(
            @NonNegative int startInclusive,
            ReservedSymbols symbolType,
            @NonNegative int depth,
            @NonNegative int endExclusive
    ) implements IndexWithDepth {
        
        @SideEffectFree
        RangeWithDepth(
                @NonNegative int@ArrayLen(5)[] startEndDepthArray,
                ReservedSymbols startType
        ) {
            this(startEndDepthArray[0], startType, startEndDepthArray[4], startEndDepthArray[3]);
        }
    }
}
