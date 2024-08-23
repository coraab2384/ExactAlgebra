package org.cb2384.exactalgebra.objects.numbers.rational;

import static org.cb2384.exactalgebra.util.PrimMathUtils.IntegralBoundaryTypes;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.LongFunction;

import org.cb2384.exactalgebra.util.BigMathObjectUtils;
import org.cb2384.exactalgebra.util.PrimMathUtils;
import org.cb2384.exactalgebra.util.corutils.StringUtils;
import org.cb2384.exactalgebra.util.corutils.ternary.Signum;
import org.cb2384.exactalgebra.util.corutils.ternary.Ternary;

import org.checkerframework.checker.index.qual.*;
import org.checkerframework.checker.nullness.qual.*;
import org.checkerframework.checker.signedness.qual.*;
import org.checkerframework.common.value.qual.*;
import org.checkerframework.dataflow.qual.*;

public class FiniteRational
        extends AbstractRational
        implements Serializable {
    @Serial
    private static final long serialVersionUID = 0x83A0B98FB73A1A5L;
    
    private static final String STRICT_EXC_MSG = "Overflow of bounds of "
            + StringUtils.getIdealName(FiniteRational.class) + ". Consider using "
            + StringUtils.getIdealName(ArbitraryRational.class) + " or factory instead";
    
    private static final long ZERO_VAL = 0x1_00_00_00_00L;
    
    private static final long ONE_VAL = ZERO_VAL + 1;
    
    private final long value;
    
    private transient @MonotonicNonNull BigInteger numerator;
    
    private transient @MonotonicNonNull BigInteger denominator;
    
    @SideEffectFree
    FiniteRational(
            @Unsigned int numerator,
            int denominator,
            BigInteger numeratorBI,
            BigInteger denominatorBI
    ) {
        this(numerator, denominator);
        this.numerator = numeratorBI;
        this.denominator = denominatorBI;
    }
    
    @SideEffectFree
    private FiniteRational(
            @Unsigned int numerator,
            int denominator
    ) {
        value = compressToLong(Integer.toUnsignedLong(numerator), denominator);
    }
    
    @SideEffectFree
    private FiniteRational(
            long val
    ) {
        this.value = val;
    }
    
    @SideEffectFree
    public static FiniteRational fromLongsStrict(
            long numerator,
            long denominator
    ) {
        return (FiniteRational) fromLongBuilder(numerator, denominator, true);
    }
    
    @SideEffectFree
    public static Rational fromLongs(
            long numerator,
            long denominator
    ) {
        return fromLongBuilder(numerator, denominator, false);
    }
    
    @SideEffectFree
    private static Rational fromLongBuilder(
            long numerator,
            long denominator,
            boolean strict
    ) {
        if (denominator == 0) {
            throw new ArithmeticException(DIV_0_EXC_MSG);
        }
        
        if (numerator < 0) {
            numerator = -numerator;
            denominator = -denominator;
        }
        
        long gcf = PrimMathUtils.gcf(denominator, numerator);
        numerator /= gcf;
        denominator /= gcf;
        
        if (doesFit(numerator, denominator)) {
            return new FiniteRational( compressToLong(numerator, denominator) );
        }
        
        if (strict) {
            throw new ArithmeticException(STRICT_EXC_MSG);
        }
        return RationalFactory.fromLongs(numerator, denominator);
    }
    
    @SideEffectFree
    public static FiniteRational fromInts(
            @Unsigned int numerator,
            int denominator
    ) {
        if (denominator == 0) {
            throw new ArithmeticException(DIV_0_EXC_MSG);
        }
        
        // We know this gcf will not need to be treated as unsigned,
        //  because one of the inputs (den) is always less than the signed max value.
        int gcf = PrimMathUtils.GCFUnsigned(Math.abs(denominator), numerator);
        numerator = Integer.divideUnsigned(numerator, gcf);
        denominator /= gcf;
        
        return new FiniteRational(numerator, denominator);
    }
    
    @Pure
    private static boolean doesFit(
            long numerator,
            long denominator
    ) {
        return PrimMathUtils.canBeInt(numerator, IntegralBoundaryTypes.UNSIGNED) &&
                PrimMathUtils.canBeInt(denominator, IntegralBoundaryTypes.EXTENDED);
    }
    
    @Pure
    private static long compressToLong(
            @IntRange(from = 0, to = PrimMathUtils.LONG_TO_INT_MASK) long numerator,
            @IntRange(from = Integer.MIN_VALUE, to = PrimMathUtils.NEG_INT_MIN) long denominator
    ) {
        // The two tested cases have identical meaning, as the denominator value of NEG_INT_MIN
        // is represented internally as 0, but it might not have been normalized yet to 0 yet
        return ((denominator == 0) || (denominator == PrimMathUtils.NEG_INT_MIN)) ?
                numerator :
                (denominator << 32) | numerator;
    }
    
    @Pure
    private @NonNegative long numeratorLongUnsigned() {
        return value & PrimMathUtils.LONG_TO_INT_MASK;
    }
    
    @Pure
    public long numeratorPrim() {
        long res = numeratorLongUnsigned();
        return (value < 0) ?
                -res :
                res;
    }
    
    @Pure
    public @Unsigned int numeratorIntUnsigned() {
        return (int) value;
    }
    
    @Pure
    public @Positive long denominatorPrim() {
        return Math.abs(denominatorLongSigned());
    }
    
    @Pure
    private long denominatorLongSigned() {
        long denominator = value >> 32;
        return (denominator == 0) ?
                Integer.MAX_VALUE + 1L :
                denominator;
    }
    
    @Pure
    public int denominatorIntSigned() {
        return (int) (value >>> 32);
    }
    
    @Pure
    public long wholePrim() {
        return numeratorLongUnsigned() / denominatorLongSigned();
    }
    
    @Override
    @Pure
    public BigInteger numeratorBI() {
        BigInteger numerator = this.numerator;
        return (numerator == null)
                ? (this.numerator = BigInteger.valueOf(numeratorPrim()))
                : numerator;
    }
    
    @Override
    @Pure
    public BigInteger denominatorBI() {
        BigInteger denominator = this.denominator;
        return (denominator == null)
                ? (this.denominator = BigInteger.valueOf(denominatorPrim()))
                : denominator;
    }
    
    @Override
    @SideEffectFree
    public BigInteger wholeBI() {
        return BigInteger.valueOf( wholePrim() );
    }
    
    @Override
    @SideEffectFree
    public BigDecimal toBigDecimal(
            MathContext mathContext
    ) {
        return getBigMathObject(
                (long l) -> new BigDecimal(l, mathContext),
                (x, y) -> x.divide(y, mathContext)
        );
    }
    
    @Override
    @SideEffectFree
    public BigInteger toBigInteger(
            RoundingMode roundingMode
    ) {
        return getBigMathObject(
                BigInteger::valueOf,
                (x, y) -> x.divide(y, 0, roundingMode).toBigInteger()
        );
    }
    
    /**
     * Specifically designed for either {@link BigDecimal} or {@link BigInteger}.
     * If this is whole, returns its value according to the first function, otherwise
     * according to the second function.
     *
     * @param   methodIfWhole   the function to use if this is whole
     *
     * @param   opIfElse    a function that takes the {@link BigDecimal} interpretations
     *                      of the numerator and denominator and gets the value to return from those
     *
     * @return  a big math object according to the provided functions
     *
     * @param   <N> the type of big math object, {@link BigInteger} or {@link BigDecimal}
     */
    @SideEffectFree
    private <N extends Number> N getBigMathObject(
            LongFunction<N> methodIfWhole,
            BiFunction<BigDecimal, BigDecimal, N> opIfElse
    ) {
        if (isWhole()) {
            return methodIfWhole.apply(numeratorPrim());
        }
        
        BigDecimal numBD = BigDecimal.valueOf(numeratorLongUnsigned());
        BigDecimal denBD = BigDecimal.valueOf(denominatorLongSigned());
        return opIfElse.apply(numBD, denBD);
    }
    
    @Override
    @Pure
    public double doubleValue() {
        return (double) numeratorLongUnsigned() / denominatorLongSigned();
    }
    
    @Override
    @SideEffectFree
    public String toString(
            int radix
    ) {
        StringBuilder ans = new StringBuilder(isNegative() ? "-" : "");
        ans.append( Integer.toUnsignedString(numeratorIntUnsigned(), radix) );
        
        if (!isWhole()) {
            ans.append("/").append(Long.toString(denominatorPrim(), radix));
        }
        
        return ans.toString();
    }
    
    @Override
    @Pure
    public boolean isZero() {
        return value == ZERO_VAL;
    }
    
    @Override
    @Pure
    public boolean isOne() {
        return value == ONE_VAL;
    }
    
    @Override
    @Pure
    public boolean isNegative() {
        return value < 0;
    }
    
    @Override
    @Pure
    public boolean isWhole() {
        int den = denominatorIntSigned();
        return (den == 1) || (den == -1);
    }
    
    @Override
    @Pure
    public Signum signum() {
        return Signum.valueOf(denominatorLongSigned());
    }
    
    @Pure
    public int compareTo(
            FiniteRational that
    ) {
        long thisNum = numeratorPrim();
        long thisDen = denominatorPrim();
        
        long thatNum = that.numeratorPrim();
        long thatDen = that.denominatorPrim();
        
        thisNum *= thatDen;
        thatNum *= thisDen;
        
        return Long.compare(thisNum, thatNum);
    }
    
    @Override
    @Pure
    public int compareTo(
            Rational that
    ) {
        return (that instanceof FiniteRational thatFR)
                ? compareTo(thatFR)
                : super.compareTo(that);
    }
    
    @Override
    @SideEffectFree
    public FiniteRational negated() {
        long newDen = -denominatorLongSigned();
        long newNum = numeratorLongUnsigned();
        return new FiniteRational( compressToLong(newNum, newDen) );
    }
    
    @Override
    @SideEffectFree
    public Rational inverted() {
        return fromLongBuilder(denominatorLongSigned(), numeratorLongUnsigned(), false);
    }
    
    @SideEffectFree
    private static Rational biOpHandler(
            Rational that,
            Function<FiniteRational, Rational> smallOp,
            Function<Rational, Rational> bigOp
    ) {
        return (that instanceof FiniteRational thatFR)
                ? smallOp.apply(thatFR)
                : bigOp.apply(that);
    }
    
    @SideEffectFree
    public Rational sum(
            FiniteRational augend
    ) {
        return arithFind(augend, false, false);
    }
    
    @Override
    @SideEffectFree
    public Rational sum(
            Rational augend
    ) {
        return biOpHandler(augend, this::sum, super::sum);
    }
    
    @SideEffectFree
    public Rational difference(
            FiniteRational subtrahend
    ) {
        return arithFind(subtrahend, true, false);
    }
    
    @Override
    @SideEffectFree
    public Rational difference(
            Rational subtrahend
    ) {
        return biOpHandler(subtrahend, this::difference, super::difference);
    }
    
    @SideEffectFree
    private Rational arithFind(
            FiniteRational that,
            boolean subtract,
            boolean strict
    ) {
        long thisNum = numeratorPrim();
        long thisDen = denominatorPrim();
        
        long thatNum = that.numeratorPrim();
        long thatDen = that.denominatorPrim();
        long newDen = thisDen * thatDen;
        
        long newThatNum = thatNum * thisDen;
        long newThisNum = thisNum * thatDen;
        long newNum = subtract ?
                newThisNum - newThatNum :
                newThisNum + newThatNum;
        return fromLongBuilder(newNum, newDen, strict);
    }
    
    @SideEffectFree
    public Rational product(
            FiniteRational multiplicand
    ) {
        return multRes(multiplicand, false, false, Ternary.DEFAULT);
    }
    
    @Override
    @SideEffectFree
    public Rational product(
            Rational multiplicand
    ) {
        return biOpHandler(multiplicand, this::product, super::product);
    }
    
    @SideEffectFree
    public Rational quotient(
            FiniteRational divisor
    ) {
        return multRes(divisor, true, false, Ternary.DEFAULT);
    }
    
    @Override
    @SideEffectFree
    public Rational quotient(
            Rational divisor
    ) {
        return biOpHandler(divisor, this::quotient, super::quotient);
    }
    
    @SideEffectFree
    private Rational multRes(
            FiniteRational that,
            boolean divide,
            boolean strict,
            Ternary remainIfT_ZIfF_NormalIfDef
    ) {
        if (divide && that.isZero()) {
            throw new ArithmeticException(DIV_0_EXC_MSG);
        }
        
        long newNum, newDen;
        if (divide) {
            newNum = that.denominatorLongSigned();
            newDen = that.numeratorLongUnsigned();
        } else {
            newNum = that.numeratorLongUnsigned();
            newDen = that.denominatorLongSigned();
        }
        
        newNum *= numeratorLongUnsigned();
        newDen *= denominatorLongSigned();
        
        return switch (remainIfT_ZIfF_NormalIfDef) {
            case DEFAULT -> fromLongBuilder(newNum, newDen, strict);
            case FALSE -> fromLongBuilder(newNum / newDen, 1, strict);
            case TRUE -> {
                Rational tempQuo = fromLongs(newNum / newDen, 1);
                Rational ans = difference(tempQuo);
                
                if (strict) {
                    if (ans instanceof FiniteRational) {
                        yield ans;
                    }
                    //else
                    BigInteger numBI = ans.numeratorBI();
                    BigInteger denBI = ans.denominatorBI();
                    long num = BigMathObjectUtils.canBeLong(numBI, null) ?
                            numBI.longValue() :
                            Long.MAX_VALUE;
                    long den = BigMathObjectUtils.canBeLong(denBI, null) ?
                            denBI.longValue() :
                            Long.MIN_VALUE / -2;
                    yield fromLongBuilder(num, den, true);
                }
                //else
                yield ans;
            }
        };
    }
}
