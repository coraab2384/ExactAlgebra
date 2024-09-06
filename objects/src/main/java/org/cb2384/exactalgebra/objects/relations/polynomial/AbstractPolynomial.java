package org.cb2384.exactalgebra.objects.relations.polynomial;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SequencedCollection;
import java.util.Spliterator;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.cb2384.exactalgebra.objects.internalaccess.CacheInteger;
import org.cb2384.exactalgebra.objects.numbers.AlgebraNumber;
import org.cb2384.exactalgebra.objects.numbers.integral.AlgebraInteger;
import org.cb2384.exactalgebra.objects.numbers.integral.FiniteInteger;
import org.cb2384.exactalgebra.objects.pair.FunctionRemainderPair;
import org.cb2384.exactalgebra.util.MiscUtils;
import org.cb2384.exactalgebra.util.corutils.ternary.Signum;

import org.checkerframework.checker.index.qual.*;
import org.checkerframework.checker.nullness.qual.*;
import org.checkerframework.checker.signedness.qual.*;
import org.checkerframework.common.value.qual.*;
import org.checkerframework.dataflow.qual.*;

/**
 * <p>Skeletal implementations for {@link Polynomial}s.</p>
 *
 * <p>Throws:&ensp;{@link NullPointerException} &ndash; on any {@code null} argument,
 * unless otherwise specified</p>
 *
 * @param <N>   the type of the coefficients, which also determines the type of the output if the input
 *              is also of this type
 *
 * @author  Corinne Buxton
 */
public abstract class AbstractPolynomial<N extends AlgebraNumber>
        implements Polynomial<N> {
    
    /**
     * Max degree; while one could create an implementation with a higher max, this the max used by
     * most implementations
     */
    public static final int DEFAULT_MAX_DEGREE = Character.MAX_VALUE;
    
    /**
     * Max length; while one could create an implementation with a higher max, this the max used by
     * most implementations
     */
    public static final int DEFAULT_MAX_LENGTH = DEFAULT_MAX_DEGREE + 1;
    
    /**
     * Max degree but as an {@link AlgebraInteger}
     */
    static final FiniteInteger MAX_DEG_AS_AI = FiniteInteger.valueOfStrict(DEFAULT_MAX_DEGREE);
    
    /**
     * FiniteInteger value of 0, for easy access
     */
    protected static final FiniteInteger ZERO = CacheInteger.CACHE.get(1).getFirst();
    
    /**
     * FiniteInteger value of 1, for easy access
     */
    protected static final FiniteInteger ONE = CacheInteger.CACHE.get(2).getFirst();
    
    /**
     * build constant 0
     * @return  a Polynomial of constant value 0
     *
     * @param <R>   the coefficient type of the returned polynomial, which must be a superclass of
     *              {@link FiniteInteger}
     */
    @SideEffectFree @SuppressWarnings("unchecked")
    private <R extends AlgebraNumber> Polynomial<R> buildZero() {
        return (Polynomial<R>) constantOf(ZERO);
    }
    
    /**
     * Builds a constant polynomial from the given value
     *
     * @param constant  the constant value
     *
     * @return  the constant, but wrapped up as a Polynomial instead of just some AlgebraNumber
     *
     * @param <R>   the type of the Polynomial
     */
    @SideEffectFree
    abstract <R extends AlgebraNumber> Polynomial<R> constantOf(R constant);
    
    /**
     * Builds a monomial from the given value
     *
     * @param coefficient   the coefficient of the monomial
     * @param degree        the degree to give the monomial
     *
     * @return  the monomial with given term and degree
     *
     * @param <R>   the type of the Polynomial
     */
    @SideEffectFree
    abstract <R extends AlgebraNumber> Polynomial<R> monomialOf(
            R coefficient,
            @IntRange(from = 0, to = DEFAULT_MAX_DEGREE) int degree
    );
    
    /**
     * Builds a polynomial out of the given SequencedCollection. The collection is assumed to start with the
     * smallest value, the constant first, and the highest-degree-coefficient last.
     *
     * @param sequencedCollection   the collection of coefficients
     *
     * @return  the polynomial with the same coefficients as the input
     *
     * @param <R>   the type of the Polynomial
     */
    @SideEffectFree
    abstract <R extends AlgebraNumber> Polynomial<R>
            fromSeqCollecBigEndian(SequencedCollection<R> sequencedCollection);
    
    /**
     * Similar to {@link Collections#nCopies}, but the object is always 0.
     *
     * @param number    the number of 0s to copy
     *
     * @return          a List of {@code number} 0s
     */
    @SideEffectFree
    protected static List<FiniteInteger> dummyZeros(
            @IntRange(from = 0, to = DEFAULT_MAX_LENGTH) int number
    ) {
        return Collections.nCopies(number, ZERO);
    }
    
    /**
     * Checks if the degree is valid.
     *
     * @param degree    the degree to check
     *
     * @throws IndexOutOfBoundsException    if {@code degree} is too low or high
     */
    @Pure
    protected static void checkDeg(
            int degree
    ) {
        if (degree > DEFAULT_MAX_DEGREE) {
            throw new IndexOutOfBoundsException( degree
                    + " is greater than the maximum degree of  " + DEFAULT_MAX_DEGREE);
        } else if (degree < 0) {
            throw new IndexOutOfBoundsException( degree
                    + " is less than the minimum degree of 0");
        }
    }
    
    /**
     * Checks if the degree is valid.
     *
     * @param length    the length to check
     *
     * @throws IndexOutOfBoundsException    if {@code length} is too low or high
     */
    @Pure
    protected static void checkLength(
            int length
    ) {
        if (length > DEFAULT_MAX_LENGTH) {
            throw new IndexOutOfBoundsException( length
                    + " is greater than the maximum length of  " + DEFAULT_MAX_LENGTH);
        } else if (length <= 0) {
            throw new IndexOutOfBoundsException( length
                    + " is less than the minimum length of 1");
        }
    }
    
    /**
     * Creates a string to represent this Polynomial. The string uses the default radix of 10, and the variable
     * of the Polynomial is signified by "x".
     *
     * @return  the constructed string
     */
    @Override
    @SideEffectFree
    public String toString() {
        return stringBuilder("x", 10);
    }
    
    /**
     * Creates a string to represent this Polynomial. The string uses the variable of "x" to represent the variable,
     * unless the radix is high enough that 'x' becomes part of the numbers; in that case, the variable is
     * instead represented by the underscore "_".
     *
     * @return  the constructed string
     *
     * @throws NumberFormatException    if {@code radix < }{@link Character#MIN_RADIX} or
     *                                  {@code radix > }{@link Character#MAX_RADIX}
     */
    @Override
    @SideEffectFree
    public String toString(
            @IntRange(from = Character.MIN_RADIX, to = Character.MAX_RADIX) int radix
    ) {
        MiscUtils.checkRadix(radix);
        return stringBuilder(
                (Character.digit('x', Character.MAX_RADIX) > radix) ? "x" : "_",
                radix
        );
    }
    
    /**
     * {@inheritDoc}
     *
     * @throws NumberFormatException    if {@code radix < }{@link Character#MIN_RADIX} or
     *                                  {@code radix > }{@link Character#MAX_RADIX}
     */
    @Override
    @SideEffectFree
    public String toString(
            String variable,
            @IntRange(from = Character.MIN_RADIX, to = Character.MAX_RADIX) int radix
    ) {
        checkRadixAndVariable(variable, radix);
        return stringBuilder(variable, radix);
    }
    
    /**
     * Actual implementation of the various toString functions
     *
     * @param variable  the variable
     * @param radix     the radix
     *
     * @return  the constructed string
     *
     * @throws NumberFormatException    if {@code radix < }{@link Character#MIN_RADIX} or
     *                                  {@code radix > }{@link Character#MAX_RADIX}
     */
    @SideEffectFree
    private String stringBuilder(
            String variable,
            @IntRange(from = Character.MIN_RADIX, to = Character.MAX_RADIX) int radix
    ) {
        int currDeg = degree();
        StringBuilder resBuilder = new StringBuilder( coeffString(leadCoefficient(), radix) );
        switch (currDeg) {
            default:
                while (currDeg > 2) {
                    resBuilder.append(variable)
                            .append("^")
                            .append(Integer.toString(currDeg--, radix));
                    appendCoeffString(resBuilder, currDeg, radix);
                }
                // Fall through after loop to second power
            case 2:
                resBuilder.append(variable);
                resBuilder.append((radix > 2) ? "2" : "10");
                appendCoeffString(resBuilder, 1, radix);
                // Fall through to first power
            case 1:
                resBuilder.append(variable);
                appendCoeffString(resBuilder, 0, radix);
        }
        return resBuilder.toString();
    }
    
    /**
     * Helper function for {@link #stringBuilder}. Appends the coefficient of the
     * term given by degree to toWhichToAppend, but with some checking of signs and other decoration-type things.
     *
     * @param toWhichToAppend   the returned thing, before appending
     * @param degree            the degree of the coefficient to process and append
     * @param radix             the radix of the representation
     */
    private void appendCoeffString(
            StringBuilder toWhichToAppend,
            int degree,
            int radix
    ) {
        N coefficient = coefficient(degree);
        if (!coefficient.isZero()) {
            toWhichToAppend.append(coefficient.isNegative() ? " - " : " + ")
                    .append(coeffString(coefficient.magnitude(), radix));
        }
    }
    
    /**
     * Build the coefficient, depending on its type.
     *
     * @param coefficient   the coefficient to turn to a string
     * @param radix         the radix of the string
     *
     * @return  the built string for the coefficient
     */
    @SideEffectFree
    private static String coeffString(
            AlgebraNumber coefficient,
            @IntRange(from = Character.MIN_RADIX, to = Character.MAX_RADIX) int radix
    ) {
        return coefficient.isWhole()
                ? coefficient.toString(radix)
                : "(" + coefficient.toString(radix) + ")";
    }
    
    /**
     * Checks that both the radix and variable are valid.
     *
     * @param variable  the variable to check&mdash;it cannot be within the bounds of
     *                  characters used for {@code radix}
     * @param radix     the radix of the representation
     *
     * @throws IllegalArgumentException if the variable could be mistaken for a digit with the current {@code radix}
     * @throws NumberFormatException    if {@code radix < }{@link Character#MIN_RADIX} or
     *                                  {@code radix > }{@link Character#MAX_RADIX}
     */
    @Pure
    protected static void checkRadixAndVariable(
            String variable,
            @IntRange(from = Character.MIN_RADIX, to = Character.MAX_RADIX) int radix
    ) {
        MiscUtils.checkRadix(radix);
        switch (variable.codePointCount(0, variable.length())) {
            case 0 -> throw new IllegalArgumentException("Cannot have empty String as variable!");
            case 1 -> {
                if (isProblemDigit(variable.codePointAt(0), radix)) {
                    throw new IllegalArgumentException("Variable " + variable
                            + " is greater in value than allowed by radix " + radix + "!");
                }
            }
            default -> {
                IntConsumer validityChecker = i -> {
                    if (isProblemDigit(i, radix)) {
                        throw new IllegalArgumentException("Variable " + variable + " at " + i
                                + " is greater in value than allowed by radix " + radix + "!");
                    }
                };
                variable.codePoints().forEach(validityChecker);
            }
        }
    }
    
    /**
     * Checks that a codePoint does not clash with the radix or other symbols.
     *
     * @param codePoint the thing to check
     * @param radix     the radix of the representation
     *
     * @return  {@code true} if {@code codePoint} would cause problems, otherwise {@code false}
     */
    @Pure
    private static boolean isProblemDigit(
            int codePoint,
            @IntRange(from = Character.MIN_RADIX, to = Character.MAX_RADIX) int radix
    ) {
        String reservedChars = "+-./()^";
        if (reservedChars.contains(Character.toString(codePoint))) {
            return true;
        }
        
        int digit = Character.digit(codePoint, Character.MAX_RADIX);
        return (digit != -1) && (digit <= radix);
    }
    
    /**
     * {@inheritDoc}
     *
     * @implNote    This skeletal implementation simply returns {@link #length()}<code> - 1</code>.
     */
    @Override
    @Pure
    public @NonNegative int degree() {
        return length() - 1;
    }
    
    /**
     * {@inheritDoc}
     *
     * @implNote    This skeletal implementation simply checks {@link #isConstant()}<code> &amp;&amp;
     *              </code>{@link #constant()}{@link AlgebraNumber#isZero() .isZero()}.
     */
    @Override
    @Pure
    public boolean isZero() {
        return isConstant() && constant().isZero();
    }
    
    /**
     * {@inheritDoc}
     *
     * @implNote    This skeletal implementation simply checks {@link #isConstant()}<code> &amp;&amp;
     *              </code>{@link #constant()}{@link AlgebraNumber#isOne() .isOne()}.
     */
    @Override
    @Pure
    public boolean isOne() {
        return isConstant() && constant().isOne();
    }
    
    /**
     * {@inheritDoc}
     *
     * @implNote    This skeletal implementation simply calls {@link #leadCoefficient()}{@link
     *              AlgebraNumber#isNegative() .isNegative()}.
     */
    @Override
    @Pure
    public boolean isNegative() {
        return leadCoefficient().isNegative();
    }
    
    /**
     * {@inheritDoc}
     *
     * @implNote    This skeletal implementation relies on {@link #isConstant()} and/or {@link
     *              #stream(Integer, Integer)}.
     */
    @Override
    public boolean isMonomial() {
        return isConstant() || stream(0, -1).allMatch(AlgebraNumber::isZero);
    }
    
    /**
     * {@inheritDoc}
     *
     * @implNote    This skeletal implementation relies on {@link #length()} and {@link #coefficient(int)}.
     */
    @Override
    @Pure
    public int compareTo(
            Polynomial<?> that
    ) {
        int thatLength = that.length();
        int lengthComp = length() - thatLength;
        return (lengthComp != 0)
                ? lengthComp
                : compareIfSameSize(that, thatLength);
    }
    
    /**
     * {@inheritDoc}
     *
     * @implNote    This skeletal implementation relies on {@link #compareDeg(Polynomial)}, {@link #length()},
     *              and {@link #coefficient(int)}.
     */
    @Override
    @Pure
    public Signum compare(
            Polynomial<?> that
    ) {
        Signum compResult = compareDeg(that);
        return (compResult != Signum.ZERO)
                ? compResult
                : Signum.valueOf(compareIfSameSize(that, length()));
    }
    
    /**
     * Runs the comparison coefficient by coefficient.
     *
     * @param that      the other polynomial
     * @param length    the length of this and that
     *
     * @return  a negative integer, zero, or a positive integer as this is less than, equal to,
     *          or greater than {@code o}
     */
    @Pure
    private int compareIfSameSize(
            Polynomial<? extends AlgebraNumber> that,
            @Positive int length
    ) {
        assert (length == length()) && (length == that.length());
        for (int i = 0; i < length; i++) {
            int compResult = coefficient(i).compareTo(that.coefficient(i));
            if (compResult != 0) {
                return compResult;
            }
        }
        //else
        return 0;
    }
    
    /**
     * {@inheritDoc}
     *
     * @implNote    This skeletal implementation simply calls {@link Signum#valueOf(int) Signum.valueOf(}{@link
     *              #length()}<code> - that</code>{@link Polynomial#length() .length()}{@link Signum#valueOf(int) )}.
     */
    @Override
    @Pure
    public Signum compareDeg(
            Polynomial<?> that
    ) {
        return Signum.valueOf(length() - that.length());
    }
    
    /**
     * The hashcode of a Polynomial is as follows: the product of the hashcodes of all coefficients, excepting
     * those with hash 0 who use 1 plus their degree instead. This value is then added to
     * {@code (short) (31 * (short) }{@link #length()}{@code )}.
     *
     * @return  the hash
     */
    @Override
    @Pure
    public int hashCode() {
        int length = length();
        int hash = 1;
        for (int i = 0; i < length;) {
            // Step up i here to prevent product by 0
            int thisHash = coefficient(i++).hashCode();
            hash *= (thisHash == 0) ? i : thisHash;
        }
        return hash + (short) (31 * (short) length);
    }
    
    /**
     * Two Polynomials are equal if they have the same coefficients and are in the same "Rank"&mdash;which is
     * easy for Polynomials as they all have the same "Rank" anyway. Thus, for Polynomials, this will be exactly
     * the same as {@link #equiv}.
     *
     * @param obj   The object to test for equality
     *
     * @return  {@code true} if these values are equal and have the same "Rank" or over-arching
     *          interface membership; otherwise {@code false}
     */
    @Override
    @Pure
    public boolean equals(
            @Nullable Object obj
    ) {
        return (obj instanceof Polynomial<?> oPoly) && equiv(oPoly);
    }
    
    /**
     * Performs a unary op, like negation, where the action performed on each coefficient is the same
     *
     * @param unaryOp   the op to perform
     *
     * @return  a new Polynomial with the op performed on its coefficients
     */
    @SideEffectFree
    private Polynomial<?> unaryOp(
            Function<? super AlgebraNumber, ? extends AlgebraNumber> unaryOp
    ) {
        return fromSeqCollecBigEndian(parallelStream()
                .map(unaryOp)
                .toList());
    }
    
    /**
     * {@inheritDoc}
     *
     * @implNote    This skeletal implementation simply negates each coefficient.
     *              It relies on {@link #iterator()}, through {@link #spliterator()}, through {@link #stream()}.
     */
    @Override
    @SideEffectFree @SuppressWarnings("unchecked")
    public Polynomial<N> negated() {
        return (Polynomial<N>) unaryOp(AlgebraNumber::negated);
    }
    
    /**
     * {@inheritDoc}
     *
     * @implNote    This skeletal implementation simply takes the absolute value ({@link AlgebraNumber#magnitude()})
     *              each coefficient. It relies on {@link #isOne()} and {@link #isZero()} and
     *              {@link #iterator()}, through {@link #spliterator()}, through {@link #stream()}.
     */
    @Override
    @SideEffectFree @SuppressWarnings("unchecked")
    public Polynomial<N> absoluteValue() {
        if (isZero() || isOne() || stream().noneMatch(AlgebraNumber::isNegative)) {
            return this;
        }
        return (Polynomial<N>) unaryOp(AlgebraNumber::magnitude);
    }
    
    /**
     * {@inheritDoc}
     *
     * @implNote    This skeletal implementation simply calls {@code (N) }{@link #evaluate(AlgebraNumber)
     *              evaluate(}{@code n}{@link #evaluate(AlgebraNumber) )}.
     */
    @Override
    @SideEffectFree @SuppressWarnings("unchecked")
    public N apply(
            N n
    ) {
        return (N) evaluate(n);
    }
    
    /**
     * {@inheritDoc}
     *
     * @implNote    This skeletal implementation relies on {@link #coefficient(int)} and {@link #iterator()}
     */
    @Override
    @SideEffectFree
    public AlgebraNumber evaluate(
            AlgebraNumber input
    ) {
        int length = length();
        AlgebraNumber res = constant();
        for (int i = 1; i < length; i++) {
            AlgebraNumber term = coefficient(i).product(input.raised(i));
            res = res.sum(term);
        }
        return res;
    }
    
    /**
     * {@inheritDoc}
     *
     * @implNote    This skeletal implementation relies on {@link Polynomial#coefficient(int)} and
     *              {@link Polynomial#length()}.
     */
    @Override
    @SideEffectFree
    public Polynomial<?> sum(
            Polynomial<?> augend
    ) {
        return arithRes(augend, false, AlgebraNumber::sum);
    }
    
    /**
     * {@inheritDoc}
     *
     * @implNote    This skeletal implementation relies on {@link Polynomial#coefficient(int)} and
     *              {@link Polynomial#length()}.
     */
    @Override
    @SideEffectFree
    public Polynomial<?> difference(
            Polynomial<?> subtrahend
    ) {
        return arithRes(subtrahend, true, AlgebraNumber::difference);
    }
    
    /**
     * <p>Finds the result for an addition or subtraction operation. Whether one is adding or subtracting
     * is determined by both {@code subtract} but also by the operation of {@code op}. <em>These should
     * agree, and there is not a mechanism provided to enforce this; it is your responsibility.</em>
     * That's part of why this is protected.</p>
     *
     * <p>The returned Polynomial will likely have to be cast to a specific coefficient type;
     * the cast will be safe if that type (let's call it {@code R}) is:<ul>
     *      <li>Closed under {@link AlgebraNumber#sum}</li>
     *      <li>Closed under {@link AlgebraNumber#difference}</li>
     *      <li>A superclass/interface of {@link FiniteInteger}</li>
     * </ul>The examples within this module of possible types {@code R} are {@link AlgebraNumber},
     * {@link org.cb2384.exactalgebra.objects.numbers.rational.Rational Rational}, and {@link AlgebraInteger}.</p>
     *
     * @param that      the Polynomial to add or subtract from this
     * @param subtract  determines whether adding or subtracting
     * @param op        the operation (addition or subtraction) of the individual coefficients;
     *                  should be {@link AlgebraNumber#difference} if {@code subtract == true}
     *                  otherwise {@link AlgebraNumber#sum}, but it could be a specialization,
     *                  for example {@link
     *                  org.cb2384.exactalgebra.objects.numbers.rational.Rational#sum(
     *                  org.cb2384.exactalgebra.objects.numbers.rational.Rational) Rational.sum},
     *                  and so it is left as an argument
     *
     * @return  the sum or difference
     */
    @SideEffectFree
    protected final Polynomial<?> arithRes(
            Polynomial<?> that,
            boolean subtract,
            BiFunction<? super AlgebraNumber, ? super AlgebraNumber, ? extends AlgebraNumber> op
    ) {
        // Initialize since it will be needed in later blocks
        List<AlgebraNumber> res;
        int thisLength = length();
        int thatLength = that.length();
        // Length of result
        int ansLength = Math.max(thisLength, thatLength);
        if (thisLength == thatLength) {
            // Need to check for cancellation
            // Initialize this now for use outside assigning block
            int compResult;
            
            if (subtract) {
                // If subtract, then every leading slot where they are equal
                // must be cancelled
                do {
                    compResult = coefficient(--ansLength).compareTo( that.coefficient(ansLength) );
                } while ( (ansLength > 0) && (compResult == 0) );
            } else {
                // Else, every leading slot where the negation of one equals the other
                // must be cancelled
                do {
                    AlgebraNumber bXNeg = that.coefficient(--ansLength).negated();
                    compResult = coefficient(ansLength).compareTo(bXNeg);
                } while ( (ansLength > 0) && (compResult == 0) );
            }
            // If there are still slots left
            if (ansLength > 1) {
                // In either branch of the earlier if
                // cLen will come out being the leading degree, not the length
                // Incrememnt by 1 here to fix
                res = new ArrayList<>(++ansLength);
                for (int i = 0; i < ansLength; i++) {
                    AlgebraNumber arithRes = op.apply(coefficient(i), that.coefficient(i));
                    res.add(arithRes);
                }
            } else {
                return buildZero();
            }
        } else {
            res = new ArrayList<>(ansLength);
            int shortLen = Math.min(thisLength, thatLength);
            for (int i = 0; i < shortLen; i++) {
                AlgebraNumber arithRes = op.apply(coefficient(i), that.coefficient(i));
                res.add(arithRes);
            }
            if (ansLength > thatLength) {
                for (int i = shortLen; i < ansLength; i++) {
                    res.add(coefficient(i));
                }
            } else if (subtract) {
                for (int i = shortLen; i < ansLength; i++) {
                    res.add(that.coefficient(i).negated());
                }
            } else {
                for (int i = shortLen; i < ansLength; i++) {
                    res.add(that.coefficient(i));
                }
            }
        }
        return fromSeqCollecBigEndian(res);
    }
    
    /**
     * {@inheritDoc}
     *
     * @implNote    This skeletal implementation relies on {@link #iterator()},
     *              through {@link #spliterator()}, through {@link #stream()}.
     */
    @Override
    @SideEffectFree
    public Polynomial<?> scaled(
            AlgebraNumber scalar
    ) {
        return scalar(scalar, AlgebraNumber::product);
    }
    
    /**
     * <p>Scales this by a constant. The type of scaling (stretch or compression) is determined by the given
     * argument {@code op}. The first argument given to {@code op} will always be a coefficient from this
     * Polynomial, and the second will always be {@code scale}.</p>
     *
     * <p>The returned Polynomial will likely have to be cast to a specific coefficient type;
     * the cast will be safe if that type (let's call it {@code R}) is closed under {@code op}.
     * Assuming {@code op} is either {@link AlgebraNumber#product} or {@link AlgebraNumber#quotient},
     * the examples within this module of possible types {@code R} are {@link AlgebraNumber} and
     * {@link org.cb2384.exactalgebra.objects.numbers.rational.Rational Rational}. For {@link AlgebraNumber#product}
     * only, {@link AlgebraInteger} is also valid.</p>
     *
     * @param scale     the factor by which to scale this
     * @param op        the operation to perform on each coefficient; the scaling operation; this should likely
     *                  be either {@link AlgebraNumber#product} or {@link AlgebraNumber#quotient},
     *                  but it could be a specialization, for example {@link
     *                  org.cb2384.exactalgebra.objects.numbers.rational.Rational#product(
     *                  org.cb2384.exactalgebra.objects.numbers.rational.Rational) Rational.product},
     *                  and so it is left as an argument
     *
     * @return  a copy of this, but scaled accordingly
     */
    @SideEffectFree
    protected final Polynomial<?> scalar(
            AlgebraNumber scale,
            BiFunction<? super AlgebraNumber, ? super AlgebraNumber, ? extends AlgebraNumber> op
    ) {
        if (scale.isZero()) {
            return buildZero();
        }
        
        if (scale.isOne()) {
            return this;
        }
        return unaryOp(n -> op.apply(n, scale));
    }
    
    /**
     * {@inheritDoc}
     *
     * @implNote    This skeletal implementation relies on {@link Polynomial#coefficient(int)} and
     *              {@link Polynomial#length()}, as well as possibly {@link Polynomial#constant()} and
     *              {@link #iterator()}, through {@link #spliterator()}, through {@link #stream()}.
     */
    @Override
    @SideEffectFree
    public Polynomial<?> product(
            Polynomial<?> multiplicand
    ) {
        return mult(multiplicand);
    }
    
    /**
     * Polynomial multiplication implementation, provided if one wishes to use it in a custom implementation.
     * The returned Polynomial will likely have to be cast to a specific coefficient type;
     * the cast will be safe if that type (let's call it {@code R}) is:<ul>
     *      <li>Closed under {@link AlgebraNumber#sum}</li>
     *      <li>Closed under {@link AlgebraNumber#product}</li>
     *      <li>A superclass/interface of {@link FiniteInteger}</li>
     * </ul>The examples within this module of possible types {@code R} are {@link AlgebraNumber},
     * {@link org.cb2384.exactalgebra.objects.numbers.rational.Rational Rational}, and {@link AlgebraInteger}.
     *
     * @param that    the polynomial being multiplied with this
     *
     * @return  the product
     */
    @SideEffectFree
    protected final Polynomial<?> mult(
            Polynomial<?> that
    ) {
        if (isZero()) {
            return buildZero();
        }
        
        int thisLength = length();
        int thatLength = that.length();
        if (thatLength == 1) {
            AlgebraNumber scalar = that.constant();
            return scalar(scalar, AlgebraNumber::product);
        }
        
        int ansLength = thisLength + thatLength - 1;
        checkLength(ansLength);
        List<AlgebraNumber> res = new ArrayList<>(ansLength);
        res.addAll(dummyZeros(ansLength));
        for (int i = 0; i < thisLength; i++) {
            for (int j = 0; j < thatLength; j++) {
                int index =  i + j;
                AlgebraNumber prod = coefficient(i).product( that.coefficient(j) );
                AlgebraNumber sum = res.get(index).sum(prod);
                res.set(index, sum);
            }
        }
        return fromSeqCollecBigEndian(res);
    }
    
    /**
     * {@inheritDoc}
     *
     * @implNote    This skeletal implementation relies on {@link Polynomial#coefficient(int)} and
     *              {@link Polynomial#length()}, as well as possibly {@link Polynomial#constant()} and
     *              {@link #iterator()}, through {@link #spliterator()}, through {@link #stream()}.
     */
    @Override
    @SideEffectFree
    public FunctionRemainderPair<? extends Polynomial<?>, ? extends Polynomial<?>> quotientZWithRemainder(
            Polynomial<?> divisor
    ) {
        return divRouter(divisor, true);
    }
    
    /**
     * {@inheritDoc}
     *
     * @implNote    This skeletal implementation relies on {@link Polynomial#coefficient(int)} and
     *              {@link Polynomial#length()}, as well as possibly {@link Polynomial#constant()} and
     *              {@link #iterator()}, through {@link #spliterator()}, through {@link #stream()}.
     */
    @Override
    @SideEffectFree
    public Polynomial<?> quotientZ(
            Polynomial<?> divisor
    ) {
        return divRouter(divisor, false).value();
    }
    
    /**
     * {@inheritDoc}
     *
     * @implNote    This skeletal implementation relies on {@link Polynomial#coefficient(int)} and
     *              {@link Polynomial#length()}, as well as possibly {@link Polynomial#constant()} and
     *              {@link #iterator()}, through {@link #spliterator()}, through {@link #stream()}.
     */
    @Override
    @SideEffectFree
    public Polynomial<?> remainder(
            Polynomial<?> divisor
    ) {
        return divRouter(divisor, true).remainder();
    }
    
    /**
     * <p>Determines which division function to use, and then calls the appropriate one. Also checks for division
     * by 0.</p>
     *
     * <p>The returned FunctionRemainderPair will likely have to be cast to a specific coefficient type;
     * the cast will be safe if that type (let's call it {@code R}) is:<ul>
     *      <li>Closed under {@link AlgebraNumber#sum}</li>
     *      <li>Closed under {@link AlgebraNumber#product}</li>
     *      <li>Closed under {@link AlgebraNumber#difference}</li>
     *      <li>A superclass/interface of {@link FiniteInteger}</li>
     * </ul>The examples within this module of possible types {@code R} are {@link AlgebraNumber},
     * {@link org.cb2384.exactalgebra.objects.numbers.rational.Rational Rational}, and {@link AlgebraInteger}.</p>
     *
     * @param divisor       the divisor for this
     * @param wantRemainder whether we want the remainder or not; if not, no reason to do the extra work
     *
     * @return  An array of two {@link PolyArrayList}s: the first is the quotient,
     *          and the second is the remainder, or could be {@code null} if {@code wantRemainder == false}.
     *          The quotient should not be null.
     */
    @SideEffectFree
    protected final FunctionRemainderPair<? extends Polynomial<?>, @Nullable ? extends Polynomial<?>> divRouter(
            Polynomial<?> divisor,
            boolean wantRemainder
    ) {
        if (divisor.isZero()) {
            throw new ArithmeticException("Cannot divide by 0!");
        }
        
        int divisorLength = divisor.length();
        
        return switch (divisorLength) {
            case 1 -> {
                AlgebraNumber scalar = divisor.constant();
                Polynomial<?> quotient = scalar(scalar, AlgebraNumber::quotient);
                yield new FunctionRemainderPair<>(quotient, buildZero());
            }
            case 2 -> {
                AlgebraNumber thatConstNeg = divisor.constant().negated();
                yield divSyn(thatConstNeg, divisor.leadCoefficient());
            }
            // Now switch by which is larger
            default -> switch ( Signum.valueOf(length() - divisorLength) ) {
                case POSITIVE -> divLong(divisor, wantRemainder);
                case ZERO -> {
                    AlgebraNumber quotient = divisor.leadCoefficient();
                    Polynomial<?> remainder = scalar(quotient, AlgebraNumber::quotient);
                    yield new FunctionRemainderPair<>(constantOf(quotient), remainder);
                }
                case NEGATIVE -> new FunctionRemainderPair<>(buildZero(), this);
            };
        };
    }
    
    /**
     * <p>Synthetic division implementation, provided if one wishes to use it in a custom implementation
     * for when the divisor is linear. This is also used for a faster way to evaluate the polynomial for a value:
     * the remainder when dividing by {@code x - r} is the result when evaluating the polynomial at {@code x == r}.
     * To make this function work for boh instances, it is expected that divConNeg is {@code r}, not {@code -r};
     * that is, it is already negated if it came from a polynomial.</p>
     *
     * <p>The returned FunctionRemainderPair will likely have to be cast to a specific coefficient type;
     * the cast will be safe if that type (let's call it {@code R}) is:<ul>
     *      <li>Closed under {@link AlgebraNumber#sum}</li>
     *      <li>Closed under {@link AlgebraNumber#product}</li>
     *      <li>Closed under {@link AlgebraNumber#difference}</li>
     * </ul>The examples within this module of possible types {@code R} are {@link AlgebraNumber},
     * {@link org.cb2384.exactalgebra.objects.numbers.rational.Rational Rational}, and {@link AlgebraInteger}.</p>
     *
     * @param divConNeg the constant term&mdash;but negated&mdash;of the divisor for this,
     *                  OR the value for x that is being evaluated
     * @param divCoeff  the slope, or linear coefficient of the divisor
     *
     * @return an array of two PolyArrayLists; the first is the quotient, and the second is
     *         the remainder if {@code wantRemainder == true}, or {@code null} if we didn't want the remainder
     */
    @SideEffectFree
    protected final FunctionRemainderPair<? extends Polynomial<?>, ? extends Polynomial<?>> divSyn(
            AlgebraNumber divConNeg,
            AlgebraNumber divCoeff
    ) {
        int thisLength = length();
        if (thisLength == 1) {
            return new FunctionRemainderPair<>(buildZero(), this);
        }
        // Else, divide this by the coefficient of the divisor
        List<AlgebraNumber> dividendScaled = stream()
                .map(n -> n.quotient(divCoeff))
                .toList();
        // Also divide the main divisor, divCon, as well
        AlgebraNumber divisor = divConNeg.quotient(divCoeff);
        // Initialize the quotient values
        List<N> dividendNotConst = slicedStreamFixedBounds(1, length(), false)
                .toList();
        List<AlgebraNumber> quotient = new ArrayList<>(thisLength - 1);
        quotient.addAll(dividendNotConst);
        AlgebraNumber remainder = dividendScaled.getLast();
        for (int i = thisLength - 3; i >= 0; i--) {
            remainder = remainder.product(divisor);
            remainder = dividendNotConst.get(i).sum(remainder);
            quotient.set(i, remainder);
        }
        
        remainder = remainder.product(divisor);
        remainder = remainder.sum(dividendScaled.getFirst());
        
        return new FunctionRemainderPair<>(
                fromSeqCollecBigEndian(quotient),
                constantOf(remainder)
        );
    }
    
    /**
     * Polynomial long division implementation, provided if one wishes to use it in a custom implementation.
     * The returned FunctionRemainderPair will likely have to be cast to a specific coefficient type;
     * the cast will be safe if that type (let's call it {@code R}) is:<ul>
     *      <li>Closed under {@link AlgebraNumber#sum}</li>
     *      <li>Closed under {@link AlgebraNumber#product}</li>
     *      <li>Closed under {@link AlgebraNumber#difference}</li>
     *      <li>A superclass/interface of {@link FiniteInteger}</li>
     * </ul>The examples within this module of possible types {@code R} are {@link AlgebraNumber},
     * {@link org.cb2384.exactalgebra.objects.numbers.rational.Rational Rational}, and {@link AlgebraInteger}.
     *
     * @param divisor       the divisor
     * @param wantRemainder whether we want the remainder
     *                      (which sometimes can be more effort than the quotient)
     *
     * @return an array of two PolyArrayLists; the first is the quotient, and the second is
     *         the remainder if {@code wantRemainder == true}, or {@code null} if we didn't want the remainder
     */
    @SideEffectFree
    protected final FunctionRemainderPair<? extends Polynomial<?>, @Nullable ? extends Polynomial<?>> divLong(
            Polynomial<?> divisor,
            boolean wantRemainder
    ) {
        int thisLength = length();
        int thatLength = divisor.length();
        int quoCurr = thisLength - thatLength;
        int quoLength = quoCurr + 1;
        List<AlgebraNumber> quotient = new ArrayList<>(quoLength);
        quotient.addAll(dummyZeros(quoLength));
        int remLength = thatLength - 1;
        
        for (; quoCurr >= 0; quoCurr--) {
            AlgebraNumber term = coefficient(remLength + quoCurr);
            quotient.set(quoCurr, coefficient(remLength + quoCurr));
            int i = quoCurr + 1;
            int j = remLength;
            while (j > 0 && i < quoLength) {
                AlgebraNumber prod = divisor.coefficient(--j).product( quotient.get(i++) );
                term = term.difference(prod);
            }
            term = term.quotient( divisor.coefficient(remLength) );
            quotient.set(quoCurr, term);
        }
        List<AlgebraNumber> remainder = null;
        if (wantRemainder) {
            boolean first = true;
            int remCurr = remLength;
            while (remCurr > 0) {
                AlgebraNumber remain = remainBuild(divisor, quotient, quoLength, --remCurr);
                if (!remain.isZero()) {
                    remainder = stream(null, remCurr)
                            .collect( Collectors.toCollection(ArrayList::new) );
                    remainder.set(remCurr, remain);
                    first = false;
                    break;
                }
            }
            if (first) {
                remainder = new ArrayList<>(1);
                remainder.add(ZERO);
            } else {
                remCurr--;
                for (; remCurr >= 0; remCurr--) {
                    AlgebraNumber remain = remainBuild(divisor, quotient, quoLength, remCurr);
                    AlgebraNumber diff = remainder.get(remCurr).difference(remain);
                    remainder.set(remCurr, diff);
                }
            }
        }
        return new FunctionRemainderPair<>(
                fromSeqCollecBigEndian(quotient),
                fromSeqCollecBigEndian(remainder)
        );
    }
    
    /**
     * Gets a term of the remainder. The remainder calculations for
     * {@link #divLong} happen in two parts, but this particular process is used
     * in both of them. Thus it is itself a function.
     *
     * @param dX        the divisor
     * @param qX        the previously-found quotient
     * @param quoLength the length of the quotient; while it is easy to find,
     *                  in the situations that this function is called, we have already found it
     * @param remCurr   the current index of the term of the remainder that we are now building
     *
     * @return  the term of the remainder for index {@code remCurr}
     */
    @SideEffectFree
    private AlgebraNumber remainBuild(
            Polynomial<?> dX,
            List<? extends AlgebraNumber> qX,
            int quoLength,
            int remCurr
    ) {
        AlgebraNumber remain = coefficient(remCurr);
        int j = 0;
        for (int i = remCurr; i > 0; j++) {
            if (quoLength > i--) {
                AlgebraNumber prod = dX.coefficient(j).product(qX.get(i));
                remain = remain.sum(prod);
            }
        }
        return remain;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @SideEffectFree
    public Polynomial<?> raisedUS(
            @Unsigned short exponent
    ) {
        return polyRaiser(Short.toUnsignedInt(exponent));
    }
    
    /**
     * {@inheritDoc}
     *
     * @implNote    This skeletal implementation relies on {@link #isConstant()}, and {@link #polyRaiser},
     *              which itself relies on {@link #length()}, {@link #squared()}, {@link #product(Polynomial)},
     *              and {@link #isZero()}.
     *
     * @throws IllegalArgumentException     if exponent is negative and this is not constant
     * @throws IndexOutOfBoundsException    if the result would be too large to store
     */
    @Override
    @SideEffectFree
    public Polynomial<?> raisedZ(
            int exponent
    ) {
        if (isConstant()) {
            return constantOf(constant().raised(exponent));
        }
        if (exponent < 0) {
            throw new IllegalArgumentException("Cannot raise to a negative power while still being a Polynomial!");
        }
        
        return polyRaiser(exponent);
    }
    
    /**
     * {@inheritDoc}
     *
     * @implNote    This skeletal implementation relies on {@link #isConstant()}, and {@link #polyRaiser},
     *              which itself relies on {@link #length()}, {@link #squared()}, {@link #product(Polynomial)},
     *              and {@link #isZero()}.
     *
     * @throws IllegalArgumentException     if exponent is negative and this is not constant
     * @throws IndexOutOfBoundsException    if the result would be too large to store
     */
    @Override
    @SideEffectFree
    public Polynomial<?> raisedZ(
            AlgebraInteger exponent
    ) {
        if (isConstant()) {
            return constantOf(constant().raised(exponent));
        }
        
        if (exponent.isNegative()) {
            throw new IndexOutOfBoundsException(
                    "Answer would have degree greater than the maximum degree of" + DEFAULT_MAX_DEGREE);
        }
        if (exponent.compareTo(MAX_DEG_AS_AI) > 0) {
            throw new IllegalArgumentException("Cannot raise to a negative power while still being a Polynomial!");
        }
        
        return polyRaiser(exponent.intValue());
    }
    
    /**
     * The implementation of {@link #raisedZ(int)} or {@link #raisedZ(AlgebraInteger) or {@link #raisedUS(short)}}
     * after the exponent has already been checked. One can override this and
     * then not have to rerwite all of the validation parts
     *
     * @implNote    It should be taken as a guarantee that {@code exponent} is always nonnegative,
     *              as it is when this function is called from any of the non-invertible raised functions.
     *              But it is possible that one could call this during some other implementation; in that case,
     *              it is necessary that {@code exponent} is positive, or else it ends up effectively defaulting
     *              to 2. But this should not be something that should be used at all!
     *
     * @param exponent  the power to raise this to; can't be negative
     *
     * @return  this to the power of {@code exponent}
     *
     * @throws IndexOutOfBoundsException    if the result would be too large to store
     */
    @SideEffectFree
    Polynomial<?> polyRaiser(
            int exponent
    ) {
        return switch (exponent) {
            case 0 -> {
                if (isZero()) {
                    throw new ArithmeticException("0^0 is undefined!");
                }
                yield constantOf(ONE);
            }
            case 1 -> this;
            case 2 -> squared();
            default -> {
                int newLength = exponent * (length() - 1) + 1;
                checkLength(newLength);
                
                Polynomial<?> ans = squared();
                for (int i = 3; i < exponent; i++) {
                    ans = mult(ans);
                }
                yield ans;
            }
        };
    }
    
    /**
     * {@inheritDoc}
     *
     * @implNote    This skeletal implementation relies on {@link #spliterator()}, and through that,
     *              {@link #iterator()} and {@link #length()}. The returned Spliterator is technically
     *              early-binding, but since all implementations of {@link Polynomial} are immutable,
     *              the spliterator returns as {@link Spliterator#ORDERED}, {@link Spliterator#SIZED},
     *              {@link Spliterator#NONNULL}, {@link Spliterator#IMMUTABLE}, and {@link Spliterator#SUBSIZED}.
     *              Should this not be accurate for some other implementation, <em>this must be overridden</em>.
     */
    @Override
    @SideEffectFree
    public Spliterator<N> spliterator() {
        return MiscUtils.spliteratorOrderedNonnullImmutableIterator(iterator(), length());
    }
    
    /**
     * {@inheritDoc}
     *
     * @implNote    This skeletal implementation relies on {@link #spliterator()}, and through that,
     *              {@link #iterator()} and {@link #length()}.
     */
    @Override
    @SideEffectFree
    public Stream<N> stream() {
        return StreamSupport.stream(spliterator(), false);
    }
    
    /**
     * {@inheritDoc}
     *
     * @implNote    This skeletal implementation relies on {@link #length()}, and then
     *              {@link #slicedStreamFixedBounds} which itself by default further relies on
     *              {@link #coefficient(int)}, and {@link #spliterator()} (and through that, {@link #iterator()}).
     */
    @Override
    @SideEffectFree
    public Stream<N> stream(
            @Nullable Integer startInclusive,
            @Nullable Integer endExclusive
    ) {
        return slicerStream(startInclusive, endExclusive, false);
    }
    
    /**
     * Does input validation and transformation for the given indices to account for them being null,
     * or for the negative end index case. The actual stream creation is not done here, but rather in
     * {@link #slicedStreamFixedBounds} which is called at the end.
     *
     * @param startInclusive    the starting point, inclusive, which defaults to 0 if null
     * @param endExclusive      the ending point, exclusive, which defaults to the length of the polynomial if null
     * @param parallel          whether the final stream is parallel or sequential
     *
     * @return  the stream from {@link #slicedStreamFixedBounds}, to be handed off to
     *          {@link #stream(Integer, Integer)} or {@link #parallelStream(Integer, Integer)}
     */
    @SideEffectFree
    private Stream<N> slicerStream(
            @Nullable Integer startInclusive,
            @Nullable Integer endExclusive,
            boolean parallel
    ) {
        int start = (startInclusive != null) ? startInclusive : 0;
        if (start < 0) {
            throw new IndexOutOfBoundsException("Starting index "
                    + start + " is negative!");
        }
        
        int end;
        int length = length();
        if (endExclusive == null) {
            end = length;
        } else if (endExclusive < 0) {
            end = length + endExclusive;
        } else {
            end = endExclusive;
        }
        
        if (end > length) {
            throw new IndexOutOfBoundsException("Ending index "
                    + end + "is higher than the max length " + length);
        }
        if (end < start) {
            throw new IllegalArgumentException("Ending index " + end
                    + " is less than starting index " + start);
        }
        
        if ((start == 0) && (end == length)) {
            return parallel
                    ? parallelStream()
                    : stream();
        }
        if (start == end) {
            return Stream.empty();
        }
        
        return slicedStreamFixedBounds(start, end, parallel);
    }
    
    /**
     * The implementation of {@link #stream(Integer, Integer)} or {@link #parallelStream(Integer, Integer)} after
     * the bounds have already been checked for {@code null}, invalid indices, all that. One can override this and
     * then not have to rerwite all of the parameter validation or transformation parts.
     *
     * @implNote    It should be taken as a guarantee that the parameters will always be properly ordered,
     *              as they are when this function is called from either of the index-bounded stream functions.
     *              But it is possible that one could call this during some other implementation; in that case,
     *              it is necessary that {@code startInclusive} is less than {@code endExclusive}, and if not,
     *              rather than an error being thrown (as that is handled by the function that calls this one)
     *              simply an empty stream is returned.
     *
     * @param startInclusive    the starting degree, inclusive
     * @param endExclusive      the ending degree, exclusive
     * @param parallel          whether to return a parallel or sequential stream
     *
     * @return   the indicated stream, which to be passed to {@link #stream(Integer, Integer)} or
     *          {@link #parallelStream(Integer, Integer)}
     */
    @SideEffectFree
    protected Stream<N> slicedStreamFixedBounds(
            @NonNegative @LessThan("endExclusive") int startInclusive,
            int endExclusive,
            boolean parallel
    ) {
        int usedLength = endExclusive - startInclusive;
        List<N> resList = new ArrayList<>(usedLength);
        for (int i = startInclusive; i < endExclusive; i++) {
            resList.add(coefficient(i));
        }
        return parallel
                ? resList.parallelStream()
                : resList.stream();
    }
    
    /**
     * {@inheritDoc}
     *
     * @implNote    This skeletal implementation relies on {@link #spliterator()}, and through that,
     *              {@link #iterator()} {@link #length()}.
     */
    @Override
    @SideEffectFree
    public Stream<N> parallelStream() {
        return StreamSupport.stream(spliterator(), true);
    }
    
    /**
     * {@inheritDoc}
     *
     * @implNote    This skeletal implementation relies on {@link #length()}, and then
     *              {@link #slicedStreamFixedBounds} which itself by default further relies on
     *              {@link #coefficient(int)}, and {@link #spliterator()} (and through that, {@link #iterator()}).
     */
    @Override
    @SideEffectFree
    public Stream<N> parallelStream(
            @Nullable Integer startInclusive,
            @Nullable Integer endExclusive
    ) {
        return slicerStream(startInclusive, endExclusive, true);
    }
}
