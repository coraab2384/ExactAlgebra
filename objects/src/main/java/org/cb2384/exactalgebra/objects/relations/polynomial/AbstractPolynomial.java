package org.cb2384.exactalgebra.objects.relations.polynomial;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SequencedCollection;
import java.util.Spliterator;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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

public abstract class AbstractPolynomial<N extends AlgebraNumber>
        implements Polynomial<N> {
    
    protected static final int MAX_DEGREE = Character.MAX_VALUE;
    
    protected static final int MAX_LENGTH = MAX_DEGREE + 1;
    
    protected static final FiniteInteger MAX_DEG_AS_AI = FiniteInteger.valueOf(MAX_DEGREE);
    
    static final PolyArrayList<? super FiniteInteger> ZERO_PX
            = new PolyArrayList<>( FiniteInteger.valueOf(0) );
    
    static final PolyArrayList<? super FiniteInteger> ONE_PX
            = new PolyArrayList<>( FiniteInteger.valueOf(1) );
    
    @SideEffectFree
    private <R extends AlgebraNumber> Polynomial<R> buildZero() {
        return constantOf((R) FiniteInteger.valueOf(0));
    }
    
    @SideEffectFree
    protected abstract <R extends AlgebraNumber> Polynomial<R> constantOf(R constant);
    
    @SideEffectFree
    protected abstract <R extends AlgebraNumber> Polynomial<R> monomialOf(
            R coefficient,
            @IntRange(from = 0, to = MAX_DEGREE) int degree
    );
    
    @SideEffectFree
    protected abstract <R extends AlgebraNumber> Polynomial<R>
            fromSeqCollecBigEndian(SequencedCollection<R> sequencedCollection);
    
    @SideEffectFree
    protected static <N extends AlgebraNumber> List<N> dummyZeros(
            @IntRange(from = 0, to = MAX_LENGTH) int number
    ) {
        return Collections.nCopies(number, (N) FiniteInteger.valueOf(0));
    }
    
    /**
     * Checks if the degree is valid.
     * This is done here rather than waiting for a more typical {@link RuntimeException} situation
     *  so that the exceptions thrown are standardized, rather than being different ones depending on
     *  where in particular something goes wrong.
     * @param degree the degree to check
     * @throws IndexOutOfBoundsException if degree is too low or high
     */
    @Pure
    protected static void checkDeg(
            int degree
    ) {
        if (degree > MAX_DEGREE) {
            throw new IndexOutOfBoundsException( degree
                    + " is greater than the maximum degree of  " + MAX_DEGREE );
        } else if (degree < 0) {
            throw new IndexOutOfBoundsException( degree
                    + " is less than the minimum degree of 0");
        }
    }
    
    /**
     * Checks if the degree is valid.
     * This is done here rather than waiting for a more typical {@link RuntimeException} situation
     *  so that the exceptions thrown are standardized, rather than being different ones depending on
     *  where in particular something goes wrong.
     * @param length the degree to check
     * @throws IndexOutOfBoundsException if length is too low or high
     */
    @Pure
    protected static void checkLength(
            int length
    ) {
        if (length > MAX_LENGTH) {
            throw new IndexOutOfBoundsException( length
                    + " is greater than the maximum length of  " + MAX_LENGTH );
        } else if (length < 0) {
            throw new IndexOutOfBoundsException( length
                    + " is less than the minimum length of 1");
        }
    }
    
    @Override
    @SideEffectFree
    public String toString() {
        return stringBuilder("x", 10);
    }
    
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
    
    @Override
    @SideEffectFree
    public String toString(
            String variable,
            @IntRange(from = Character.MIN_RADIX, to = Character.MAX_RADIX) int radix
    ) {
        checkRadixAndVariable(variable, radix);
        return stringBuilder(variable, radix);
    }
    
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
    
    
    @Deterministic
    private StringBuilder appendCoeffString(
            StringBuilder toWhichtoAppend,
            int degree,
            int radix
    ) {
        N coefficient = coefficient(degree);
        if (!coefficient.isZero()) {
            toWhichtoAppend.append(coefficient.isNegative() ? " - " : " + ")
                    .append(coeffString(coefficient.magnitude(), radix));
        }
        return toWhichtoAppend;
    }
    
    @SideEffectFree
    private static String coeffString(
            AlgebraNumber coefficient,
            @IntRange(from = Character.MIN_RADIX, to = Character.MAX_RADIX) int radix
    ) {
        return coefficient.isWhole()
                ? coefficient.toString(radix)
                : "(" + coefficient.toString(radix) + ")";
    }
    
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
    
    @Override
    @Pure
    public @NonNegative int degree() {
        return length() - 1;
    }
    
    @Override
    @Pure
    public boolean isZero() {
        return isConstant() && constant().isZero();
    }
    
    @Override
    @Pure
    public boolean isOne() {
        return isConstant() && constant().isOne();
    }
    
    @Override
    @Pure
    public boolean isNegative() {
        return leadCoefficient().isNegative();
    }
    
    @Override
    public boolean isMonomial() {
        return isConstant() || parallelStream(0, -1).allMatch(AlgebraNumber::isZero);
    }
    
    @Override
    @SideEffectFree
    public N coefficientUS(
            @Unsigned short degree
    ) {
        return coefficient(Short.toUnsignedInt(degree));
    }
    
    @Override
    @Pure
    public int compareTo(
            Polynomial<? extends AlgebraNumber> that
    ) {
        int thatLength = that.length();
        int lengthComp = length() - thatLength;
        return (lengthComp != 0)
                ? lengthComp
                : compareIfSameSize(that, thatLength);
    }
    
    @Override
    @Pure
    public Signum compare(
            Polynomial<? extends AlgebraNumber> that
    ) {
        Signum compResult = compareDeg(that);
        return (compResult != Signum.ZERO)
                ? compResult
                : Signum.valueOf(compareIfSameSize(that, length()));
    }
    
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
    
    @Override
    @Pure
    public Signum compareDeg(
            Polynomial<? extends AlgebraNumber> that
    ) {
        return Signum.valueOf(length() - that.length());
    }
    
    @Override
    @Pure
    public int hashCode() {
        int length = length();
        int hash = 1;
        for (int i = 0; i < length; i++) {
            int thisHash = coefficient(i).hashCode();
            hash *= (thisHash == 0)
                    ? 0x55555555 ^ i
                    : 31 * thisHash;
        }
        return hash + (short) (59 * (short) length);
    }
    
    @Override
    @Pure
    public boolean equals(
            @Nullable Object obj
    ) {
        return (obj instanceof Polynomial<?> oPoly)
                && (hashCode() == obj.hashCode()) && equiv(oPoly);
    }
    
    @SideEffectFree
    private <R extends AlgebraNumber> Polynomial<R> unaryOp(
            Function<? super N, R> unaryOp
    ) {
        return fromSeqCollecBigEndian(parallelStream()
                .map(unaryOp)
                .toList());
    }
    
    @Override
    @SideEffectFree
    public Polynomial<N> negated() {
        return unaryOp(n -> (N) n.negated());
    }
    
    @Override
    @SideEffectFree
    public Polynomial<N> absoluteValue() {
        if (isZero() || isOne() || parallelStream().noneMatch(AlgebraNumber::isNegative)) {
            return this;
        }
        return unaryOp(n -> (N) n.magnitude());
    }
    
    @Override
    @SideEffectFree
    public N apply(
            N n
    ) {
        return (N) evaluate(n);
    }
    
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
    
    @Override
    @SideEffectFree
    public Polynomial<? extends AlgebraNumber> sum(
            Polynomial<? extends AlgebraNumber> augend
    ) {
        return arithRes(augend, false, AlgebraNumber::sum);
    }
    
    @Override
    @SideEffectFree
    public Polynomial<? extends AlgebraNumber> difference(
            Polynomial<? extends AlgebraNumber> subtrahend
    ) {
        return arithRes(subtrahend, true, AlgebraNumber::difference);
    }
    
    /**
     * Finds the result for an addition or subtraction operation
     *
     * @param   that    the PolyArrayList to add or subtract from this
     *
     * @param   subtract     determines whether adding or subtracting
     *
     * @param   op  the operation (addition or subtraction) of the individual coefficients;
     *              should be {@link AlgebraNumber#difference} if {@code subtract == true}
     *              otherwise {@link AlgebraNumber#sum}, but it could be a specialization,
     *              for example {@link org.cb2384.exactalgebra.objects.numbers.rational.Rational#sum(
     *              org.cb2384.exactalgebra.objects.RationalField) Rational.sum}, and so it is left as an argument
     *
     * @return  the sum or difference
     */
    @SideEffectFree
    protected final <R extends AlgebraNumber, S extends R> Polynomial<R> arithRes(
            Polynomial<S> that,
            boolean subtract,
            BiFunction<? super N, ? super S, R> op
    ) {
        // Initialize since it will be needed in later blocks
        List<R> res;
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
                    R arithRes = op.apply(coefficient(i), that.coefficient(i));
                    res.add(arithRes);
                }
            } else {
                return buildZero();
            }
        } else {
            res = new ArrayList<>(ansLength);
            int shortLen = Math.min(thisLength, thatLength);
            for (int i = 0; i < shortLen; i++) {
                R arithRes = op.apply(coefficient(i), that.coefficient(i));
                res.add(arithRes);
            }
            if (ansLength > thatLength) {
                for (int i = shortLen; i < ansLength; i++) {
                    res.add((R) coefficient(i));
                }
            } else if (subtract) {
                for (int i = shortLen; i < ansLength; i++) {
                    res.add((R) that.coefficient(i).negated());
                }
            } else {
                for (int i = shortLen; i < ansLength; i++) {
                    res.add(that.coefficient(i));
                }
            }
        }
        return fromSeqCollecBigEndian(res);
    }
    
    @Override
    @SideEffectFree
    public Polynomial<? extends AlgebraNumber> scaled(
            AlgebraNumber scalar
    ) {
        return scalar(scalar, false, n -> n.product(scalar));
    }
    
    /**
     * Scales this by a constant.
     *
     * @param   scale   the factor by which to scale this
     *
     * @param   divide  whether scale is multiplicative or divisive (vertical stretch or compression)
     *
     * @return  a copy of this, but scaled
     */
    @SideEffectFree
    protected final <R extends AlgebraNumber> Polynomial<R> scalar(
            R scale,
            boolean divide,
            Function<? super N, R> op
    ) {
        if (scale.isZero()) {
            if (divide) {
                throw new ArithmeticException("Cannot divide by 0");
            }
            
            return buildZero();
        }
        
        if (scale.isOne()) {
            return (Polynomial<R>) this;
        }
        return unaryOp(op);
    }
    
    @Override
    @SideEffectFree
    public Polynomial<? extends AlgebraNumber> product(
            Polynomial<? extends AlgebraNumber> multiplicand
    ) {
        return mult(multiplicand);
    }
    
    /**
     * Multiplies this by that
     *
     * @param   that    the polynomial being multiplied with this
     *
     * @return  the product
     */
    @SideEffectFree
    protected final <R extends AlgebraNumber, S extends R> Polynomial<R> mult(
            Polynomial<S> that
    ) {
        if (isZero()) {
            return buildZero();
        }
        
        int thisLength = length();
        int thatLength = that.length();
        if (thatLength == 1) {
            S scalar = that.constant();
            Function<? super N, R> op = n -> (R) n.product(scalar);
            return scalar(scalar, false, op);
        }
        
        int ansLength = thisLength + thatLength - 1;
        checkLength(ansLength);
        List<R> res = new ArrayList<>(ansLength);
        res.addAll(dummyZeros(ansLength));
        for (int i = 0; i < thisLength; i++) {
            for (int j = 0; j < thatLength; j++) {
                int index =  i + j;
                R prod = (R) coefficient(i).product( that.coefficient(j) );
                R sum = (R) res.get(index).sum(prod);
                res.set(index, sum);
            }
        }
        return fromSeqCollecBigEndian(res);
    }
    
    @Override
    @SideEffectFree
    public FunctionRemainderPair<? extends Polynomial<?>, ? extends Polynomial<?>> quotientZWithRemainder(
            Polynomial<?> divisor
    ) {
        return divRouter(divisor, true);
    }
    
    @Override
    @SideEffectFree
    public Polynomial<? extends AlgebraNumber> quotientZ(
            Polynomial<? extends AlgebraNumber> divisor
    ) {
        return divRouter(divisor, false).value();
    }
    
    @Override
    @SideEffectFree
    public Polynomial<? extends AlgebraNumber> remainder(
            Polynomial<? extends AlgebraNumber> divisor
    ) {
        return divRouter(divisor, true).remainder();
    }
    
    /**
     * Determines which division function to use.
     *
     * @param   divisor    the divisor for this
     *
     * @param   wantRemainder   whether we want the remainder or not; if not, no reason to do the extra work
     *
     * @return  An array of two {@link PolyArrayList}s: the first is the quotient,
     *          and the second is the remainder, or could be {@code null} if {@code wantRemainder == false}.
     *          The quotient should not be null.
     */
    @SideEffectFree
    protected final <R extends AlgebraNumber> FunctionRemainderPair<Polynomial<R>, @Nullable Polynomial<R>> divRouter(
            Polynomial<? extends R> divisor,
            boolean wantRemainder
    ) {
        if (divisor.isZero()) {
            throw new ArithmeticException("Cannot divide by 0!");
        }
        //else
        int divisorLength = divisor.length();
        
        return switch (divisorLength) {
            case 1 -> {
                R scalar = divisor.constant();
                Polynomial<R> quotient = scalar(scalar, true, n -> (R) n.quotient(scalar));
                yield new FunctionRemainderPair<>(quotient, buildZero());
            }
            case 2 -> {
                R thatConstNeg = (R) divisor.constant().negated();
                yield divSyn(thatConstNeg, divisor.leadCoefficient());
            }
            // Now switch by which is larger
            default -> switch ( Signum.valueOf(length() - divisorLength) ) {
                case POSITIVE -> divLong(divisor, wantRemainder);
                case ZERO -> {
                    R quotient = divisor.leadCoefficient();
                    Polynomial<R> remainder = scalar(quotient, true, n -> (R) n.quotient(quotient));
                    yield new FunctionRemainderPair<>(constantOf(quotient), remainder);
                }
                case NEGATIVE -> new FunctionRemainderPair<>(buildZero(), (Polynomial<R>) this);
            };
        };
    }
    
    /**
     * Synthetic division, for when the divisor is linear.
     * This is also used for a faster way to evaluate the polynomial for a value:
     * the remainder when dividing by {@code x - r} is the result when evaluating the polynomial at {@code x == r}.
     * To make this function work for boh instances, it is expected that divConNeg is {@code r}, not {@code -r};
     * that is, it is already negated if it came from a polynomial.
     *
     * @param   divConNeg   the constant term&mdash;but negated&mdash;of the divisor for this,
     *                      OR the value for x that is being evaluated
     *
     * @param   divCoeff    the slope, or linear coefficient of the divisor
     *
     * @return an array of two PolyArrayLists; the first is the quotient, and the second is
     *         the remainder if {@code wantRemainder == true}, or {@code null} if we didn't want the remainder
     */
    @SideEffectFree
    protected final <R extends AlgebraNumber> FunctionRemainderPair<Polynomial<R>, Polynomial<R>> divSyn(
            R divConNeg,
            R divCoeff
    ) {
        int thisLength = length();
        if (thisLength == 1) {
            return new FunctionRemainderPair<>(buildZero(), (Polynomial<R>) this);
        }
        // Else, divide this by the coefficient of the divisor
        List<AlgebraNumber> thisScaled = parallelStream()
                .map(n -> n.quotient(divCoeff))
                .toList();
        // Also divide the main divisor, divCon, as well
        AlgebraNumber divisor = divConNeg.quotient(divCoeff);
        // Initialize the quotient values
        List<R> thisNotConst = slicedStreamFixedBounds(0, length(), true)
                .map(n -> (R) n)
                .toList();
        List<R> quotient = new ArrayList<>(thisLength - 1);
        quotient.addAll(thisNotConst);
        AlgebraNumber remainder = thisScaled.getLast();
        for (int i = thisLength - 3; i >= 0; i--) {
            remainder = remainder.product(divisor);
            remainder = thisNotConst.get(i).sum(remainder);
            quotient.set(i, (R) remainder);
        }
        
        remainder = remainder.product(divisor);
        remainder = remainder.sum(thisScaled.getFirst());
        
        return new FunctionRemainderPair<>(
                fromSeqCollecBigEndian(quotient),
                constantOf((R) remainder)
        );
    }
    
    /**
     * Polynomial long division
     *
     * @param   divisor the divisor
     *
     * @param   wantRemainder   whether we want the remainder
     *                          (which sometimes can be more effort than the quotient)
     *
     * @return an array of two PolyArrayLists; the first is the quotient, and the second is
     *         the remainder if {@code wantRemainder == true}, or {@code null} if we didn't want the remainder
     */
    @SideEffectFree
    protected final <R extends AlgebraNumber> FunctionRemainderPair<Polynomial<R>, @Nullable Polynomial<R>> divLong(
            Polynomial<? extends R> divisor,
            boolean wantRemainder
    ) {
        int thisLength = length();
        int thatLength = divisor.length();
        int quoCurr = thisLength - thatLength;
        int quoLength = quoCurr + 1;
        List<R> quotient = new ArrayList<>(quoLength);
        quotient.addAll(dummyZeros(quoLength));
        int remLength = thatLength - 1;
        
        for (; quoCurr >= 0; quoCurr--) {
            AlgebraNumber term = coefficient(remLength + quoCurr);
            quotient.set(quoCurr, (R) coefficient(remLength + quoCurr));
            int i = quoCurr + 1;
            int j = remLength;
            while (j > 0 && i < quoLength) {
                AlgebraNumber prod = divisor.coefficient(--j).product( quotient.get(i++) );
                term = term.difference(prod);
            }
            term = term.quotient( divisor.coefficient(remLength) );
            quotient.set(quoCurr, (R) term);
        }
        List<R> remainder = null;
        if (wantRemainder) {
            boolean first = true;
            int remCurr = remLength;
            while (remCurr > 0) {
                R remain = (R) remainBuild(divisor, quotient, quoLength, --remCurr);
                if (!remain.isZero()) {
                    remainder = new ArrayList<>(parallelStream(null, remCurr)
                            .map(n -> (R) n)
                            .toList()
                    );
                    remainder.set(remCurr, remain);
                    first = false;
                    break;
                }
            }
            if (first) {
                remainder = new ArrayList<>(1);
                remainder.add((R) FiniteInteger.valueOf(0));
            } else {
                remCurr--;
                for (; remCurr >= 0; remCurr--) {
                    AlgebraNumber remain = remainBuild(divisor, quotient, quoLength, remCurr);
                    R diff = (R) remainder.get(remCurr).difference(remain);
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
     * {@link AbstractPolynomial#divLong} happen in two parts, but this particular process is used
     * in both of them. Thus it is itself a function.
     *
     * @param   dX  the divisor
     *
     * @param   qX  the previously-found quotient
     *
     * @param   quoLength   the length of the quotient; while it is easy to find,
     *                      in the situations that this function is called, we have already found it
     *
     * @param   remCurr the current index of the term of the remainder that we are now building
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
    
    @Override
    @SideEffectFree
    public Polynomial<N> raisedUS(
            @Unsigned short exponent
    ) {
        return polyRaiser(Short.toUnsignedInt(exponent));
    }
    
    @Override
    @SideEffectFree
    public Polynomial<N> raisedZ(
            int exponent
    ) {
        if (exponent < 0) {
            if (isConstant()) {
                return constantOf((N) constant().raised(exponent));
            }
            throw new IllegalArgumentException("Cannot raise to a negative power while still being a Polynomial!");
        }
        
        return polyRaiser(exponent);
    }
    
    @Override
    @SideEffectFree
    public Polynomial<N> raisedZ(
            AlgebraInteger exponent
    ) {
        boolean exponentTooBig = exponent.compareTo(MAX_DEG_AS_AI) > 0;
        if (exponentTooBig || exponent.isNegative()) {
            if (isConstant()) {
                return constantOf((N) constant().raised(exponent));
            }
            if (exponentTooBig) {
                throw new IndexOutOfBoundsException(
                        "Answer would have degree greater than the maximum degree of" + MAX_DEGREE);
            }
            throw new IllegalArgumentException("Cannot raise to a negative power while still being a Polynomial!");
        }
        
        return polyRaiser(exponent.intValue());
    }
    
    /**
     * Raises this to a power
     *
     * @param   exponent    the power to raise this to; can't be negative
     *
     * @return  this to the power of {@code exponent}
     */
    @SideEffectFree
    protected Polynomial<N> polyRaiser(
            int exponent
    ) {
        return switch (exponent) {
            case 0 -> {
                if (isZero()) {
                    throw new ArithmeticException("0^0 is undefined!");
                }
                yield constantOf((N) FiniteInteger.valueOf(1));
            }
            case 1 -> this;
            case 2 -> squared();
            default -> {
                int newLength = exponent * (length() - 1) + 1;
                checkLength(newLength);
                
                Polynomial<N> ans = squared();
                for (int i = 3; i < exponent; i++) {
                    ans = mult(ans);
                }
                yield ans;
            }
        };
    }
    
    @Override
    @SideEffectFree
    public Spliterator<N> spliterator() {
        return MiscUtils.spliteratorOrderedSizedNonnullImmutableSubsizedIterator(iterator(), length());
    }
    
    @Override
    @SideEffectFree
    public Stream<N> stream() {
        return StreamSupport.stream(spliterator(), false);
    }
    
    @Override
    @SideEffectFree
    public Stream<N> stream(
            @Nullable Integer startInclusive,
            @Nullable Integer endExclusive
    ) {
        return slicerStream(startInclusive, endExclusive, false);
    }
    
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
    
    @Override
    @SideEffectFree
    public Stream<N> parallelStream() {
        return StreamSupport.stream(spliterator(), true);
    }
    
    @Override
    @SideEffectFree
    public Stream<N> parallelStream(
            @Nullable Integer startInclusive,
            @Nullable Integer endExclusive
    ) {
        return slicerStream(startInclusive, endExclusive, true);
    }
}
