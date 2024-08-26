package org.cb2384.exactalgebra.objects;

import org.cb2384.exactalgebra.objects.numbers.integral.AlgebraInteger;
import org.cb2384.exactalgebra.objects.pair.RemainderPair;

import org.checkerframework.dataflow.qual.*;

public interface AlgebraicRing<T extends AlgebraicRing<T, S> & AlgebraObject<S>, S extends AlgebraObject<S>> {
    /**
     * Returns the larger of this and {@code that}.
     *
     * @param   that    the value to compare and possibly return
     *
     * @return  {@code that} or this, whichever is larger
     */
    @SideEffectFree
    T max(T that);
    
    /**
     * Returns the smaller of this and {@code that}.
     *
     * @param   that    the value to compare and possibly return
     *
     * @return  {@code that} or this, whichever is smaller
     */
    @SideEffectFree
    T min(T that);
    
    /**
     * Adds this to {@code augend}.
     *
     * @param   augend  the value to add to this
     *
     * @return  {@code this + that}
     */
    @SideEffectFree
    T sum(T augend);
    
    /**
     * Subtracts {@code subtrahend} from this.
     *
     * @param   subtrahend  the value to subtract from this
     *
     * @return  {@code this - subtrahend}
     */
    @SideEffectFree
    T difference(T subtrahend);
    
    /**
     * Adds this to {@code multiplicand}.
     *
     * @param   multiplicand    the value to multiply this by
     *
     * @return  {@code this * multiplicand}
     */
    @SideEffectFree
    T product(T multiplicand);
    
    /**
     * Divides this by {@code divisor}, with the caveat that the quotient is a whole number
     * ({@link AlgebraInteger}).
     * That is, the quotient is the highest value such divisor {@code divisor * quotient <= this}.
     * The second value is the remainder, which is {@code this - (divisor * quotient)}.
     *
     * @param   divisor the value to divide this by
     *
     * @return  an array with the quotient, followed by the remainder
     *
     * @throws  ArithmeticException if dividing by {@code 0}
     */
    @SideEffectFree
    RemainderPair<? extends S, ? extends S, S, ?> quotientZWithRemainder(T divisor);
    
    /**
     * Divides this by {@code divisor}, with the caveat that the quotient is a whole number
     * ({@link AlgebraInteger}).
     * That is, the quotient is the highest value such divisor {@code divisor * quotient <= this}.
     *
     * @param   divisor the value to divide this by
     *
     * @return  the quotient of {@code this / divisor}, specifically using integer division
     *
     * @throws  ArithmeticException if dividing by {@code 0}
     */
    @SideEffectFree
    T quotientZ(T divisor);
    
    /**
     * Finds the remainder of this if it were divided by {@code divisor}, in line with {@code %}.
     * Specifically, this is the remainder and not the modulo, so it will not throw errors on negatives.
     *
     * @implNote    The remainder is as if from {@code this - (divisor * quotient)}, but there is no requirement
     *              the quotient be specifically calculated if it is not needed to find the result.
     *
     * @param   divisor    the value to pretend divide this by for the purpose of finding the remainder.
     *
     * @return  the remainder as if from {@code this % divisor}
     *
     * @throws  ArithmeticException if dividing by {@code 0}
     */
    @SideEffectFree
    T remainder(T divisor);
    
    /**
     * Does this times this.
     *
     * @return {@code this * this}, or this²
     */
    @SideEffectFree
    T squared();
    
    /**
     * Performs {@code this ^ exponent} (with {@code ^} being exponentiation, not logical xor);
     * that is, multiplication of this by itself the indicated number of times.
     *
     * @param   exponent    the exponent to use
     *
     * @return  {@code this ^ exponent}
     *
     * @throws  ArithmeticException if {@code exponent < 0}
     */
    @SideEffectFree
    T raisedZ(int exponent);
    
    /**
     * Performs {@code this ^ exponent} (with {@code ^} being exponentiation, not logical xor);
     * that is, multiplication of this by itself the indicated number of times.
     *
     * @param   exponent    the exponent to use
     *
     * @return  {@code this ^ exponent}
     *
     * @throws  ArithmeticException if {@code exponent < 0}
     */
    @SideEffectFree
    T raisedZ(AlgebraInteger exponent);
}
