package org.cb2384.exactalgebra.objects.relations.polynomial;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Queue;
import java.util.SequencedCollection;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.cb2384.exactalgebra.objects.numbers.AlgebraNumber;
import org.cb2384.exactalgebra.objects.numbers.integral.AlgebraInteger;
import org.cb2384.exactalgebra.objects.numbers.rational.Rational;
import org.cb2384.exactalgebra.objects.numbers.rational.RationalFactory;
import org.cb2384.exactalgebra.objects.pair.FunctionRemainderPair;
import org.cb2384.exactalgebra.util.BigMathObjectUtils;
import org.cb2384.exactalgebra.util.MiscUtils;
import org.cb2384.exactalgebra.util.corutils.NullnessUtils;

import org.checkerframework.checker.index.qual.*;
import org.checkerframework.checker.nullness.qual.*;
import org.checkerframework.checker.signedness.qual.*;
import org.checkerframework.common.value.qual.*;
import org.checkerframework.dataflow.qual.*;

public final class PolyRat
        extends AbstractPolynomial<Rational>
        implements Serializable {
    @Serial
    private static final long serialVersionUID = 0x83A0B98FB73A1A9L;
    
    private static final PolyArrayList<Rational> ZERO_PX = (PolyArrayList<Rational>) AbstractPolynomial.ZERO_PX;
    
    public static final PolyRat ZERO = new PolyRat(ZERO_PX);
    
    private static final PolyArrayList<Rational> ONE_PX = (PolyArrayList<Rational>) AbstractPolynomial.ONE_PX;
    
    public static final PolyRat ONE = new PolyRat(ONE_PX);
    
    private static final Rational ONE_RAT = ONE_PX.getFirst();
    
    private static final PolyRat ONE_X = new PolyRat(PolyArrayList.monomial(ONE_RAT, 1));
    
    private final PolyArrayList<Rational> pX;
    
    /**
     *
     * @param length
     * @param inputToCopy
     */
    private PolyRat(
            int length,
            SequencedCollection<? extends Rational> inputToCopy
    ) {
        assert length == inputToCopy.size();
        checkLength(length);
        pX = new PolyArrayList<>(length);
        pX.addAll(inputToCopy);
    }
    
    /**
     * A constructor for internal use.
     * This constructor is only for situations where the input does not need to be copied;
     *  that is, where it should not afterwards be edited by any function other than one called on this.
     * @param pX a {@link PolyArrayList} that will be directly given to this object, not copied.
     */
    private PolyRat(
           PolyArrayList<Rational> pX
    ) {
        assert pX.size() <= MAX_LENGTH;
        this.pX = pX;
    }
    
    /**
     * Static factory for constant polynomials (degree == 0)
     * @param constant the constant value
     * @return the constant
     * @throws NullPointerException if val is null
     */
    public static PolyRat constant(
            Rational constant
    ) {
        if (constant.isZero()) {
            return ZERO;
        }
        if (constant.isOne()) {
            return ONE;
        }
        return new PolyRat(PolyArrayList.constant(constant));
    }
    
    /**
     * Static factory for monomials, polynomials with one term, though that term could have any (allowed) degree
     * @param coefficient The coefficient of the monomial.
     * @param degree The degree to give the term val; the degree of the polynomial itself.
     *               This degree is an unsigned short.
     * @return The constructed monomial
     * @throws NullPointerException if val is null
     */
    @SideEffectFree
    public static PolyRat monomialUS(
            Rational coefficient,
            @Unsigned short degree
    ) {
        return monomialFactory(coefficient, Short.toUnsignedInt(degree));
    }
    
    /**
     * Static factory for monomials, polynomials with one term, though that term could have any (allowed) degree
     * @param coefficient The coefficient of the monomial.
     * @param degree The degree to give the term val; the degree of the polynomial itself.
     * @return The constructed monomial
     * @throws NullPointerException if val is null
     * @throws IndexOutOfBoundsException if degree is invalid.
     */
    @SideEffectFree
    public static PolyRat monomial(
            Rational coefficient,
            int degree
    ) {
        checkDeg(degree);
        return monomialFactory(coefficient, degree);
    }
    
    @SideEffectFree
    private static PolyRat monomialFactory(
            Rational coefficient,
            int degree
    ) {
        if ((degree == 0) || coefficient.isZero()) {
            return constant(coefficient);
        }
        if ((degree == 1) && coefficient.isOne()) {
            return ONE_X;
        }
        
        return new PolyRat( PolyArrayList.monomial(coefficient, degree) );
    }
    
    @SideEffectFree
    public static PolyRat fromOrderedLittleEndianCollection(
            SequencedCollection<? extends Rational> sequencedCollection
    ) {
        return fromOrderedBigEndianCollection(sequencedCollection.reversed());
    }
    
    @SideEffectFree
    public static PolyRat fromOrderedBigEndianCollection(
            SequencedCollection<? extends Rational> sequencedCollection
    ) {
        NullnessUtils.checkNullCollec(sequencedCollection);
        return new PolyRat(sequencedCollection.size(), sequencedCollection);
    }
    
    @SideEffectFree
    public static PolyRat fromLittleEndianArray(
            Rational... array
    ) {
        NullnessUtils.checkNullArray(array);
        return new PolyRat(array.length, Arrays.asList(array).reversed());
    }
    
    @SideEffectFree
    public static PolyRat fromBigEndianArray(
            Rational... array
    ) {
        NullnessUtils.checkNullArray(array);
        return new PolyRat(array.length, Arrays.asList(array));
    }
    
    @Override
    @SideEffectFree
    protected <R extends AlgebraNumber> Polynomial<R> constantOf(
            R constant
    ) {
        return (Polynomial<R>) constant((Rational) constant);
    }
    
    @Override
    @SideEffectFree
    protected <R extends AlgebraNumber> Polynomial<R> monomialOf(
            R coefficient,
            @IntRange(from = 0, to = MAX_DEGREE) int degree
    ) {
        return (Polynomial<R>) monomialFactory((Rational) coefficient, degree);
    }
    
    @Override
    @SideEffectFree
    protected <R extends AlgebraNumber> Polynomial<R> fromSeqCollecBigEndian(
            SequencedCollection<R> sequencedCollection
    ) {
        return (Polynomial<R>) new PolyRat(
                sequencedCollection.size(),
                (SequencedCollection<? extends Rational>) sequencedCollection
        );
    }
    
    @Override
    @Pure
    public Class<Rational> coefficientHighestRank() {
        return Rational.class;
    }
    
    @Override
    @Pure
    public @Positive int length() {
        return pX.size();
    }
    
    @Override
    @Pure
    public Rational constant() {
        return pX.getFirst();
    }
    
    /**
     * Gets the coefficient of the leading term.
     * @return the coefficient of the leading term, which is the value with the highest degree/index.
     */
    @Override
    @Pure
    public Rational leadCoefficient() {
        return pX.getLast();
    }
    
    @Override
    @Pure
    public Rational coefficient(
            int degree
    ) {
        return pX.get(degree);
    }
    
    @Override
    @Pure
    public boolean isZero() {
        return pX.isZero();
    }
    
    @Override
    @Pure
    public boolean isMonomial() {
        int length = pX.size();
        return (length == 1) || pX.subList(0, length - 1)
                .parallelStream()
                .allMatch(Rational::isZero);
    }
    
    @SideEffectFree
    private static Polynomial<?> biPolyOp(
            Polynomial<? extends AlgebraNumber> that,
            Function<PolyRat, PolyRat> smallOp,
            Function<Polynomial<? extends AlgebraNumber>, ? extends Polynomial<? extends AlgebraNumber>> bigOp
    ) {
        return (that instanceof PolyRat thatPR)
                ? smallOp.apply(thatPR)
                : bigOp.apply(that);
    }
    
    @Override
    @Pure
    public int compareTo(
            Polynomial<?> that
    ) {
        if (that instanceof PolyRat TPR) {
            return pX.compareTo(TPR.pX);
        }
        int thatLen = that.length();
        PolyArrayList<AlgebraNumber> thatPX = new PolyArrayList<>(thatLen);
        for (AlgebraNumber algebraNumber : that) {
            thatPX.add(algebraNumber);
        }
        return pX.compareTo(thatPX);
    }
    
    @Override
    @Pure
    public boolean equals(
            @Nullable Object obj
    ) {
        return switch (obj) {
            case PolyRat oPR -> equiv(oPR);
            case Polynomial<?> oPoly -> (hashCode() == oPoly.hashCode()) && equiv(oPoly);
            case null, default -> false;
        };
    }
    
    @Override
    @SideEffectFree
    public PolyRat negated() {
        if (isZero()) {
            return this;
        }
        return new PolyRat(pX.unaryOp(Rational::negated));
    }
    
    @Override
    @SideEffectFree
    public PolyRat absoluteValue() {
        if (isZero() || isOne() || pX.parallelStream().noneMatch(Rational::isNegative) ) {
            return this;
        }
        return new PolyRat(pX.unaryOp(Rational::magnitude));
    }
    
    @Override
    @SideEffectFree
    public Rational apply(
            Rational input
    ) {
        PolyArrayList<Rational>[] quoRemain = pX.divSyn(input, ONE_RAT, true, ZERO_PX);
        return quoRemain[1].getFirst();
    }
    
    @Override
    @SideEffectFree
    public AlgebraNumber evaluate(
            AlgebraNumber input
    ) {
        return (input instanceof Rational inputR)
                ? apply(inputR)
                : super.evaluate(input);
    }
    
    @SideEffectFree
    public PolyRat max(
            PolyRat that
    ) {
        return compareTo(that) < 0
                ? that
                : this;
    }
    
    @SideEffectFree
    public PolyRat min(
            PolyRat that
    ) {
        return compareTo(that) < 0
                ? this
                : that;
    }
    
    @SideEffectFree
    public PolyRat sum(
            PolyRat augend
    ) {
        return new PolyRat( pX.arithRes(augend.pX, false, ZERO_PX) );
    }
    
    @Override
    @SideEffectFree
    public Polynomial<?> sum(
            Polynomial<?> augend
    ) {
        return biPolyOp(augend, this::sum, super::sum);
    }
    
    @SideEffectFree
    public PolyRat difference(
            PolyRat subtrahend
    ) {
        return new PolyRat( pX.arithRes(subtrahend.pX, true, ZERO_PX) );
    }
    
    @Override
    @SideEffectFree
    public Polynomial<?> difference(
            Polynomial<?> subtrahend
    ) {
        return biPolyOp(subtrahend, this::difference, super::difference);
    }
    
    @SideEffectFree
    public PolyRat scaled(
            Rational scalar
    ) {
        return new PolyRat( pX.scalar(scalar, false, ZERO_PX) );
    }
    
    @Override
    @SideEffectFree
    public Polynomial<?> scaled(
            AlgebraNumber scalar
    ) {
        return (scalar instanceof Rational scalaR)
                ? scaled(scalaR)
                : super.scaled(scalar);
    }
    
    @SideEffectFree
    public PolyRat product(
            PolyRat multiplicand
    ) {
        return new PolyRat( pX.mult(multiplicand.pX, ZERO_PX) );
    }
    
    @Override
    @SideEffectFree
    public Polynomial<?> product(
            Polynomial<?> multiplicand
    ) {
        return biPolyOp(multiplicand, this::product, super::product);
    }
    
    @SideEffectFree
    public FunctionRemainderPair<? extends PolyRat, ? extends PolyRat> quotientZWithRemainder(
            PolyRat divisor
    ) {
        PolyArrayList<Rational>[] polyArray = pX.divRouter(divisor.pX, true, ZERO_PX);
        return new FunctionRemainderPair<>(
                new PolyRat(polyArray[0]),
                new PolyRat(polyArray[1])
        );
    }
    
    @Override
    @SideEffectFree
    public FunctionRemainderPair<? extends Polynomial<?>, ? extends Polynomial<?>> quotientZWithRemainder(
            Polynomial<?> divisor
    ) {
        return (divisor instanceof PolyRat divisorPR)
                ? quotientZWithRemainder(divisorPR)
                : super.quotientZWithRemainder(divisor);
    }
    
    @SideEffectFree
    public PolyRat quotientZ(
            PolyRat divisor
    ) {
        return new PolyRat( pX.divRouter(divisor.pX, false, ZERO_PX)[0] );
    }
    
    @Override
    @SideEffectFree
    public Polynomial<?> quotientZ(
            Polynomial<?> divisor
    ) {
        return biPolyOp(divisor, this::quotientZ, super::quotientZ);
    }
    
    @SideEffectFree
    public PolyRat remainder(
            PolyRat divisor
    ) {
        return new PolyRat( pX.divRouter(divisor.pX, true, ZERO_PX)[1] );
    }
    
    @Override
    @SideEffectFree
    public Polynomial<?> remainder(
            Polynomial<?> divisor
    ) {
        return biPolyOp(divisor, this::remainder, super::remainder);
    }
    
    /**
     * Divide this by another polynomial
     * @param divisor the polynomial to divide by
     * @param wantRemainder determines if the remainder is wanted
     * @return an array containing the quotient first, then the remainder.
     */
    @SideEffectFree
    private FunctionRemainderPair<PolyRat, @Nullable PolyRat> divNexus(
            PolyRat divisor,
            boolean wantRemainder
    ) {
        PolyArrayList<Rational>[] polyArray = pX.divRouter(divisor.pX, wantRemainder, ZERO_PX);
        PolyRat quotient = new PolyRat(polyArray[0]);
        PolyRat remainder = wantRemainder ? new PolyRat(polyArray[1]) : null;
        return new FunctionRemainderPair<>(quotient, remainder);
    }
    
    @Override
    @SideEffectFree
    public PolyRat squared() {
        return new PolyRat(pX.squared(ZERO_PX));
    }
    
    @SideEffectFree
    protected PolyRat polyRaiser(
            @NonNegative int exponent
    ) {
        if (simpleRaisable(exponent)) {
            return this;
        }
        
        return new PolyRat( pX.raised(exponent, ONE_PX, ZERO_PX) );
    }
    
    @Pure
    private boolean simpleRaisable(
            int exponent
    ) {
        if (isConstant()) {
            boolean isZero = isZero();
            if (isZero && (exponent == 0)) {
                throw new ArithmeticException("0^0 is undefined");
            }
            //else
            return isZero || isOne() || (exponent == 1);
        }
        return false;
    }
    
    @SideEffectFree
    public List<PolyRat> ratFactors() {
        PolyArrayList<Rational> workingCopy = new PolyArrayList<>(pX);
        int curDeg = workingCopy.size() - 1;
        List<PolyRat> ratFactors = new ArrayList<>(curDeg);
        
        curDeg = removeTrailingZeroes(ratFactors, workingCopy, curDeg);
        
        Rational mainP = workingCopy.getFirst();
        Rational mainQ = workingCopy.getLast();
        if (curDeg >= 1) {
            Rational scalar = getFactoredOutCoeff(workingCopy, mainQ, curDeg);
            if (scalar != null) {
                workingCopy = workingCopy.scalar(scalar, true, ZERO_PX);
                mainP = workingCopy.getFirst();
                mainQ = workingCopy.getLast();
                if (ratFactors.isEmpty()) {
                    ratFactors.add(constant(scalar));
                } else {
                    ratFactors.set(0, monomial(scalar, 1));
                }
            }
        }
        
        if (curDeg > 1) {
            Queue<Rational> posFactors = getPosFactorList(mainP, mainQ);
            while (curDeg > 1) {
                Rational posFactor = posFactors.poll();
                if (posFactor == null) {
                    break;
                }
                //else
                while (posFactorIsValid(workingCopy, posFactor)) {
                    Rational divCoeff = posFactor.denominatorAI();
                    Rational divConst = posFactor.numeratorAI();
                    PolyArrayList<Rational>[] quoRemain =
                            workingCopy.divSyn(divConst.negated(), divCoeff, true, ZERO_PX);
                    if (quoRemain[1].isZero()) {
                        workingCopy = quoRemain[0];
                        curDeg--;
                        
                        ratFactors.add(new PolyRat(2, List.of(divConst, divCoeff)));
                    } else {
                        break;
                    }
                }
            }
        }
        ratFactors.add(new PolyRat(workingCopy));
        return ratFactors;
    }
    
    @Pure
    private static boolean posFactorIsValid(
            PolyArrayList<Rational> workingCopy,
            Rational posFactor
    ) {
        BigInteger numP = workingCopy.getFirst().numeratorBI();
        BigInteger numQ = workingCopy.getLast().numeratorBI();
        
        BigInteger factorNum = posFactor.numeratorBI();
        BigInteger factorDen = posFactor.denominatorBI();
        return BigMathObjectUtils.isDivisible(numP, factorNum) &&
                BigMathObjectUtils.isDivisible(numQ, factorDen);
    }
    
    @SideEffectFree
    private static LinkedList<Rational> getPosFactorList(
            Rational ratP,
            Rational ratQ
    ) {
        LinkedList<Rational> posFactors = new LinkedList<>();
        
        AlgebraInteger numP = ratP.numeratorAI();
        AlgebraInteger numQ = ratQ.numeratorAI();
        SequencedCollection<? extends AlgebraInteger> factorsPDesc = numP.factors().reversed();
        SequencedCollection<? extends AlgebraInteger> factorsQAsc = numQ.factors();
        
        for (AlgebraInteger factorQ : factorsQAsc) {
            ListIterator<Rational> posFactorIter = posFactors.listIterator();
            
            for (AlgebraInteger factorP : factorsPDesc) {
                Rational posFactor = factorP.quotient(factorQ);
                
                boolean uninserted = true;
                while (posFactorIter.hasNext()) {
                    Rational lastVal = posFactorIter.next();
                    if (lastVal.compareTo(posFactor) < 0) {
                        posFactorIter.previous();
                        posFactorIter.add(posFactor);
                        uninserted = false;
                        break;
                    }
                }
                if (uninserted) {
                    posFactorIter.add(posFactor);
                }
            }
        }
        
        ListIterator<Rational> factorIter = posFactors.listIterator();
        while (factorIter.hasNext()) {
            Rational val = factorIter.next();
            factorIter.add( val.negated() );
        }
        
        return posFactors;
    }
    
    @SideEffectFree
    private static @Nullable Rational getFactoredOutCoeff(
            PolyArrayList<Rational> workingCopy,
            Rational ratQ,
            @Positive int curDeg
    ) {
        BigInteger denLCM;
        if (workingCopy.stream().allMatch(Rational::isWhole)) {
            denLCM = BigInteger.ONE;
        } else {
            denLCM = ratQ.denominatorBI();
            for (int i = curDeg; i > 0;) {
                Rational term = workingCopy.get(--i);
                denLCM = BigMathObjectUtils.lcm(term.denominatorBI(), denLCM);
            }
        }
        
        if (ratQ.isNegative()) {
            denLCM = denLCM.negate();
        }
        //while we're scaling things, lets make the polynomial positive
        Rational scalar = RationalFactory.newRational().denominator(denLCM).build();
        //we could scale the whole polynomial now, but we might need to do so again
        // later
        //so we'll scale each term as we check it, but not in the original, yet
        //instead, term will hold the scaled term
        Rational tempTerm = ratQ.product(scalar);
        BigInteger numGCF = tempTerm.numeratorBI();
        //i is currently the last term, but we already have that one
        //so we deincrement i before using
        //we can also stop if the gcd becomes 1
        while ( (curDeg > 0) && !numGCF.equals(BigInteger.ONE) ) {
            //scalaing first
            tempTerm = workingCopy.get(--curDeg).product(scalar);
            numGCF = numGCF.gcd( tempTerm.numeratorBI() );
        }
        return numGCF.equals(denLCM) ?
                null :
                RationalFactory.fromBigIntegers(numGCF, denLCM);
    }
    
    private static @NonNegative int removeTrailingZeroes(
            List<PolyRat> toStoreOneX,
            PolyArrayList<Rational> workingCopy,
            @Positive int curDeg
    ) {
        ListIterator<Rational> zeroIterator = workingCopy.listIterator();
        for (; (curDeg > 1) && zeroIterator.next().isZero(); curDeg--) {
            zeroIterator.remove();
            toStoreOneX.add(ONE_X);
        }
        return curDeg;
    }
    
    @Override
    @SideEffectFree
    public Iterator<Rational> iterator() {
        return pX.iterator();
    }
    
    @Override
    public void forEach(
            Consumer<? super Rational> action
    ) {
        pX.forEach(action);
    }
    
    @Override
    @SideEffectFree
    protected Stream<Rational> slicedStreamFixedBounds(
            @NonNegative @LessThan("endExclusive") int startInclusive,
            @LTEqLengthOf("pX") int endExclusive,
            boolean parallel
    ) {
        List<Rational> resList = pX.subList(startInclusive, endExclusive);
        return StreamSupport.stream(MiscUtils.spliteratorOrderedSizedNonnullImmutableSubsizedIterator(
                resList.iterator(), resList.size()), parallel);
    }
}
