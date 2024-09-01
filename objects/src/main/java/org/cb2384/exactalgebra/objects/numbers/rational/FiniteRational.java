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

import org.cb2384.exactalgebra.objects.numbers.integral.FiniteInteger;
import org.cb2384.exactalgebra.objects.numbers.integral.IntegerFactory;
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

/**
 * <p>Rationals with a finite length. Specifically, this class supports a maximum numerator magnitude of
 * an unsigned {@code int} ({@code -2 * }{@link Integer#MIN_VALUE} or {@link PrimMathUtils#LONG_TO_INT_MASK}
 * or {@code 0xFF_FF_FF_FF} or {@code 4_294_967_296}) and a maximum denominator magnitude equal to the maximum signed
 * integer magnitude (specifically the negative directions, so {@code -}{@link Integer#MIN_VALUE} or
 * {@link Integer#MAX_VALUE}<code>&nbsp;+ 1</code>. If the numerator or denominator use less space, the other
 * does not pick up the remaining space.</p>
 *
 * <p>Throws: {@link NullPointerException} &ndash; on any {@code null} input unless otherwise noted</p>
 *
 * @author  Corinne Buxton
 */
public final class FiniteRational
        extends AbstractRational
        implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 0x83A0B98FB73A1A5L;
    
    /**
     * Value of 0 in this format
     */
    private static final long ZERO_VAL = 0x1_00_00_00_00L;
    
    /**
     * value of 1 in this format
     */
    private static final long ONE_VAL = ZERO_VAL + 1;
    
    /**
     * The value; the long value is actually two ints; the first 32 bits are the denominator, and the second
     * the numerator
     */
    private final long value;
    
    /**
     * cached BigInteger value, for calculations
     */
    private transient @MonotonicNonNull BigInteger numerator;
    
    /**
     * cached BigInteger value, for calculations
     */
    private transient @MonotonicNonNull BigInteger denominator;
    
    /**
     * Constructor for if the pre-cached values already happen to be known
     *
     * @param numerator     the numerator that the new object will have; will be interpreted as unsigned
     * @param denominator   the denominator that the new object will have; will however carry the sign
     * @param numeratorBI   BigInteger version of numerator for caching
     * @param denominatorBI BigInteger version of denominator for caching
     */
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
    
    /**
     * Constructor for when values are in int form
     *
     * @param numerator     the numerator that the new object will have; will be interpreted as unsigned
     * @param denominator   the denominator that the new object will have; will however carry the sign
     */
    @SideEffectFree
    private FiniteRational(
            @Unsigned int numerator,
            int denominator
    ) {
        value = compressToLong(Integer.toUnsignedLong(numerator), denominator);
    }
    
    /**
     * Constructor for when values are in long form
     *
     * @param numerator     the numerator that the new object will have; must be positive
     * @param denominator   the denominator that the new object will have; will however carry the sign
     */
    @SideEffectFree
    private FiniteRational(
            @IntRange(from = 0, to = PrimMathUtils.LONG_TO_INT_MASK) long numerator,
            @IntRange(from = Integer.MIN_VALUE, to = PrimMathUtils.NEG_INT_MIN) long denominator
    ) {
        this.value = compressToLong(numerator, denominator);
    }
    
    /**
     * Creates a FiniteRational representation of the given {@code numerator} and {@code denominator}; unlike
     * {@link #valueOf(long, long)}, the result will always be a FiniteRational, even if the value is actually
     * whole. However, this means that the numerator must be within the appropriate range or else an exception
     * will be thrown!
     *
     * @param numerator     the numerator the Rational will have
     * @param denominator   the denominator the Rational will have
     *
     * @return  a FiniteRational representing the given {@code numerator} and {@code denominator}
     *
     * @throws ArithmeticException  if the {@code numerator} or {@code denominator} are too large,
     *                              or if {@code denominator == 0}
     */
    @SideEffectFree
    public static FiniteRational valueOfStrict(
            long numerator,
            long denominator
    ) {
        return (FiniteRational) fromLongBuilder(numerator, denominator, true);
    }
    
    /**
     * Creates a {@link Rational}&mdash;either an {@link ArbitraryRational} or a
     * {@link FiniteRational} or possibly an {@link org.cb2384.exactalgebra.objects.numbers.integral.AlgebraInteger
     * AlgebraInteger} type if whole&mdash;depending on the magnitude of the given values.
     * Note that the inputs will be simplified!
     *
     * @param numerator     the numerator the Rational will have
     * @param denominator   the denominator the Rational will have
     *
     * @return  a Rational representing the given {@code numerator} and {@code denominator}
     *
     * @throws ArithmeticException  if {@code denominator == 0}
     */
    @SideEffectFree
    public static Rational valueOf(
            long numerator,
            long denominator
    ) {
        return fromLongBuilder(numerator, denominator, false);
    }
    
    /**
     * simplifies the long inputs, if applicable, and calls the appropriate constructor
     *
     * @param numerator     the numerator
     * @param denominator   the denominator
     * @param strict        whether to use strict mode, which requires a FiniteRational result,
     *                      or use normal mode with automatic shrinking or widening
     *
     * @return  the built Rational
     *
     * @throws ArithmeticException  if the {@code numerator} or {@code denominator} are too large
     *                              when {@code strict == true}, or if {@code denominator == 0}
     */
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
        
        if (strict) {
            if (doesFit(numerator, denominator)) {
                return new FiniteRational(numerator, denominator);
            }
            throw new ArithmeticException("Overflow of bounds of "
                    + StringUtils.getIdealName(FiniteRational.class) + ". Consider using "
                    + StringUtils.getIdealName(ArbitraryRational.class) + " or factory instead");
        }
        
        if (denominator == 1) {
            return IntegerFactory.fromLong(numerator);
        }
        if (denominator == -1) {
            return IntegerFactory.fromLong(-numerator);
        }
        
        if (doesFit(numerator, denominator)) {
            return new FiniteRational(numerator, denominator);
        }
        return ArbitraryRational.fromLongsStrict(numerator, denominator);
    }
    
    /**
     * Creates a {@link Rational}&mdash;either a {@link FiniteRational} or possibly a
     * {@link FiniteInteger} if whole.
     * Note that the inputs will be simplified!
     *
     * @param numerator     the numerator the Rational will have
     * @param denominator   the denominator the Rational will have
     *
     * @return  a Rational representing the given {@code numerator} and {@code denominator}
     *
     * @throws ArithmeticException  if {@code denominator == 0}
     */
    @SideEffectFree
    public static Rational valueOf(
            @Unsigned int numerator,
            int denominator
    ) {
        return fromIntBuilder(numerator, denominator, false);
    }
    
    /**
     * Creates a FiniteRational representation of the given {@code numerator} and {@code denominator}; unlike
     * {@link #valueOf(int, int)}, the result will always be a FiniteRational, even if the value is actually
     * whole.
     *
     * @param numerator     the numerator the Rational will have
     * @param denominator   the denominator the Rational will have
     *
     * @return  a FiniteRational representing the given {@code numerator} and {@code denominator}
     *
     * @throws ArithmeticException  if {@code denominator == 0}
     */
    @SideEffectFree
    public static Rational valueOfStrict(
            @Unsigned int numerator,
            int denominator
    ) {
        return fromIntBuilder(numerator, denominator, true);
    }
    
    /**
     * simplifies the int inputs, if applicable, and calls the appropriate constructor
     *
     * @param numerator     the numerator
     * @param denominator   the denominator
     * @param strict        whether to use strict mode, which requires a FiniteRational result,
     *                      or use normal mode with automatic shrinking or widening
     *
     * @return  the built Rational
     *
     * @throws ArithmeticException  if {@code denominator == 0}
     */
    @SideEffectFree
    private static Rational fromIntBuilder(
            @Unsigned int numerator,
            int denominator,
            boolean strict
    ) {
        if (denominator == 0) {
            throw new ArithmeticException(DIV_0_EXC_MSG);
        }
        
        // We know this gcf will not need to be treated as unsigned,
        //  because one of the inputs (den) is always less than the signed max value.
        int gcf = PrimMathUtils.GCFUnsigned(Math.abs(denominator), numerator);
        numerator = Integer.divideUnsigned(numerator, gcf);
        denominator /= gcf;
        
        if (strict) {
            return new FiniteRational(numerator, denominator);
        }
        
        if (denominator == 1) {
            return FiniteInteger.valueOfStrict(numerator);
        }
        if (denominator == -1) {
            return FiniteInteger.valueOfStrict(-numerator);
        }
        return new FiniteRational(numerator, denominator);
    }
    
    /**
     * checks if the numerator and denominator fit
     *
     * @param numerator     the numerator
     * @param denominator   the denominator
     *
     * @return  true if they would fit, else false
     */
    @Pure
    private static boolean doesFit(
            long numerator,
            long denominator
    ) {
        return PrimMathUtils.canBeInt(numerator, IntegralBoundaryTypes.UNSIGNED) &&
                PrimMathUtils.canBeInt(denominator, IntegralBoundaryTypes.EXTENDED);
    }
    
    /**
     * Takes the two inputs and writes them in the representation used here; first 32 bits denominator,
     * last 32 bits numerator.
     *
     * @param numerator     the numerator
     * @param denominator   the denominator
     *
     * @return   the compressed long value
     */
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
    
    /**
     * The numerator, but unsigned
     *
     * @return  the numerator, but unsigned
     */
    @Pure
    private @NonNegative long numeratorLongUnsigned() {
        return value & PrimMathUtils.LONG_TO_INT_MASK;
    }
    
    /**
     * The numerator, but as a primitive value (a {@code long})
     *
     * @return  a {@code long} representation of the numerator of this Rational
     */
    @Pure
    public long numeratorPrim() {
        return (value < 0)
                ? -numeratorLongUnsigned()
                : numeratorLongUnsigned();
    }
    
    /**
     * The denominator, but unsigned
     *
     * @return  the denominator, but unsigned
     */
    @Pure
    public @Unsigned int numeratorIntUnsigned() {
        return (int) value;
    }
    
    /**
     * The denominator, but as a primitive value (a {@code long})
     *
     * @return  a {@code long} representation of the denominator of this Rational
     */
    @Pure
    public @Positive long denominatorPrim() {
        return Math.abs(denominatorLongSigned());
    }
    
    /**
     * The denominator as a long
     *
     * @return  the denominator with the sign, as a long
     */
    @Pure
    private long denominatorLongSigned() {
        long denominator = value >> 32;
        return (denominator == 0) ?
                Integer.MAX_VALUE + 1L :
                denominator;
    }
    
    /**
     * The denominator, but as a signed {@code int}. Note that if the denominator is the largest possible
     * positive denominator, it is returned as {@code 0}!
     *
     * @return  the denominator in {@code int} form, unless it happens to be 2147483648, in which case it
     *          overflows to {@code 0}
     */
    @Pure
    public int denominatorIntSigned() {
        return (int) (value >>> 32);
    }
    
    /**
     * The whole number value of this, in primitive ({@code long}) form. Like {@link #wholeBI()}
     * or {@link #wholeAI()}, the value is rounded towards 0, or truncated.
     *
     * @return  the whole number; the value that would be in front of the mixed number representation
     */
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
     * @param methodIfWhole the function to use if this is whole
     * @param opIfElse      a function that takes the {@link BigDecimal} interpretations
     *                      of the numerator and denominator and gets the value to return from those
     *
     * @return  a big math object according to the provided functions
     *
     * @param <N>   the type of big math object, {@link BigInteger} or {@link BigDecimal}
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
        return new FiniteRational(newNum, newDen);
    }
    
    @Override
    @SideEffectFree
    public Rational inverted() {
        long numerator = numeratorPrim();
        if (numerator == 0) {
            throw new ArithmeticException(DIV_0_EXC_MSG);
        }
        long denominator = denominatorPrim();
        
        return doesFit(denominator, numerator)
                ? new FiniteRational(denominator, numerator)
                : new ArbitraryRational(denominatorBI(), numeratorBI());
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
        return (augend instanceof FiniteRational augendFR)
                ? sum(augendFR)
                : super.sum(augend);
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
        return (subtrahend instanceof FiniteRational subtrahendFR)
                ? difference(subtrahendFR)
                : super.difference(subtrahend);
    }
    
    /**
     * Either adds or subtracts this and that, depending on the subtract argument.
     *
     * @param that      the thing to add or subtract from this
     * @param subtract  whether to subtract or add
     * @param strict    whether strict mode is on, meaning that the answer should only be a FiniteRational
     *
     * @return  the sum/difference
     *
     * @throws ArithmeticException  if the sum/difference ends up not fitting in a FiniteRational
     */
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
        return (multiplicand instanceof FiniteRational multiplicandFR)
                ? product(multiplicandFR)
                : super.product(multiplicand);
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
        return (divisor instanceof FiniteRational divisorFR)
                ? quotient(divisorFR)
                : super.quotient(divisor);
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
                Rational tempQuo = valueOf(newNum / newDen, 1);
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
