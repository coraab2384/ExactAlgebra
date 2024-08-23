package org.cb2384.exactalgebra.objects.relations.polynomial;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.SequencedCollection;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.cb2384.corutils.Arrayz;
import org.cb2384.corutils.ternary.ComparableSwitchSignum;
import org.cb2384.corutils.ternary.Signum;
import org.cb2384.exactalgebra.objects.numbers.AlgebraNumber;

import org.checkerframework.checker.index.qual.*;
import org.checkerframework.checker.nullness.qual.*;
import org.checkerframework.common.value.qual.*;
import org.checkerframework.dataflow.qual.*;

/**
 * This class is an {@link ArrayList} of some {@link AlgebraNumber}.
 * The index of a term is its degree.
 * The primary reason for this class's existence if for the underlying logic of the major operations.
 * Almost all methods in {@link PolynomBase} use one or more methods from this class for major parts of
 * the operation that they carry out.
 * In a sense, this is the actual Polynomial class, and the ones with the public methods are handlers to ensure
 * proper encapsulation of the values.
 * This is to prevent editing that makes sense for a normal {@link List}, but that would cause major
 * structural changes to the polynomial, like, for example List::remove, which would have
 * the side effect of reducing the effective degree of all terms after the term being removed.
 */
final class PolyArrayList<N extends AlgebraNumber>
        extends ArrayList<N>
        implements ComparableSwitchSignum<PolyArrayList<? extends AlgebraNumber>> {
    
    /**
     * Extension of {@link ArrayList#ArrayList(int)}
     *
     * @param   initialCapacity the initial capacity of the list
     */
    @SideEffectFree
    PolyArrayList(
            @IntRange(from = 1, to = AbstractPolynomial.MAX_LENGTH) int initialCapacity
    ) {
        super(initialCapacity);
    }
    
    /**
     * Extension equivalent of {@link ArrayList#ArrayList(Collection)}
     *
     * @param   c   the collection whose elements are to be placed into this list
     */
    @SideEffectFree
    PolyArrayList(
            SequencedCollection<? extends N> c
    ) {
        this(c.size());
        addAll(c);
    }
    
    @SideEffectFree
    PolyArrayList(
            N constant
    ) {
        super(1);
        add(constant);
    }
    
    /**
     * Static factory for constant polynomials (degree == 0)
     * @param val the value to give the constant term
     * @return the constructed {@link PolyArrayList} monomial
     */
    @SideEffectFree
    static <N extends AlgebraNumber> PolyArrayList<N> constant(
            N val
    ) {
        PolyArrayList<N> ans = new PolyArrayList<>(1);
        ans.add(val);
        return ans;
    }
    
    /**
     * Static factory for monomials of one term
     * @param coefficient the value to give the non-zero term.
     * @return the constructed {@link PolyArrayList} monomial
     */
    @SideEffectFree
    static <N extends AlgebraNumber> PolyArrayList<N> monomial(
            N coefficient,
            @IntRange(from = 1, to = AbstractPolynomial.MAX_DEGREE) int degree
    ) {
        PolyArrayList<N> ans = new PolyArrayList<>(degree + 1);
        ans.addAll( AbstractPolynomial.dummyZeros(degree) );
        ans.add(coefficient);
        return ans;
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
            PolyArrayList<? extends AlgebraNumber> that
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
     * @param   unaryOp the op; should be stateless and otherwise comply with the requirements of
     *                  {@link java.util.stream.Stream#map}
     *
     * @return  a new PolyArrayList with the results of the op, in order
     */
    @SideEffectFree
    PolyArrayList<N> unaryOp(
            Function<N, N> unaryOp
    ) {
        int size = size();
        return stream()
                .map(unaryOp)
                .collect( Collectors.toCollection(() -> new PolyArrayList<>(size)) );
    }
    
    /**
     * Finds the result for an addition or subtraction operation
     *
     * @param   that    the PolyArrayList to add or subtract from this
     *
     * @param   subtract    to determine if we are subtracting or adding
     *
     * @return  the sum or difference
     */
    @SideEffectFree
    PolyArrayList<N> arithRes(
            PolyArrayList<? extends N> that,
            boolean subtract,
            PolyArrayList<N> zero
    ) {
        // Initialize since it will be needed in later blocks
        PolyArrayList<N> ans;
        BiFunction<N, N, N> op = subtract ?
                (x, y) -> (N) x.difference(y) :
                (x, y) -> (N) x.sum(y);
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
                    N arithRes = op.apply(get(i), that.get(i));
                    ans.add(arithRes);
                }
            } else {
                return zero;
            }
        } else {
            ans = new PolyArrayList<>(ansLength);
            int shortLen = Math.min(thisLength, thatLength);
            for (int i = 0; i < shortLen; i++) {
                N arithRes = op.apply(get(i), that.get(i));
                ans.add(arithRes);
            }
            if (ansLength > thatLength) {
                List<N> aXRem = subList(shortLen, ansLength);
                ans.addAll(aXRem);
            } else if (subtract) {
                for (int i = shortLen; i < ansLength; i++) {
                    N bXNeg = (N) that.get(i).negated();
                    ans.add(bXNeg);
                }
            } else {
                List<? extends N> bXRem = that.subList(shortLen, ansLength);
                ans.addAll(bXRem);
            }
        }
        return ans;
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
    PolyArrayList<N> scalar(
            N scale,
            boolean divide,
            PolyArrayList<N> zero
    ) {
        if (scale.isZero()) {
            if (divide) {
                throw new ArithmeticException("Cannot divide by 0");
            }
            
            return zero;
        }
        
        if (scale.isOne()) {
            return this;
        }
        
        Function<N, N> op = divide ?
                x -> (N) x.quotient(scale) :
                x -> (N) x.product(scale);
        return unaryOp(op);
    }
    
    /**
     * Multiplies this by multiplicand
     *
     * @param   multiplicand    the polynomial being multiplied with this
     *
     * @return  the product
     */
    @SideEffectFree
    PolyArrayList<N> mult(
            PolyArrayList<? extends N> multiplicand,
            PolyArrayList<N> zero
    ) throws ArithmeticException {
        int thisLength = size();
        if (thisLength == 1) {
            return ((PolyArrayList<N>) multiplicand).scalar(getFirst(), false, zero);
        }
        
        int thatLength = multiplicand.size();
        if (thatLength == 1) {
            return scalar(multiplicand.getFirst(), false, zero);
        }
        
        int ansLength = thisLength + thatLength - 1;
        AbstractPolynomial.checkLength(ansLength);
        PolyArrayList<N> ans = new PolyArrayList<>( AbstractPolynomial.dummyZeros(ansLength) );
        for (int i = 0; i < thisLength; i++) {
            for (int j = 0; j < thatLength; j++) {
                int index =  i + j;
                N prod = (N) get(i).product( multiplicand.get(j) );
                N sum = (N) ans.get(index).sum(prod);
                ans.set(index, sum);
            }
        }
        return ans;
    }
    
    /**
     * Wraps the two into an UNCHECKED array
     *
     * @param   first   the first member of the array
     *
     * @param   second  the second member of the array
     *
     * @return  a recast {@code Object[2]}
     *
     * @param   <N> the type of the coefficient of the polynomial of the resulting array
     */
    @SideEffectFree @SuppressWarnings({"deprecation", "unchecked"})
    private static <N extends AlgebraNumber> @PolyNull PolyArrayList<N>@ArrayLen(2)[] arrayPair(
             @PolyNull PolyArrayList<? extends N> first,
             @PolyNull PolyArrayList<? extends N> second
    ) {
        // Since this array will never be exposed, an unchecked array is acceptable
        PolyArrayList<N>[] res = Arrayz.unCheckedArrayGenerator(2);
        res[0] = (PolyArrayList<N>) first;
        res[1] = (PolyArrayList<N>) second;
        return res;
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
    @Nullable PolyArrayList<N>@ArrayLen(2)[] divRouter(
            PolyArrayList<? extends N> divisor,
            boolean wantRemainder,
            PolyArrayList<N> zero
    ) {
        if (divisor.isZero()) {
            throw new ArithmeticException("Cannot divide by 0!");
        }
        //else
        int divisorLength = divisor.size();
        
        return switch (divisorLength) {
            case 1 -> {
                    PolyArrayList<N> quotient = scalar(divisor.getFirst(), true, zero);
                    yield arrayPair(quotient, zero);
            }
            case 2 -> {
                N thatConstNeg = (N) divisor.getFirst().negated();
                yield divSyn(thatConstNeg, divisor.getLast(), wantRemainder, zero);
            }
            // Now switch by which is larger
            default -> switch ( Signum.valueOf(size() - divisorLength) ) {
                case POSITIVE -> divLong(divisor, wantRemainder, zero);
                case ZERO -> {
                    N quotient = divisor.getLast();
                    PolyArrayList<N> remainder = scalar(quotient, true, zero);
                    yield arrayPair(constant(quotient), remainder);
                }
                case NEGATIVE -> arrayPair(zero, this);
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
    PolyArrayList<N>@ArrayLen(2)[] divSyn(
            N divConNeg,
            N divCoeff,
            boolean wantRemainder,
            PolyArrayList<N> zero
    ) {
        int thisLength = size();
        if (thisLength == 1) {
            return arrayPair(zero, this);
        }
        // Else, divide this by the coefficient of the divisor
        PolyArrayList<? extends N> thisScaled = scalar(divCoeff, true, zero);
        // Also divide the main divisor, divCon, as well
        N divisor = (N) divConNeg.quotient(divCoeff);
        // Initialize the quotient values
        List<? extends N> thisNotConst = thisScaled.subList(1, thisLength);
        PolyArrayList<N> quotient = new PolyArrayList<>(thisNotConst);
        N remainder = thisScaled.getLast();
        for (int i = thisLength - 3; i >= 0; i--) {
            remainder = (N) remainder.product(divisor);
            remainder = (N) thisNotConst.get(i).sum(remainder);
            quotient.set(i, remainder);
        }
        if (wantRemainder) {
            remainder = (N) remainder.product(divisor);
            remainder = (N) thisScaled.getFirst().sum(remainder);
            
            return arrayPair(quotient, constant(remainder));
        }
        return arrayPair(quotient, null);
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
    private @Nullable PolyArrayList<N>@ArrayLen(2)[] divLong(
            PolyArrayList<? extends N> divisor,
            boolean wantRemainder,
            PolyArrayList<N> zero
    ) {
        int thisLength = size();
        int thatLength = divisor.size();
        int quoCurr = thisLength - thatLength;
        int quoLength = quoCurr + 1;
        PolyArrayList<N> quotient = new PolyArrayList<>( AbstractPolynomial.dummyZeros(quoLength) );
        int remLength = thatLength - 1;
        
        for (; quoCurr >= 0; quoCurr--) {
            N term = get(remLength + quoCurr);
            quotient.set(quoCurr, get(remLength + quoCurr));
            int i = quoCurr + 1;
            int j = remLength;
            while (j > 0 && i < quoLength) {
                N prod = (N) divisor.get(--j).product( quotient.get(i++) );
                term = (N) term.difference(prod);
            }
            term = (N) term.quotient( divisor.get(remLength) );
            quotient.set(quoCurr, term);
        }
        PolyArrayList<N> remainder = null;
        if (wantRemainder) {
            boolean first = true;
            int remCurr = remLength;
            while (remCurr > 0) {
                N remain = remainBuild(divisor, quotient, quoLength, --remCurr);
                if (!remain.isZero()) {
                    remainder = new PolyArrayList<>( subList(0, remCurr) );
                    remainder.set(remCurr, remain);
                    first = false;
                    break;
                }
            }
            if (first) {
                remainder = zero;
            } else {
                remCurr--;
                for (; remCurr >= 0; remCurr--) {
                    N remain = remainBuild(divisor, quotient, quoLength, remCurr);
                    N diff = (N) remainder.get(remCurr).difference(remain);
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
    private N remainBuild(
            PolyArrayList<? extends N> dX,
            PolyArrayList<? extends N> qX,
            int quoLength,
            int remCurr
    ) {
        N remain = get(remCurr);
        int j = 0;
        for (int i = remCurr; i > 0; j++) {
            if (quoLength > i--) {
                N prod = (N) dX.get(j).product(qX.get(i));
                remain = (N) remain.sum(prod);
            }
        }
        return remain;
    }
    
    @SideEffectFree
    PolyArrayList<N> squared(
            PolyArrayList<N> zero
    ) {
        return mult(this, zero);
    }
    
    /**
     * Raises this to a power
     *
     * @param   exponent    the power to raise this to; can't be negative
     *
     * @return  this to the power of {@code exponent}
     */
    @SideEffectFree
    PolyArrayList<N> raised(
            @NonNegative int exponent,
            PolyArrayList<N> one,
            PolyArrayList<N> zero
    ) {
        return switch (exponent) {
            case 0 -> {
                if (isZero()) {
                    throw new ArithmeticException("0^0 is undefined!");
                }
                yield one;
            }
            case 1 -> this;
            case 2 -> squared(zero);
            default -> {
                int newLength = exponent * (size() - 1) + 1;
                AbstractPolynomial.checkLength(newLength);
                
                PolyArrayList<N> ans = squared(zero);
                for (int i = 3; i < exponent; i++) {
                    ans = mult(ans, zero);
                }
                yield ans;
            }
        };
    }
}
