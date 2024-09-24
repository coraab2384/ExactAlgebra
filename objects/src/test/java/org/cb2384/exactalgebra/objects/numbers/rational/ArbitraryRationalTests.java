package org.cb2384.exactalgebra.objects.numbers.rational;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import org.cb2384.exactalgebra.objects.AlgebraObject;
import org.cb2384.exactalgebra.objects.Sigmagnum;
import org.cb2384.exactalgebra.objects.numbers.AlgebraNumber;
import org.cb2384.exactalgebra.objects.numbers.integral.AlgebraInteger;
import org.cb2384.exactalgebra.objects.numbers.integral.FiniteInteger;
import org.cb2384.exactalgebra.objects.numbers.integral.IntegerFactory;
import org.cb2384.exactalgebra.objects.pair.NumberRemainderPair;
import org.cb2384.exactalgebra.util.BigMathObjectUtils;
import org.cb2384.exactalgebra.util.corutils.StringUtils;
import org.cb2384.exactalgebra.util.corutils.ternary.Signum;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public final class ArbitraryRationalTests {
    
    private static final ArbitraryRational ZERO = ArbitraryRational.valueOfStrict(0, 1);
    
    private static final List<ArbitraryRational> ONE_TO_SIXTEEN = LongStream.rangeClosed(1, 32)
            .mapToObj(l -> ArbitraryRational.valueOfStrict(l, 2))
            .toList();
    
    private static final List<ArbitraryRational> NEG_ONE_TO_SIXTEEN = LongStream.rangeClosed(1, 32)
            .mapToObj(l -> ArbitraryRational.valueOfStrict(-l, 2))
            .toList();
    
    private static final int BIG_TEST_INT = 0x12345678;
    
    private static final BigInteger BIG_TEST_BI = BigInteger.valueOf(BIG_TEST_INT);
    
    private static final ArbitraryRational BIG_TEST_NUM = ArbitraryRational.valueOfStrict(BIG_TEST_INT, 1);
    
    private static final ArbitraryRational BIG_TEST_NEG_DEN = ArbitraryRational.valueOfStrict(1, -BIG_TEST_INT);
    
    private static final List<ArbitraryRational> ALL_AR = Stream.of(NEG_ONE_TO_SIXTEEN.stream(),
                    Stream.of(ZERO, BIG_TEST_NUM, BIG_TEST_NEG_DEN), ONE_TO_SIXTEEN.stream())
            .flatMap(UnaryOperator.identity())
            .sorted()
            .toList();
    
    private static ArbitraryRational get(
            int numerator,
            int denominator
    ) {
        if ((numerator == 0) && (denominator != 0)) {
            return ZERO;
        }
        return switch (denominator) {
            case -2 -> {
                if (numerator < 0) {
                    yield (numerator >= -32)
                            ? ONE_TO_SIXTEEN.get(-numerator - 1)
                            : ArbitraryRational.valueOfStrict(-numerator, -denominator);
                }
                yield (numerator <= 32)
                        ? NEG_ONE_TO_SIXTEEN.get(numerator - 1)
                        : ArbitraryRational.valueOfStrict(numerator, denominator);
            }
            case -1 -> {
                if (numerator < 0) {
                    yield (numerator >= -16)
                            ? ONE_TO_SIXTEEN.get(-2 * numerator - 1)
                            : ArbitraryRational.valueOfStrict(-numerator, -denominator);
                }
                yield (numerator <= 16)
                        ? NEG_ONE_TO_SIXTEEN.get(2 * numerator - 1)
                        : ArbitraryRational.valueOfStrict(numerator, denominator);
            }
            case 0 -> throw new ArithmeticException();
            case 1 -> {
                if (numerator < 0) {
                    yield (numerator >= -16)
                            ? NEG_ONE_TO_SIXTEEN.get(-2 * numerator - 1)
                            : ArbitraryRational.valueOfStrict(-numerator, -denominator);
                }
                yield (numerator <= 16)
                        ? ONE_TO_SIXTEEN.get(2 * numerator - 1)
                        : ArbitraryRational.valueOfStrict(numerator, denominator);
            }
            case 2 -> {
                if (numerator < 0) {
                    yield (numerator >= -32)
                            ? NEG_ONE_TO_SIXTEEN.get(-numerator - 1)
                            : ArbitraryRational.valueOfStrict(-numerator, -denominator);
                }
                yield (numerator <= 32)
                        ? ONE_TO_SIXTEEN.get(numerator - 1)
                        : ArbitraryRational.valueOfStrict(numerator, denominator);
            }
            default -> (numerator < 0)
                    ? ArbitraryRational.valueOfStrict(-numerator, -denominator)
                    : ArbitraryRational.valueOfStrict(numerator, denominator);
        };
    }
    
    private static void tryNull(
            Consumer<?>... toTest
    ) {
        for (Consumer<?> test : toTest) {
            assertThrows(NullPointerException.class, () -> test.accept(null));
        }
    }
    
    private static int trimExp(
            int val,
            int cut
    ) {
        String valS = String.valueOf(val);
        String first = valS.substring(0, cut);
        String second = "0".repeat(valS.length() - cut);
        return Integer.parseInt(first + second);
    }
    
    private static <T extends AlgebraObject<T>> void assertEquiv(
            T expected,
            T actual
    ) {
        assertTrue(
                expected.equiv(actual),
                "Expected: " + expected + System.lineSeparator() + "Actual: " + actual
        );
    }
    
    @Nested
    public final class ValueOfStrictTests {
        @Test
        public void test0LongDen() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> ArbitraryRational.valueOfStrict(3934867523948L, 0L)
            );
        }
        
        @Test
        public void test0BIDen() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> ArbitraryRational.valueOfStrict(BigInteger.valueOf(-349587623), BigInteger.ZERO)
            );
        }
        
        @Test
        public void testStrictness() {
            var expected = new ArbitraryRational(BigInteger.valueOf(5), BigInteger.ONE);
            var actual = ArbitraryRational.valueOfStrict(5, 1);
            
            assertTrue(
                    (actual instanceof ArbitraryRational) && expected.equals(actual),
                    () -> new StringBuilder("Expected ")
                            .append(StringUtils.getIdealName(ArbitraryRational.class))
                            .append(" of: ")
                            .append(expected)
                            .append(System.lineSeparator())
                            .append("Actual: ")
                            .append(StringUtils.getIdealName(actual.getClass()))
                            .append(" of " )
                            .append(actual)
                            .toString()
            );
        }
    }
    
    @Nested
    public final class ValueOfTests {
        @Test
        public void test0LongDen() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> ArbitraryRational.valueOfStrict(3934867523948L, 0L)
            );
        }
        
        @Test
        public void test0BIDen() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> ArbitraryRational.valueOfStrict(BigInteger.valueOf(-349587623), BigInteger.ZERO)
            );
        }
    }
    
    @Nested
    public final class NumeratorTests {
        
        @Test
        public void testNumeratorAI() {
            assertEquals(IntegerFactory.fromBigInteger(BIG_TEST_BI), BIG_TEST_NUM.numeratorAI());
        }
        
        @Test
        public void testNumeratorBI() {
            assertEquals(BIG_TEST_BI, BIG_TEST_NUM.numeratorBI());
        }
    }
    
    @Nested
    public final class DenominatorTests {
        
        @Test
        public void testDenominatorAI() {
            assertEquals(IntegerFactory.fromLong(BIG_TEST_INT), BIG_TEST_NEG_DEN.denominatorAI());
        }
        
        @Test
        public void testDenominatorBI() {
            assertEquals(BIG_TEST_BI, BIG_TEST_NEG_DEN.denominatorBI());
        }
    }
    
    @Nested
    public final class WholeTests {
        
        @Test
        public void testWholeAI() {
            assertEquals(
                    ArbitraryRational.valueOf(15, 1),
                    get(31, 2).wholeAI()
            );
        }
        
        @Test
        public void testWholeBI() {
            assertEquals(BigInteger.TEN, get(21, 2).wholeBI());
        }
    }
    
    @Nested
    public final class RoundQTests {
        
        @Test
        public void testThisness() {
            assertSame(BIG_TEST_NEG_DEN, BIG_TEST_NEG_DEN.roundQ());
        }
        
        @Test
        public void testNoTrimSplit() {
            assertEquals(BIG_TEST_NEG_DEN,
                    BIG_TEST_NEG_DEN.roundQ(Long.toString(Long.MAX_VALUE).length(), null));
        }
        
        @Test
        public void testTrimSplit() {
            assertEquals(
                    get(1, -trimExp(BIG_TEST_INT, 5)),
                    BIG_TEST_NEG_DEN.roundQ(5, RoundingMode.DOWN)
            );
        }
        
        @Test
        public void testNoTrimMC() {
            assertSame(BIG_TEST_NUM, BIG_TEST_NUM.roundQ(null));
        }
        
        @Test
        public void testTrimMC() {
            assertEquiv(
                    get(trimExp(BIG_TEST_INT, 7), 1),
                    BIG_TEST_NUM.roundQ(new MathContext(7, RoundingMode.DOWN))
            );
        }
    }
    
    @Nested
    public final class RoundZTests {
        @Test
        public void testThisNoArg() {
            assertEquals(
                    ArbitraryRational.valueOf(BIG_TEST_INT, 1),
                    BIG_TEST_NUM.roundZ()
            );
        }
        
        @Test
        public void testThisArg() {
            assertEquals(
                    ArbitraryRational.valueOf(-1, 1),
                    BIG_TEST_NEG_DEN.roundZ(RoundingMode.FLOOR)
            );
        }
    }
    
    @Nested
    public final class ToBigDecimalTests {
        private static final int LONG_LEN = Long.toString(Long.MAX_VALUE).length();
        
        private static final int ANS_DEN = BigInteger.TEN.pow(4).intValue();
        
        private static final ArbitraryRational TEST = ArbitraryRational.valueOfStrict(BIG_TEST_INT, -ANS_DEN);
        
        private static final BigDecimal ANS_BD = new BigDecimal(BIG_TEST_BI.negate(), 4);
        
        private static void assertBDEquiv(
                BigDecimal expected,
                BigDecimal actual
        ) {
            assertTrue(
                    expected.compareTo(actual) == 0,
                    "Expected: " + expected + System.lineSeparator() + "Actual: " + actual
            );
        }
        
        @Test
        public void testNoArg() {
            assertBDEquiv(ANS_BD, TEST.toBigDecimal());
        }
        
        @Test
        public void testNoTrimSplit() {
            var actual = TEST.toBigDecimal(LONG_LEN, null);
            assertBDEquiv(ANS_BD, actual);
        }
        
        @Test
        public void testTrimSplit() {
            var expected = ANS_BD.round(new MathContext(5, RoundingMode.UP));
            var actual = TEST.toBigDecimal(5, RoundingMode.UP);
            assertBDEquiv(expected, actual);
        }
        
        @Test
        public void testNoTrimMC() {
            var actual = TEST.toBigDecimal(new MathContext(LONG_LEN, RoundingMode.HALF_EVEN));
            assertBDEquiv(ANS_BD, actual);
        }
        
        @Test
        public void testTrimMC() {
            var expected = ANS_BD.round(new MathContext(3, RoundingMode.CEILING));
            var actual = TEST.toBigDecimal(new MathContext(3, RoundingMode.CEILING));
            assertBDEquiv(expected, actual);
        }
    }
    
    @Nested
    public final class ToBigIntegerTests {
        @Test
        public void testThisNoArg() {
            assertEquals(BigInteger.TWO, get(3, 2).toBigInteger());
        }
        
        @Test
        public void testThisArg() {
            assertEquals(BigInteger.valueOf(-5), get(-35, 6).toBigInteger(RoundingMode.CEILING));
        }
    }
    
    @Nested
    public final class ToStringTests {
        @Test
        public void testWORadix() {
            String val = "345/2";
            assertEquals(val, get(345, 2).toString());
        }
        
        @Test
        public void testWRadix() {
            String val = "-10/b";
            assertEquals(val, get(-16, 11).toString(16));
        }
        
        @Test
        public void testWRadixAndVar() {
            var expected = "f/20";
            var actual = get(15, 32).toString(16, "sdga", "zuieroew", "");
            assertEquals(expected, actual);
        }
    }
    
    @Nested
    public final class PrimitiveValueTests {
        
        @Test
        public void testDoubleValue() {
            assertEquals(2.5, get(5, 2).doubleValue());
        }
        
        @Test
        public void testFloatValue() {
            assertEquals(-15.25f, get(-61, 4).floatValue());
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
            assertTrue(ONE_TO_SIXTEEN.stream().noneMatch(ArbitraryRational::isZero));
        }
        
        @Test
        public void test1is1() {
            assertTrue(get(1, 1).isOne());
        }
        
        @Test
        public void testNot1() {
            assertTrue(NEG_ONE_TO_SIXTEEN.stream().noneMatch(ArbitraryRational::isOne));
        }
        
        @Test
        public void testNegIsNeg() {
            assertTrue(NEG_ONE_TO_SIXTEEN.stream().allMatch(ArbitraryRational::isNegative));
        }
        
        @Test
        public void testNotNeg() {
            assertTrue(ONE_TO_SIXTEEN.stream().noneMatch(ArbitraryRational::isNegative));
        }
        
        @Test
        public void testWhole() {
            assertTrue(ALL_AR.stream()
                    .filter(r -> BigMathObjectUtils.isOne(r.denominatorBI()))
                    .allMatch(ArbitraryRational::isWhole) );
        }
    }
    
    @Nested
    public final class SignumTests {
        @Test
        public void testPositive() {
            assertTrue(ONE_TO_SIXTEEN.stream()
                    .map(ArbitraryRational::signum)
                    .allMatch(Signum.POSITIVE::equals));
        }
        
        @Test
        public void testZero() {
            assertSame(Signum.ZERO, ZERO.signum());
        }
        
        @Test
        public void testNegative() {
            assertTrue(NEG_ONE_TO_SIXTEEN.stream()
                    .map(ArbitraryRational::signum)
                    .allMatch(Signum.NEGATIVE::equals));
        }
        
        @Test
        public void testPositiveSupOne() {
            assertTrue(ONE_TO_SIXTEEN.subList(2, ONE_TO_SIXTEEN.size())
                    .stream()
                    .map(ArbitraryRational::sigmagnum)
                    .peek(System.out::println)
                    .allMatch(Sigmagnum.POSITIVE_SUP_ONE::equals));
        }
        
        @Test
        public void testPositiveOne() {
            assertSame(Sigmagnum.POSITIVE_ONE, get(1, 1).sigmagnum());
        }
        
        @Test
        public void testPositiveSubOne() {
            assertSame(Sigmagnum.POSITIVE_SUB_ONE, ONE_TO_SIXTEEN.getFirst().sigmagnum());
        }
        
        @Test
        public void testZeroSigMagNum() {
            assertSame(Sigmagnum.ZERO, ZERO.sigmagnum());
        }
        
        @Test
        public void testNegativeOneSupOne() {
            assertSame(Sigmagnum.NEGATIVE_SUP_MINUS_ONE, NEG_ONE_TO_SIXTEEN.getFirst().sigmagnum());
        }
        
        @Test
        public void testNegativeOne() {
            assertSame(Sigmagnum.NEGATIVE_ONE, get(-1, 1).sigmagnum());
        }
        
        @Test
        public void testNegativeSubMinusOne() {
            assertTrue(NEG_ONE_TO_SIXTEEN.subList(2, NEG_ONE_TO_SIXTEEN.size())
                    .stream()
                    .map(ArbitraryRational::sigmagnum)
                    .allMatch(Sigmagnum.NEGATIVE_SUB_MINUS_ONE::equals));
        }
    }
    
    @Nested
    public final class NegateAndAbsTests {
        @Test
        public void negationTest() {
            var actual = NEG_ONE_TO_SIXTEEN.stream().map(ArbitraryRational::negated).toList();
            assertTrue(IntStream.range(1, 32).allMatch(i -> ONE_TO_SIXTEEN.get(i).equiv(actual.get(i))));
        }
        
        @Test
        public void negAbsTest() {
            var actual = NEG_ONE_TO_SIXTEEN.stream().map(ArbitraryRational::magnitude).toList();
            assertTrue(IntStream.range(1, 32).allMatch(i -> ONE_TO_SIXTEEN.get(i).equiv(actual.get(i))));
        }
        
        @Test
        public void posAbsTest() {
            assertTrue(ONE_TO_SIXTEEN.stream().allMatch(r -> r == r.magnitude()));
        }
    }
    
    @Nested
    public final class ComparisonTests {
        @Test
        public void testCompareToFR() {
            assertTrue(ZERO.compareTo(NEG_ONE_TO_SIXTEEN.getLast()) > 0);
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
        public void testNull() {
            tryNull(
                    (Consumer<ArbitraryRational>) ZERO::compareTo,
                    (Consumer<Rational>) ZERO::compareTo,
                    (Consumer<AlgebraNumber>) ZERO::compareTo
            );
        }
    }
    
    @Nested
    public final class EquivTests {
        @Test
        public void testEquivFR() {
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
        public void testNull() {
            tryNull(
                    (Consumer<ArbitraryRational>) ZERO::equiv,
                    (Consumer<Rational>) ZERO::equiv,
                    (Consumer<AlgebraNumber>) ZERO::equiv
            );
        }
    }
    
    @Nested
    public final class EqualsAndHCTests {
        @Test
        public void testEqualsAndHCContract() {
            int size = ALL_AR.size();
            
            for (int i = 0; i < size; i++) {
                ArbitraryRational testI = ALL_AR.get(i);
                for (int j = 0; j <size; j++) {
                    ArbitraryRational testJ = ALL_AR.get(j);
                    assertFalse((i == j) ^ (testI.equals(testJ) && (testI.hashCode() == testJ.hashCode())) );
                }
            }
        }
        
        @Test
        public void testSubclassNotEqual() {
            assertTrue(ALL_AR.stream().allMatch(r -> {
                Rational testR = ArbitraryRational.valueOf(r.numeratorBI(), r.denominatorBI());
                return (testR instanceof AlgebraInteger) != r.equals(testR);
            }));
        }
        
        @Test
        public void testNull() {
            assertTrue(ALL_AR.stream().noneMatch(r -> r.equals(null)));
        }
    }
    
    @Nested
    public final class SumTests {
        @Test
        public void testAddFR() {
            var expected = get(-7, 2);
            var actual = get(11, 2).sum(get(-9, 1));
            assertEquals(expected, actual);
            
        }
        @Test
        public void testAddR() {
            var expected = FiniteInteger.valueOf(7);
            var actual = get(-1, 1).sum(FiniteInteger.valueOf(8));
            assertSame(expected, actual);
        }
        
        @Test
        public void testAddAN() {
            var expected = get(5, 1);
            var actual = get(-7, 2).sum((AlgebraNumber) get(17, 2));
            assertEquiv(expected, actual);
        }
        
        @Test
        public void testNull() {
            tryNull(
                    (Consumer<ArbitraryRational>) ZERO::sum,
                    (Consumer<Rational>) ZERO::sum,
                    (Consumer<AlgebraNumber>) ZERO::sum
            );
        }
    }
    
    @Nested
    public final class DifferenceTests {
        @Test
        public void testDiffFR() {
            var expected = FiniteInteger.valueOf(-1);
            var actual = get(3, 2).difference(get(5, 2));
            assertSame(expected, actual);
        }
        
        @Test
        public void testDiffR() {
            var expected = get(6, 1);
            var actual = get(3, 2).difference(get(-9, 2));
            assertEquiv(expected, actual);
        }
        
        @Test
        public void testDiffAN() {
            var expected = get(-11, 2);
            var actual = get(-4, 1).difference((AlgebraNumber) get(3, 2));
            assertEquals(expected, actual);
        }
        
        @Test
        public void testNull() {
            tryNull(
                    (Consumer<ArbitraryRational>) ZERO::difference,
                    (Consumer<Rational>) ZERO::difference,
                    (Consumer<AlgebraNumber>) ZERO::difference
            );
        }
    }
    
    @Nested
    public final class ProductTests {
        @Test
        public void testProdAI() {
            var expected = get(15, 1);
            var actual = get(-15, 2).product(FiniteInteger.valueOfStrict(-2));
            assertEquiv(expected, actual);
        }
        
        @Test
        public void testProdFR() {
            var expected = get(5, 6);
            var actual = get(5, -2).product(get(-1, 3));
            assertEquals(expected, actual);
        }
        
        @Test
        public void testProdR() {
            var expected = get(9, 2);
            var actual = get(3, 1).product((Rational) get(3, 2));
            assertEquals(expected, actual);
        }
        
        @Test
        public void testProdAN() {
            var expected = get(-14, 1);
            var actual = get(7, -2).product((AlgebraNumber) get(4, 1));
            assertEquiv(expected, actual);
        }
        
        @Test
        public void testNull() {
            tryNull(
                    (Consumer<AlgebraInteger>) ZERO::product,
                    (Consumer<ArbitraryRational>) ZERO::product,
                    (Consumer<Rational>) ZERO::product,
                    (Consumer<AlgebraNumber>) ZERO::product
            );
        }
    }
    
    @Nested
    public final class QuotientTests {
        @Test
        public void testQuotientFR() {
            var expected = NEG_ONE_TO_SIXTEEN.getFirst();
            var actual = get(3, 5).quotient(get(-6, 5));
            assertEquals(expected, actual);
        }
        
        @Test
        public void testQuotientDiv0FR() {
            assertThrows(ArithmeticException.class, () -> NEG_ONE_TO_SIXTEEN.getFirst().quotient(ZERO));
        }
        
        @Test
        public void testQuotientAI() {
            var expected = ArbitraryRational.valueOfStrict(5, -32);
            var actual = get(5, 2).quotient(FiniteInteger.valueOfStrict(-16));
            assertEquals(expected, actual);
        }
        
        @Test
        public void testQuotientDiv0AI() {
            assertThrows(ArithmeticException.class,
                    () -> NEG_ONE_TO_SIXTEEN.getFirst().quotient(FiniteInteger.valueOfStrict(0)));
        }
        
        @Test
        public void testQuotientR() {
            var expected = ONE_TO_SIXTEEN.getFirst();
            var actual = get(8, -1).quotient(FiniteInteger.valueOfStrict(-16));
            assertEquals(expected, actual);
        }
        
        @Test
        public void testQuotientDiv0R() {
            assertThrows(ArithmeticException.class, () -> ONE_TO_SIXTEEN.getFirst().quotient((Rational) ZERO));
        }
        
        @Test
        public void testQuotientAN() {
            var expected = get(15, -2);
            var actual = get(9, 1).quotient(
                    (AlgebraNumber) ArbitraryRational.valueOfStrict(6, -5));
            assertEquals(expected, actual);
        }
        
        @Test
        public void testQuotientDiv0AN() {
            assertThrows(ArithmeticException.class, () -> ONE_TO_SIXTEEN.getFirst().quotient((AlgebraNumber) ZERO));
        }
        
        @Test
        public void testNull() {
            tryNull(
                    (Consumer<ArbitraryRational>) ZERO::quotient,
                    (Consumer<AlgebraInteger>) ZERO::quotient,
                    (Consumer<Rational>) ZERO::quotient,
                    (Consumer<AlgebraNumber>) ZERO::quotient
            );
        }
    }
    
    @Nested
    public final class QuotientZTests {
        @Test
        public void testQuotientZFI() {
            var expected = FiniteInteger.valueOfStrict(-3);
            var actual = get(31, 2).quotientZ(get(-4, 1));
            assertEquals(expected, actual);
        }
        
        @Test
        public void testQuotientZDiv0FI() {
            assertThrows(ArithmeticException.class, () -> NEG_ONE_TO_SIXTEEN.getFirst().quotientZ(ZERO));
        }
        
        @Test
        public void testNull() {
            assertThrows(NullPointerException.class, () -> ONE_TO_SIXTEEN.getFirst().quotientZ(null));
        }
    }
    
    @Nested
    public final class QuotientZWithRemainderTests {
        @Test
        public void testQuoRemainR() {
            var expected = new NumberRemainderPair<>(FiniteInteger.valueOfStrict(-5),
                    get(5, -2));
            var actual = get(75, -2).quotientZWithRemainder(
                    get(7, 1));
            assertEquals(expected, actual);
        }
        
        @Test
        public void testQuoRemainDiv0R() {
            assertThrows(ArithmeticException.class,
                    () -> NEG_ONE_TO_SIXTEEN.getFirst().quotientZWithRemainder(ZERO) );
        }
        
        @Test
        public void testQuoRemainAN() {
            var expected = new NumberRemainderPair<>(FiniteInteger.valueOfStrict(-4),
                    ArbitraryRational.valueOfStrict(5, -4));
            var actual = ArbitraryRational.valueOfStrict(69, -4).quotientZWithRemainder(
                    (AlgebraNumber) get(4, 1));
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
                    (Consumer<Rational>) ZERO::quotientZWithRemainder,
                    (Consumer<AlgebraNumber>) ZERO::quotientZWithRemainder
            );
        }
    }
    
    @Nested
    public final class QuotientRoundZTests {
        @Test
        public void testRoundingMode() {
            var expected = get(7, 1);
            var actual = get(25, -2).quotientRoundZ(
                    get(2, -1),
                    RoundingMode.UP
            );
            assertEquiv(expected, actual);
        }
        
        @Test
        public void testNoRoundingMode() {
            var actual = ArbitraryRational.valueOfStrict(11, -1224).quotientRoundZ(
                    FiniteInteger.valueOfStrict(3445),
                    null
            );
            assertEquiv(ZERO, actual);
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
        public void testRemainderR() {
            var expected = get(3, -2);
            var actual = get(19, -2).remainder(get(4, -1));
            assertEquals(expected, actual);
        }
        
        @Test
        public void testRemainderDiv0R() {
            assertThrows(ArithmeticException.class, () -> NEG_ONE_TO_SIXTEEN.getFirst().remainder(ZERO));
        }
        
        @Test
        public void testRemainderAN() {
            var expected = get(5, -2);
            var actual = get(15, -2).remainder((AlgebraNumber) get(5, -1));
            assertEquals(expected, actual);
        }
        
        @Test
        public void testRemainderDiv0AN() {
            assertThrows(ArithmeticException.class, () -> ONE_TO_SIXTEEN.getFirst().remainder((AlgebraNumber) ZERO));
        }
        
        @Test
        public void testNull() {
            tryNull(
                    (Consumer<Rational>) ZERO::remainder,
                    (Consumer<AlgebraNumber>) ZERO::remainder
            );
        }
    }
    
    @Nested
    public final class ModuloTests {
        @Test
        public void testQuotientR() {
            var expected = FiniteInteger.valueOfStrict(1);
            var actual = get(15, -1).modulo(get(4, 1));
            assertEquals(expected, actual);
        }
        
        @Test
        public void testModuloDiv0R() {
            assertThrows(ArithmeticException.class, () -> ONE_TO_SIXTEEN.getFirst().modulo(ZERO));
        }
        
        @Test
        public void testModuloDivNegR() {
            assertThrows(ArithmeticException.class,
                    () -> ONE_TO_SIXTEEN.getFirst().modulo(NEG_ONE_TO_SIXTEEN.getFirst()) );
        }
        
        @Test
        public void testQuotientAN() {
            var expected = get(3, 5);
            var actual = get(8, 5).modulo((AlgebraNumber) FiniteInteger.valueOfStrict(1));
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
                    (Consumer<Rational>) ZERO::modulo,
                    (Consumer<AlgebraNumber>) ZERO::modulo
            );
        }
    }
    
    @Nested
    public final class RaiseTests {
        @Test
        public void testSquare() {
            var expected = ONE_TO_SIXTEEN.getLast();
            var actual = get(-4, 1).squared();
            assertEquiv(expected, actual);
        }
        
        @Test
        public void testIntRaise() {
            var expected = get(-125, 64);
            var actual = get(5, -4).raised(3);
            assertEquals(expected, actual);
        }
        
        @Test
        public void testAIRaise() {
            var expected = FiniteInteger.valueOfStrict(256);
            var actual = get(4, -1).raised(FiniteInteger.valueOfStrict(4));
            assertEquals(expected, actual);
        }
        
        @Test
        public void testInt0To0() {
            assertThrows(ArithmeticException.class, () -> ZERO.raised(0));
        }
        
        @Test
        public void testAI0To0() {
            assertThrows(ArithmeticException.class, () -> ZERO.raised(FiniteInteger.valueOfStrict(0)));
        }
        
        @Test
        public void testNull() {
            assertThrows(NullPointerException.class, () -> NEG_ONE_TO_SIXTEEN.getFirst().raised(null));
        }
    }
    
    @Nested
    public final class RootWithRemainderTests {
        @Test
        public void testSqrtWRemain() {
            var expected = new NumberRemainderPair<>(FiniteInteger.valueOfStrict(4),
                    get(3, 2));
            var actual = get(35, 2).sqrtZWithRemainder();
            assertEquals(expected, actual);
        }
        
        @Test
        public void testSqrtWRemainNeg() {
            assertThrows(ArithmeticException.class, () -> NEG_ONE_TO_SIXTEEN.getFirst().sqrtZWithRemainder());
        }
        
        @Test
        public void testIntRootWRemain() {
            var expected = new NumberRemainderPair<>(FiniteInteger.valueOfStrict(0),
                    FiniteInteger.valueOfStrict(-16));
            var actual = NEG_ONE_TO_SIXTEEN.getLast().rootZWithRemainder(-3);
            assertEquals(expected, actual);
        }
        
        @Test
        public void testAIRootWRemain() {
            var expected = new NumberRemainderPair<>(FiniteInteger.valueOfStrict(-3),
                    get(3, -2));
            var actual = ArbitraryRational.valueOfStrict(489, -2)
                    .rootZWithRemainder(FiniteInteger.valueOfStrict(5));
            assertEquals(expected, actual);
        }
        
        @Test
        public void testIntNegRoot() {
            assertThrows(ArithmeticException.class, () -> NEG_ONE_TO_SIXTEEN.getFirst().rootZWithRemainder(4));
        }
        
        @Test
        public void testAIRaiseNeg() {
            assertThrows(ArithmeticException.class,
                    () -> NEG_ONE_TO_SIXTEEN.getFirst().rootZWithRemainder(FiniteInteger.valueOfStrict(4)));
        }
        
        @Test
        public void testInt0Root0() {
            assertThrows(ArithmeticException.class, () -> ONE_TO_SIXTEEN.getFirst().rootZWithRemainder(0));
        }
        
        @Test
        public void testAI0Root0() {
            assertThrows(ArithmeticException.class,
                    () -> ONE_TO_SIXTEEN.getFirst().rootZWithRemainder(FiniteInteger.valueOfStrict(0)) );
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
            var expected = FiniteInteger.valueOfStrict(4);
            var actual = get(39, 2).sqrtRoundZ(RoundingMode.FLOOR);
            assertEquals(expected, actual);
        }
        
        @Test
        public void testSqrtWRemainNeg() {
            assertThrows(ArithmeticException.class, () -> NEG_ONE_TO_SIXTEEN.getFirst().sqrtRoundZ(null));
        }
        
        @Test
        public void testIntRootWRemain() {
            var expected = FiniteInteger.valueOfStrict(-4);
            var actual = ArbitraryRational.valueOfStrict(85, -3)
                    .rootRoundZ(3, RoundingMode.UP);
            assertEquals(expected, actual);
        }
        
        @Test
        public void testAIRootWRemain() {
            var expected = FiniteInteger.valueOfStrict(1);
            var actual = get(68, 1).rootRoundZ(-5, RoundingMode.CEILING);
            assertEquals(expected, actual);
        }
        
        @Test
        public void testIntNegRoot() {
            assertThrows(ArithmeticException.class,
                    () -> NEG_ONE_TO_SIXTEEN.getFirst().rootRoundZ(4, null) );
        }
        
        @Test
        public void testAIRaiseNeg() {
            assertThrows(ArithmeticException.class,
                    () -> NEG_ONE_TO_SIXTEEN.getFirst().rootRoundZ(FiniteInteger.valueOfStrict(2), null) );
        }
        
        @Test
        public void testInt0Root0() {
            assertThrows(ArithmeticException.class,
                    () -> ONE_TO_SIXTEEN.getFirst().rootRoundZ(0, null) );
        }
        
        @Test
        public void testAI0Root0() {
            assertThrows(ArithmeticException.class,
                    () -> ONE_TO_SIXTEEN.getFirst().rootRoundZ(FiniteInteger.valueOfStrict(0), null) );
        }
        
        @Test
        public void testDefaultRound() {
            assertTrue(ALL_AR.stream().allMatch(r -> {
                if (r.isWhole()) {
                    FiniteInteger three = FiniteInteger.valueOfStrict(3);
                    return r.rootRoundZ(three, null)
                            .equals( r.rootRoundZ(three, AlgebraNumber.DEFAULT_ROUNDING) );
                }
                return r.rootRoundZ(3, null)
                        .equals( r.rootRoundZ(3, AlgebraNumber.DEFAULT_ROUNDING) );
            }));
        }
        
        @Test
        public void testNull() {
            assertThrows(NullPointerException.class, () -> ZERO.rootRoundZ(null, null));
        }
    }
}
