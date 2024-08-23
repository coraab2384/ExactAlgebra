package org.cb2384.exactalgebra.objects.numbers;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Objects;

import com.numericalmethod.suanshu.Constant;
import com.numericalmethod.suanshu.number.big.BigDecimalUtils;
import org.cb2384.exactalgebra.objects.internalaccess.FiniteIntegerFabricator;
import org.cb2384.exactalgebra.objects.numbers.integral.AbstractAlgebraInteger;
import org.cb2384.exactalgebra.objects.numbers.integral.AlgebraInteger;
import org.cb2384.exactalgebra.objects.numbers.integral.FiniteInteger;
import org.cb2384.exactalgebra.objects.numbers.integral.IntegerFactory;
import org.cb2384.exactalgebra.objects.numbers.rational.Rational;
import org.cb2384.exactalgebra.objects.numbers.rational.RationalFactory;
import org.cb2384.exactalgebra.objects.pair.NumberRemainderPair;
import org.cb2384.exactalgebra.util.BigMathObjectUtils;
import org.cb2384.exactalgebra.util.corutils.functional.ObjectThenIntToObjectFunction;
import org.cb2384.exactalgebra.util.corutils.ternary.Signum;

import org.checkerframework.checker.nullness.qual.*;
import org.checkerframework.common.value.qual.*;
import org.checkerframework.dataflow.qual.*;

public abstract class AbstractAlgebraNumber
        implements AlgebraNumber {
    
    /**
     * {@link MathContext#MathContext(int, RoundingMode) new MathContext(}{@link #DEFAULT_PRECISION}{@code
     * , }{@link #DEFAULT_ROUNDING}{@link MathContext#MathContext(int, RoundingMode) )}
     */
    protected static final MathContext DEFAULT_CONTEXT =
            new MathContext(DEFAULT_PRECISION, DEFAULT_ROUNDING);
    
    /**
     * {@link MathContext#MathContext(int, RoundingMode) new MathContext(}{@link #DEFAULT_PRECISION}{@code
     * , }{@link RoundingMode#DOWN}{@link MathContext#MathContext(int, RoundingMode) )}
     */
    protected static final MathContext MAX_PREC_CONTEXT =
            new MathContext(Constant.MACH_SCALE, DEFAULT_ROUNDING);
    
    /**
     * The default first op's precision, which is higher than the final {@link #DEFAULT_PRECISION}
     * by {@link #PRECISION_TO_ADD_FOR_FIRST_OP}
     */
    protected static final int DEFAULT_FIRST_OP_PREC = Long.SIZE;
    
    /**
     * The difference between the first op's precision and the final desired precision
     */
    protected static final int PRECISION_TO_ADD_FOR_FIRST_OP
            = Math.min(Long.SIZE - DEFAULT_PRECISION, 2);
    
    protected static final String DIV_0_EXC_MSG = "Cannot have denominator of 0";
    
    /**
     * Cache for common values (from {@link #CACHE_DEPTH} to {@code -}{@link #CACHE_DEPTH}, integers)
     */
    protected static final class Cache {
        /**
         * actual cache; non-final to prevent class-loading deadlock, but is treated similarly to a lazily-built
         * singleton.
         */
        private static FiniteInteger[][] CACHE;
        
        /**
         * This should never be called
         *
         * @throws  IllegalAccessException    always
         */
        private Cache() throws IllegalAccessException {
            throw new IllegalAccessException("This should never be called‽");
        }
        
        /**
         * Builds the cache; to be run only when the current cache is null
         */
        private static void buildCache() {
            
            CACHE = new FiniteInteger[][]{
                    new FiniteInteger[CACHE_DEPTH],
                    new FiniteInteger[1],
                    new FiniteInteger[CACHE_DEPTH]
            };
            
            CACHE[1][0] = new FiniteIntegerFabricator(0);
            for (short i = -1; i <= 1; i++) {
                short iOld = i++;
                for (short j = CACHE_DEPTH; j > 0;) {
                    CACHE[i][j] = new FiniteIntegerFabricator(iOld * j--);
                }
            }
        }
        
        /**
         * Retrieves a value from the cache
         *
         * @param   value   the actual value of the integer-type to retrieve
         *
         * @return  the corresponding {@link AlgebraInteger}
         */
        public static FiniteInteger get(
                @IntRange(from = -CACHE_DEPTH, to = CACHE_DEPTH) int value
        ) {
            if (CACHE == null) {
                buildCache();
            }
            
            int firstIndex = Integer.signum(value) + 1;
            int secondIndex = switch (firstIndex) {
                case 0 -> -value - 1;
                case 2 -> value - 1;
                default -> 0;
            };
            return CACHE[firstIndex][secondIndex];
        }
    }
    
    @Override
    public AlgebraInteger roundZ(
            RoundingMode roundingMode
    ) {
        return IntegerFactory.fromBigInteger(toBigInteger(roundingMode));
    }
    
    /**
     * {@inheritDoc}
     *
     * @implNote    The default implementation uses the default context (which is itself composed from
     *              {@link MathContext#MathContext(int, RoundingMode)} using {@link #DEFAULT_PRECISION} and
     *              {@link #DEFAULT_ROUNDING}), calling {@link #roundQ(MathContext) roundQ(}{@link
     *              AbstractAlgebraInteger#DEFAULT_CONTEXT}{@link #roundQ(MathContext) )}.
     */
    @Override
    @SideEffectFree
    public Rational roundQ() {
        return roundQ(DEFAULT_CONTEXT);
    }
    
    /**
     * {@inheritDoc}
     *
     * @implNote    This skeletal implementation uses the two arguments to create a {@link MathContext},
     *              which is then passed to {@link #roundQ(MathContext)}.
     *
     * @see MathContext#MathContext(int, RoundingMode)
     */
    @Override
    @SideEffectFree
    public Rational roundQ(
            @Nullable Integer precision,
            @Nullable RoundingMode roundingMode
    ) {
        return roundQ( getContextFrom(precision, roundingMode) );
    }
    
    /**
     * For {@link #toBigDecimal(MathContext)} and {@link #roundQ(MathContext)}.
     *
     * @param   precision   the precision
     *
     * @param   roundingMode    the rounding mode
     *
     * @return  the new math context
     */
    @SideEffectFree
    protected static MathContext getContextFrom(
            @Nullable Integer precision,
            @Nullable RoundingMode roundingMode
    ) {
        return new MathContext(
                (precision != null) ? Math.max(precision, MAX_PRECISION) : DEFAULT_PRECISION,
                Objects.requireNonNullElse(roundingMode, DEFAULT_ROUNDING)
        );
    }
    
    /**
     * {@inheritDoc}
     *
     * @implNote    The default implementation uses the default context (which is itself composed from
     *              {@link MathContext#MathContext(int, RoundingMode)} using {@link #DEFAULT_PRECISION} and
     *              {@link #DEFAULT_ROUNDING}), calling {@link #roundQ(MathContext) roundQ(}{@link
     *              AbstractAlgebraNumber#DEFAULT_CONTEXT}{@link #roundQ(MathContext) )}.
     */
    @SideEffectFree
    public BigDecimal toBigDecimal() {
        return toBigDecimal(DEFAULT_CONTEXT);
    }
    
    /**
     * {@inheritDoc}
     *
     * @implNote    This skeletal implementation uses the two arguments to create a {@link MathContext},
     *              which is then passed to {@link #toBigDecimal(MathContext)}.
     *
     * @see MathContext#MathContext(int, RoundingMode)
     */
    @Override
    @SideEffectFree
    public BigDecimal toBigDecimal(
            @Nullable Integer precision,
            @Nullable RoundingMode roundingMode
    ) {
        return toBigDecimal( getContextFrom(precision, roundingMode) );
    }
    
    /**
     * Returns a string representation of this {@link AlgebraNumber}.
     * Equivalent to {@link AlgebraNumber#toString(int) toString(10)}
     * @return a base-10 string of this
     */
    @Override
    @SideEffectFree
    public String toString() {
        return toString(10);
    }
    
    /**
     * {@inheritDoc}
     *
     * @implNote    This skeletal implementation simply casts {@link #doubleValue()} to a
     *              {@code float}.
     */
    @Override
    @Pure
    public float floatValue() {
        return (float) doubleValue();
    }
    
    /**
     * {@inheritDoc}
     *
     * @implNote    This skeletal implementation simply calls {@link #sigmagnum()} and maps that to the
     *              corresponding {@link Signum} value.
     */
    @Override
    @Pure
    public Signum signum() {
        return sigmagnum().signum();
    }
    
    @Override
    @Pure
    public boolean equals(
            @Nullable Object obj
    ) {
        return switch (obj) {
            case Rational oRat -> false;
            case AlgebraNumber oAN -> (hashCode() == oAN.hashCode()) && equiv(oAN);
            case null, default -> false;
        };
    }
    
    /**
     * {@inheritDoc}
     *
     * @implNote    This skeletal implementation uses {@link #quotientZ} and subtraction to find the remainder,
     *              and so {@link #quotientZ} cannot call this method unless overridden.
     */
    @Override
    @SideEffectFree
    public NumberRemainderPair<? extends AlgebraInteger, ? extends AlgebraNumber> quotientZWithRemainder(
            AlgebraNumber divisor
    ) {
        AlgebraInteger quotient = quotientZ(divisor);
        return new NumberRemainderPair<>(this, quotient, quotient.product(divisor));
    }
    
    @Override
    @SideEffectFree
    public AlgebraInteger quotientZ(
            AlgebraNumber divisor
    ) {
        return quotient(divisor).roundZ(RoundingMode.DOWN);
    }
    
    @Override
    @SideEffectFree
    public AlgebraInteger quotientRoundZ(
            AlgebraNumber divisor,
            @Nullable RoundingMode roundingMode
    ) {
        return quotient(divisor).roundZ( Objects.requireNonNullElse(roundingMode, DEFAULT_ROUNDING) );
    }
    
    /**
     * {@inheritDoc}
     *
     * @implNote    This skeletal implementation takes the remainder from {@link #quotientZWithRemainder}, and so
     *              that method cannot itself take the remainder found here.
     */
    @Override
    @SideEffectFree
    public AlgebraNumber remainder(
            AlgebraNumber divisor
    ) {
        return quotientZWithRemainder(divisor).remainder();
    }
    
    /**
     * {@inheritDoc}
     *
     * @implNote    This skeletal implementation obtains the modulo result with help from {@link #remainder}, and so
     *              that method cannot itself call this one.
     */
    @Override
    @SideEffectFree
    public AlgebraNumber modulo(
            AlgebraNumber modulus
    ) {
        return getModulo(modulus);
    }
    
    /**
     * Find the modulo result, by means of the remainder.
     *
     * @param   modulus the modulus; one can also think of this as being similar to a divisor, except that it may
     *                  not be negative.
     *
     * @return  {@code this % modulus}, so long as {@code modulus > 0}
     *
     * @throws  ArithmeticException if {@code modulus <= 0}
     *
     * @param   <N> the currently-used type of this, {@code modulus}, and the result
     */
    @SideEffectFree
    protected final <N extends AlgebraNumber> N getModulo(
            N modulus
    ) {
        if (isNegative() || modulus.isNegative()) {
            throw new ArithmeticException("Cannot do modulo with negative numbers!");
        }
        
        return (N) remainder(modulus);
    }
    
    /**
     * {@inheritDoc}
     *
     * @implNote    This skeletal implementation actually just multiplies this by itself
     *              {@code |exponent|} (absolute value of exponent) times, and then inverts the value
     *              if {@code exponent} was negative. There is almost always a more efficient way to
     *              do this!
     */
    @Override
    @SideEffectFree
    public AlgebraNumber raised(
            int exponent
    ) {
        ObjectThenIntToObjectFunction<AlgebraNumber, AlgebraNumber> raiser = (algebraNumber, integer) -> {
            // Start at 2 since the switch in intRaiser only would call this method
            // if exponent > 2
            AlgebraNumber ans = algebraNumber.squared();
            for (int i = 2; i < integer; i++) {
                ans = ans.product(algebraNumber);
            }
            return ans;
        };
        
        return intRaiser(exponent, raiser);
    }
    
    /**
     * Executes and finishes this to the power of {@code exponent}. If {@code exponent} is negative, it
     * uses the absolute value and then inverts the result as a finishing operation; otherwise, the positive value
     * is given to {@code raiser}, whose result is then directly returned.
     *
     * @param   exponent    the exponent to raise this to the power of
     *
     * @param   raiser  the actual way for this to be raised to the positive {@code int} power, which must be
     *                  provided
     *
     * @return  this raised to the power of {@code exponent}
     *
     * @throws  ArithmeticException if {@code exponent <= 0} while this is 0
     *
     * @throws  ClassCastException  if {@link N} is not closed under multiplication
     *                              ({@link AlgebraNumber#product}) or inversion
     *                              ({@link AlgebraNumber#inverted}) or is not a
     *                              superclass of {@link FiniteInteger}
     *
     * @param   <N> the currently-used class of this, and the class of the result
     */
    @SideEffectFree
    protected final <N extends AlgebraNumber> N intRaiser(
            int exponent,
            ObjectThenIntToObjectFunction<N, N> raiser
    ) {
        checkIndex(Integer.signum(exponent), true, true);
        return intRaiser((N) this, exponent, raiser);
    }
    
    /**
     * Executes and finishes {@code base} to the power of {@code exponent}.
     *
     * @param   base    the base to raise
     *
     * @param   exponent    the exponent to raise {@code base} to the power of
     *
     * @param   raiser  the actual way for {@code base} to be raised to the positive {@code int} power,
     *                  which must be provided
     *
     * @return  {@code base ^ exponent}
     *
     * @throws  ArithmeticException if {@code exponent <= 0} while this is 0
     *
     * @throws  ClassCastException  if {@link N} is not closed under multiplication
     *                              ({@link AlgebraNumber#product}) or inversion
     *                              ({@link AlgebraNumber#inverted}) or is not a
     *                              superclass of {@link FiniteInteger}
     *
     * @param   <N> and the class of {@code base} and the result
     */
    @SideEffectFree
    private static <N extends AlgebraNumber> N intRaiser(
            N base,
            int exponent,
            ObjectThenIntToObjectFunction<N, N> raiser
    ) {
        boolean isMinVal = (exponent == Integer.MIN_VALUE);
        if (isMinVal) {
            exponent++;
        }
        int exponentAbs = Math.abs(exponent);
        N res = switch(exponentAbs) {
            case 0 -> (N) Cache.get(1);
            case 1 -> base;
            case 2 -> (N) base.squared();
            default -> raiser.apply(base, exponentAbs);
        };
        if (isMinVal) {
            res = (N) res.product(base);
        }
        return (exponent < 0) ? (N) res.inverted() : res;
    }
    
    @Override
    @SideEffectFree
    public AlgebraNumber raised(
            AlgebraInteger exponent
    ) {
        return longRaiser(exponent.toBigInteger(), AlgebraNumber::raised, true);
    }
    
    /**
     * Executes and finishes this to the power of {@code exponent}. If {@code exponent} is negative, it
     * uses the absolute value and then inverts the result as a finishing operation; otherwise, the positive value
     * is given to {@code raiser}, whose result is then directly returned. If the value of {@code exponent} is
     * too large to be an {@code int}, then it is broken up into {@code int}-sized parts.
     *
     * @param   exponent    the exponent to raise this to the power of
     *
     * @param   raiser  the actual way for this to be raised to the positive {@code int} power, which must be
     *                  provided
     *
     * @param   canBeNegative   for checking the arguments, a flag to show that negative exponents should not
     *                          be allowed (such as in the case of {@link AlgebraInteger#raisedZ})
     *
     * @return  this raised to the power of {@code exponent}
     *
     * @throws  ArithmeticException if {@code exponent <= 0} while this is 0, or if
     *                              {@code exponent < 0} while {@code canBeNegative == false}
     *
     * @throws  ClassCastException  if {@link N} is not closed under multiplication
     *                              ({@link AlgebraNumber#product}) or inversion
     *                              ({@link AlgebraNumber#inverted}) or is not a
     *                              superclass of {@link FiniteInteger}
     *
     * @param   <N> the currently-used class of this, and the class of the result
     */
    @SideEffectFree
    protected final <N extends AlgebraNumber> N longRaiser(
            BigInteger exponent,
            ObjectThenIntToObjectFunction<N, N> raiser,
            boolean canBeNegative
    ) {
        checkIndex(exponent.signum(), canBeNegative, true);
        N thisN = (N) this;
        
        if (BigMathObjectUtils.canBeInt(exponent, null)) {
            return intRaiser(thisN, exponent.intValue(), raiser);
        }
        
        N result = recurRaiser(thisN, exponent.abs(), raiser);
        return BigMathObjectUtils.isNegative(exponent)
                ? (N) result.inverted()
                : result;
    }
    
    /**
     * Raises {@code base} to the power of {@code exponent}. If the value of {@code exponent} is
     * too large to be an {@code int}, then it is broken up into {@code int}-sized parts which are
     * raised recursively.
     *
     * @param   base    the base to raise
     *
     * @param   exponent    the positive exponent to raise this to the power of
     *
     * @param   raiser  the actual way for this to be raised to the positive {@code int} power, which must be
     *                  provided
     *
     * @return  this raised to the power of {@code exponent}
     *
     * @throws  ClassCastException  if {@link N} is not closed under multiplication
     *                              ({@link AlgebraNumber#product})
     *
     * @param   <N> the class of {@code base} and the result
     */
    @SideEffectFree
    private static <N extends AlgebraNumber> N recurRaiser(
            N base,
            BigInteger exponent,
            ObjectThenIntToObjectFunction<N, N> raiser
    ) {
        BigInteger[] expQuoRemain = exponent.divideAndRemainder(BigMathObjectUtils.INTEGER_MAX_BI);
        N res = raiser.apply(base, expQuoRemain[1].intValue());
        
        BigInteger expQuo = expQuoRemain[0];
        N baseToMaxInt = raiser.apply(base, Integer.MAX_VALUE);
        
        if (BigMathObjectUtils.canBeInt(expQuo, null)) {
            N baseToQuo = raiser.apply(baseToMaxInt, expQuo.intValue());
            return (N) res.product(baseToQuo);
        }
        
        return (N) res.product( recurRaiser(baseToMaxInt, expQuo, raiser) );
    }
    
    /**
     * Checks that the index or exponent will not result in an exception, or throws said exception.
     *
     * @param   exponentSignum  the signum of the exponent or index to check; must be in {@code [-1, 1]} ({@code 1},
     *                          {@code 0}, or {@code -1})
     *
     * @param   canBeNegative   indicates whether negatives should throw an exception
     *
     * @param   isExponent  for the exception message, indicates whether to call it an exponent or index
     *
     * @throws  ArithmeticException if {@code exponent <= 0} while this is 0, or if
     *                              {@code exponent < 0} while {@code canBeNegative == false}
     */
    @Pure
    protected final void checkIndex(
            @IntRange(from = -1, to = 1) int exponentSignum,
            boolean canBeNegative,
            boolean isExponent
    ) {
        assert Integer.signum(exponentSignum) == exponentSignum;
        if (isZero()) {
            switch (exponentSignum) {
                case -1 -> throw new ArithmeticException("0 to negative exponent");
                case 0 -> throw new ArithmeticException("0^0 case");
            }
        }
        
        if ((exponentSignum == -1) && !canBeNegative) {
            throw new IllegalArgumentException(
                    "Disallowed negative " + (isExponent ? "exponent" : "index"));
        }
    }
    
    @Override
    @SideEffectFree
    public AlgebraNumber root(
            int index
    ) {
        return rootRoundQ(index, MAX_PREC_CONTEXT);
    }
    
    @Override
    @SideEffectFree
    public AlgebraNumber root(
            AlgebraInteger index
    ) {
        return rootRoundQ(index, MAX_PREC_CONTEXT);
    }
    
    @Override
    @SideEffectFree
    public NumberRemainderPair<? extends AlgebraInteger, ? extends AlgebraNumber> rootZWithRemainder(
            int index
    ) {
        AlgebraInteger floor = rootRoundZ(index, RoundingMode.DOWN);
        return new NumberRemainderPair<>(this, floor, floor.raised(index));
    }
    
    @Override
    @SideEffectFree
    public NumberRemainderPair<? extends AlgebraInteger, ? extends AlgebraNumber> rootZWithRemainder(
            AlgebraInteger index
    ) {
        AlgebraInteger floor = rootRoundZ(index, RoundingMode.DOWN);
        return new NumberRemainderPair<>(this, floor, floor.raised(index));
    }
    
    @Override
    @SideEffectFree
    public NumberRemainderPair<? extends Rational, ? extends AlgebraNumber> rootQWithRemainder(
            int index,
            @Nullable Integer precision
    ) {
        Rational floor = rootRoundQ(index, getContextFrom(precision, RoundingMode.DOWN));
        return new NumberRemainderPair<>(this, floor, floor.raised(index));
    }
    
    @Override
    @SideEffectFree
    public NumberRemainderPair<? extends Rational, ? extends AlgebraNumber> rootQWithRemainder(
            AlgebraInteger index,
            @Nullable Integer precision
    ) {
        Rational floor = rootRoundQ(index, getContextFrom(precision, RoundingMode.DOWN));
        return new NumberRemainderPair<>(this, floor, floor.raised(index));
    }
    
    @Override
    @SideEffectFree
    public AlgebraInteger rootRoundZ(
            int index,
            @Nullable RoundingMode roundingMode
    ) {
        BigDecimal resBD = rootRound(index, getInitContextZ(roundingMode), null);
        return IntegerFactory.fromBigInteger(resBD.toBigInteger());
    }
    
    @Override
    @SideEffectFree
    public AlgebraInteger rootRoundZ(
            AlgebraInteger index,
            @Nullable RoundingMode roundingMode
    ) {
        BigDecimal resBD = rootRound(index, getInitContextZ(roundingMode), null);
        return IntegerFactory.fromBigInteger(resBD.toBigInteger());
    }
    
    @Override
    @SideEffectFree
    public Rational rootRoundQ(
            int index,
            @Nullable MathContext mathContext
    ) {
        MathContext ratContext = getRatContext(mathContext);
        return RationalFactory.fromBigDecimal(rootRound(
                index,
                getInitContextQ(ratContext),
                ratContext
        ));
    }
    
    @Override
    @SideEffectFree
    public Rational rootRoundQ(
            AlgebraInteger index,
            @Nullable MathContext mathContext
    ) {
        MathContext ratContext = getRatContext(mathContext);
        return RationalFactory.fromBigDecimal(rootRound(
                index,
                getInitContextQ(ratContext),
                ratContext
        ));
    }
    
    /**
     * Takes the {@code index}<sup>th</sup> root of this, as a {@link BigDecimal}.
     * Both contexts should have the same {@link RoundingMode}, but the precision of {@code initContext}
     * should be larger, to keep precision-based errors minimal for the value with the final context.
     *
     * @param   index   the index of the root
     *
     * @param   initContext the initialization context to use for turning the receiver and arguments
     *                      into {@link BigDecimal}s; this also dictates the {@link RoundingMode}
     *
     * @param   rationalContext the context to provide the value in; if it is {@code null}, then the value will
     *                          be provided as a {@link BigDecimal} that has a scale of {@code 0}, and is therefore
     *                          ready to be turned into a {@link BigInteger} without loss of precision
     *
     * @return  the {@code index}<sup>th</sup> root of this number, in line with the given contexts
     */
    @SideEffectFree
    protected final BigDecimal rootRound(
            int index,
            MathContext initContext,
            @Nullable MathContext rationalContext
    ) {
        int firstPrec = initContext.getPrecision();
        BigDecimal resBD = BigDecimalUtils.pow(
                toBigDecimal(initContext),
                rootIndexToPower(BigInteger.valueOf(index), initContext),
                firstPrec
        );
        assert (rationalContext == null) || (rationalContext.getPrecision() <= firstPrec);
        return finalProcessForRoundedRealOp(resBD,
                initContext, rationalContext);
    }
    
    /**
     * Since the root operation is actually a power, need to turn the root index into a BigDecimal
     *
     * @param index the index to transform
     *
     * @param   initContext the initialization context for the resulting BigDecimal
     *
     * @return  {@code 1 / index}, as a BigDecimal with the given context
     */
    @SideEffectFree
    private static BigDecimal rootIndexToPower(
            BigInteger index,
            MathContext initContext
    ) {
        return BigDecimal.ONE.divide(new BigDecimal(index), initContext);
    }
    
    /**
     * Takes the {@code index}<sup>th</sup> root of this, as a {@link BigDecimal}.
     * Both contexts should have the same {@link RoundingMode}, but the precision of {@code initContext}
     * should be larger, to keep precision-based errors minimal for the value with the final context.
     *
     * @param   index   the index of the root
     *
     * @param   initContext the initialization context to use for turning the receiver and arguments
     *                      into {@link BigDecimal}s; this also dictates the {@link RoundingMode}
     *
     * @param   rationalContext the context to provide the value in; if it is {@code null}, then the value will
     *                          be provided as a {@link BigDecimal} that has a scale of {@code 0}, and is therefore
     *                          ready to be turned into a {@link BigInteger} without loss of precision
     *
     * @return  the {@code index}<sup>th</sup> root of this number, in line with the given contexts
     */
    @SideEffectFree
    protected final BigDecimal rootRound(
            AlgebraInteger index,
            MathContext initContext,
            @Nullable MathContext rationalContext
    ) {
        int firstPrec = initContext.getPrecision();
        BigDecimal resBD = BigDecimalUtils.pow(
                toBigDecimal(initContext),
                rootIndexToPower(index.toBigInteger(), initContext),
                firstPrec
        );
        assert (rationalContext == null) || (rationalContext.getPrecision() <= firstPrec);
        return finalProcessForRoundedRealOp(resBD,
                initContext, rationalContext);
    }
    
    @Override
    @SideEffectFree
    public AlgebraNumber exp() {
        return expRoundQ(MAX_PREC_CONTEXT);
    }
    
    @Override
    @SideEffectFree
    public AlgebraNumber power(
            AlgebraNumber power
    ) {
        return powerRoundQ(power, MAX_PREC_CONTEXT);
    }
    
    @Override
    @SideEffectFree
    public AlgebraNumber logNatural() {
        return lnRoundQ(MAX_PREC_CONTEXT);
    }
    
    @Override
    @SideEffectFree
    public AlgebraNumber logBase(
            AlgebraNumber base
    ) {
        return powerRoundQ(base, MAX_PREC_CONTEXT);
    }
    
    @Override
    @SideEffectFree
    public NumberRemainderPair<? extends AlgebraInteger, ? extends AlgebraNumber> expZWithRemainder() {
        AlgebraInteger floor = expRoundZ(RoundingMode.DOWN);
        return new NumberRemainderPair<>(this, floor, floor);
    }
    
    @Override
    @SideEffectFree
    public AlgebraInteger expRoundZ(
            @Nullable RoundingMode roundingMode
    ) {
        BigDecimal resBD = expRound(getInitContextZ(roundingMode), null);
        return IntegerFactory.fromBigInteger(resBD.toBigInteger());
    }
    
    @Override
    @SideEffectFree
    public NumberRemainderPair<? extends AlgebraInteger, ? extends AlgebraNumber> powerZWithRemainder(
            AlgebraNumber power
    ) {
        AlgebraInteger floor = powerRoundZ(power, RoundingMode.DOWN);
        return power.isZero()
                ? new NumberRemainderPair<>(floor, Cache.get(0))
                : new NumberRemainderPair<>(this, floor, floor.power(power.inverted()));
    }
    
    @Override
    @SideEffectFree
    public AlgebraInteger powerRoundZ(
            AlgebraNumber power,
            @Nullable RoundingMode roundingMode
    ) {
        BigDecimal resBD = powerRound(
                power,
                getInitContextZ(roundingMode),
                null
        );
        return IntegerFactory.fromBigInteger(resBD.toBigInteger());
    }
    
    @Override
    @SideEffectFree
    public NumberRemainderPair<? extends AlgebraInteger, ? extends AlgebraNumber> lnZWithRemainder() {
        AlgebraInteger floor = lnRoundZ(RoundingMode.DOWN);
        return new NumberRemainderPair<>(this, floor, floor);
    }
    
    @Override
    @SideEffectFree
    public AlgebraInteger lnRoundZ(
            @Nullable RoundingMode roundingMode
    ) {
        BigDecimal resBD = lnRound(getInitContextZ(roundingMode), null);
        return IntegerFactory.fromBigInteger(resBD.toBigInteger());
    }
    
    @Override
    @SideEffectFree
    public NumberRemainderPair<? extends AlgebraInteger, ? extends AlgebraNumber> logBaseZWithRemainder(
            AlgebraNumber base
    ) {
        AlgebraInteger floor = logBaseRoundZ(base, RoundingMode.DOWN);
        return new NumberRemainderPair<>(this, floor, floor);
    }
    
    @Override
    @SideEffectFree
    public AlgebraInteger logBaseRoundZ(
            AlgebraNumber base,
            @Nullable RoundingMode roundingMode
    ) {
        BigDecimal resBD = logBaseRound(
                base,
                getInitContextZ(roundingMode),
                null
        );
        return IntegerFactory.fromBigInteger(resBD.toBigInteger());
    }
    
    @Override
    @SideEffectFree
    public NumberRemainderPair<? extends Rational, ? extends AlgebraNumber> expQWithRemainder(
            @Nullable Integer precision
    ) {
        Rational floor = expRoundQ(getContextFrom(precision, RoundingMode.DOWN));
        return new NumberRemainderPair<>(this, floor, floor);
    }
    
    @Override
    @SideEffectFree
    public Rational expRoundQ(
            @Nullable MathContext mathContext
    ) {
        MathContext ratContext = getRatContext(mathContext);
        return RationalFactory.fromBigDecimal(expRound(
                getInitContextQ(ratContext),
                ratContext
        ));
    }
    
    @Override
    @SideEffectFree
    public NumberRemainderPair<? extends Rational, ? extends AlgebraNumber> powerQWithRemainder(
            AlgebraNumber power,
            @Nullable Integer precision
    ) {
        Rational floor = powerRoundQ(power, getContextFrom(precision, RoundingMode.DOWN));
        return power.isZero()
                ? new NumberRemainderPair<>(floor, Cache.get(0))
                : new NumberRemainderPair<>(this, floor, floor.power(power.inverted()));
    }
    
    @Override
    @SideEffectFree
    public Rational powerRoundQ(
            AlgebraNumber power,
            @Nullable MathContext mathContext
    ) {
        MathContext ratContext = getRatContext(mathContext);
        return RationalFactory.fromBigDecimal(powerRound(
                power,
                getInitContextQ(ratContext),
                ratContext
        ));
    }
    
    @Override
    @SideEffectFree
    public NumberRemainderPair<? extends Rational, ? extends AlgebraNumber> lnQWithRemainder(
            @Nullable Integer precision
    ) {
        Rational floor = lnRoundQ(getContextFrom(precision, RoundingMode.DOWN));
        return new NumberRemainderPair<>(this, floor, floor);
    }
    
    @Override
    @SideEffectFree
    public Rational lnRoundQ(
            @Nullable MathContext mathContext
    ) {
        MathContext ratContext = getRatContext(mathContext);
        return RationalFactory.fromBigDecimal(lnRound(
                getInitContextQ(ratContext),
                ratContext
        ));
    }
    
    @Override
    @SideEffectFree
    public NumberRemainderPair<? extends Rational, ? extends AlgebraNumber> logBaseQWithRemainder(
            AlgebraNumber base,
            @Nullable Integer precision
    ) {
        Rational floor = logBaseRoundQ(base, getContextFrom(precision, RoundingMode.DOWN));
        return new NumberRemainderPair<>(this, floor, floor);
    }
    
    @Override
    @SideEffectFree
    public Rational logBaseRoundQ(
            AlgebraNumber base,
            @Nullable MathContext mathContext
    ) {
        MathContext ratContext = getRatContext(mathContext);
        return RationalFactory.fromBigDecimal(logBaseRound(
                base,
                getInitContextQ(ratContext),
                ratContext
        ));
    }
    
    @SideEffectFree
    protected static MathContext getInitContextZ(
            @Nullable RoundingMode roundingMode
    ) {
        return new MathContext(
                DEFAULT_FIRST_OP_PREC,
                Objects.requireNonNullElse(roundingMode, DEFAULT_ROUNDING)
        );
    }
    
    @SideEffectFree
    protected static MathContext getRatContext(
            @Nullable MathContext mathContext
    ) {
        int precision;
        RoundingMode roundingMode;
        
        if (mathContext != null) {
            if (mathContext.getPrecision() <= MAX_PRECISION) {
                return mathContext;
            }
            precision = MAX_PRECISION;
            roundingMode = mathContext.getRoundingMode();
        } else {
            precision = DEFAULT_PRECISION;
            roundingMode = DEFAULT_ROUNDING;
        }
        
        return new MathContext(precision, roundingMode);
    }
    
    protected static MathContext getInitContextQ(
            MathContext ratContext
    ) {
        return new MathContext(
                Math.max(ratContext.getPrecision() + PRECISION_TO_ADD_FOR_FIRST_OP, MAX_PRECISION),
                ratContext.getRoundingMode()
        );
    }
    
    @SideEffectFree
    private static BigDecimal finalProcessForRoundedRealOp(
            BigDecimal answer,
            MathContext initContext,
            @Nullable MathContext rationalContext
    ) {
        return (rationalContext != null)
                ? answer.round(rationalContext)
                : answer.setScale(0, initContext.getRoundingMode());
    }
    
    @SideEffectFree
    protected final BigDecimal expRound(
            MathContext initContext,
            @Nullable MathContext rationalContext
    ) {
        int firstPrec = initContext.getPrecision();
        BigDecimal resBD = BigDecimalUtils.exp(
                toBigDecimal(initContext),
                firstPrec
        );
        assert (rationalContext == null) || (rationalContext.getPrecision() <= firstPrec);
        return finalProcessForRoundedRealOp(resBD,
                initContext, rationalContext);
    }
    
    @SideEffectFree
    protected final BigDecimal powerRound(
            AlgebraNumber power,
            MathContext initContext,
            @Nullable MathContext rationalContext
    ) {
        int firstPrec = initContext.getPrecision();
        BigDecimal resBD = BigDecimalUtils.pow(
                toBigDecimal(initContext),
                power.toBigDecimal(initContext),
                firstPrec
        );
        assert (rationalContext == null) || (rationalContext.getPrecision() <= firstPrec);
        return finalProcessForRoundedRealOp(resBD,
                initContext, rationalContext);
    }
    
    @SideEffectFree
    protected final BigDecimal lnRound(
            MathContext initContext,
            @Nullable MathContext rationalContext
    ) {
        int firstPrec = initContext.getPrecision();
        BigDecimal resBD = BigDecimalUtils.log(
                toBigDecimal(initContext),
                firstPrec
        );
        assert (rationalContext == null) || (rationalContext.getPrecision() <= firstPrec);
        return finalProcessForRoundedRealOp(resBD,
                initContext, rationalContext);
    }
    
    @SideEffectFree
    protected final BigDecimal logBaseRound(
            AlgebraNumber base,
            MathContext initContext,
            @Nullable MathContext rationalContext
    ) {
        int firstPrec = initContext.getPrecision();
        BigDecimal topBD = BigDecimalUtils.log(
                toBigDecimal(initContext),
                firstPrec
        );
        BigDecimal botBD = BigDecimalUtils.log(
                base.toBigDecimal(initContext),
                firstPrec
        );
        assert (rationalContext == null) || (rationalContext.getPrecision() <= firstPrec);
        return (rationalContext != null)
                ? topBD.divide(botBD, rationalContext)
                : topBD.divide(botBD, 0, initContext.getRoundingMode());
    }
}
