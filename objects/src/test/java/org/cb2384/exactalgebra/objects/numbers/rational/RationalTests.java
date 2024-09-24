package org.cb2384.exactalgebra.objects.numbers.rational;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import org.cb2384.exactalgebra.objects.numbers.integral.AlgebraInteger;
import org.junit.jupiter.api.Test;

public final class RationalTests {
    private static final Rational ZERO = RationalFactory.fromDouble(0);
    
    private static final List<Rational> ONE_TO_SIXTEEN = LongStream.rangeClosed(1, 32)
            .mapToObj(l -> RationalFactory.fromLongs(l, 2))
            .toList();
    
    private static final List<Rational> NEG_ONE_TO_SIXTEEN = LongStream.rangeClosed(1, 32)
            .mapToObj(l -> RationalFactory.fromLongs(-l, 2))
            .toList();
    
    private static final long BIG_TEST_LONG = 0x123456789ABCDEFL;
    
    private static final BigInteger BIG_TEST_BI = BigInteger.valueOf(BIG_TEST_LONG);
    
    private static final Rational BIG_TEST_NUM = RationalFactory.fromBigIntegers(BIG_TEST_BI, BigInteger.ONE);
    
    private static final Rational BIG_TEST_NEG_DEN = RationalFactory.fromLongs(1, -BIG_TEST_LONG);
    
    private static final List<Rational> ALL_RAT = Stream.of(NEG_ONE_TO_SIXTEEN.stream(),
                    Stream.of(ZERO, BIG_TEST_NUM, BIG_TEST_NEG_DEN), ONE_TO_SIXTEEN.stream())
            .flatMap(UnaryOperator.identity())
            .sorted()
            .toList();
    
    @Test
    public void testMixedNumString() {
        assertTrue(ALL_RAT.stream().allMatch(n -> {
            IntStream testStream = IntStream.rangeClosed(Character.MIN_RADIX, Character.MAX_RADIX);
            if (n.isWhole()) {
                return testStream.allMatch(i -> n.asMixedNumber(i).equals(n.toString(i)));
            }
            return testStream.allMatch(i -> {
                AlgebraInteger whole = n.wholeAI();
                String exp = whole.toString(i) + (n.isNegative() ? "-(" : "+(")
                        + n.difference(whole).toString(i) + ")";
                System.out.println();
                return exp.equals(n.asMixedNumber(i));
            });
        }));
    }
    
    @Test
    public void testMixedNumNull() {
        assertTrue(ALL_RAT.stream().allMatch(i -> i.asMixedNumber(null).equals(i.asMixedNumber(10))));
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
