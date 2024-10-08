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

public final class FiniteIntegerTests {
    
    private static final FiniteInteger ZERO = FiniteInteger.valueOfStrict(0);
    
    private static final List<FiniteInteger> ONE_TO_SIXTEEN = LongStream.rangeClosed(1, 16)
            .mapToObj(FiniteInteger::valueOfStrict)
            .toList();
    
    private static final List<FiniteInteger> NEG_ONE_TO_SIXTEEN = LongStream.rangeClosed(1, 16)
            .map(l -> -l)
            .mapToObj(FiniteInteger::valueOfStrict)
            .toList();
    
    private static final long LONG_TEST_LONG = 0x123456789ABCDEFL;
    
    private static final BigInteger LONG_TEST_BI = BigInteger.valueOf(LONG_TEST_LONG);
    
    private static final FiniteInteger LONG_TEST_FI = FiniteInteger.valueOfStrict(LONG_TEST_BI);
    
    private static final List<FiniteInteger> ALL_FI = Stream.of(NEG_ONE_TO_SIXTEEN.stream(),
                    Stream.of(ZERO, LONG_TEST_FI), ONE_TO_SIXTEEN.stream())
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
        public void testTooBigInputLong() {
            assertThrows(IllegalArgumentException.class, () -> FiniteInteger.valueOfStrict(Long.MIN_VALUE));
        }
        
        @Test
        public void testTooBigInputBI() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> FiniteInteger.valueOfStrict(BigMathObjectUtils.LONG_MIN_BI)
            );
        }
        
        @Test
        public void testCacheIdentityLong() {
            FiniteInteger a = FiniteInteger.valueOfStrict(15);
            FiniteInteger b = FiniteInteger.valueOfStrict(15);
            assertSame(a, b);
        }
        
        @Test
        public void valueTestLong() {
            assertEquals(-18, FiniteInteger.valueOfStrict(-18).longValue());
        }
        
        @Test
        public void valueTestBI() {
            assertEquals(BigInteger.ONE, FiniteInteger.valueOfStrict(BigInteger.ONE).toBigInteger());
        }
    }
    
    @Nested
    public final class ValueOfTests {
        @Test
        public void testTooBigInputLong() {
            assertInstanceOf(ArbitraryInteger.class, FiniteInteger.valueOf(Long.MIN_VALUE));
        }
        
        @Test
        public void testTooBigInputBI() {
            AlgebraInteger test = FiniteInteger.valueOf(BigMathObjectUtils.LONG_MIN_BI.pow(2));
            assertInstanceOf(ArbitraryInteger.class, test);
        }
        
        @Test
        public void testCacheIdentityLong() {
            AlgebraInteger a = FiniteInteger.valueOf(15);
            AlgebraInteger b = FiniteInteger.valueOf(15);
            assertSame(a, b);
        }
        
        @Test
        public void valueTestLong() {
            assertEquals(-18, FiniteInteger.valueOf(-18).longValue());
        }
        
        @Test
        public void valueTestBI() {
            assertEquals(BigInteger.ONE, FiniteInteger.valueOf(BigInteger.ONE).toBigInteger());
        }
    }
    
    @Nested
    public final class RoundQTests {
        private static final int LONG_LEN = Long.toString(Long.MAX_VALUE).length();
        
        @Test
        public void testThisness() {
            assertSame(LONG_TEST_FI, LONG_TEST_FI.roundQ());
        }
        
        @Test
        public void testNoTrimSplit() {
            assertSame(LONG_TEST_FI, LONG_TEST_FI.roundQ(LONG_LEN, null));
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
                    FiniteInteger.valueOfStrict(trimExp(LONG_TEST_LONG, 5)),
                    LONG_TEST_FI.roundQ(5, RoundingMode.DOWN)
            );
        }
        
        @Test
        public void testNoTrimMC() {
            assertSame(LONG_TEST_FI, LONG_TEST_FI.roundQ(null));
        }
        
        @Test
        public void testTrimMC() {
            assertEquals(
                    FiniteInteger.valueOfStrict(trimExp(LONG_TEST_LONG, 7)),
                    LONG_TEST_FI.roundQ(new MathContext(7, RoundingMode.DOWN))
            );
        }
    }
    
    @Nested
    public final class RoundZTests {
        @Test
        public void testThisNoArg() {
            assertSame(LONG_TEST_FI, LONG_TEST_FI.roundZ());
        }
        
        @Test
        public void testThisArg() {
            assertSame(LONG_TEST_FI, LONG_TEST_FI.roundZ(RoundingMode.UP));
        }
    }
    
    @Nested
    public final class ToBigDecimalTests {
        private static final int LONG_LEN = Long.toString(Long.MAX_VALUE).length();
        
        private static final BigDecimal ANS_BD = BigDecimal.valueOf(LONG_TEST_LONG);
        
        @Test
        public void testNoArg() {
            assertEquals(ANS_BD, LONG_TEST_FI.toBigDecimal());
        }
        
        @Test
        public void testNoTrimSplit() {
            assertEquals(ANS_BD, LONG_TEST_FI.toBigDecimal(LONG_LEN, null));
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
            BigDecimal exp = BigDecimal.valueOf(trimExp(LONG_TEST_FI.longValue(), 5));
            BigDecimal act = LONG_TEST_FI.toBigDecimal(5, RoundingMode.DOWN);
            assertTrue(exp.compareTo(act) == 0);
        }
        
        @Test
        public void testNoTrimMC() {
            assertEquals(ANS_BD, LONG_TEST_FI.toBigDecimal(new MathContext(LONG_LEN, RoundingMode.HALF_EVEN)));
        }
        
        @Test
        public void testTrimMC() {
            BigDecimal exp = BigDecimal.valueOf(trimExp(LONG_TEST_FI.longValue(), 7));
            BigDecimal act = LONG_TEST_FI.toBigDecimal(new MathContext(7, RoundingMode.DOWN));
            assertTrue(exp.compareTo(act) == 0);
        }
    }
    
    @Nested
    public final class ToBigIntegerTests {
        @Test
        public void testThisNoArg() {
            assertSame(LONG_TEST_BI, LONG_TEST_FI.toBigInteger());
        }
        
        @Test
        public void testThisArg() {
            assertSame(LONG_TEST_BI, LONG_TEST_FI.toBigInteger(null));
        }
    }
    
    @Nested
    public final class ToStringTests {
        @Test
        public void testWORadix() {
            String val = "2375423485912345713";
            assertEquals(val, FiniteInteger.valueOfStrict(Long.parseLong(val)).toString());
        }
        
        @Test
        public void testWRadix() {
            String val = "3236E5FE5674DC";
            assertEquals(val.toLowerCase(),
                    FiniteInteger.valueOfStrict(Long.parseLong(val, 16)).toString(16));
        }
        
        @Test
        public void testWRadixAndVar() {
            String val = "3236E5FE5674DC";
            assertEquals(
                    val.toLowerCase(),
                    FiniteInteger.valueOfStrict(Long.parseLong(val, 16))
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
            assertThrows(DisallowedNarrowingException.class, LONG_TEST_FI::intValueExact);
        }
        
        @Test
        public void testShortExc() {
            assertThrows(DisallowedNarrowingException.class, LONG_TEST_FI::shortValueExact);
        }
        
        @Test
        public void testCharExc() {
            assertThrows(DisallowedNarrowingException.class, LONG_TEST_FI::charValueExact);
        }
        
        @Test
        public void testByteExc() {
            assertThrows(DisallowedNarrowingException.class, LONG_TEST_FI::byteValueExact);
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
            assertTrue(ONE_TO_SIXTEEN.stream().noneMatch(FiniteInteger::isZero));
        }
        
        @Test
        public void test1i1() {
            assertTrue(ONE_TO_SIXTEEN.getFirst().isOne());
        }
        
        @Test
        public void testNot1() {
            assertTrue(NEG_ONE_TO_SIXTEEN.stream().noneMatch(FiniteInteger::isOne));
        }
        
        @Test
        public void testNegIsNeg() {
            assertTrue(NEG_ONE_TO_SIXTEEN.stream().allMatch(FiniteInteger::isNegative));
        }
        
        @Test
        public void testNotNeg() {
            assertTrue(ONE_TO_SIXTEEN.stream().noneMatch(FiniteInteger::isNegative));
        }
        
        @Test
        public void testWhole() {
            assertTrue(ALL_FI.stream().allMatch(FiniteInteger::isWhole));
        }
    }
    
    @Nested
    public final class SignumTests {
        @Test
        public void testPositive() {
            assertTrue(ONE_TO_SIXTEEN.stream()
                    .map(FiniteInteger::signum)
                    .allMatch(Signum.POSITIVE::equals));
        }
        
        @Test
        public void testZero() {
            assertSame(Signum.ZERO, ZERO.signum());
        }
        
        @Test
        public void testNegative() {
            assertTrue(NEG_ONE_TO_SIXTEEN.stream()
                    .map(FiniteInteger::signum)
                    .allMatch(Signum.NEGATIVE::equals));
        }
        
        @Test
        public void testPositiveSupOne() {
            assertTrue(ONE_TO_SIXTEEN.subList(1, ONE_TO_SIXTEEN.size())
                    .stream()
                    .map(FiniteInteger::sigmagnum)
                    .allMatch(Sigmagnum.POSITIVE_SUP_ONE::equals));
        }
        
        @Test
        public void testPositiveOne() {
            assertSame(Sigmagnum.POSITIVE_ONE, ONE_TO_SIXTEEN.getFirst().sigmagnum());
        }
        
        @Test
        public void testZeroSigMagNum() {
            assertSame(Sigmagnum.ZERO, ZERO.sigmagnum());
        }
        
        @Test
        public void testNegativeOne() {
            assertSame(Sigmagnum.NEGATIVE_ONE, NEG_ONE_TO_SIXTEEN.getFirst().sigmagnum());
        }
        
        @Test
        public void testNegativeSubMinusOne() {
            assertTrue(NEG_ONE_TO_SIXTEEN.subList(1, NEG_ONE_TO_SIXTEEN.size())
                    .stream()
                    .map(FiniteInteger::sigmagnum)
                    .allMatch(Sigmagnum.NEGATIVE_SUB_MINUS_ONE::equals));
        }
    }
    
    @Nested
    public final class NegateAndAbsTests {
        @Test
        public void negationTest() {
            assertArrayEquals(ONE_TO_SIXTEEN.toArray(), NEG_ONE_TO_SIXTEEN.stream()
                    .map(FiniteInteger::negated)
                    .toArray());
        }
        
        @Test
        public void posAbsTest() {
            assertArrayEquals(ONE_TO_SIXTEEN.toArray(), NEG_ONE_TO_SIXTEEN.stream()
                    .map(FiniteInteger::magnitude)
                    .toArray());
        }
        
        @Test
        public void negAbsTest() {
            assertTrue(ONE_TO_SIXTEEN.stream()
                    .map(FiniteInteger::magnitude)
                    .allMatch(i -> ONE_TO_SIXTEEN.get(i.intValue() - 1) == i));
        }
    }
    
    @Nested
    public final class MaxTests {
        @Test
        public void testAI() {
            assertSame(ONE_TO_SIXTEEN.get(5), ONE_TO_SIXTEEN.get(2).max(ONE_TO_SIXTEEN.get(5)));
        }
        
        @Test
        public void testBI1() {
            assertSame(ONE_TO_SIXTEEN.get(14), ONE_TO_SIXTEEN.get(14).max(BigInteger.TWO));
        }
        
        @Test
        public void testBI2() {
            assertSame(ONE_TO_SIXTEEN.get(15), ONE_TO_SIXTEEN.get(5).max(BigInteger.valueOf(16)));
        }
        
        @Test
        public void testL1() {
            assertSame(NEG_ONE_TO_SIXTEEN.get(12), NEG_ONE_TO_SIXTEEN.get(12).max(-65233565));
        }
        
        @Test
        public void testL2() {
            assertSame(NEG_ONE_TO_SIXTEEN.get(2), NEG_ONE_TO_SIXTEEN.get(12).max(-3));
        }
        
        @Test
        public void testR1() {
            assertSame(ONE_TO_SIXTEEN.get(14), ONE_TO_SIXTEEN.get(14).max((Rational) NEG_ONE_TO_SIXTEEN.getLast()));
        }
        
        @Test
        public void testR2() {
            assertSame(ONE_TO_SIXTEEN.get(15), ONE_TO_SIXTEEN.get(5).max((Rational) ONE_TO_SIXTEEN.getLast()));
        }
        
        @Test
        public void testAN1() {
            assertSame(
                    NEG_ONE_TO_SIXTEEN.get(12),
                    NEG_ONE_TO_SIXTEEN.get(12).max((AlgebraNumber) NEG_ONE_TO_SIXTEEN.getLast())
            );
        }
        
        @Test
        public void testAN2() {
            assertSame(NEG_ONE_TO_SIXTEEN.get(2), NEG_ONE_TO_SIXTEEN.get(12).max(AlgebraNumber.valueOf(-3)));
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
            assertSame(ONE_TO_SIXTEEN.get(2), ONE_TO_SIXTEEN.get(5).min(ONE_TO_SIXTEEN.get(2)));
        }
        
        @Test
        public void testBI1() {
            assertSame(ONE_TO_SIXTEEN.get(1), ONE_TO_SIXTEEN.get(14).min(BigInteger.TWO));
        }
        
        @Test
        public void testBI2() {
            assertSame(ONE_TO_SIXTEEN.get(5), ONE_TO_SIXTEEN.get(5).min(BigInteger.valueOf(19)));
        }
        
        @Test
        public void testL1() {
            assertSame(NEG_ONE_TO_SIXTEEN.get(12), NEG_ONE_TO_SIXTEEN.get(12).min(65233565));
        }
        
        @Test
        public void testL2() {
            assertSame(NEG_ONE_TO_SIXTEEN.get(2), ONE_TO_SIXTEEN.get(12).min(-3));
        }
        
        @Test
        public void testR1() {
            assertSame(ONE_TO_SIXTEEN.get(14), ONE_TO_SIXTEEN.get(14).min((Rational) ONE_TO_SIXTEEN.getLast()));
        }
        
        @Test
        public void testR2() {
            assertSame(
                    NEG_ONE_TO_SIXTEEN.get(15),
                    ONE_TO_SIXTEEN.get(5).min((Rational) NEG_ONE_TO_SIXTEEN.getLast())
            );
        }
        
        @Test
        public void testAN1() {
            assertSame(
                    NEG_ONE_TO_SIXTEEN.get(12),
                    NEG_ONE_TO_SIXTEEN.get(12).min((AlgebraNumber) ONE_TO_SIXTEEN.getLast())
            );
        }
        
        @Test
        public void testAN2() {
            assertSame(NEG_ONE_TO_SIXTEEN.get(2), NEG_ONE_TO_SIXTEEN.get(2).min(AlgebraNumber.valueOf(45)));
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
        
        private static final FiniteInteger ANS = FiniteInteger.valueOfStrict(20);
        
        @Test
        public void testL() {
            assertSame(ANS, FiniteInteger.valueOfStrict(A).gcf(B));
        }
        
        @Test
        public void testBI() {
            assertSame(ANS, FiniteInteger.valueOfStrict(A).gcf(BigInteger.valueOf(B)));
        }
        
        @Test
        public void testAI() {
            assertSame(ANS, FiniteInteger.valueOfStrict(A).gcf(FiniteInteger.valueOfStrict(-B)));
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
        private static final FiniteInteger ANS = FiniteInteger.valueOfStrict(30);
        
        @Test
        public void testL() {
            assertSame(ANS, ONE_TO_SIXTEEN.get(14).lcm(-10));
        }
        
        @Test
        public void testBI() {
            assertSame(ANS, ONE_TO_SIXTEEN.get(9).lcm(BigInteger.valueOf(-15)));
        }
        
        @Test
        public void testAI() {
            assertSame(ANS, ONE_TO_SIXTEEN.get(14).lcm(ONE_TO_SIXTEEN.get(9)));
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
        public void testCompareToFI() {
            assertTrue(ZERO.compareTo(NEG_ONE_TO_SIXTEEN.getLast()) > 0);
        }
        
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
            assertSame(Signum.MINUS_ONE, ZERO.compare(ONE_TO_SIXTEEN.getFirst()));
        }
        
        @Test
        public void testNull() {
            tryNull(
                    (Consumer<FiniteInteger>) ZERO::compareTo,
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
            assertSame(Signum.MINUS_ONE, ZERO.compare(ONE_TO_SIXTEEN.getFirst()));
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
            int size = ALL_FI.size();
            
            for (int i = 0; i < size; i++) {
                FiniteInteger testI = ALL_FI.get(i);
                for (int j = 0; j <size; j++) {
                    FiniteInteger testJ = ALL_FI.get(j);
                    assertFalse((i == j) ^ (testI.equals(testJ) && (testI.hashCode() == testJ.hashCode())) );
                }
            }
        }
        
        @Test
        public void testNull() {
            assertTrue(ALL_FI.stream().noneMatch(i -> i.equals(null)));
        }
    }
    
    @Nested
    public final class SumTests {
        @Test
        public void testAddAI() {
            var expected = ONE_TO_SIXTEEN.get(2);
            var actual = ONE_TO_SIXTEEN.get(7).sum(NEG_ONE_TO_SIXTEEN.get(4));
            assertSame(expected, actual);
        }
        
        @Test
        public void testAddR() {
            var expected = NEG_ONE_TO_SIXTEEN.get(9);
            var actual = ONE_TO_SIXTEEN.get(2).sum((Rational) NEG_ONE_TO_SIXTEEN.get(12));
            assertSame(expected, actual);
        }
        
        @Test
        public void testAddAN() {
            var expected = ONE_TO_SIXTEEN.getLast();
            var actual = ONE_TO_SIXTEEN.get(11).sum((AlgebraNumber) ONE_TO_SIXTEEN.get(3));
            assertSame(expected, actual);
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
            assertSame(expected, actual);
        }
        
        @Test
        public void testDiffR() {
            var expected = NEG_ONE_TO_SIXTEEN.get(9);
            var actual = ONE_TO_SIXTEEN.get(2).difference((Rational) ONE_TO_SIXTEEN.get(12));
            assertSame(expected, actual);
        }
        
        @Test
        public void testDiffAN() {
            var expected = ONE_TO_SIXTEEN.getLast();
            var actual = ONE_TO_SIXTEEN.get(11).difference((AlgebraNumber) NEG_ONE_TO_SIXTEEN.get(3));
            assertSame(expected, actual);
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
            assertSame(expected, actual);
        }
        
        @Test
        public void testProdR() {
            var expected = FiniteInteger.valueOfStrict(24);
            var actual = NEG_ONE_TO_SIXTEEN.get(7).product((Rational) NEG_ONE_TO_SIXTEEN.get(2));
            assertSame(expected, actual);
        }
        
        @Test
        public void testProdAN() {
            var expected = ONE_TO_SIXTEEN.get(14);
            var actual = ONE_TO_SIXTEEN.get(4).product((AlgebraNumber) ONE_TO_SIXTEEN.get(2));
            assertSame(expected, actual);
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
        public void testQuotientZFI() {
            var expected = NEG_ONE_TO_SIXTEEN.get(2);
            var actual = ONE_TO_SIXTEEN.get(14).quotientZ(NEG_ONE_TO_SIXTEEN.get(4));
            assertSame(expected, actual);
        }
        
        @Test
        public void testQuotientZDiv0FI() {
            assertThrows(ArithmeticException.class, () -> ONE_TO_SIXTEEN.getFirst().quotientZ(ZERO));
        }
        
        @Test
        public void testQuotientZAI() {
            var expected = NEG_ONE_TO_SIXTEEN.get(3);
            var actual = NEG_ONE_TO_SIXTEEN.getLast().quotientZ((AlgebraInteger) ONE_TO_SIXTEEN.get(3));
            assertSame(expected, actual);
        }
        
        @Test
        public void testQuotientZDiv0AI() {
            assertThrows(ArithmeticException.class, () -> ONE_TO_SIXTEEN.getFirst().quotientZ((AlgebraInteger) ZERO));
        }
        
        @Test
        public void testQuotientZR() {
            var expected = ONE_TO_SIXTEEN.get(6);
            var actual = NEG_ONE_TO_SIXTEEN.get(13).quotientZ((Rational) NEG_ONE_TO_SIXTEEN.get(1));
            assertSame(expected, actual);
        }
        
        @Test
        public void testQuotientZDiv0R() {
            assertThrows(ArithmeticException.class, () -> ONE_TO_SIXTEEN.getFirst().quotientZ((Rational) ZERO));
        }
        
        @Test
        public void testQuotientZAN() {
            var expected = ONE_TO_SIXTEEN.getLast();
            var actual = FiniteInteger.valueOfStrict(64).quotientZ((AlgebraNumber) ONE_TO_SIXTEEN.get(3));
            assertSame(expected, actual);
        }
        
        @Test
        public void testQuotientZDiv0AN() {
            assertThrows(ArithmeticException.class, () -> ONE_TO_SIXTEEN.getFirst().quotientZ((AlgebraNumber) ZERO));
        }
        
        @Test
        public void testNull() {
            tryNull(
                    (Consumer<FiniteInteger>) ZERO::quotientZ,
                    (Consumer<AlgebraInteger>) ZERO::quotientZ,
                    (Consumer<Rational>) ZERO::quotientZ,
                    (Consumer<AlgebraNumber>) ZERO::quotientZ
            );
        }
    }
    
    @Nested
    public final class QuotientZWithRemainderTests {
        @Test
        public void testQuoRemainFI() {
            var expected = new NumberRemainderPair<>(NEG_ONE_TO_SIXTEEN.get(1), ONE_TO_SIXTEEN.getFirst());
            var actual = ONE_TO_SIXTEEN.get(14).quotientZWithRemainder(NEG_ONE_TO_SIXTEEN.get(6));
            assertEquals(expected, actual);
        }
        
        @Test
        public void testQuoRemainDiv0FI() {
            assertThrows(ArithmeticException.class, () -> ONE_TO_SIXTEEN.getFirst().quotientZWithRemainder(ZERO));
        }
        
        @Test
        public void testQuoRemainAI() {
            var expected = new NumberRemainderPair<>(ONE_TO_SIXTEEN.get(2), ONE_TO_SIXTEEN.get(1));
            var actual = ONE_TO_SIXTEEN.get(10).quotientZWithRemainder((AlgebraInteger) ONE_TO_SIXTEEN.get(2));
            assertEquals(expected, actual);
        }
        
        @Test
        public void testQuoRemainDiv0AI() {
            assertThrows(ArithmeticException.class,
                    () -> ONE_TO_SIXTEEN.getFirst().quotientZWithRemainder((AlgebraInteger) ZERO) );
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
                    (Consumer<FiniteInteger>) ZERO::quotientZWithRemainder,
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
            assertSame(expected, actual);
        }
        
        @Test
        public void testNoRoundingMode() {
            var expected = ONE_TO_SIXTEEN.get(3);
            var actual = ONE_TO_SIXTEEN.get(14).quotientRoundZ(ONE_TO_SIXTEEN.get(3), null);
            assertSame(expected, actual);
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
        public void testRemainderFI() {
            var expected = NEG_ONE_TO_SIXTEEN.getFirst();
            var actual = NEG_ONE_TO_SIXTEEN.getLast().remainder(NEG_ONE_TO_SIXTEEN.get(4));
            assertSame(expected, actual);
        }
        
        @Test
        public void testRemainderDiv0FI() {
            assertThrows(ArithmeticException.class, () -> ONE_TO_SIXTEEN.getFirst().remainder(ZERO));
        }
        
        @Test
        public void testRemainderAI() {
            var expected = ONE_TO_SIXTEEN.get(3);
            var actual = ONE_TO_SIXTEEN.get(10).remainder((AlgebraInteger) NEG_ONE_TO_SIXTEEN.get(6));
            assertSame(expected, actual);
        }
        
        @Test
        public void testRemainderDiv0AI() {
            assertThrows(ArithmeticException.class, () -> ONE_TO_SIXTEEN.getFirst().remainder((AlgebraInteger) ZERO));
        }
        
        @Test
        public void testRemainderR() {
            var expected = NEG_ONE_TO_SIXTEEN.get(6);
            var actual = NEG_ONE_TO_SIXTEEN.get(14).remainder((Rational) ONE_TO_SIXTEEN.get(7));
            assertSame(expected, actual);
        }
        
        @Test
        public void testRemainderDiv0R() {
            assertThrows(ArithmeticException.class, () -> ONE_TO_SIXTEEN.getFirst().remainder((Rational) ZERO));
        }
        
        @Test
        public void testRemainderAN() {
            var expected = ONE_TO_SIXTEEN.getFirst();
            var actual = ONE_TO_SIXTEEN.getLast().remainder((AlgebraNumber) ONE_TO_SIXTEEN.get(4));
            assertSame(expected, actual);
        }
        
        @Test
        public void testRemainderDiv0AN() {
            assertThrows(ArithmeticException.class, () -> ONE_TO_SIXTEEN.getFirst().remainder((AlgebraNumber) ZERO));
        }
        
        @Test
        public void testNull() {
            tryNull(
                    (Consumer<FiniteInteger>) ZERO::remainder,
                    (Consumer<AlgebraInteger>) ZERO::remainder,
                    (Consumer<Rational>) ZERO::remainder,
                    (Consumer<AlgebraNumber>) ZERO::remainder
            );
        }
    }
    
    @Nested
    public final class ModuloTests {
        @Test
        public void testModuloFI() {
            var expected = ONE_TO_SIXTEEN.get(3);
            var actual = NEG_ONE_TO_SIXTEEN.getLast().modulo(ONE_TO_SIXTEEN.get(4));
            assertSame(expected, actual);
        }
        
        @Test
        public void testModuloDiv0FI() {
            assertThrows(ArithmeticException.class, () -> ONE_TO_SIXTEEN.getFirst().modulo(ZERO));
        }
        
        @Test
        public void testModuloDivNegFI() {
            assertThrows(ArithmeticException.class,
                    () -> ONE_TO_SIXTEEN.getFirst().modulo(NEG_ONE_TO_SIXTEEN.getFirst()) );
        }
        
        @Test
        public void testModulusAI() {
            var expected = ONE_TO_SIXTEEN.get(3);
            var actual = ONE_TO_SIXTEEN.get(10).modulo((AlgebraInteger) ONE_TO_SIXTEEN.get(6));
            assertSame(expected, actual);
        }
        
        @Test
        public void testModuloDiv0AI() {
            assertThrows(ArithmeticException.class, () -> ONE_TO_SIXTEEN.getFirst().modulo((AlgebraInteger) ZERO));
        }
        
        @Test
        public void testModuloDivNegAI() {
            assertThrows(ArithmeticException.class,
                    () -> ONE_TO_SIXTEEN.getFirst().modulo((AlgebraInteger) NEG_ONE_TO_SIXTEEN.getFirst()) );
        }
        
        @Test
        public void testQuotientR() {
            var expected = ONE_TO_SIXTEEN.getFirst();
            var actual = NEG_ONE_TO_SIXTEEN.get(14).modulo((Rational) ONE_TO_SIXTEEN.get(7));
            assertSame(expected, actual);
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
            assertSame(expected, actual);
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
                    (Consumer<FiniteInteger>) ZERO::modulo,
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
            assertSame(expected, actual);
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
            assertSame(expected, actual);
        }
        
        @Test
        public void testIntRaise() {
            var expected = FiniteInteger.valueOfStrict(-125);
            var actual = NEG_ONE_TO_SIXTEEN.get(4).raised(3);
            assertEquals(expected, actual);
        }
        
        @Test
        public void testIntRaiseZ() {
            var expected = FiniteInteger.valueOfStrict(625);
            var actual = NEG_ONE_TO_SIXTEEN.get(4).raisedZ(4);
            assertEquals(expected, actual);
        }
        
        @Test
        public void testAIRaise() {
            var expected = FiniteInteger.valueOfStrict(64);
            var actual = ONE_TO_SIXTEEN.get(7).raised(ONE_TO_SIXTEEN.get(1));
            assertEquals(expected, actual);
        }
        
        @Test
        public void testAIRaiseZ() {
            var expected = ONE_TO_SIXTEEN.getFirst();
            var actual = NEG_ONE_TO_SIXTEEN.getFirst()
                    .raisedZ(FiniteInteger.valueOf(
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
            var actual = FiniteInteger.valueOfStrict(68).rootZWithRemainder(ONE_TO_SIXTEEN.get(2));
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
            var actual = FiniteInteger.valueOfStrict(-28).rootRoundZ(3, RoundingMode.FLOOR);
            assertEquals(expected, actual);
        }
        
        @Test
        public void testAIRootWRemain() {
            var expected = ONE_TO_SIXTEEN.getFirst();
            var actual = FiniteInteger.valueOfStrict(68).rootRoundZ(-5, RoundingMode.CEILING);
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
            assertTrue(ALL_FI.stream().allMatch(RootRoundTests::testDefault));
        }
        
        private static boolean testDefault(
                FiniteInteger input
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
                    FiniteInteger.valueOfStrict(24)
            );
            var actual = FiniteInteger.valueOfStrict(-24).factors();
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
            var actual = FiniteInteger.valueOfStrict(210).primeFactorization();
            assertEquals(expected, actual);
        }
        
        
        @Test
        public void testNegPrimeFactorization() {
            var expected = List.of();
            var actual = FiniteInteger.valueOfStrict(-349875623).primeFactorization();
            assertEquals(expected, actual);
        }
    }
}
