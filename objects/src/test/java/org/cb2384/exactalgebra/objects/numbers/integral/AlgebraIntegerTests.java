package org.cb2384.exactalgebra.objects.numbers.integral;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigInteger;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

public final class AlgebraIntegerTests {
    private static final AlgebraInteger ZERO = IntegerFactory.fromLong(0);
    
    private static final List<AlgebraInteger> ONE_TO_SIXTEEN = LongStream.rangeClosed(1, 16)
            .mapToObj(IntegerFactory::fromLong)
            .toList();
    
    private static final List<AlgebraInteger> NEG_ONE_TO_SIXTEEN = LongStream.rangeClosed(1, 16)
            .map(l -> -l)
            .mapToObj(IntegerFactory::fromLong)
            .toList();
    
    private static final long LONG_TEST_LONG = 0x123456789ABCDEFL;
    
    private static final BigInteger LONG_TEST_BI = BigInteger.valueOf(LONG_TEST_LONG);
    
    private static final AlgebraInteger LONG_TEST_AI = IntegerFactory.fromBigInteger(LONG_TEST_BI);
    
    private static final List<AlgebraInteger> ALL_AI = Stream.of(NEG_ONE_TO_SIXTEEN.stream(),
            Stream.of(ZERO, LONG_TEST_AI), ONE_TO_SIXTEEN.stream())
            .flatMap(UnaryOperator.identity())
            .sorted()
            .toList();
    
    @Test
    public void testNumeratorAI() {
        assertTrue(ALL_AI.stream().allMatch(i -> i == i.numeratorAI()));
    }
    
    @Test
    public void testDenominatorAI() {
        assertTrue(ALL_AI.stream().allMatch(i -> i.denominatorAI() == ONE_TO_SIXTEEN.getFirst()));
    }
    
    @Test
    public void testWholeAI() {
        assertTrue(ALL_AI.stream().allMatch(i -> i == i.wholeAI()));
    }
    
    @Test
    public void testNumeratorBI() {
        assertTrue(ALL_AI.stream().allMatch(i -> i.toBigInteger().equals(i.numeratorBI())));
    }
    
    @Test
    public void testDenominatorBI() {
        assertTrue(ALL_AI.stream().allMatch(i -> BigInteger.ONE == i.denominatorBI()));
    }
    
    @Test
    public void testWholeBI() {
        assertTrue(ALL_AI.stream().allMatch(i -> i.toBigInteger().equals(i.wholeBI())));
    }
    
    @Test
    public void testMixedNum() {
        assertTrue(ALL_AI.stream().allMatch(i -> i.asMixedNumber(null).equals(i.toString())));
    }
    
    @Test
    public void testDoubleVal() {
        assertTrue(ALL_AI.stream()
                .filter(i -> !LONG_TEST_AI.equiv(i))
                .allMatch(i -> i.longValue() == i.doubleValue()) );
    }
    
    @Test
    public void testFloatVal() {
        assertTrue(ALL_AI.stream()
                .filter(i -> !LONG_TEST_AI.equiv(i))
                .allMatch(i -> i.intValue() == i.floatValue()) );
    }
    
    @Test
    public void testMixedNumString() {
        assertTrue(ALL_AI.stream().allMatch(n -> {
            for (int i = Character.MAX_RADIX; i >= Character.MIN_RADIX; i--) {
                if (!n.asMixedNumber(i).equals(n.toString(i))) {
                    return false;
                }
            }
            return true;
        }));
    }
    
    @Test
    public void testMixedNumNull() {
        assertTrue(ALL_AI.stream().allMatch(i -> i.asMixedNumber(null).equals(i.toString())));
    }
    
    @Test
    public void textMixedNumBounds() {
        assertTrue(IntStream.of(-2, 0, 1, 39, 108).allMatch(i -> {
            try {
                ZERO.asMixedNumber(i);
                return false;
            } catch (NumberFormatException ignored) {
                return true;
            }
        }));
    }
}
