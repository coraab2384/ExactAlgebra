package org.cb2384.exactalgebra.objects.numbers.integral;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Random;

import org.cb2384.exactalgebra.objects.Sigmagnum;
import org.cb2384.exactalgebra.objects.exceptions.DisallowedNarrowingException;
import org.cb2384.exactalgebra.objects.numbers.AlgebraNumber;
import org.cb2384.exactalgebra.objects.numbers.rational.Rational;
import org.cb2384.exactalgebra.objects.pair.NumberRemainderPair;
import org.cb2384.exactalgebra.util.corutils.ternary.Signum;
import org.junit.jupiter.api.Test;

public class FiniteIntegerTests {
    
    private static final FiniteInteger ONE = FiniteInteger.valueOfStrict(1);
    
    private static final FiniteInteger ZERO = FiniteInteger.valueOfStrict(0);
    
    private static final FiniteInteger NEG_ONE = FiniteInteger.valueOfStrict(-1);
    
    @Test
    public void baseOpTests() {
        FiniteInteger a = FiniteInteger.valueOfStrict(15783);
        FiniteInteger b = FiniteInteger.valueOfStrict(-34981236490325684L);
        FiniteInteger c = FiniteInteger.valueOfStrict(-17490618245162842L);
        assertEquals(-17490618245162842L + 15783, a.sum(c).longValue());
        assertEquals(-17490618245162842L - 15783, c.difference(a).longValue());
        assertEquals(15783 * 15783, a.squared().longValue());
        assertEquals(-17490618245162842L, b.quotientZ(FiniteInteger.valueOfStrict(2)).longValue());
    }
    
    @Test
    public void constructionTests() {
        assertThrows(IllegalArgumentException.class, () -> FiniteInteger.valueOfStrict(Long.MIN_VALUE));
        FiniteInteger a = FiniteInteger.valueOfStrict(15);
        FiniteInteger b = FiniteInteger.valueOfStrict(15);
        assertSame(a, b);
        FiniteInteger c = FiniteInteger.valueOfStrict(-(long) Integer.MIN_VALUE);
        FiniteInteger d = (FiniteInteger) FiniteInteger.valueOfStrict(Integer.MAX_VALUE).sum(FiniteInteger.valueOfStrict(1));
        assertEquals(c, d);
        assertNotSame(c, d);
    }
    
    @Test
    public void stringTests() {
        FiniteInteger a = FiniteInteger.valueOfStrict(2);
        assertEquals(String.valueOf(2), a.toString());
        FiniteInteger b = FiniteInteger.valueOfStrict(18);
        assertEquals("12", b.toString(16));
    }
    
    @Test
    public void truncationTests() {
        FiniteInteger i = FiniteInteger.valueOfStrict(Long.MAX_VALUE);
        FiniteInteger s = FiniteInteger.valueOfStrict(Integer.MAX_VALUE);
        FiniteInteger b = FiniteInteger.valueOfStrict(Short.MAX_VALUE);
        assertThrows(DisallowedNarrowingException.class, i::intValueExact);
        assertThrows(DisallowedNarrowingException.class, s::shortValueExact);
        assertThrows(DisallowedNarrowingException.class, b::byteValueExact);
        assertThrows(DisallowedNarrowingException.class, s::charValueExact);
        assertEquals(Long.MAX_VALUE, i.longValue());
        assertEquals(Integer.MAX_VALUE, s.intValue());
        assertEquals(Short.MAX_VALUE, b.intValue());
        assertEquals(-1, b.byteValue());
        assertEquals(Short.MAX_VALUE, b.charValueExact());
    }
    
    @Test
    public void queryTests() {
        assertTrue(ZERO.isZero());
        assertTrue(ONE.isOne());
        assertFalse(NEG_ONE.isOne());
        assertFalse(ONE.isZero());
        assertFalse(ZERO.isNegative());
        assertTrue(FiniteInteger.valueOfStrict(-1134542554635L).isNegative());
        assertTrue(FiniteInteger.valueOfStrict(489567242).isWhole());
    }
    
    @Test
    public void signumTests() {
        FiniteInteger pos = FiniteInteger.valueOfStrict(16);
        FiniteInteger neg = FiniteInteger.valueOfStrict(-8);
        assertSame(Signum.POSITIVE, pos.signum());
        assertSame(Signum.ZERO, ZERO.signum());
        assertSame(Signum.NEGATIVE, NEG_ONE.signum());
        assertSame(Sigmagnum.POSITIVE_ONE, ONE.sigmagnum());
        assertSame(Sigmagnum.POSITIVE_SUP_ONE, pos.sigmagnum());
        assertSame(Sigmagnum.ZERO, ZERO.sigmagnum());
        assertSame(Sigmagnum.NEGATIVE_ONE, NEG_ONE.sigmagnum());
        assertSame(Sigmagnum.NEGATIVE_SUB_MINUS_ONE, neg.sigmagnum());
    }
    
    @Test
    public void unaryTests() {
        long testL = 3498576238975423L;
        FiniteInteger test = FiniteInteger.valueOfStrict(testL);
        assertSame(test, test.magnitude());
        FiniteInteger testNeg = test.negated();
        assertEquals(FiniteInteger.valueOfStrict(-testL), testNeg);
        assertEquals(test, testNeg.magnitude());
    }
    
    @Test
    public void minMaxTests() {
        long a = 123456;
        long b = 555666;
        long c = -45678;
        long d = -12;
        FiniteInteger aFI = FiniteInteger.valueOfStrict(a);
        FiniteInteger bFI = FiniteInteger.valueOfStrict(b);
        FiniteInteger cFI = FiniteInteger.valueOfStrict(c);
        FiniteInteger dFI = FiniteInteger.valueOfStrict(d);
        BigInteger aBI = BigInteger.valueOf(a);
        BigInteger bBI = BigInteger.valueOf(b);
        BigInteger cBI = BigInteger.valueOf(c);
        BigInteger dBI = BigInteger.valueOf(d);
        
        assertSame(aFI, aFI.max(aBI));
        assertSame(dFI, dFI.max(d));
        assertEquals(cFI, cFI.min(cBI));
        assertEquals(bFI, bFI.min(b));
        
        assertEquals(bFI, aFI.max(b));
        assertEquals(bFI, aFI.max(bBI));
        assertEquals(bFI, bFI.max((AlgebraInteger) cFI));
        assertEquals(bFI, bFI.max((Rational) dFI));
        assertEquals(bFI, bFI.max((AlgebraNumber) aFI));
        
        assertEquals(dFI, aFI.min(d));
        assertEquals(dFI, aFI.min(dBI));
        assertEquals(dFI, dFI.min((AlgebraInteger) bFI));
        assertEquals(dFI, aFI.min((Rational) dFI));
        assertEquals(dFI, dFI.min((AlgebraNumber) aFI));
        
        assertThrows(NullPointerException.class, () -> aFI.max((AlgebraInteger) null));
        assertThrows(NullPointerException.class, () -> aFI.max((AlgebraNumber) null));
        assertThrows(NullPointerException.class, () -> aFI.max((Rational) null));
        assertThrows(NullPointerException.class, () -> aFI.max((BigInteger) null));
        assertThrows(NullPointerException.class, () -> aFI.min((AlgebraInteger) null));
        assertThrows(NullPointerException.class, () -> aFI.min((AlgebraNumber) null));
        assertThrows(NullPointerException.class, () -> aFI.min((Rational) null));
        assertThrows(NullPointerException.class, () -> aFI.min((BigInteger) null));
    }
    
    @Test
    public void gcfLcmTests() {
        long a = 2500;
        long b = 1040;
        FiniteInteger aFI = FiniteInteger.valueOfStrict(a);
        FiniteInteger bFI = FiniteInteger.valueOfStrict(b);
        BigInteger aBI = BigInteger.valueOf(a);
        BigInteger bBI = BigInteger.valueOf(b);
        
        assertEquals(FiniteInteger.valueOfStrict(20), aFI.gcf(b));
        assertEquals(FiniteInteger.valueOfStrict(20), bFI.gcf(aBI));
        assertEquals(FiniteInteger.valueOfStrict(20), aFI.gcf((FiniteInteger) bFI));
        assertEquals(FiniteInteger.valueOfStrict(20), bFI.gcf((AlgebraInteger) aFI));
        
        assertEquals(FiniteInteger.valueOfStrict(130_000), aFI.lcm(b));
        assertEquals(FiniteInteger.valueOfStrict(130_000), aFI.lcm(bBI));
        assertEquals(FiniteInteger.valueOfStrict(130_000), bFI.lcm((FiniteInteger) aFI));
        assertEquals(FiniteInteger.valueOfStrict(130_000), aFI.lcm((FiniteInteger) bFI));
        
        assertThrows(ArithmeticException.class, () -> ZERO.gcf(ZERO));
        assertThrows(ArithmeticException.class, () -> bFI.lcm(ZERO));
        assertThrows(NullPointerException.class, () -> ONE.gcf((BigInteger) null));
        assertThrows(NullPointerException.class, () -> ONE.gcf((FiniteInteger) null));
        assertThrows(NullPointerException.class, () -> NEG_ONE.gcf((AlgebraInteger) null));
        assertThrows(NullPointerException.class, () -> NEG_ONE.lcm((BigInteger) null));
        assertThrows(NullPointerException.class, () -> aFI.lcm((FiniteInteger) null));
        assertThrows(NullPointerException.class, () -> bFI.lcm((AlgebraInteger) null));
    }
    
    @Test
    public void canDivideTests() {
        long a = 750;
        long b = 25;
        FiniteInteger aFI = FiniteInteger.valueOfStrict(a);
        FiniteInteger bFI = FiniteInteger.valueOfStrict(b);
        BigInteger aBI = BigInteger.valueOf(a);
        BigInteger bBI = BigInteger.valueOf(b);
        
        assertTrue(aFI.canDivideBy(bFI));
        assertTrue(aFI.canDivideBy(bBI));
        assertTrue(aFI.canDivideBy(b));
        
        assertFalse(bFI.canDivideBy(a));
        assertFalse(bFI.canDivideBy(aBI));
        assertFalse(bFI.canDivideBy(aFI));
        
        assertTrue(aFI.canDivideBy((AlgebraInteger) aFI));
        
        assertThrows(NullPointerException.class, () -> ONE.canDivideBy((BigInteger) null));
        assertThrows(NullPointerException.class, () -> NEG_ONE.canDivideBy((AlgebraInteger) null));
        assertThrows(NullPointerException.class, () -> aFI.canDivideBy((FiniteInteger) null));
        assertThrows(ArithmeticException.class, () -> bFI.canDivideBy(ZERO));
    }
    
    @Test
    public void isPrimeTests() {
        assertFalse(ONE.isPrime());
        assertFalse(FiniteInteger.valueOfStrict(-2).isPrime());
        assertTrue(FiniteInteger.valueOfStrict(2).isPrime());
        assertTrue(FiniteInteger.valueOfStrict(31).isPrime());
    }
    
    @Test
    public void compareToTests() {
        long a = -750;
        long b = 25;
        FiniteInteger aFI = FiniteInteger.valueOfStrict(a);
        FiniteInteger bFI = FiniteInteger.valueOfStrict(b);
        BigInteger aBI = BigInteger.valueOf(a);
        BigInteger bBI = BigInteger.valueOf(b);
        
        assertFalse(aFI.compareTo((FiniteInteger) bFI) >= 0);
        assertTrue(bFI.compareTo((FiniteInteger) aFI.magnitude()) < 0);
        assertThrows(NullPointerException.class, () -> ONE.compareTo((FiniteInteger) null));
        
        assertFalse(aFI.compareTo((AlgebraInteger) bFI) >= 0);
        assertTrue(bFI.compareTo((AlgebraInteger) aFI.magnitude()) < 0);
        assertThrows(NullPointerException.class, () -> ONE.compareTo((AlgebraInteger) null));
        
        assertFalse(aFI.compareTo((Rational) bFI) >= 0);
        assertTrue(bFI.compareTo((Rational) aFI.magnitude()) < 0);
        assertThrows(NullPointerException.class, () -> ONE.compareTo((Rational) null));
        
        assertFalse(aFI.compareTo((AlgebraNumber) bFI) >= 0);
        assertTrue(bFI.compareTo((AlgebraNumber) aFI.magnitude()) < 0);
        assertThrows(NullPointerException.class, () -> ONE.compareTo((AlgebraNumber) null));
    }
    
    @Test
    public void equivTests() {
        long a = -750;
        long b = 25;
        FiniteInteger aFI = FiniteInteger.valueOfStrict(a);
        FiniteInteger bFI = FiniteInteger.valueOfStrict(b);
        BigInteger aBI = BigInteger.valueOf(a);
        BigInteger bBI = BigInteger.valueOf(b);
        
        assertFalse(aFI.equiv((FiniteInteger) bFI));
        assertTrue(bFI.equiv((FiniteInteger) bFI.magnitude()));
        assertThrows(NullPointerException.class, () -> ONE.equiv((FiniteInteger) null));
        
        assertFalse(aFI.equiv((AlgebraInteger) bFI));
        assertTrue(bFI.equiv((AlgebraInteger) bFI.magnitude()));
        assertThrows(NullPointerException.class, () -> ONE.equiv((AlgebraInteger) null));
        
        assertFalse(aFI.equiv((Rational) bFI));
        assertTrue(aFI.negated().equiv((Rational) aFI.magnitude()));
        assertThrows(NullPointerException.class, () -> ONE.equiv((Rational) null));
        
        assertFalse(aFI.equiv((AlgebraNumber) bFI));
        assertTrue(ONE.equiv((AlgebraNumber) NEG_ONE.magnitude()));
        assertThrows(NullPointerException.class, () -> ONE.equiv((AlgebraNumber) null));
    }
    
    @Test
    public void equalHashTests() {
        long a = -750;
        long b = 25;
        FiniteInteger aFI = FiniteInteger.valueOfStrict(a);
        FiniteInteger bFI = FiniteInteger.valueOfStrict(b);
        BigInteger aBI = BigInteger.valueOf(a);
        BigInteger bBI = BigInteger.valueOf(b);
        
        FiniteInteger[] rands = new Random().longs(40)
                .mapToObj(FiniteInteger::valueOfStrict)
                .toArray(FiniteInteger[]::new);
        FiniteInteger last = ZERO;
        for (int i = 0; i < rands.length; i++) {
            FiniteInteger iFI = rands[i];
            assertEquals(iFI.equals(iFI), iFI.hashCode() == iFI.hashCode());
            assertEquals(iFI.equals(last), iFI.hashCode() == last.hashCode());
            last = iFI;
        }
        
        assertFalse(ZERO.equals(null));
    }
    
    @Test
    public void sumDiffTests() {
        long a = 512;
        long b = 415;
        long c = b - a; // -97
        long d = a + b; // 927
        FiniteInteger aFI = FiniteInteger.valueOfStrict(a);
        FiniteInteger bFI = FiniteInteger.valueOfStrict(b);
        FiniteInteger cFI = FiniteInteger.valueOfStrict(c);
        FiniteInteger dFI = FiniteInteger.valueOfStrict(d);
        
        assertEquals(dFI, aFI.sum((FiniteInteger) bFI));
        assertEquals(dFI, aFI.sum((AlgebraInteger) bFI));
        assertEquals(dFI, aFI.sum((Rational) bFI));
        assertEquals(dFI, aFI.sum((AlgebraNumber) bFI));
        
        // C should be cached
        assertSame(cFI, bFI.difference((FiniteInteger) aFI));
        assertSame(cFI, bFI.difference((AlgebraInteger) aFI));
        assertSame(cFI, bFI.difference((Rational) aFI));
        assertSame(cFI, bFI.difference((AlgebraNumber) aFI));
        
        assertThrows(NullPointerException.class, () -> ONE.sum((FiniteInteger) null));
        assertThrows(NullPointerException.class, () -> ONE.sum((AlgebraInteger) null));
        assertThrows(NullPointerException.class, () -> ONE.sum((Rational) null));
        assertThrows(NullPointerException.class, () -> ONE.sum((AlgebraNumber) null));
        assertThrows(NullPointerException.class, () -> ONE.difference((FiniteInteger) null));
        assertThrows(NullPointerException.class, () -> ONE.difference((AlgebraInteger) null));
        assertThrows(NullPointerException.class, () -> ONE.difference((Rational) null));
        assertThrows(NullPointerException.class, () -> ONE.difference((AlgebraNumber) null));
    }
    
    @Test
    public void multQuoTests() {
        long a = 512;
        long b = -16;
        long c = -32;
        FiniteInteger aFI = FiniteInteger.valueOfStrict(a);
        FiniteInteger bFI = FiniteInteger.valueOfStrict(b);
        FiniteInteger cFI = FiniteInteger.valueOfStrict(c);
        
        assertEquals(aFI, bFI.product((FiniteInteger) cFI));
        assertEquals(aFI, bFI.product((AlgebraInteger) cFI));
        assertEquals(aFI, bFI.product((Rational) cFI));
        assertEquals(aFI, bFI.product((AlgebraNumber) cFI));
        
        // Should be cached
        assertSame(cFI, aFI.quotient((FiniteInteger) bFI));
        assertSame(cFI, aFI.quotient((AlgebraInteger) bFI));
        assertSame(cFI, aFI.quotient((Rational) bFI));
        assertSame(cFI, aFI.quotient((AlgebraNumber) bFI));
        
        assertThrows(NullPointerException.class, () -> ONE.product((FiniteInteger) null));
        assertThrows(NullPointerException.class, () -> ONE.product((AlgebraInteger) null));
        assertThrows(NullPointerException.class, () -> ONE.product((Rational) null));
        assertThrows(NullPointerException.class, () -> ONE.product((AlgebraNumber) null));
        
        assertThrows(NullPointerException.class, () -> ONE.quotient((FiniteInteger) null));
        assertThrows(ArithmeticException.class, () -> ONE.quotient(ZERO));
        assertThrows(NullPointerException.class, () -> ONE.quotient((AlgebraInteger) null));
        assertThrows(ArithmeticException.class, () -> ONE.quotient((AlgebraInteger) ZERO));
        assertThrows(NullPointerException.class, () -> ONE.quotient((Rational) null));
        assertThrows(ArithmeticException.class, () -> ONE.quotient((Rational) ZERO));
        assertThrows(NullPointerException.class, () -> ONE.quotient((AlgebraNumber) null));
        assertThrows(ArithmeticException.class, () -> ONE.quotient((AlgebraNumber) ZERO));
    }
    
    @Test
    public void quotientZTests() {
        FiniteInteger two = FiniteInteger.valueOfStrict(2);
        FiniteInteger three = FiniteInteger.valueOfStrict(3);
        FiniteInteger five = FiniteInteger.valueOfStrict(5);
        FiniteInteger eight = FiniteInteger.valueOfStrict(8);
        FiniteInteger ten = FiniteInteger.valueOfStrict(10);
        
        assertSame(three, eight.quotientRoundZ(three, null));
        assertSame(two, eight.quotientRoundZ(three, RoundingMode.DOWN));
        assertSame(three, five.quotientRoundZ(two, RoundingMode.UP));
        
        assertSame(three, eight.quotientRoundZ((AlgebraInteger) three, null));
        assertSame(two, eight.quotientRoundZ((AlgebraInteger) three, RoundingMode.DOWN));
        assertSame(three, five.quotientRoundZ((AlgebraInteger) two, RoundingMode.UP));
        
        assertSame(three, eight.quotientRoundZ((Rational) three, null));
        assertSame(two, eight.quotientRoundZ((Rational) three, RoundingMode.DOWN));
        assertSame(three, five.quotientRoundZ((Rational) two, RoundingMode.UP));
        
        assertSame(three, eight.quotientRoundZ((AlgebraNumber) three, null));
        assertSame(two, eight.quotientRoundZ((AlgebraNumber) three, RoundingMode.DOWN));
        assertSame(three, five.quotientRoundZ((AlgebraNumber) two, RoundingMode.UP));
        
        assertSame(three, ten.quotientZ(three));
        assertSame(three, ten.quotientZ((AlgebraInteger) three));
        assertSame(three, ten.quotientZ((Rational) three));
        assertSame(three, ten.quotientZ((AlgebraNumber) three));
        
        assertThrows(NullPointerException.class, () -> ONE.quotientZ((FiniteInteger) null));
        assertThrows(ArithmeticException.class, () -> ONE.quotientZ(ZERO));
        assertThrows(NullPointerException.class, () -> ONE.quotientZ((AlgebraInteger) null));
        assertThrows(ArithmeticException.class, () -> ONE.quotientZ((AlgebraInteger) ZERO));
        assertThrows(NullPointerException.class, () -> ONE.quotientZ((Rational) null));
        assertThrows(ArithmeticException.class, () -> ONE.quotientZ((Rational) ZERO));
        assertThrows(NullPointerException.class, () -> ONE.quotientZ((AlgebraNumber) null));
        assertThrows(ArithmeticException.class, () -> ONE.quotientZ((AlgebraNumber) ZERO));
        
        assertThrows(NullPointerException.class, ()
                -> ONE.quotientRoundZ((FiniteInteger) null, null));
        assertThrows(ArithmeticException.class, () -> ONE.quotientRoundZ(ZERO, null));
        assertThrows(NullPointerException.class, ()
                -> ONE.quotientRoundZ((AlgebraInteger) null, null));
        assertThrows(ArithmeticException.class, ()
                -> ONE.quotientRoundZ((AlgebraInteger) ZERO, null));
        assertThrows(NullPointerException.class, ()
                -> ONE.quotientRoundZ((Rational) null, null));
        assertThrows(ArithmeticException.class, ()
                -> ONE.quotientRoundZ((Rational) ZERO, null));
        assertThrows(NullPointerException.class, ()
                -> ONE.quotientRoundZ((AlgebraNumber) null, null));
        assertThrows(ArithmeticException.class, ()
                -> ONE.quotientRoundZ((AlgebraNumber) ZERO, null));
    }
    
    @Test
    public final void remainderTests() {
        FiniteInteger three = FiniteInteger.valueOfStrict(3);
        FiniteInteger negThree = FiniteInteger.valueOfStrict(-3);
        FiniteInteger fifteen = FiniteInteger.valueOfStrict(15);
        FiniteInteger negFifteen = FiniteInteger.valueOfStrict(-15);
        FiniteInteger four = FiniteInteger.valueOfStrict(4);
        
        assertSame(negThree, negFifteen.remainder(four));
        assertSame(negThree, negFifteen.remainder((AlgebraInteger) four));
        assertSame(negThree, negFifteen.remainder((Rational) four));
        assertSame(negThree, negFifteen.remainder((AlgebraNumber) four));
        
        assertSame(three, fifteen.modulo(four));
        assertSame(three, fifteen.modulo((AlgebraInteger) four));
        assertSame(three, fifteen.modulo((Rational) four));
        assertSame(three, fifteen.modulo((AlgebraNumber) four));
        
        assertThrows(NullPointerException.class, () -> ONE.remainder((FiniteInteger) null));
        assertThrows(ArithmeticException.class, () -> ONE.remainder(ZERO));
        assertThrows(NullPointerException.class, () -> ONE.remainder((AlgebraInteger) null));
        assertThrows(ArithmeticException.class, () -> ONE.remainder((AlgebraInteger) ZERO));
        assertThrows(NullPointerException.class, () -> ONE.remainder((Rational) null));
        assertThrows(ArithmeticException.class, () -> ONE.remainder((Rational) ZERO));
        assertThrows(NullPointerException.class, () -> ONE.remainder((AlgebraNumber) null));
        assertThrows(ArithmeticException.class, () -> ONE.remainder((AlgebraNumber) ZERO));
        
        assertThrows(NullPointerException.class, () -> ONE.modulo((FiniteInteger) null));
        assertThrows(ArithmeticException.class, () -> ONE.modulo(ZERO));
        assertThrows(ArithmeticException.class, () -> ONE.modulo(NEG_ONE));
        assertThrows(NullPointerException.class, () -> ONE.modulo((AlgebraInteger) null));
        assertThrows(ArithmeticException.class, () -> ONE.modulo((AlgebraInteger) ZERO));
        assertThrows(ArithmeticException.class, () -> ONE.modulo((AlgebraInteger) NEG_ONE));
        assertThrows(NullPointerException.class, () -> ONE.modulo((Rational) null));
        assertThrows(ArithmeticException.class, () -> ONE.modulo((Rational) ZERO));
        assertThrows(ArithmeticException.class, () -> ONE.modulo((Rational) NEG_ONE));
        assertThrows(NullPointerException.class, () -> ONE.modulo((AlgebraNumber) null));
        assertThrows(ArithmeticException.class, () -> ONE.modulo((AlgebraNumber) ZERO));
        assertThrows(ArithmeticException.class, () -> ONE.modulo((AlgebraNumber) NEG_ONE));
    }
    
    @Test
    public void squareTests() {
        FiniteInteger twentyFive = FiniteInteger.valueOfStrict(25);
        FiniteInteger twentySeven = FiniteInteger.valueOfStrict(27);
        FiniteInteger five = FiniteInteger.valueOfStrict(5);
        
        assertSame(five, twentyFive.sqrtRoundZ(null));
        assertSame(five, twentySeven.sqrtRoundZ(null));
        assertSame(FiniteInteger.valueOfStrict(6), twentySeven.sqrtRoundZ(RoundingMode.UP));
        
        assertSame(five, twentyFive.sqRoot());
        NumberRemainderPair<FiniteInteger, FiniteInteger> fiveRTwo = new NumberRemainderPair<>(
                five, FiniteInteger.valueOfStrict(2));
        assertEquals(fiveRTwo, twentySeven.sqrtZWithRemainder());
        
        assertSame(twentyFive, five.negated().squared());
        
        assertThrows(ArithmeticException.class, NEG_ONE::sqRoot);
        assertThrows(ArithmeticException.class, () -> NEG_ONE.sqrtRoundQ(null));
        assertThrows(ArithmeticException.class, () -> NEG_ONE.sqrtRoundZ(null));
        assertThrows(ArithmeticException.class, () -> NEG_ONE.sqrtQWithRemainder(null));
        assertThrows(ArithmeticException.class, NEG_ONE::sqrtZWithRemainder);
    }
    
    @Test
    public void raisedZTests() {
        FiniteInteger negTwo = FiniteInteger.valueOfStrict(-2);
        assertSame(FiniteInteger.valueOfStrict(16), negTwo.raisedZ(4));
        assertSame(FiniteInteger.valueOfStrict(-32), negTwo.raisedZ(FiniteInteger.valueOfStrict(5)));
        
        assertThrows(NullPointerException.class, () -> NEG_ONE.raisedZ(null));
        assertThrows(ArithmeticException.class, () -> negTwo.raisedZ(-3));
        assertThrows(ArithmeticException.class, () -> ONE.raisedZ(negTwo));
    }
    
    @Test
    public void raisingTests() {
        FiniteInteger sixTwentyFive = FiniteInteger.valueOfStrict(625);
        FiniteInteger twentySeven = FiniteInteger.valueOfStrict(27);
        FiniteInteger negFive = FiniteInteger.valueOfStrict(-5);
        FiniteInteger three = FiniteInteger.valueOfStrict(3);
        
        assertSame(twentySeven, three.raised(3));
        assertSame(twentySeven, three.raised(three));
        assertEquals(sixTwentyFive, negFive.raised(4));
        assertEquals(sixTwentyFive, negFive.raised(FiniteInteger.valueOfStrict(4)));
        
        assertSame(ONE, ONE.raised(-15));
        assertEquals(sixTwentyFive, sixTwentyFive.raised(NEG_ONE).inverted());
        
        assertThrows(NullPointerException.class, () -> ONE.raised(null));
        assertThrows(ArithmeticException.class, () -> ZERO.raised(ZERO));
        assertThrows(ArithmeticException.class, () -> ZERO.raised(0));
    }
    
    @Test
    public void rootTests() {
        FiniteInteger sixTwentyFive = FiniteInteger.valueOfStrict(625);
        FiniteInteger negTwentySeven = FiniteInteger.valueOfStrict(-27);
        FiniteInteger negNine = FiniteInteger.valueOfStrict(-9);
        FiniteInteger eight = FiniteInteger.valueOfStrict(8);
        FiniteInteger five = FiniteInteger.valueOfStrict(5);
        FiniteInteger four = FiniteInteger.valueOfStrict(4);
        FiniteInteger three = FiniteInteger.valueOfStrict(3);
        FiniteInteger negThree = FiniteInteger.valueOfStrict(-3);
        
        //assertSame(negThree, negTwentySeven.root(3));
        //assertSame(negThree, negTwentySeven.root(three));
        //assertSame(five, sixTwentyFive.root(4));
        //assertSame(five, sixTwentyFive.root(four));
        
        assertSame(negThree, negNine.rootRoundZ(3, RoundingMode.FLOOR));
        assertSame(negThree, negNine.rootRoundZ(three, RoundingMode.UP));
        
        NumberRemainderPair<FiniteInteger, FiniteInteger> negTwoRNegOne
                = new NumberRemainderPair<>(FiniteInteger.valueOfStrict(-2), NEG_ONE);
        assertEquals(negTwoRNegOne, negNine.rootZWithRemainder(3));
        assertEquals(negTwoRNegOne, negNine.rootZWithRemainder(three));
        
        assertSame(five, sixTwentyFive.rootRoundQ(4, null));
        assertSame(five, sixTwentyFive.rootRoundQ(four, null));
        NumberRemainderPair<FiniteInteger, FiniteInteger> fiveRZero = new NumberRemainderPair<>(five, ZERO);
        //assertEquals(fiveRZero, sixTwentyFive.rootQWithRemainder(4, null));
        //assertEquals(fiveRZero, sixTwentyFive.rootQWithRemainder(four, null));
        
        //assertSame(ONE, ONE.root(-15));
        
        assertThrows(NullPointerException.class, () -> ONE.root(null));
        assertThrows(NullPointerException.class, () -> ONE.rootRoundZ(null, null));
        assertThrows(NullPointerException.class, () -> ONE.rootZWithRemainder(null));
        assertThrows(NullPointerException.class, () -> ONE.rootQWithRemainder(null, null));
        assertThrows(NullPointerException.class, () -> ONE.rootRoundQ(null, null));
        
        assertThrows(ArithmeticException.class, () -> ONE.root(ZERO));
        assertThrows(ArithmeticException.class, () -> ONE.root(0));
        assertThrows(ArithmeticException.class, () -> ONE.rootRoundQ(0, null));
        assertThrows(ArithmeticException.class, () -> ONE.rootRoundZ(0, null));
        assertThrows(ArithmeticException.class, () -> ONE.rootZWithRemainder(0));
        assertThrows(ArithmeticException.class, () -> ONE.rootQWithRemainder(0, null));
        assertThrows(ArithmeticException.class, () -> ONE.rootRoundQ(ZERO, null));
        assertThrows(ArithmeticException.class, () -> ONE.rootRoundZ(ZERO, null));
        assertThrows(ArithmeticException.class, () -> ONE.rootZWithRemainder(ZERO));
        assertThrows(ArithmeticException.class, () -> ONE.rootQWithRemainder(ZERO, null));
        
        assertThrows(ArithmeticException.class, () -> negThree.root(8));
        assertThrows(ArithmeticException.class, () -> negThree.root(eight));
        assertThrows(ArithmeticException.class, () -> negThree.rootRoundQ(8, null));
        assertThrows(ArithmeticException.class, () -> negThree.rootRoundZ(8, null));
        assertThrows(ArithmeticException.class, () -> negThree.rootZWithRemainder(8));
        assertThrows(ArithmeticException.class, () -> negThree.rootQWithRemainder(8, null));
        assertThrows(ArithmeticException.class, () -> negThree.rootRoundQ(eight, null));
        assertThrows(ArithmeticException.class, () -> negThree.rootRoundZ(eight, null));
        assertThrows(ArithmeticException.class, () -> negThree.rootZWithRemainder(eight));
        assertThrows(ArithmeticException.class, () -> negThree.rootQWithRemainder(eight, null));
    }
}
