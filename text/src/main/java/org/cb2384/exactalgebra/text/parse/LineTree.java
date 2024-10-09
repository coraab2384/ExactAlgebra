package org.cb2384.exactalgebra.text.parse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.regex.MatchResult;
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
        
        SortedMap<@GTENegativeOne int@ArrayLen(5)[], ReservedSymbols> keyIndices
                = new TreeMap<>(Comparator.comparingInt(a -> a[0]));
        
        placeAppearances(keyIndices);
        
        new Populator(keyIndices).populateAndCheckSymmetry();
        
        finish();
    }
    
    private void placeAppearances(
            SortedMap<@GTENegativeOne int@ArrayLen(5)[], ReservedSymbols> keyIndices
    ) {
        final class Placer {
            private ReservedSymbols symbol;
            
            @Deterministic
            private boolean putKey(
                    @GTENegativeOne int@ArrayLen(5)[] key
            ) {
                ReservedSymbols old = keyIndices.remove(key);
                keyIndices.put(key, symbol);
                return old != symbol;
            }
            
            @Deterministic
            private boolean placeOpener(
                    MatchResult match
            ) {
                assert GROUPERS.contains(symbol);
                int end = match.end();
                if (symbol == ReservedSymbols.OBJECT_GROUP) {
                    keyIndices.putIfAbsent(dummyIndex(end), ReservedSymbols.SPACE);
                } else {
                    keyIndices.putIfAbsent(dummyIndex(end), ReservedSymbols.ARG_SEP);
                }
                return putKey(buildIndex(match.start(), end));
            }
            
            @Deterministic
            private boolean placeCloser(
                    MatchResult match
            ) {
                assert GROUPERS.contains(symbol);
                int end = match.end();
                keyIndices.putIfAbsent(dummyIndex(end), ReservedSymbols.SPACE);
                return putKey(new int[]{match.start(), end, 0, 0, -1});
            }
            
            @Deterministic
            private boolean placeMatch(
                    MatchResult match
            ) {
                assert !GROUPERS.contains(symbol);
                int end = match.end();
                if (symbol != ReservedSymbols.COMMAND_KEY) {
                    keyIndices.putIfAbsent(dummyIndex(end), ReservedSymbols.SPACE);
                }
                return putKey(buildIndex(match.start(), end));
            }
        }
        Placer placer = new Placer();
        for (ReservedSymbols symbol : ALL_BUT_SPACE) {
            placer.symbol = symbol;
            if (GROUPERS.contains(symbol)) {
                symbol.asMatcher(source)
                        .results()
                        .forEachOrdered(placer::placeOpener);
                symbol.secondaryPattern()
                        .matcher(source)
                        .results()
                        .forEachOrdered(placer::placeCloser);
            } else {
                symbol.asMatcher(source)
                        .results()
                        .forEachOrdered(placer::placeMatch);
            }
        }
        keyIndices.forEach((k, v) -> System.out.println(Arrays.toString(k) + ", " + v));
    }
    
    @SideEffectFree
    private static @GTENegativeOne int@ArrayLen(5)[] dummyIndex(
            @NonNegative int start
    ) {
        return new int[]{start, -1, -1, -1, -1};
    }
    
    @SideEffectFree
    private static @GTENegativeOne int@ArrayLen(5)[] buildIndex(
            @NonNegative int start,
            @GTENegativeOne int end
    ) {
        return new int[]{start, end, -1, -1, -1};
    }
    
    private final class TripleArrayList
            extends ArrayList<@GTENegativeOne int[]@ArrayLen(5)[]> {
        
        private static final int WIDTH = GROUPERS.size();
        
        @Deterministic
        private boolean add(
                @GTENegativeOne int@ArrayLen(5)[] index,
                ReservedSymbols type
        ) {
            assert GROUPERS.contains(type) && (index[4] == size());
            int[][] depthBox = new int[WIDTH][];
            depthBox[GROUPERS.indexOf(type)] = index;
            return add(depthBox);
        }
        
        @Deterministic
        private boolean place(
                @GTENegativeOne int@ArrayLen(5)[] index,
                ReservedSymbols type
        ) {
            assert (type == ReservedSymbols.COMMAND_KEY) || GROUPERS.contains(type);
            int depth = index[4];
            return switch (Signum.valueOf(size() - depth)) {
                case POSITIVE -> {
                    int[][] depthBox = get(depth);
                    int typeIndex = GROUPERS.indexOf(type);
                    int[] old = depthBox[typeIndex];
                    depthBox[typeIndex] = index;
                    yield old == null;
                }
                case ZERO -> add(index, type);
                case NEGATIVE -> throw CommandFormatException.forInputString(source);
            };
        }
        
        private @GTENegativeOne int@ArrayLen(5)[] pull(
                ReservedSymbols type,
                @NonNegative int depth
        ) {
            assert (type == ReservedSymbols.COMMAND_KEY) || GROUPERS.contains(type);
            int[][] depthBox = get(depth);
            int typeIndex = GROUPERS.indexOf(type);
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
    
    private final class Populator {
        
        private final Iterator<Entry<@GTENegativeOne int@ArrayLen(5)[], ReservedSymbols>> keyIndexIter;
        
        private final TripleArrayList previousOpeners = new TripleArrayList();
        
        private Entry<@GTENegativeOne int@ArrayLen(5)[], ReservedSymbols> currentEntry;
        
        private @GTENegativeOne int@ArrayLen(5)[] currentKey;
        
        private ReservedSymbols currentSymbol;
        
        private int currentDepth = 0;
        
        @SideEffectFree
        private Populator(
                SortedMap<@GTENegativeOne int@ArrayLen(5)[], ReservedSymbols> keyIndices
        ) {
            if (keyIndices.isEmpty()) {
                throw CommandFormatException.forInputString(source);
            }
            keyIndexIter = keyIndices.entrySet().iterator();
        }
        
        private void populateAndCheckSymmetry() {
            currentEntry = keyIndexIter.next();
            boolean end = false;
            while (!end) {
                currentKey = currentEntry.getKey();
                currentSymbol = currentEntry.getValue();
                if (currentSymbol == ReservedSymbols.COMMAND_KEY) {
                    end = putCommand();
                } else if (currentKey[2] == 0) {
                    end = putCloser();
                } else if (GROUPERS.contains(currentSymbol)) {
                    end = putOpener();
                } else {
                    end = putNonGrouperOrArg();
                }
            }
            
            if ((currentDepth != 0) || !previousOpeners.isNulled()) {
                throw CommandFormatException.forInputString(source);
            }
        }
        
        @Deterministic
        private boolean putCommand() {
            assert currentSymbol == ReservedSymbols.COMMAND_KEY;
            IndexWithDepth commandIndex
                    = new StartWithDepth(currentKey[1], ReservedSymbols.COMMAND_KEY, currentDepth);
            if (keyIndexIter.hasNext()) {
                currentEntry = keyIndexIter.next();
                ReservedSymbols nextSymbol = currentEntry.getValue();
                put(commandIndex, source.substring(currentKey[1], currentEntry.getKey()[0]));
                
                if (nextSymbol == ReservedSymbols.ARG_GROUP) {
                    currentKey[4] = currentDepth++;
                    previousOpeners.place(currentKey, ReservedSymbols.ARG_GROUP);
                    if (keyIndexIter.hasNext()) {
                        currentEntry = keyIndexIter.next();
                        return false;
                    }
                    return true;
                }
                return false;
            }
            put(commandIndex, source.substring(currentKey[1]));
            return true;
        }
        
        @Deterministic
        private boolean putOpener() {
            assert GROUPERS.contains(currentSymbol);
            currentKey[4] = currentDepth++;
            previousOpeners.place(currentKey, currentSymbol);
            return loopIterFinisher();
        }
        
        @Deterministic
        private boolean putCloser() {
            assert GROUPERS.contains(currentSymbol) && (currentKey[3] == 0);
            int[] previousOpener = previousOpeners.pull(currentSymbol, --currentDepth);
            if ((currentDepth < 0) || previousOpener == null) {
                throw CommandFormatException.forInputString(source);
            }
            previousOpener[2] = currentKey[0];
            previousOpener[3] = currentKey[1];
            put(
                    new RangeWithDepth(previousOpener, currentSymbol),
                    source.substring(previousOpener[0], previousOpener[3])
            );
            return loopIterFinisher();
        }
        
        @Deterministic
        private boolean putNonGrouperOrArg() {
            assert !GROUPERS.contains(currentSymbol) && (currentSymbol != ReservedSymbols.COMMAND_KEY);
            currentKey[4] = currentDepth;
            IndexWithDepth key = new StartWithDepth(currentKey, currentSymbol);
            if (keyIndexIter.hasNext()) {
                currentEntry = keyIndexIter.next();
                put(key, source.substring(currentKey[0], currentEntry.getKey()[0]));
                return false;
            }
            put(key, source.substring(currentKey[0]));
            return true;
        }
        
        @Deterministic
        private boolean loopIterFinisher() {
            if (keyIndexIter.hasNext()) {
                currentEntry = keyIndexIter.next();
                return false;
            }
            return true;
        }
    }
    
    private void finish() {
        IndexWithDepth lastKey = lastKey();
        if (lastKey.startInclusive() == source.length()) {
            remove(lastKey);
        }
        
        replaceAll((k, v) -> v.trim());
        entrySet().removeIf(e -> ReservedSymbols.ARG_SEP.asMatcher(e.getValue()).replaceAll("").isEmpty());
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
    
    /*@SideEffectFree
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
    private static Predicate<Entry<? extends IndexWithDepth, String>> grouperToOpener(
            ReservedSymbols potentialGrouper
    ) {
        int grouperIndex = GROUPERS.indexOf(potentialGrouper);
        ReservedSymbols toTest = (grouperIndex % 2 == 1)
                ? GROUPERS.get(grouperIndex - 1)
                : potentialGrouper;
        return e -> e.getKey().symbolType() == toTest;
    }*/
    
    @Pure
    @NonNegative int countBy(
            Predicate<? super Entry<? extends IndexWithDepth, String>> toCount
    ) {
        return (int) entrySet()
                .parallelStream()
                .filter(toCount)
                .count();
    }
    
    @Pure
    @NonNegative int countBy(
            Predicate<? super Entry<? extends IndexWithDepth, String>> toCount,
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
            Predicate<? super Entry<? extends IndexWithDepth, String>> toCount
    ) {
        return (int) subMap(start, startInclusive, end, endInclusive)
                .entrySet()
                .stream()
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
    private static Predicate<Entry<? extends IndexWithDepth, String>> keepByDepth(
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
            Predicate<? super Entry<? extends IndexWithDepth, String>> filterCriterion
    ) {
        return entrySet().stream()
                .filter(filterCriterion)
                .collect(this::emptyCopy, Collectionz::put, Collectionz::putAllUnordered);
    }
    
    @SideEffectFree
    LineTree filterBy(
            Predicate<? super Entry<? extends IndexWithDepth, String>> filterCriterion,
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
            Predicate<? super Entry<? extends IndexWithDepth, String>> filterCriterion
    ) {
        return filteredSequencialStream(start, startInclusive, end, endInclusive, filterCriterion)
                .collect(this::emptyCopy, Collectionz::put, Collectionz::putAllUnordered);
    }
    
    @SideEffectFree
    Stream<Entry<IndexWithDepth, String>> filteredSequencialStream(
            IndexWithDepth start,
            boolean startInclusive,
            IndexWithDepth end,
            boolean endInclusive,
            Predicate<? super Entry<? extends IndexWithDepth, String>> filterCriterion
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
