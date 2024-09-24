package org.cb2384.exactalgebra.objects.relations.polynomial;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.SequencedCollection;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.cb2384.exactalgebra.objects.numbers.AlgebraNumber;
import org.cb2384.exactalgebra.util.corutils.Arrayz;
import org.cb2384.exactalgebra.util.corutils.ternary.ComparableSwitchSignum;
import org.cb2384.exactalgebra.util.corutils.ternary.Signum;

import org.checkerframework.checker.index.qual.*;
import org.checkerframework.checker.nullness.qual.*;
import org.checkerframework.common.value.qual.*;
import org.checkerframework.dataflow.qual.*;

/**
 * This class is an {@link ArrayList} of some {@link AlgebraNumber}.
 * The index of a term is its degree.
 * The primary reason for this class's existence if for the underlying logic of the major operations.
 * Almost all methods in {@link PolyRat} use one or more methods from this class for major parts of
 * the operation that they carry out.
 * In a sense, this is the actual Polynomial class, and the ones with the public methods are handlers to ensure
 * proper encapsulation of the values.
 * This is to prevent editing that makes sense for a normal {@link List}, but that would cause major
 * structural changes to the polynomial, like, for example List::remove, which would have
 * the side effect of reducing the effective degree of all terms after the term being removed.
 */
final class PolyArrayList<N extends AlgebraNumber>
        extends ArrayList<N>
        implements ComparableSwitchSignum<PolyArrayList<?>> {
    
    /**
     * static PolyArrayList of 0, though with an unset type
     */
    static final PolyArrayList<?> ZERO_PX
            = new PolyArrayList<>(AbstractPolynomial.ZERO);
    
    /**
     * static PolyArrayList of 1, though with an unset type
     */
    static final PolyArrayList<?> ONE_PX
            = new PolyArrayList<>(AbstractPolynomial.ONE);
    
    /**
     * Extension of {@link ArrayList#ArrayList(int)}
     *
     * @param initialCapacity the initial capacity of the list
     */
    @SideEffectFree
    PolyArrayList(
            @IntRange(from = 1, to = AbstractPolynomial.DEFAULT_MAX_LENGTH) int initialCapacity
    ) {
        super(initialCapacity);
    }
    
    /**
     * Extension equivalent of {@link ArrayList#ArrayList(Collection)}.
     *
     * @param c   the collection whose elements are to be placed into this list
     */
    @SideEffectFree
    PolyArrayList(
            SequencedCollection<? extends N> c
    ) {
        super(c);
        assert !c.isEmpty();
    }
    
    /**
     * Constructor that makes a constant PolyArrayList.
     *
     * @param constant  The constant value of the new PolyArrayList
     */
    @SideEffectFree
    PolyArrayList(
            N constant
    ) {
        super(1);
        add(constant);
    }
    
    /**
     * Constructor for a monomial PolyArrayList
     *
     * @param coefficient   the non-zero coefficient
     * @param degree        the degree of the monomial
     */
    @SideEffectFree
    PolyArrayList(
            N coefficient,
            @IntRange(from = 1, to = AbstractPolynomial.DEFAULT_MAX_DEGREE) int degree
    ) {
        super(degree + 1);
        addAll((Collection<? extends N>) AbstractPolynomial.dummyZeros(degree));
        add(coefficient);
    }
    
    @Pure
    boolean isZero() {
        return (size() == 1) && getFirst().isZero();
    }
    
    /**
     * To allow the comparing of one {@link PolyArrayList} to another,
     *  in accordance with the Interface {@link Comparable}.
     * @param that the object to be compared.
     * @return 1 if this is greater, 0 if they are the same (for all terms)
     *         and -1 if this is smaller.
     * @see PolyArrayList#compareTo for the actual implementation
     */
    @Override
    @Pure
    public int compareTo(
            PolyArrayList<?> that
    ) {
        int thisLength = size();
        int lengthDiff = thisLength - that.size();
        if (lengthDiff != 0) {
            return lengthDiff;
        }
        
        for (int i = thisLength - 1; i >= 0; i--) {
            int test = get(i).compareTo( that.get(i) );
            if (test != 0) {
                return test;
            }
        }
        //else
        return 0;
    }
    
    /**
     * Performs the given {@code unaryOp} on each coefficient of this polynomial. This is done through
     * a stream and collecting the result directly into a new PolyArrayList.
     *
     * @param op the op; should be stateless and otherwise comply with the requirements of
     *                  {@link java.util.stream.Stream#map}
     *
     * @return  a new PolyArrayList with the results of the op, in order
     */
    @SideEffectFree
    <R extends AlgebraNumber> PolyArrayList<R> unaryOp(
            Function<? super N, ? extends R> op
    ) {
        return stream()
                .map(op)
                .collect( Collectors.toCollection(() -> new PolyArrayList<>(size())) );
    }
    
    /**
     * Finds the result for an addition or subtraction operation.
     *
     * @param that      the PolyArrayList to add or subtract from this
     * @param subtract  to determine if we are subtracting or adding
     * @param op        while this could be determined from subtract, there is more flexibility leaving it as
     *                  a parameter
     *
     * @return  the sum or difference
     */
    @SideEffectFree
    <R extends AlgebraNumber> PolyArrayList<R> arithRes(
            PolyArrayList<? extends R> that,
            boolean subtract,
            BiFunction<? super N, ? super R, ? extends R> op
    ) {
        // Initialize since it will be needed in later blocks
        PolyArrayList<R> ans;
        int thisLength = size();
        int thatLength = that.size();
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
                    compResult = get(--ansLength).compareTo( that.get(ansLength) );
                } while ( (ansLength > 0) && (compResult == 0) );
            } else {
                // Else, every leading slot where the negation of one equals the other
                // must be cancelled
                do {
                    AlgebraNumber bXNeg = that.get(--ansLength).negated();
                    compResult = get(ansLength).compareTo(bXNeg);
                } while ( (ansLength > 0) && (compResult == 0) );
            }
            // If there are still slots left
            if (ansLength > 1) {
                // In either branch of the earlier if
                // cLen will come out being the leading degree, not the length
                // Incrememnt by 1 here to fix
                ans = new PolyArrayList<>(++ansLength);
                for (int i = 0; i < ansLength; i++) {
                    ans.add( op.apply(get(i), that.get(i)) );
                }
            } else {
                return (PolyArrayList<R>) ZERO_PX;
            }
        } else {
            ans = new PolyArrayList<>(ansLength);
            int shortLen = Math.min(thisLength, thatLength);
            for (int i = 0; i < shortLen; i++) {
                ans.add( op.apply(get(i), that.get(i)) );
            }
            if (ansLength > thatLength) {
                ans.addAll((Collection<? extends R>) subList(shortLen, ansLength));
            } else if (subtract) {
                for (int i = shortLen; i < ansLength; i++) {
                    ans.add((R) that.get(i).negated());
                }
            } else {
                ans.addAll( that.subList(shortLen, ansLength) );
            }
        }
        return ans;
    }
    
    /**
     * Scales the first argument by a constant (the second arg). This is static because it cannot be made virtual
     * without causing more type parameter issues.
     *
     * @param scale     the scalar
     * @param divide    whether to divide or multiply
     * @param op        while this could be determined from divide, there is more flexibility leaving it as
     *                  a parameter
     *
     * @return  the sum or difference
     *
     * @throws ArithmeticException  if scale is 0 and dividing
     */
    @SideEffectFree
    <R extends AlgebraNumber> PolyArrayList<R> scalar(
            R scale,
            boolean divide,
            BiFunction<? super N, R, R> op
    ) {
        if (scale.isZero()) {
            if (divide) {
                throw new ArithmeticException("Cannot divide by 0");
            }
            return (PolyArrayList<R>) ZERO_PX;
        }
        
        if (scale.isOne()) {
            return (PolyArrayList<R>) this;
        }
        return unaryOp(n -> op.apply(n, scale));
    }
    
    /**
     * Multiplies this by multiplicand
     *
     * @param multiplicand    the polynomial being multiplied with this
     *
     * @return  the product
     */
    @SideEffectFree
    <R extends AlgebraNumber> PolyArrayList<R> mult(
            PolyArrayList<? extends R> multiplicand,
            BiFunction<? super N, R, R> multiplier,
            BinaryOperator<R> adder
    ) {
        int thisLength = size();
        if (thisLength == 1) {
            return multiplicand.scalar((R) getFirst(), false, (BinaryOperator<R>) multiplier);
        }
        
        int thatLength = multiplicand.size();
        if (thatLength == 1) {
            return scalar(multiplicand.getFirst(), false, multiplier);
        }
        
        int ansLength = thisLength + thatLength - 1;
        AbstractPolynomial.checkLength(ansLength);
        PolyArrayList<R> ans = new PolyArrayList<>((List<? extends R>) AbstractPolynomial.dummyZeros(ansLength));
        for (int i = 0; i < thisLength; i++) {
            for (int j = 0; j < thatLength; j++) {
                int index =  i + j;
                R prod = multiplier.apply(get(i), multiplicand.get(j));
                R sum = adder.apply(ans.get(index), prod);
                ans.set(index, sum);
            }
        }
        return ans;
    }
    
    @SideEffectFree
    private static <N extends AlgebraNumber> @PolyNull PolyArrayList<N>@ArrayLen(2)[] arrayPair(
            @PolyNull PolyArrayList<N> answer,
            @PolyNull PolyArrayList<N> remainder
    ) {
        PolyArrayList<N>[] result = Arrayz.checkedArrayGenerator(2, PolyArrayList.class);
        result[0] = answer;
        result[1] = remainder;
        return result;
    }
    
    /**
     * Determines which division function to use.
     *
     * @param divisor    the divisor for this
     *
     * @param wantRemainder   whether we want the remainder or not; if not, no reason to do the extra work
     *
     * @return  An array of two {@link PolyArrayList}s: the first is the quotient,
     *          and the second is the remainder, or could be {@code null} if {@code wantRemainder == false}.
     *          The quotient should not be null.
     */
    @SideEffectFree
    <R extends AlgebraNumber> @Nullable PolyArrayList<R>@ArrayLen(2)[] divRouter(
            PolyArrayList<? extends R> divisor,
            boolean wantRemainder,
            BinaryOperator<R> adder,
            BinaryOperator<R> subtracter,
            BinaryOperator<R> multiplier,
            BinaryOperator<R> divider
    ) {
        if (divisor.isZero()) {
            throw new ArithmeticException("Cannot divide by 0!");
        }
        //else
        int divisorLength = divisor.size();
        
        return switch (divisorLength) {
            case 1 -> {
                    PolyArrayList<R> quotient = scalar(divisor.getFirst(), true,
                            (BiFunction<? super N, R, R>) divider);
                    yield new PolyArrayList[]{quotient, ZERO_PX};
            }
            case 2 -> {
                R thatConstNeg = (R) divisor.getFirst().negated();
                yield divSyn(thatConstNeg, divisor.getLast(), wantRemainder, adder, multiplier, divider);
            }
            // Now switch by which is larger
            default -> switch ( Signum.valueOf(size() - divisorLength) ) {
                case POSITIVE -> divLong(divisor, wantRemainder, adder, subtracter, multiplier, divider);
                case ZERO -> {
                    R quotient = divisor.getLast();
                    PolyArrayList<R> remainder = scalar(quotient, true, (BiFunction<? super N, R, R>) divider);
                    yield arrayPair(new PolyArrayList<>(quotient), remainder);
                }
                case NEGATIVE -> arrayPair((PolyArrayList<R>) ZERO_PX, (PolyArrayList<R>) this);
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
     * @param divConNeg   the constant term&mdash;but negated&mdash;of the divisor for this,
     *                      OR the value for x that is being evaluated
     *
     * @param divCoeff    the slope, or linear coefficient of the divisor
     *
     * @return an array of two PolyArrayLists; the first is the quotient, and the second is
     *         the remainder if {@code wantRemainder == true}, or {@code null} if we didn't want the remainder
     */
    @SideEffectFree
    <R extends AlgebraNumber> PolyArrayList<R>@ArrayLen(2)[] divSyn(
            R divConNeg,
            R divCoeff,
            boolean wantRemainder,
            BinaryOperator<R> adder,
            BinaryOperator<R> multiplier,
            BinaryOperator<R> divider
    ) {
        int thisLength = size();
        if (thisLength == 1) {
            return arrayPair((PolyArrayList<R>) ZERO_PX, (PolyArrayList<R>) this);
        }
        // Else, divide this by the coefficient of the divisor
        PolyArrayList<R> thisScaled = scalar(divCoeff, true, (BiFunction<? super N, R, R>) divider);
        // Also divide the main divisor, divCon, as well
        R divisor = divider.apply(divConNeg, divCoeff);
        // Initialize the quotient values
        List<R> thisNotConst = thisScaled.subList(1, thisLength);
        PolyArrayList<R> quotient = new PolyArrayList<>(thisNotConst);
        R remainder = thisScaled.getLast();
        for (int i = thisLength - 3; i >= 0; i--) {
            remainder = multiplier.apply(remainder, divisor);
            remainder = adder.apply(remainder, thisNotConst.get(i));
            quotient.set(i, remainder);
        }
        if (wantRemainder) {
            remainder = multiplier.apply(remainder, divisor);
            remainder = adder.apply(remainder, thisScaled.getFirst());
            
            return arrayPair(quotient, new PolyArrayList<>(remainder));
        }
        return arrayPair(quotient, null);
    }
    
    /**
     * Polynomial long division
     *
     * @param divisor the divisor
     *
     * @param wantRemainder   whether we want the remainder
     *                          (which sometimes can be more effort than the quotient)
     *
     * @return an array of two PolyArrayLists; the first is the quotient, and the second is
     *         the remainder if {@code wantRemainder == true}, or {@code null} if we didn't want the remainder
     */
    @SideEffectFree
    private <R extends AlgebraNumber> @Nullable PolyArrayList<R>@ArrayLen(2)[] divLong(
            PolyArrayList<? extends R> divisor,
            boolean wantRemainder,
            BinaryOperator<R> adder,
            BinaryOperator<R> subtracter,
            BinaryOperator<R> multiplier,
            BinaryOperator<R> divider
    ) {
        int thisLength = size();
        int thatLength = divisor.size();
        int quoCurr = thisLength - thatLength;
        int quoLength = quoCurr + 1;
        PolyArrayList<R> quotient = new PolyArrayList<>(
                (List<? extends R>) AbstractPolynomial.dummyZeros(quoLength));
        int remLength = thatLength - 1;
        
        for (; quoCurr >= 0; quoCurr--) {
            R term = (R) get(remLength + quoCurr);
            quotient.set(quoCurr, (R) get(remLength + quoCurr));
            int i = quoCurr + 1;
            int j = remLength;
            while (j > 0 && i < quoLength) {
                R prod = multiplier.apply(divisor.get(--j), quotient.get(i++));
                term = subtracter.apply(term, prod);
            }
            term = divider.apply(term, divisor.get(remLength));
            quotient.set(quoCurr, term);
        }
        PolyArrayList<R> remainder = null;
        if (wantRemainder) {
            boolean first = true;
            int remCurr = remLength;
            while (remCurr > 0) {
                R remain = remainBuild(divisor, quotient, quoLength, --remCurr, adder, multiplier);
                if (!remain.isZero()) {
                    remainder = new PolyArrayList<>((List<? extends R>) subList(0, remCurr));
                    remainder.set(remCurr, remain);
                    first = false;
                    break;
                }
            }
            if (first) {
                remainder = (PolyArrayList<R>) ZERO_PX;
            } else {
                remCurr--;
                for (; remCurr >= 0; remCurr--) {
                    R remain = remainBuild(divisor, quotient, quoLength, remCurr, adder, multiplier);
                    R diff = subtracter.apply(remainder.get(remCurr), remain);
                    remainder.set(remCurr, diff);
                }
            }
        }
        return arrayPair(quotient, remainder);
    }
    
    /**
     * Gets a term of the remainder. The remainder calculations for
     * {@link PolyArrayList#divLong} happen in two parts, but this particular process is used
     * in both of them. Thus it is itself a function.
     *
     * @param dX  the divisor
     *
     * @param qX  the previously-found quotient
     *
     * @param quoLength   the length of the quotient; while it is easy to find,
     *                      in the situations that this function is called, we have already found it
     *
     * @param remCurr the current index of the term of the remainder that we are now building
     *
     * @return  the term of the remainder for index {@code remCurr}
     */
    @SideEffectFree
    private <R extends AlgebraNumber> R remainBuild(
            PolyArrayList<? extends R> dX,
            PolyArrayList<R> qX,
            int quoLength,
            int remCurr,
            BinaryOperator<R> adder,
            BinaryOperator<R> multiplier
    ) {
        R remain = (R) get(remCurr);
        int j = 0;
        for (int i = remCurr; i > 0; j++) {
            if (quoLength > i--) {
                R prod = multiplier.apply(dX.get(j), qX.get(i));
                remain = adder.apply(remain, prod);
            }
        }
        return remain;
    }
    
    @SideEffectFree
    PolyArrayList<N> squared(
            BinaryOperator<N> multiplier,
            BinaryOperator<N> adder
    ) {
        return mult(this, multiplier, adder);
    }
    
    /**
     * Raises this to a power
     *
     * @param exponent    the power to raise this to; can't be negative
     *
     * @return  this to the power of {@code exponent}
     */
    @SideEffectFree
    PolyArrayList<N> raised(
            @NonNegative int exponent,
            BinaryOperator<N> multiplier,
            BinaryOperator<N> adder
    ) {
        return switch (exponent) {
            case 0 -> {
                if (isZero()) {
                    throw new ArithmeticException("0^0 is undefined!");
                }
                yield (PolyArrayList<N>) ONE_PX;
            }
            case 1 -> this;
            case 2 -> squared(multiplier, adder);
            default -> {
                int newLength = exponent * (size() - 1) + 1;
                AbstractPolynomial.checkLength(newLength);
                
                PolyArrayList<N> ans = squared(multiplier, adder);
                for (int i = 3; i < exponent; i++) {
                    ans = mult(ans, multiplier, adder);
                }
                yield ans;
            }
        };
    }
}
