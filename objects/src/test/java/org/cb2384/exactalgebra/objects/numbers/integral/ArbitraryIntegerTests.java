package org.cb2384.exactalgebra.objects.numbers.integral;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import org.cb2384.exactalgebra.objects.Sigmagnum;
import org.cb2384.exactalgebra.objects.exceptions.DisallowedNarrowingException;
import org.cb2384.exactalgebra.objects.numbers.AlgebraNumber;
import org.cb2384.exactalgebra.objects.numbers.rational.Rational;
import org.cb2384.exactalgebra.objects.pair.NumberRemainderPair;
import org.cb2384.exactalgebra.util.BigMathObjectUtils;
import org.cb2384.exactalgebra.util.corutils.ternary.Signum;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public final class ArbitraryIntegerTests {
    
    private static final ArbitraryInteger ZERO = ArbitraryInteger.valueOfStrict(0);
    
    private static final List<ArbitraryInteger> ONE_TO_SIXTEEN = LongStream.rangeClosed(1, 16)
            .mapToObj(ArbitraryInteger::valueOfStrict)
            .toList();
    
    private static final List<ArbitraryInteger> NEG_ONE_TO_SIXTEEN = LongStream.rangeClosed(1, 16)
            .map(l -> -l)
            .mapToObj(ArbitraryInteger::valueOfStrict)
            .toList();
    
    private static final long LONG_TEST_LONG = 0x123456789ABCDEFL;
    
    private static final BigInteger LONG_TEST_BI = BigInteger.valueOf(LONG_TEST_LONG);
    
    private static final ArbitraryInteger LONG_TEST_AI = ArbitraryInteger.valueOfStrict(LONG_TEST_BI);
    
    private static final List<ArbitraryInteger> ALL_AI = Stream.of(NEG_ONE_TO_SIXTEEN.stream(),
                    Stream.of(ZERO, LONG_TEST_AI), ONE_TO_SIXTEEN.stream())
            .flatMap(UnaryOperator.identity())
            .sorted()
            .toList();
    
    private static void tryNull(
            Consumer<?>... toTest
    ) {
        for (Consumer<?> test : toTest) {
            assertThrows(NullPointerException.class, () -> test.accept(null));
        }
    }
    
    @Nested
    public final class ValueOfStrictTests {
        @Test
        public void valueTestLong() {
            assertEquals(-18, ArbitraryInteger.valueOfStrict(-18).longValue());
        }
        
        @Test
        public void valueTestBI() {
            assertEquals(BigInteger.ONE, ArbitraryInteger.valueOfStrict(BigInteger.ONE).toBigInteger());
        }
        
        @Test
        public void testNarrowLong() {
            assertInstanceOf(ArbitraryInteger.class, ArbitraryInteger.valueOfStrict(-7));
        }
        
        @Test
        public void testNarrowBI() {
            assertInstanceOf(ArbitraryInteger.class, ArbitraryInteger.valueOfStrict(BigInteger.TWO));
        }
    }
    
    @Nested
    public final class ValueOfTests {
        @Test
        public void testTooBigInputLong() {
            assertInstanceOf(ArbitraryInteger.class, ArbitraryInteger.valueOf(Long.MIN_VALUE));
        }
        
        @Test
        public void testTooBigInputBI() {
            AlgebraInteger test = ArbitraryInteger.valueOf(BigMathObjectUtils.LONG_MIN_BI.pow(2));
            assertInstanceOf(ArbitraryInteger.class, test);
        }
        
        @Test
        public void valueTestLong() {
            assertEquals(-18, ArbitraryInteger.valueOf(-18).longValue());
        }
        
        @Test
        public void valueTestBI() {
            assertEquals(BigInteger.ONE, ArbitraryInteger.valueOf(BigInteger.ONE).toBigInteger());
        }
        
        @Test
        public void testNarrowLong() {
            assertInstanceOf(FiniteInteger.class, ArbitraryInteger.valueOf(-7));
        }
        
        @Test
        public void testNarrowBI() {
            assertInstanceOf(FiniteInteger.class, ArbitraryInteger.valueOf(BigInteger.TWO));
        }
    }
    
    @Nested
    public final class RoundQTests {
        private static final int LONG_LEN = Long.toString(Long.MAX_VALUE).length();
        
        @Test
        public void testThisness() {
            assertEquals(LONG_TEST_AI, LONG_TEST_AI.roundQ());
        }
        
        @Test
        public void testNoTrimSplit() {
            assertEquals(LONG_TEST_AI, LONG_TEST_AI.roundQ(LONG_LEN, null));
        }
        
        private static long trimExp(
                long val,
                int cut
        ) {
            String valS = String.valueOf(val);
            String first = valS.substring(0, cut);
            String second = "0".repeat(valS.length() - cut);
            return Long.parseLong(first + second);
        }
        
        @Test
        public void testTrimSplit() {
            assertEquals(
                    ArbitraryInteger.valueOfStrict(trimExp(LONG_TEST_LONG, 5)),
                    LONG_TEST_AI.roundQ(5, RoundingMode.DOWN)
            );
        }
        
        @Test
        public void testNoTrimMC() {
            assertEquals(LONG_TEST_AI, LONG_TEST_AI.roundQ(null));
        }
        
        @Test
        public void testTrimMC() {
            assertEquals(
                    ArbitraryInteger.valueOfStrict(trimExp(LONG_TEST_LONG, 7)),
                    LONG_TEST_AI.roundQ(new MathContext(7, RoundingMode.DOWN))
            );
        }
    }
    
    @Nested
    public final class RoundZTests {
        @Test
        public void testThisNoArg() {
            assertEquals(LONG_TEST_AI, LONG_TEST_AI.roundZ());
        }
        
        @Test
        public void testThisArg() {
            assertEquals(LONG_TEST_AI, LONG_TEST_AI.roundZ(RoundingMode.UP));
        }
    }
    
    @Nested
    public final class ToBigDecimalTests {
        private static final int LONG_LEN = Long.toString(Long.MAX_VALUE).length();
        
        private static final BigDecimal ANS_BD = BigDecimal.valueOf(LONG_TEST_LONG);
        
        @Test
        public void testNoArg() {
            assertEquals(ANS_BD, LONG_TEST_AI.toBigDecimal());
        }
        
        @Test
        public void testNoTrimSplit() {
            assertEquals(ANS_BD, LONG_TEST_AI.toBigDecimal(LONG_LEN, null));
        }
        
        private static long trimExp(
                long val,
                int cut
        ) {
            String valS = String.valueOf(val);
            String first = valS.substring(0, cut);
            String second = "0".repeat(valS.length() - cut);
            return Long.parseLong(first + second);
        }
        
        @Test
        public void testTrimSplit() {
            BigDecimal exp = BigDecimal.valueOf(trimExp(LONG_TEST_AI.longValue(), 5));
            BigDecimal act = LONG_TEST_AI.toBigDecimal(5, RoundingMode.DOWN);
            assertTrue(exp.compareTo(act) == 0);
        }
        
        @Test
        public void testNoTrimMC() {
            assertEquals(ANS_BD, LONG_TEST_AI.toBigDecimal(new MathContext(LONG_LEN, RoundingMode.HALF_EVEN)));
        }
        
        @Test
        public void testTrimMC() {
            BigDecimal exp = BigDecimal.valueOf(trimExp(LONG_TEST_AI.longValue(), 7));
            BigDecimal act = LONG_TEST_AI.toBigDecimal(new MathContext(7, RoundingMode.DOWN));
            assertTrue(exp.compareTo(act) == 0);
        }
    }
    
    @Nested
    public final class ToBigIntegerTests {
        @Test
        public void testThisNoArg() {
            assertEquals(LONG_TEST_BI, LONG_TEST_AI.toBigInteger());
        }
        
        @Test
        public void testThisArg() {
            assertEquals(LONG_TEST_BI, LONG_TEST_AI.toBigInteger(null));
        }
    }
    
    @Nested
    public final class ToStringTests {
        @Test
        public void testWORadix() {
            String val = "2375423485912345713";
            assertEquals(val, ArbitraryInteger.valueOfStrict(Long.parseLong(val)).toString());
        }
        
        @Test
        public void testWRadix() {
            String val = "3236E5FE5674DC";
            assertEquals(val.toLowerCase(),
                    ArbitraryInteger.valueOfStrict(Long.parseLong(val, 16)).toString(16));
        }
        
        @Test
        public void testWRadixAndVar() {
            String val = "3236E5FE5674DC";
            assertEquals(
                    val.toLowerCase(),
                    ArbitraryInteger.valueOfStrict(Long.parseLong(val, 16))
                            .toString(16, "sdga", "zuieroew", "")
            );
        }
    }
    
    /**
     * The non-exceptional cases of these functions are just narrowing primitive conversions of the stored long
     */
    @Nested
    public final class PrimitiveValueTests {
        @Test
        public void testIntExc() {
            assertThrows(DisallowedNarrowingException.class, LONG_TEST_AI::intValueExact);
        }
        
        @Test
        public void testShortExc() {
            assertThrows(DisallowedNarrowingException.class, LONG_TEST_AI::shortValueExact);
        }
        
        @Test
        public void testCharExc() {
            assertThrows(DisallowedNarrowingException.class, LONG_TEST_AI::charValueExact);
        }
        
        @Test
        public void testByteExc() {
            assertThrows(DisallowedNarrowingException.class, LONG_TEST_AI::byteValueExact);
        }
    }
    
    @Nested
    public final class QueryTests {
        @Test
        public void test0is0() {
            assertTrue(ZERO.isZero());
        }
        
        @Test
        public void testNot0() {
            assertTrue(ONE_TO_SIXTEEN.stream().noneMatch(ArbitraryInteger::isZero));
        }
        
        @Test
        public void test1i1() {
            assertTrue(ONE_TO_SIXTEEN.getFirst().isOne());
        }
        
        @Test
        public void testNot1() {
            assertTrue(NEG_ONE_TO_SIXTEEN.stream().noneMatch(ArbitraryInteger::isOne));
        }
        
        @Test
        public void testNegIsNeg() {
            assertTrue(NEG_ONE_TO_SIXTEEN.stream().allMatch(ArbitraryInteger::isNegative));
        }
        
        @Test
        public void testNotNeg() {
            assertTrue(ONE_TO_SIXTEEN.stream().noneMatch(ArbitraryInteger::isNegative));
        }
        
        @Test
        public void testWhole() {
            assertTrue(ALL_AI.stream().allMatch(ArbitraryInteger::isWhole));
        }
    }
    
    @Nested
    public final class SignumTests {
        @Test
        public void testPositive() {
            assertTrue(ONE_TO_SIXTEEN.stream()
                    .map(ArbitraryInteger::signum)
                    .allMatch(Signum.POSITIVE::equals));
        }
        
        @Test
        public void testZero() {
            assertEquals(Signum.ZERO, ZERO.signum());
        }
        
        @Test
        public void testNegative() {
            assertTrue(NEG_ONE_TO_SIXTEEN.stream()
                    .map(ArbitraryInteger::signum)
                    .allMatch(Signum.NEGATIVE::equals));
        }
        
        @Test
        public void testPositiveSupOne() {
            assertTrue(ONE_TO_SIXTEEN.subList(1, ONE_TO_SIXTEEN.size())
                    .stream()
                    .map(ArbitraryInteger::sigmagnum)
                    .allMatch(Sigmagnum.POSITIVE_SUP_ONE::equals));
        }
        
        @Test
        public void testPositiveOne() {
            assertEquals(Sigmagnum.POSITIVE_ONE, ONE_TO_SIXTEEN.getFirst().sigmagnum());
        }
        
        @Test
        public void testZeroSigMagNum() {
            assertEquals(Sigmagnum.ZERO, ZERO.sigmagnum());
        }
        
        @Test
        public void testNegativeOne() {
            assertEquals(Sigmagnum.NEGATIVE_ONE, NEG_ONE_TO_SIXTEEN.getFirst().sigmagnum());
        }
        
        @Test
        public void testNegativeSubMinusOne() {
            assertTrue(NEG_ONE_TO_SIXTEEN.subList(1, NEG_ONE_TO_SIXTEEN.size())
                    .stream()
                    .map(ArbitraryInteger::sigmagnum)
                    .allMatch(Sigmagnum.NEGATIVE_SUB_MINUS_ONE::equals));
        }
    }
    
    @Nested
    public final class NegateAndAbsTests {
        @Test
        public void negationTest() {
            assertArrayEquals(ONE_TO_SIXTEEN.toArray(), NEG_ONE_TO_SIXTEEN.stream()
                    .map(ArbitraryInteger::negated)
                    .toArray());
        }
        
        @Test
        public void posAbsTest() {
            assertArrayEquals(ONE_TO_SIXTEEN.toArray(), NEG_ONE_TO_SIXTEEN.stream()
                    .map(ArbitraryInteger::magnitude)
                    .toArray());
        }
        
        @Test
        public void negAbsTest() {
            assertTrue(ONE_TO_SIXTEEN.stream()
                    .map(ArbitraryInteger::magnitude)
                    .allMatch(i -> ONE_TO_SIXTEEN.get(i.intValue() - 1) == i));
        }
    }
    
    @Nested
    public final class MaxTests {
        @Test
        public void testAI() {
            assertEquals(ONE_TO_SIXTEEN.get(5), ONE_TO_SIXTEEN.get(2).max(ONE_TO_SIXTEEN.get(5)));
        }
        
        @Test
        public void testBI1() {
            assertEquals(ONE_TO_SIXTEEN.get(14), ONE_TO_SIXTEEN.get(14).max(BigInteger.TWO));
        }
        
        @Test
        public void testBI2() {
            assertEquals(ONE_TO_SIXTEEN.get(15), ONE_TO_SIXTEEN.get(5).max(BigInteger.valueOf(16)));
        }
        
        @Test
        public void testL1() {
            assertEquals(NEG_ONE_TO_SIXTEEN.get(12), NEG_ONE_TO_SIXTEEN.get(12).max(-65233565));
        }
        
        @Test
        public void testL2() {
            assertEquals(NEG_ONE_TO_SIXTEEN.get(2), NEG_ONE_TO_SIXTEEN.get(12).max(-3));
        }
        
        @Test
        public void testR1() {
            assertEquals(ONE_TO_SIXTEEN.get(14), ONE_TO_SIXTEEN.get(14).max((Rational) NEG_ONE_TO_SIXTEEN.getLast()));
        }
        
        @Test
        public void testR2() {
            assertEquals(ONE_TO_SIXTEEN.get(15), ONE_TO_SIXTEEN.get(5).max((Rational) ONE_TO_SIXTEEN.getLast()));
        }
        
        @Test
        public void testAN1() {
            assertEquals(
                    NEG_ONE_TO_SIXTEEN.get(12),
                    NEG_ONE_TO_SIXTEEN.get(12).max((AlgebraNumber) NEG_ONE_TO_SIXTEEN.getLast())
            );
        }
        
        @Test
        public void testAN2() {
            assertEquals(NEG_ONE_TO_SIXTEEN.get(2), NEG_ONE_TO_SIXTEEN.get(12).max(AlgebraNumber.valueOf(-3)));
        }
        
        @Test
        public void nullTest() {
            tryNull(
                    (Consumer<BigInteger>) ZERO::max,
                    (Consumer<AlgebraInteger>) ZERO::max,
                    (Consumer<Rational>) ZERO::max,
                    (Consumer<AlgebraNumber>) ZERO::max
            );
        }
    }
    
    @Nested
    public final class MinTests {
        @Test
        public void testAI() {
            assertEquals(ONE_TO_SIXTEEN.get(2), ONE_TO_SIXTEEN.get(5).min(ONE_TO_SIXTEEN.get(2)));
        }
        
        @Test
        public void testBI1() {
            assertEquals(ONE_TO_SIXTEEN.get(1), ONE_TO_SIXTEEN.get(14).min(BigInteger.TWO));
        }
        
        @Test
        public void testBI2() {
            assertEquals(ONE_TO_SIXTEEN.get(5), ONE_TO_SIXTEEN.get(5).min(BigInteger.valueOf(19)));
        }
        
        @Test
        public void testL1() {
            assertEquals(NEG_ONE_TO_SIXTEEN.get(12), NEG_ONE_TO_SIXTEEN.get(12).min(65233565));
        }
        
        @Test
        public void testL2() {
            assertEquals(NEG_ONE_TO_SIXTEEN.get(2), ONE_TO_SIXTEEN.get(12).min(-3));
        }
        
        @Test
        public void testR1() {
            assertEquals(ONE_TO_SIXTEEN.get(14), ONE_TO_SIXTEEN.get(14).min((Rational) ONE_TO_SIXTEEN.getLast()));
        }
        
        @Test
        public void testR2() {
            assertEquals(
                    NEG_ONE_TO_SIXTEEN.get(15),
                    ONE_TO_SIXTEEN.get(5).min((Rational) NEG_ONE_TO_SIXTEEN.getLast())
            );
        }
        
        @Test
        public void testAN1() {
            assertEquals(
                    NEG_ONE_TO_SIXTEEN.get(12),
                    NEG_ONE_TO_SIXTEEN.get(12).min((AlgebraNumber) ONE_TO_SIXTEEN.getLast())
            );
        }
        
        @Test
        public void testAN2() {
            assertEquals(NEG_ONE_TO_SIXTEEN.get(2), NEG_ONE_TO_SIXTEEN.get(2).min(AlgebraNumber.valueOf(45)));
        }
        
        @Test
        public void testNull() {
            tryNull(
                    (Consumer<BigInteger>) ZERO::min,
                    (Consumer<AlgebraInteger>) ZERO::min,
                    (Consumer<Rational>) ZERO::min,
                    (Consumer<AlgebraNumber>) ZERO::min
            );
        }
    }
    
    @Nested
    public final class GCFTests {
        private static final long A = 2500;
        
        private static final long B = 1040;
        
        private static final ArbitraryInteger ANS = ArbitraryInteger.valueOfStrict(20);
        
        @Test
        public void testL() {
            assertEquals(ANS, ArbitraryInteger.valueOfStrict(A).gcf(B));
        }
        
        @Test
        public void testBI() {
            assertEquals(ANS, ArbitraryInteger.valueOfStrict(A).gcf(BigInteger.valueOf(B)));
        }
        
        @Test
        public void testAI() {
            assertEquals(ANS, ArbitraryInteger.valueOfStrict(A).gcf(ArbitraryInteger.valueOfStrict(-B)));
        }
        
        @Test
        public void testNull() {
            tryNull(
                    (Consumer<BigInteger>) ANS::gcf,
                    (Consumer<AlgebraInteger>) ANS::gcf
            );
        }
        
        @Test
        public void testZeroL() {
            assertThrows(ArithmeticException.class, () -> ZERO.gcf(0));
        }
        
        @Test
        public void testZeroBI() {
            assertThrows(ArithmeticException.class, () -> ZERO.gcf(BigInteger.ZERO));
        }
        
        @Test
        public void testZeroAI() {
            assertThrows(ArithmeticException.class, () -> ZERO.gcf(ZERO));
        }
    }
    
    @Nested
    public final class LCMTests {
        private static final ArbitraryInteger ANS = ArbitraryInteger.valueOfStrict(30);
        
        @Test
        public void testL() {
            assertEquals(ANS, ONE_TO_SIXTEEN.get(14).lcm(-10));
        }
        
        @Test
        public void testBI() {
            assertEquals(ANS, ONE_TO_SIXTEEN.get(9).lcm(BigInteger.valueOf(-15)));
        }
        
        @Test
        public void testAI() {
            assertEquals(ANS, ONE_TO_SIXTEEN.get(14).lcm(ONE_TO_SIXTEEN.get(9)));
        }
        
        @Test
        public void testNull() {
            tryNull(
                    (Consumer<BigInteger>) ANS::lcm,
                    (Consumer<AlgebraInteger>) ANS::lcm
            );
        }
        
        @Test
        public void testZeroL() {
            assertThrows(ArithmeticException.class, () -> ONE_TO_SIXTEEN.getLast().lcm(0));
        }
        
        @Test
        public void testZeroBI() {
            assertThrows(ArithmeticException.class, () -> ZERO.lcm(BigInteger.valueOf(-7)));
        }
        
        @Test
        public void testZeroAI() {
            assertThrows(ArithmeticException.class, () -> NEG_ONE_TO_SIXTEEN.getLast().lcm(ZERO));
        }
    }
    
    @Nested
    public final class CanDivideTests {
        @Test
        public void testTrueL() {
            assertTrue(ONE_TO_SIXTEEN.get(14).canDivideBy(5));
        }
        
        @Test
        public void testFalseL() {
            assertFalse(ONE_TO_SIXTEEN.get(3).canDivideBy(-9));
        }
        
        @Test
        public void testTrueBI() {
            assertTrue(ONE_TO_SIXTEEN.get(9).canDivideBy(BigInteger.valueOf(-2)));
        }
        
        @Test
        public void testFalseBI() {
            assertFalse(ONE_TO_SIXTEEN.get(10).canDivideBy(BigInteger.TWO));
        }
        
        @Test
        public void testTrueAI() {
            assertTrue(ONE_TO_SIXTEEN.get(9).canDivideBy(NEG_ONE_TO_SIXTEEN.get(1)));
        }
        
        @Test
        public void testFalseAI() {
            assertFalse(ONE_TO_SIXTEEN.get(10).canDivideBy(ONE_TO_SIXTEEN.get(1)));
        }
        
        @Test
        public void testNull() {
            tryNull(
                    (Consumer<BigInteger>) ZERO::canDivideBy,
                    (Consumer<AlgebraInteger>) ZERO::canDivideBy
            );
        }
        
        @Test
        public void testZeroL() {
            assertFalse(ONE_TO_SIXTEEN.getLast().canDivideBy(0));
        }
        
        @Test
        public void testZeroBI() {
            assertFalse(ONE_TO_SIXTEEN.getLast().canDivideBy(BigInteger.ZERO));
        }
        
        @Test
        public void testZeroAI() {
            assertFalse(ONE_TO_SIXTEEN.getLast().canDivideBy(ZERO));
        }
    }
    
    @Nested
    public final class PrimeAndEvenTests {
        @Test
        public void testNegPrime() {
            assertFalse(NEG_ONE_TO_SIXTEEN.get(6).isPrime());
        }
        
        @Test
        public void testTruePrime() {
            assertTrue(ONE_TO_SIXTEEN.get(10).isPrime());
        }
        
        @Test
        public void testFalsePrime() {
            assertFalse(ONE_TO_SIXTEEN.getLast().isPrime());
        }
        
        @Test
        public void testTrueEven() {
            assertTrue(ZERO.isEven());
        }
        
        @Test
        public void testFalseEven() {
            assertFalse(ONE_TO_SIXTEEN.get(14).isEven());
        }
    }
    
    @Nested
    public final class ComparisonTests {
        @Test
        public void testCompareToAI() {
            assertTrue(ZERO.compareTo(ArbitraryInteger.valueOfStrict(24564)) < 0);
        }
        
        @Test
        public void testCompareToR() {
            assertEquals(0, ZERO.compareTo((Rational) ZERO));
        }
        
        @Test
        public void testCompareToAN() {
            assertTrue(ZERO.compareTo(NEG_ONE_TO_SIXTEEN.get(8)) > 0);
        }
        
        @Test
        public void testCompare() {
            assertEquals(Signum.MINUS_ONE, ZERO.compare(ONE_TO_SIXTEEN.getFirst()));
        }
        
        @Test
        public void testNull() {
            tryNull(
                    (Consumer<AlgebraInteger>) ZERO::compareTo,
                    (Consumer<Rational>) ZERO::compareTo,
                    (Consumer<AlgebraNumber>) ZERO::compareTo,
                    (Consumer<AlgebraNumber>) ZERO::compare
            );
        }
    }
    
    @Nested
    public final class EquivTests {
        @Test
        public void testEquivAI() {
            assertTrue(ONE_TO_SIXTEEN.get(7).equiv(ONE_TO_SIXTEEN.get(7)));
        }
        
        @Test
        public void testEquivR() {
            assertFalse(ZERO.equiv((Rational) NEG_ONE_TO_SIXTEEN.getLast()));
        }
        
        @Test
        public void testEquivAN() {
            assertTrue(ZERO.equiv((AlgebraNumber) ZERO));
        }
        
        @Test
        public void testCompare() {
            assertEquals(Signum.MINUS_ONE, ZERO.compare(ONE_TO_SIXTEEN.getFirst()));
        }
        
        @Test
        public void testNull() {
            tryNull(
                    (Consumer<AlgebraInteger>) ZERO::compareTo,
                    (Consumer<Rational>) ZERO::compareTo,
                    (Consumer<AlgebraNumber>) ZERO::compareTo
            );
        }
    }
    
    @Nested
    public final class EqualsAndHCTests {
        @Test
        public void testEqualsAndHCContract() {
            int size = ALL_AI.size();
            
            for (int i = 0; i < size; i++) {
                ArbitraryInteger testI = ALL_AI.get(i);
                for (int j = 0; j <size; j++) {
                    ArbitraryInteger testJ = ALL_AI.get(j);
                    assertFalse((i == j) ^ (testI.equals(testJ) && (testI.hashCode() == testJ.hashCode())) );
                }
            }
        }
        
        @Test
        public void testNull() {
            assertTrue(ALL_AI.stream().noneMatch(i -> i.equals(null)));
        }
    }
    
    @Nested
    public final class SumTests {
        @Test
        public void testAddAI() {
            var expected = ONE_TO_SIXTEEN.get(2);
            var actual = ONE_TO_SIXTEEN.get(7).sum(NEG_ONE_TO_SIXTEEN.get(4));
            assertEquals(expected, actual);
        }
        
        @Test
        public void testAddR() {
            var expected = NEG_ONE_TO_SIXTEEN.get(9);
            var actual = ONE_TO_SIXTEEN.get(2).sum((Rational) NEG_ONE_TO_SIXTEEN.get(12));
            assertEquals(expected, actual);
        }
        
        @Test
        public void testAddAN() {
            var expected = ONE_TO_SIXTEEN.getLast();
            var actual = ONE_TO_SIXTEEN.get(11).sum((AlgebraNumber) ONE_TO_SIXTEEN.get(3));
            assertEquals(expected, actual);
        }
        
        @Test
        public void testNull() {
            tryNull(
                    (Consumer<AlgebraInteger>) ZERO::sum,
                    (Consumer<Rational>) ZERO::sum,
                    (Consumer<AlgebraNumber>) ZERO::sum
            );
        }
    }
    
    @Nested
    public final class DifferenceTests {
        @Test
        public void testDiffAI() {
            var expected = ONE_TO_SIXTEEN.get(2);
            var actual = ONE_TO_SIXTEEN.get(7).difference(ONE_TO_SIXTEEN.get(4));
            assertEquals(expected, actual);
        }
        
        @Test
        public void testDiffR() {
            var expected = NEG_ONE_TO_SIXTEEN.get(9);
            var actual = ONE_TO_SIXTEEN.get(2).difference((Rational) ONE_TO_SIXTEEN.get(12));
            assertEquals(expected, actual);
        }
        
        @Test
        public void testDiffAN() {
            var expected = ONE_TO_SIXTEEN.getLast();
            var actual = ONE_TO_SIXTEEN.get(11).difference((AlgebraNumber) NEG_ONE_TO_SIXTEEN.get(3));
            assertEquals(expected, actual);
        }
        
        @Test
        public void testNull() {
            tryNull(
                    (Consumer<AlgebraInteger>) ZERO::difference,
                    (Consumer<Rational>) ZERO::difference,
                    (Consumer<AlgebraNumber>) ZERO::difference
            );
        }
    }
    
    @Nested
    public final class ProductTests {
        @Test
        public void testProdAI() {
            var expected = NEG_ONE_TO_SIXTEEN.getLast();
            var actual = ONE_TO_SIXTEEN.get(7).product(NEG_ONE_TO_SIXTEEN.get(1));
            assertEquals(expected, actual);
        }
        
        @Test
        public void testProdR() {
            var expected = ArbitraryInteger.valueOfStrict(24);
            var actual = NEG_ONE_TO_SIXTEEN.get(7).product((Rational) NEG_ONE_TO_SIXTEEN.get(2));
            assertEquals(expected, actual);
        }
        
        @Test
        public void testProdAN() {
            var expected = ONE_TO_SIXTEEN.get(14);
            var actual = ONE_TO_SIXTEEN.get(4).product((AlgebraNumber) ONE_TO_SIXTEEN.get(2));
            assertEquals(expected, actual);
        }
        
        @Test
        public void testNull() {
            tryNull(
                    (Consumer<AlgebraInteger>) ZERO::product,
                    (Consumer<Rational>) ZERO::product,
                    (Consumer<AlgebraNumber>) ZERO::product
            );
        }
    }
    
    @Nested
    public final class QuotientZTests {
        @Test
        public void testQuotientZAI() {
            var expected = NEG_ONE_TO_SIXTEEN.get(3);
            var actual = NEG_ONE_TO_SIXTEEN.getLast().quotientZ(ONE_TO_SIXTEEN.get(3));
            assertEquals(expected, actual);
        }
        
        @Test
        public void testQuotientZDiv0AI() {
            assertThrows(ArithmeticException.class, () -> ONE_TO_SIXTEEN.getFirst().quotientZ(ZERO));
        }
        
        @Test
        public void testQuotientZR() {
            var expected = ONE_TO_SIXTEEN.get(6);
            var actual = NEG_ONE_TO_SIXTEEN.get(13).quotientZ((Rational) NEG_ONE_TO_SIXTEEN.get(1));
            assertEquals(expected, actual);
        }
        
        @Test
        public void testQuotientZDiv0R() {
            assertThrows(ArithmeticException.class, () -> ONE_TO_SIXTEEN.getFirst().quotientZ((Rational) ZERO));
        }
        
        @Test
        public void testQuotientZAN() {
            var expected = ONE_TO_SIXTEEN.getLast();
            var actual = ArbitraryInteger.valueOfStrict(64).quotientZ((AlgebraNumber) ONE_TO_SIXTEEN.get(3));
            assertEquals(expected, actual);
        }
        
        @Test
        public void testQuotientZDiv0AN() {
            assertThrows(ArithmeticException.class, () -> ONE_TO_SIXTEEN.getFirst().quotientZ((AlgebraNumber) ZERO));
        }
        
        @Test
        public void testNull() {
            tryNull(
                    (Consumer<AlgebraInteger>) ZERO::quotientZ,
                    (Consumer<Rational>) ZERO::quotientZ,
                    (Consumer<AlgebraNumber>) ZERO::quotientZ
            );
        }
    }
    
    @Nested
    public final class QuotientZWithRemainderTests {
        @Test
        public void testQuoRemainAI() {
            var expected = new NumberRemainderPair<>(ONE_TO_SIXTEEN.get(2), ONE_TO_SIXTEEN.get(1));
            var actual = ONE_TO_SIXTEEN.get(10).quotientZWithRemainder(ONE_TO_SIXTEEN.get(2));
            assertEquals(expected, actual);
        }
        
        @Test
        public void testQuoRemainDiv0AI() {
            assertThrows(ArithmeticException.class,
                    () -> ONE_TO_SIXTEEN.getFirst().quotientZWithRemainder(ZERO) );
        }
        
        @Test
        public void testQuoRemainR() {
            var expected = new NumberRemainderPair<>(NEG_ONE_TO_SIXTEEN.get(3), NEG_ONE_TO_SIXTEEN.getFirst());
            var actual = NEG_ONE_TO_SIXTEEN.get(8).quotientZWithRemainder((Rational) ONE_TO_SIXTEEN.get(1));
            assertEquals(expected, actual);
        }
        
        @Test
        public void testQuoRemainDiv0R() {
            assertThrows(ArithmeticException.class,
                    () -> ONE_TO_SIXTEEN.getFirst().quotientZWithRemainder((Rational) ZERO) );
        }
        
        @Test
        public void testQuoRemainAN() {
            var expected = new NumberRemainderPair<>(ONE_TO_SIXTEEN.get(2), NEG_ONE_TO_SIXTEEN.get(1) );
            var actual = NEG_ONE_TO_SIXTEEN.get(13).quotientZWithRemainder((AlgebraNumber) NEG_ONE_TO_SIXTEEN.get(3));
            assertEquals(expected, actual);
        }
        
        @Test
        public void testQuoRemainDiv0AN() {
            assertThrows(ArithmeticException.class,
                    () -> ONE_TO_SIXTEEN.getFirst().quotientZWithRemainder((AlgebraNumber) ZERO) );
        }
        
        @Test
        public void testNull() {
            tryNull(
                    (Consumer<AlgebraInteger>) ZERO::quotientZWithRemainder,
                    (Consumer<Rational>) ZERO::quotientZWithRemainder,
                    (Consumer<AlgebraNumber>) ZERO::quotientZWithRemainder
            );
        }
    }
    
    @Nested
    public final class QuotientRoundZTests {
        @Test
        public void testRoundingMode() {
            var expected = ONE_TO_SIXTEEN.get(3);
            var actual = ONE_TO_SIXTEEN.get(12).quotientRoundZ(ONE_TO_SIXTEEN.get(3), RoundingMode.UP);
            assertEquals(expected, actual);
        }
        
        @Test
        public void testNoRoundingMode() {
            var expected = ONE_TO_SIXTEEN.get(3);
            var actual = ONE_TO_SIXTEEN.get(14).quotientRoundZ(ONE_TO_SIXTEEN.get(3), null);
            assertEquals(expected, actual);
        }
        
        @Test
        public void testDiv0() {
            assertThrows(ArithmeticException.class,
                    () -> ONE_TO_SIXTEEN.getFirst().quotientRoundZ(ZERO, null) );
        }
        
        @Test
        public void testNull() {
            assertThrows(NullPointerException.class, () -> ZERO.quotientRoundZ(null, null));
        }
    }
    
    @Nested
    public final class RemainderTests {
        @Test
        public void testRemainderAI() {
            var expected = ONE_TO_SIXTEEN.get(3);
            var actual = ONE_TO_SIXTEEN.get(10).remainder(NEG_ONE_TO_SIXTEEN.get(6));
            assertEquals(expected, actual);
        }
        
        @Test
        public void testRemainderDiv0AI() {
            assertThrows(ArithmeticException.class, () -> ONE_TO_SIXTEEN.getFirst().remainder(ZERO));
        }
        
        @Test
        public void testRemainderR() {
            var expected = NEG_ONE_TO_SIXTEEN.get(6);
            var actual = NEG_ONE_TO_SIXTEEN.get(14).remainder((Rational) ONE_TO_SIXTEEN.get(7));
            assertEquals(expected, actual);
        }
        
        @Test
        public void testRemainderDiv0R() {
            assertThrows(ArithmeticException.class, () -> ONE_TO_SIXTEEN.getFirst().remainder((Rational) ZERO));
        }
        
        @Test
        public void testRemainderAN() {
            var expected = ONE_TO_SIXTEEN.getFirst();
            var actual = ONE_TO_SIXTEEN.getLast().remainder((AlgebraNumber) ONE_TO_SIXTEEN.get(4));
            assertEquals(expected, actual);
        }
        
        @Test
        public void testRemainderDiv0AN() {
            assertThrows(ArithmeticException.class, () -> ONE_TO_SIXTEEN.getFirst().remainder((AlgebraNumber) ZERO));
        }
        
        @Test
        public void testNull() {
            tryNull(
                    (Consumer<AlgebraInteger>) ZERO::remainder,
                    (Consumer<Rational>) ZERO::remainder,
                    (Consumer<AlgebraNumber>) ZERO::remainder
            );
        }
    }
    
    @Nested
    public final class ModuloTests {
        @Test
        public void testModulusAI() {
            var expected = ONE_TO_SIXTEEN.get(3);
            var actual = ONE_TO_SIXTEEN.get(10).modulo(ONE_TO_SIXTEEN.get(6));
            assertEquals(expected, actual);
        }
        
        @Test
        public void testModuloDiv0AI() {
            assertThrows(ArithmeticException.class, () -> ONE_TO_SIXTEEN.getFirst().modulo(ZERO));
        }
        
        @Test
        public void testModuloDivNegAI() {
            assertThrows(ArithmeticException.class,
                    () -> ONE_TO_SIXTEEN.getFirst().modulo(NEG_ONE_TO_SIXTEEN.getFirst()) );
        }
        
        @Test
        public void testQuotientR() {
            var expected = ONE_TO_SIXTEEN.getFirst();
            var actual = NEG_ONE_TO_SIXTEEN.get(14).modulo((Rational) ONE_TO_SIXTEEN.get(7));
            assertEquals(expected, actual);
        }
        
        @Test
        public void testModuloDiv0R() {
            assertThrows(ArithmeticException.class, () -> ONE_TO_SIXTEEN.getFirst().modulo((Rational) ZERO));
        }
        
        @Test
        public void testModuloDivNegR() {
            assertThrows(ArithmeticException.class,
                    () -> ONE_TO_SIXTEEN.getFirst().modulo((Rational) NEG_ONE_TO_SIXTEEN.getFirst()) );
        }
        
        @Test
        public void testQuotientAN() {
            var expected = ONE_TO_SIXTEEN.getFirst();
            var actual = ONE_TO_SIXTEEN.getLast().modulo((AlgebraNumber) ONE_TO_SIXTEEN.get(4));
            assertEquals(expected, actual);
        }
        
        @Test
        public void testModuloDiv0AN() {
            assertThrows(ArithmeticException.class, () -> ONE_TO_SIXTEEN.getFirst().modulo((AlgebraNumber) ZERO));
        }
        
        @Test
        public void testModuloDivNegAN() {
            assertThrows(ArithmeticException.class,
                    () -> ONE_TO_SIXTEEN.getFirst().modulo((AlgebraNumber) NEG_ONE_TO_SIXTEEN.getFirst()) );
        }
        
        @Test
        public void testNull() {
            tryNull(
                    (Consumer<AlgebraInteger>) ZERO::modulo,
                    (Consumer<Rational>) ZERO::modulo,
                    (Consumer<AlgebraNumber>) ZERO::modulo
            );
        }
    }
    
    @Nested
    public final class ModInverseTests {
        @Test
        public void testModInv() {
            var expected = ONE_TO_SIXTEEN.get(6);
            var actual = ONE_TO_SIXTEEN.get(3).modInverse(ONE_TO_SIXTEEN.get(8));
            assertEquals(expected, actual);
        }
        
        @Test
        public void testRelPrime() {
            assertThrows(ArithmeticException.class, () -> ONE_TO_SIXTEEN.get(5).modInverse(ONE_TO_SIXTEEN.get(2)));
        }
        
        @Test
        public void testDiv0() {
            assertThrows(ArithmeticException.class, () -> ONE_TO_SIXTEEN.getFirst().modInverse(ZERO));
        }
        
        @Test
        public void testDivNeg() {
            assertThrows(ArithmeticException.class,
                    () -> ONE_TO_SIXTEEN.get(1).modInverse(NEG_ONE_TO_SIXTEEN.get(10)) );
        }
        
        @Test
        public void testNull() {
            assertThrows(NullPointerException.class, () -> ZERO.modInverse(null));
        }
    }
    
    @Nested
    public final class RaiseTests {
        @Test
        public void testSquare() {
            var expected = ONE_TO_SIXTEEN.getLast();
            var actual = NEG_ONE_TO_SIXTEEN.get(3).squared();
            assertEquals(expected, actual);
        }
        
        @Test
        public void testIntRaise() {
            var expected = ArbitraryInteger.valueOfStrict(-125);
            var actual = NEG_ONE_TO_SIXTEEN.get(4).raised(3);
            assertEquals(expected, actual);
        }
        
        @Test
        public void testIntRaiseZ() {
            var expected = ArbitraryInteger.valueOfStrict(625);
            var actual = NEG_ONE_TO_SIXTEEN.get(4).raisedZ(4);
            assertEquals(expected, actual);
        }
        
        @Test
        public void testAIRaise() {
            var expected = ArbitraryInteger.valueOfStrict(64);
            var actual = ONE_TO_SIXTEEN.get(7).raised(ONE_TO_SIXTEEN.get(1));
            assertEquals(expected, actual);
        }
        
        @Test
        public void testAIRaiseZ() {
            var expected = ONE_TO_SIXTEEN.getFirst();
            var actual = NEG_ONE_TO_SIXTEEN.getFirst()
                    .raisedZ(ArbitraryInteger.valueOfStrict(
                            BigMathObjectUtils.LONG_MIN_BI.multiply(BigInteger.valueOf(-2))) );
            assertEquals(expected, actual);
        }
        
        @Test
        public void testIntRaiseNeg() {
            assertThrows(ArithmeticException.class, () -> ONE_TO_SIXTEEN.get(6).raisedZ(-6));
        }
        
        @Test
        public void testAIRaiseNeg() {
            assertThrows(ArithmeticException.class,
                    () -> ONE_TO_SIXTEEN.get(6).raisedZ(NEG_ONE_TO_SIXTEEN.getFirst()) );
        }
        
        @Test
        public void testInt0To0() {
            assertThrows(ArithmeticException.class, () -> ZERO.raised(0));
        }
        
        @Test
        public void testAI0To0() {
            assertThrows(ArithmeticException.class, () -> ZERO.raised(ZERO));
        }
        
        @Test
        public void testNull() {
            tryNull(
                    (Consumer<AlgebraInteger>) ZERO::raisedZ,
                    (Consumer<AlgebraInteger>) ZERO::raised
            );
        }
    }
    
    @Nested
    public final class RootWithRemainderTests {
        @Test
        public void testSqrtWRemain() {
            var expected = new NumberRemainderPair<>(ONE_TO_SIXTEEN.get(2), ONE_TO_SIXTEEN.get(4));
            var actual = ONE_TO_SIXTEEN.get(13).sqrtZWithRemainder();
            assertEquals(expected, actual);
        }
        
        @Test
        public void testSqrtWRemainNeg() {
            assertThrows(ArithmeticException.class, () -> NEG_ONE_TO_SIXTEEN.getFirst().sqrtZWithRemainder());
        }
        
        @Test
        public void testIntRootWRemain() {
            var expected = new NumberRemainderPair<>(ZERO, NEG_ONE_TO_SIXTEEN.getLast());
            var actual = NEG_ONE_TO_SIXTEEN.getLast().rootZWithRemainder(-3);
            assertEquals(expected, actual);
        }
        
        @Test
        public void testAIRootWRemain() {
            var expected = new NumberRemainderPair<>(ONE_TO_SIXTEEN.get(3), ONE_TO_SIXTEEN.get(3));
            var actual = ArbitraryInteger.valueOfStrict(68).rootZWithRemainder(ONE_TO_SIXTEEN.get(2));
            assertEquals(expected, actual);
        }
        
        @Test
        public void testIntNegRoot() {
            assertThrows(ArithmeticException.class, () -> NEG_ONE_TO_SIXTEEN.get(5).rootZWithRemainder(4));
        }
        
        @Test
        public void testAIRaiseNeg() {
            assertThrows(ArithmeticException.class,
                    () -> NEG_ONE_TO_SIXTEEN.get(9).rootZWithRemainder(ONE_TO_SIXTEEN.get(5)) );
        }
        
        @Test
        public void testInt0Root0() {
            assertThrows(ArithmeticException.class, () -> ONE_TO_SIXTEEN.get(6).rootZWithRemainder(0));
        }
        
        @Test
        public void testAI0Root0() {
            assertThrows(ArithmeticException.class, () -> ONE_TO_SIXTEEN.get(3).rootZWithRemainder(ZERO));
        }
        
        @Test
        public void testNull() {
            assertThrows(NullPointerException.class, () -> ZERO.rootZWithRemainder(null));
        }
    }
    
    @Nested
    public final class RootRoundTests {
        @Test
        public void testSqrtZ() {
            var expected =ONE_TO_SIXTEEN.get(2);
            var actual = ONE_TO_SIXTEEN.get(14).sqrtRoundZ(RoundingMode.DOWN);
            assertEquals(expected, actual);
        }
        
        @Test
        public void testSqrtWRemainNeg() {
            assertThrows(ArithmeticException.class, () -> NEG_ONE_TO_SIXTEEN.getFirst().sqrtRoundZ(null));
        }
        
        @Test
        public void testIntRootWRemain() {
            var expected = NEG_ONE_TO_SIXTEEN.get(3);
            var actual = ArbitraryInteger.valueOfStrict(-28).rootRoundZ(3, RoundingMode.FLOOR);
            assertEquals(expected, actual);
        }
        
        @Test
        public void testAIRootWRemain() {
            var expected = ONE_TO_SIXTEEN.getFirst();
            var actual = ArbitraryInteger.valueOfStrict(68).rootRoundZ(-5, RoundingMode.CEILING);
            assertEquals(expected, actual);
        }
        
        @Test
        public void testIntNegRoot() {
            assertThrows(ArithmeticException.class,
                    () -> NEG_ONE_TO_SIXTEEN.get(5).rootRoundZ(4, null) );
        }
        
        @Test
        public void testAIRaiseNeg() {
            assertThrows(ArithmeticException.class,
                    () -> NEG_ONE_TO_SIXTEEN.get(9).rootRoundZ(ONE_TO_SIXTEEN.get(5), null) );
        }
        
        @Test
        public void testInt0Root0() {
            assertThrows(ArithmeticException.class,
                    () -> ONE_TO_SIXTEEN.get(6).rootRoundZ(0, null) );
        }
        
        @Test
        public void testAI0Root0() {
            assertThrows(ArithmeticException.class,
                    () -> ONE_TO_SIXTEEN.get(3).rootRoundZ(ZERO, null) );
        }
        
        @Test
        public void testDefaultRound() {
            assertTrue(ALL_AI.stream().allMatch(RootRoundTests::testDefault));
        }
        
        private static boolean testDefault(
                ArbitraryInteger input
        ) {
            return switch (input.modulo(ONE_TO_SIXTEEN.get(3)).intValue()) {
                case 0, 1 -> input.rootRoundZ(ONE_TO_SIXTEEN.get(2), null)
                        .equals( input.rootRoundZ(ONE_TO_SIXTEEN.get(2), AlgebraNumber.DEFAULT_ROUNDING) );
                default -> input.rootRoundZ(3, null)
                        .equals( input.rootRoundZ(3, AlgebraNumber.DEFAULT_ROUNDING) );
            };
        }
        
        @Test
        public void testNull() {
            assertThrows(NullPointerException.class, () -> ZERO.rootRoundZ(null, null));
        }
    }
    
    @Nested
    public final class FactorizationTests {
        @Test
        public void testFullFactorization() {
            var expected = List.of(
                    ONE_TO_SIXTEEN.getFirst(),
                    ONE_TO_SIXTEEN.get(1),
                    ONE_TO_SIXTEEN.get(2),
                    ONE_TO_SIXTEEN.get(3),
                    ONE_TO_SIXTEEN.get(5),
                    ONE_TO_SIXTEEN.get(7),
                    ONE_TO_SIXTEEN.get(11),
                    ArbitraryInteger.valueOfStrict(24)
            );
            var actual = ArbitraryInteger.valueOfStrict(-24).factors();
            assertEquals(expected, actual);
        }
        
        @Test
        public void testPosPrimeFactorization() {
            var expected = List.of(
                    ONE_TO_SIXTEEN.get(1),
                    ONE_TO_SIXTEEN.get(2),
                    ONE_TO_SIXTEEN.get(4),
                    ONE_TO_SIXTEEN.get(6)
            );
            var actual = ArbitraryInteger.valueOfStrict(210).primeFactorization();
            assertEquals(expected, actual);
        }
        
        
        @Test
        public void testNegPrimeFactorization() {
            var expected = List.of();
            var actual = ArbitraryInteger.valueOfStrict(-349875623).primeFactorization();
            assertEquals(expected, actual);
        }
    }
}
