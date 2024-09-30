package org.cb2384.exactalgebra.text.parse;

import static java.util.Map.Entry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.cb2384.exactalgebra.text.ReservedSymbols;
import org.cb2384.exactalgebra.text.parse.IndexWithDepth.RangeWithDepth;
import org.cb2384.exactalgebra.text.parse.IndexWithDepth.StartWithDepth;
import org.cb2384.exactalgebra.util.corutils.Collectionz;
import org.cb2384.exactalgebra.util.corutils.NullnessUtils;
import org.cb2384.exactalgebra.util.corutils.ternary.Signum;

import org.checkerframework.checker.index.qual.*;
import org.checkerframework.checker.nullness.qual.*;
import org.checkerframework.common.value.qual.*;
import org.checkerframework.dataflow.qual.*;

final class LineTree
        extends TreeMap<IndexWithDepth, String> {
    
    private static final Set<ReservedSymbols> ALL_BUT_SPACE = Collections.unmodifiableSet(
            EnumSet.complementOf( EnumSet.of(ReservedSymbols.SPACE) )
    );
    
    private static final List<ReservedSymbols> GROUPERS = ReservedSymbols.GROUPERS
            .stream()
            .toList();
    
    private final String source;
    
    @SideEffectFree
    private LineTree(
            LineTree toCopySource
    ) {
        source = toCopySource.source;
    }
    
    @SideEffectFree
    LineTree(
            String line
    ) {
        source = line.trim();
        
        SortedMap<@GTENegativeOne int@ArrayLen(3)[], ReservedSymbols> keyIndices
                = new TreeMap<>(Comparator.comparingInt(a -> a[0]));
        
        placeAppearances(keyIndices);
        keyIndices.forEach((k, v) -> System.out.println(Arrays.toString(k) + ", " + v));
        populateAndCheckSymmetry(keyIndices);
        int size = size();
        System.out.println(size);
        forEach((k, v) -> System.out.println(k + ", " + v));
        finish();
    }
    
    private void placeAppearances(
            SortedMap<@GTENegativeOne int@ArrayLen(3)[], ReservedSymbols> keyIndices
    ) {
        for (ReservedSymbols symbol : ALL_BUT_SPACE) {
            Matcher finder = symbol.asMatcher(source);
            finder.results().forEach(m -> placeOpener(m, keyIndices, symbol));
            
            if (GROUPERS.contains(symbol)) {
                finder = symbol.secondaryPattern().matcher(source);
                finder.results().forEach(m -> placeCloser(m, keyIndices, symbol));
            }
        }
    }
    
    private static void placeOpener(
            MatchResult match,
            SortedMap<@GTENegativeOne int@ArrayLen(3)[], ReservedSymbols> keyIndices,
            ReservedSymbols symbol
    ) {
        keyIndices.put(buildArrayIndex(match.start()), symbol);
        if (symbol != ReservedSymbols.COMMAND_KEY) {
            keyIndices.put(buildArrayIndex(match.end()), ReservedSymbols.SPACE);
        }
    }
    
    private static void placeCloser(
            MatchResult match,
            SortedMap<@GTENegativeOne int@ArrayLen(3)[], ReservedSymbols> keyIndices,
            ReservedSymbols symbol
    ) {
        keyIndices.put(new int[]{match.start(), 0, -1}, symbol);
        keyIndices.put(buildArrayIndex(match.end()), ReservedSymbols.SPACE);
    }
    
    @SideEffectFree
    private static @GTENegativeOne int@ArrayLen(3)[] buildArrayIndex(
            @NonNegative int start
    ) {
        return new int[]{start, -1, -1};
    }
    
    private void populateAndCheckSymmetry(
            SortedMap<@GTENegativeOne int@ArrayLen(3)[], ReservedSymbols> keyIndices
    ) {
        final class TripleArrayList extends ArrayList<@GTENegativeOne int[]@ArrayLen(3)[]> {
            private static final int WIDTH = GROUPERS.size();
            
            private boolean add(
                    @GTENegativeOne int@ArrayLen(3)[] index,
                    ReservedSymbols grouperType
            ) {
                int[][] depthBox = new int[WIDTH][];
                assert (index[2] == size()) && GROUPERS.contains(grouperType);
                depthBox[GROUPERS.indexOf(grouperType)] = index;
                return add(depthBox);
            }
            
            private boolean place(
                    @GTENegativeOne int@ArrayLen(3)[] index,
                    ReservedSymbols grouperType
            ) {
                assert GROUPERS.contains(grouperType);
                return switch (Signum.valueOf(size() - index[2])) {
                    case POSITIVE -> {
                        int[][] depthBox = get(index[2]);
                        int typeIndex = GROUPERS.indexOf(grouperType);
                        int[] old = depthBox[typeIndex];
                        depthBox[typeIndex] = index;
                        yield old == null;
                    }
                    case ZERO -> add(index, grouperType);
                    case NEGATIVE -> throw CommandFormatException.forInputString(source);
                };
            }
            
            @SideEffectFree
            private @GTENegativeOne int@ArrayLen(3)[] pull(
                    ReservedSymbols grouperType,
                    @GTENegativeOne int depth
            ) {
                int[][] depthBox = get(depth);
                int typeIndex = GROUPERS.indexOf(grouperType);
                int[] result = depthBox[typeIndex];
                depthBox[typeIndex] = null;
                return result;
            }
            
            @Pure
            private boolean isNulled() {
                return parallelStream()
                        .flatMap(Arrays::stream)
                        .allMatch(Objects::isNull);
            }
        }
        if (keyIndices.isEmpty()) {
            throw CommandFormatException.forInputString(source);
        }
        TripleArrayList lastOpeners = new TripleArrayList();
        boolean ignoreNextOpenerForLastOpeners = false;
        int currentDepth = 0;
        
        var keyIndexIter = keyIndices.entrySet().iterator();
        
        while (keyIndexIter.hasNext()) {
            var thisEntry = keyIndexIter.next();
            int[] thisKey = thisEntry.getKey();
            ReservedSymbols thisType = thisEntry.getValue();
            int grouperIndexOfThisType = GROUPERS.indexOf(thisType);
            
            if (grouperIndexOfThisType != -1) {
                //If it is a closer
                if (thisKey[1] == 0) {
                    int[] lastKey = lastOpeners.pull(thisType, currentDepth);
                    if ((--currentDepth < 0) || (lastKey == null)) {
                        throw CommandFormatException.forInputString(source);
                    }
                    lastKey[1] = thisKey[0];
                    IndexWithDepth newIndex = new RangeWithDepth(lastKey, thisType);
                    if (keyIndexIter.hasNext()) {
                        thisEntry = keyIndexIter.next();
                        put(newIndex, source.substring(lastKey[0], thisEntry.getKey()[0]));
                        continue;
                    }
                    
                    put(newIndex, source.substring(lastKey[0]));
                    
                } else if (ignoreNextOpenerForLastOpeners) {
                    thisKey[2] = currentDepth;
                    ignoreNextOpenerForLastOpeners = false;
                    
                } else {
                    thisKey[2] = currentDepth++;
                    lastOpeners.place(thisKey, thisType);
                }
            } else if (thisType == ReservedSymbols.COMMAND_KEY) {
                if (keyIndexIter.hasNext()) {
                    thisEntry = keyIndexIter.next();
                    if (thisEntry.getValue() == ReservedSymbols.ARG_GROUP) {
                        thisKey[2] = currentDepth++;
                        lastOpeners.place(thisKey, thisType);
                        ignoreNextOpenerForLastOpeners = true;
                    } else {
                        thisKey[2] = currentDepth;
                        put(
                                new StartWithDepth(thisKey, thisType),
                                source.substring(thisKey[0], thisEntry.getKey()[0])
                        );
                    }
                    continue;
                }
                thisKey[2] = currentDepth;
                put(
                        new StartWithDepth(thisKey, thisType),
                        source.substring(thisKey[0])
                );
            } else {
                thisKey[2] = currentDepth;
                IndexWithDepth newIndex = new StartWithDepth(thisKey, thisType);
                if (keyIndexIter.hasNext()) {
                    thisEntry = keyIndexIter.next();
                    put(newIndex, source.substring(thisKey[0], thisEntry.getKey()[0]));
                    continue;
                }
                put(newIndex, source.substring(thisKey[0]));
            }
        }
        
        if ((currentDepth != 0) || !lastOpeners.isNulled()) {
            throw CommandFormatException.forInputString(source);
        }
    }
    
    private void finish() {
        int firstIndex = firstKey().startInclusive();
        if (firstIndex != 0) {
            put(
                    new StartWithDepth(0, ReservedSymbols.SPACE, 0),
                    source.substring(0, firstIndex)
            );
        }
        replaceAll((k, v) -> v.trim());
    }
    
    @Pure
    IndexWithDepth ceilingKey(
            int index
    ) {
        return ceilingKey(IndexWithDepth.dummyIndex(index));
    }
    
    @Pure
    IndexWithDepth higherKey(
            int index
    ) {
        return higherKey(IndexWithDepth.dummyIndex(index));
    }
    
    @Pure
    IndexWithDepth floorKey(
            int index
    ) {
        return floorKey(IndexWithDepth.dummyIndex(index));
    }
    
    @Pure
    IndexWithDepth lowerKey(
            int index
    ) {
        return lowerKey(IndexWithDepth.dummyIndex(index));
    }
    
    @SideEffectFree
    private IndexWithDepth dummyIndex0Check(
            int index
    ) {
        return (index == 0) ? firstKey() : IndexWithDepth.dummyIndex(index);
    }
    
    @Pure
    @NonNegative int countByDepth(
            @NonNegative int depth
    ) {
        return countBy(e -> e.getKey().depth() == depth);
    }
    
    @Pure
    @NonNegative int countByDepth(
            @NonNegative int depth,
            @Nullable IndexWithDepth start,
            @Nullable Boolean startInclusive,
            @Nullable IndexWithDepth end,
            @Nullable Boolean endInclusive
    ) {
        return countBy(e -> e.getKey().depth() == depth, start, startInclusive, end, endInclusive);
    }
    
    @Pure
    @NonNegative int countByDepth(
            IndexWithDepth start,
            boolean startInclusive,
            IndexWithDepth end,
            boolean endInclusive,
            @NonNegative int depth
    ) {
        return countBy(start, startInclusive, end, endInclusive, e -> e.getKey().depth() == depth);
    }
    
    @Pure
    @NonNegative int countByCharType(
            ReservedSymbols toCount
    ) {
        return countBy(grouperToOpener(toCount));
    }
    
    @Pure
    @NonNegative int countByCharType(
            ReservedSymbols toCount,
            @Nullable IndexWithDepth start,
            @Nullable Boolean startInclusive,
            @Nullable IndexWithDepth end,
            @Nullable Boolean endInclusive
    ) {
        return countBy(grouperToOpener(toCount), start, startInclusive, end, endInclusive);
    }
    
    @Pure
    @NonNegative int countByCharType(
            ReservedSymbols toCount,
            IndexWithDepth start,
            boolean startInclusive,
            IndexWithDepth end,
            boolean endInclusive
    ) {
        return countBy(start, startInclusive, end, endInclusive, grouperToOpener(toCount));
    }
    
    @SideEffectFree
    private static Predicate<Entry<? extends IndexWithDepth, ?>> grouperToOpener(
            ReservedSymbols potentialGrouper
    ) {
        int grouperIndex = GROUPERS.indexOf(potentialGrouper);
        ReservedSymbols toTest = (grouperIndex % 2 == 1)
                ? GROUPERS.get(grouperIndex - 1)
                : potentialGrouper;
        return e -> e.getKey().symbolType() == toTest;
    }
    
    @Pure
    @NonNegative int countBy(
            Predicate<? super Entry<? extends IndexWithDepth, ? extends String>> toCount
    ) {
        return (int) entrySet()
                .parallelStream()
                .filter(toCount)
                .count();
    }
    
    @Pure
    @NonNegative int countBy(
            Predicate<? super Entry<? extends IndexWithDepth, ? extends String>> toCount,
            @Nullable IndexWithDepth start,
            @Nullable Boolean startInclusive,
            @Nullable IndexWithDepth end,
            @Nullable Boolean endInclusive
    ) {
        if (start == null) {
            if (end == null) {
                return countBy(toCount);
            }
            return countBy(firstKey(), true, end, NullnessUtils.nullToFalse(endInclusive), toCount);
        }
        if (end == null) {
            return countBy(start, NullnessUtils.nullToTrue(startInclusive), lastKey(), true, toCount);
        }
        return countBy(start, NullnessUtils.nullToTrue(startInclusive),
                end, NullnessUtils.nullToFalse(endInclusive), toCount);
    }
    
    @Pure
    @NonNegative int countBy(
            IndexWithDepth start,
            boolean startInclusive,
            IndexWithDepth end,
            boolean endInclusive,
            Predicate<? super Entry<? extends IndexWithDepth, ? extends String>> toCount
    ) {
        return (int) subMap(start, startInclusive, end, endInclusive)
                .entrySet()
                .parallelStream()
                .filter(toCount)
                .count();
    }
    
    @SideEffectFree
    LineTree filterByDepth(
            @NonNegative int depth,
            Signum direction,
            @Nullable Boolean inclusive
    ) {
        return filterBy(keepByDepth(depth, direction, inclusive));
    }
    
    @SideEffectFree
    Stream<String> valuesSequentialFilteredByDepth(
            IndexWithDepth start,
            boolean startInclusive,
            IndexWithDepth end,
            boolean endInclusive,
            @NonNegative int depth,
            Signum direction,
            @Nullable Boolean inclusive
    ) {
        return filteredSequencialStream(start, startInclusive, end, endInclusive,
                        keepByDepth(depth, direction, inclusive))
                .map(Entry::getValue);
    }
    
    @SideEffectFree
    private static Predicate<Entry<? extends IndexWithDepth, ?>> keepByDepth(
            @NonNegative int depth,
            Signum direction,
            @Nullable Boolean inclusive
    ) {
        assert (inclusive != null) || (direction == Signum.ZERO);
        return switch (direction) {
            case POSITIVE -> inclusive
                    ? e -> e.getKey().depth() >= depth
                    : e -> e.getKey().depth() > depth;
            case ZERO -> e -> e.getKey().depth() == depth;
            case NEGATIVE -> inclusive
                    ? e -> e.getKey().depth() <= depth
                    : e -> e.getKey().depth() < depth;
        };
    }
    
    @SideEffectFree
    LineTree filterByCharType(
            ReservedSymbols symbolType
    ) {
        return filterBy(e -> e.getKey().symbolType() == symbolType);
    }
    
    @SideEffectFree
    LineTree filterBy(
            Predicate<? super Entry<? extends IndexWithDepth, ? extends String>> filterCriterion
    ) {
        return entrySet()
                .parallelStream()
                .filter(filterCriterion)
                .collect(this::emptyCopy, Collectionz::put, Collectionz::putAllUnordered);
    }
    
    @SideEffectFree
    LineTree filterBy(
            Predicate<? super Entry<? extends IndexWithDepth, ? extends String>> filterCriterion,
            @Nullable IndexWithDepth start,
            @Nullable Boolean startInclusive,
            @Nullable IndexWithDepth end,
            @Nullable Boolean endInclusive
    ) {
        if (start == null) {
            if (end == null) {
                return filterBy(filterCriterion);
            }
            return filterBy(firstKey(), true, end,
                    NullnessUtils.nullToFalse(endInclusive), filterCriterion);
        }
        if (end == null) {
            return filterBy(start, NullnessUtils.nullToTrue(startInclusive),
                    lastKey(), true, filterCriterion);
        }
        return filterBy(start, NullnessUtils.nullToTrue(startInclusive),
                end, NullnessUtils.nullToFalse(endInclusive), filterCriterion);
    }
    
    @SideEffectFree
    LineTree filterBy(
            IndexWithDepth start,
            boolean startInclusive,
            IndexWithDepth end,
            boolean endInclusive,
            Predicate<? super Entry<? extends IndexWithDepth, ? extends String>> filterCriterion
    ) {
        return filteredSequencialStream(start, startInclusive, end, endInclusive, filterCriterion)
                .parallel()
                .collect(this::emptyCopy, Collectionz::put, Collectionz::putAllUnordered);
    }
    
    @SideEffectFree
    Stream<Entry<IndexWithDepth, String>> filteredSequencialStream(
            IndexWithDepth start,
            boolean startInclusive,
            IndexWithDepth end,
            boolean endInclusive,
            Predicate<? super Entry<? extends IndexWithDepth, ? extends String>> filterCriterion
    ) {
        return subMap(start, startInclusive, end, endInclusive)
                .entrySet()
                .stream()
                .filter(filterCriterion);
    }
    
    @SideEffectFree
    List<LineTree> splitByDepth() {
        OptionalInt maxDepth = keySet().parallelStream()
                .mapToInt(IndexWithDepth::depth)
                .max();
        if (maxDepth.isEmpty()) {
            return new ArrayList<>(0);
        }
        int length = maxDepth.getAsInt() + 1;
        List<LineTree> result = Stream.generate(this::emptyCopy)
                .limit(length)
                .collect( Collectors.toCollection(() -> new ArrayList<>(length)) );
        forEach((k, v) -> result.get(k.depth()).put(k, v));
        return result;
    }
    
    @SideEffectFree
    Map<ReservedSymbols, LineTree> splitByCharType(
            @Nullable Boolean useCloser
    ) {
        Map<ReservedSymbols, LineTree> result = new EnumMap<>(ReservedSymbols.class);
        boolean useOpener = !NullnessUtils.nullToFalse(useCloser);
        int indexRemainder = useOpener ? 0 : 1;
        
        for (ReservedSymbols thisType : ALL_BUT_SPACE) {
            int indexForThisType = GROUPERS.indexOf(thisType);
            // If is in GROUPERS and is an opener/closer (whicheverSpecified)
            if (indexForThisType != -1) {
                // Split for else to only apply to first if
                if (indexForThisType % 2 == indexRemainder) {
                    ReservedSymbols toCheckAgainst = useOpener
                            ? thisType
                            : GROUPERS.get(indexForThisType - 1);
                    
                    result.put(thisType, filterByCharType(toCheckAgainst));
                }
            } else {
                result.put(thisType, filterByCharType(thisType));
            }
        }
        
        return result;
    }
    
    @SideEffectFree
    private LineTree emptyCopy() {
        return new LineTree(this);
    }
}
