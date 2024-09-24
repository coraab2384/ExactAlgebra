package org.cb2384.exactalgebra.objects;

import org.cb2384.exactalgebra.objects.numbers.AlgebraNumber;
import org.cb2384.exactalgebra.objects.numbers.integral.AlgebraInteger;
import org.cb2384.exactalgebra.objects.pair.RemainderPair;

import org.checkerframework.dataflow.qual.*;

public interface RealField<I extends AlgebraicRing<I, R> & AlgebraObject<R>,
        R extends RealField<I, R> & AlgebraObject<R>> {
    /**
     * Adds this to {@code augend}.
     *
     * @param   augend  the value to add to this
     *
     * @return  {@code this + that}
     */
    @SideEffectFree
    R sum(R augend);
    
    /**
     * Subtracts {@code subtrahend} from this.
     *
     * @param   subtrahend  the value to subtract from this
     *
     * @return  {@code this - subtrahend}
     */
    @SideEffectFree
    R difference(R subtrahend);
    
    /**
     * Adds this to {@code multiplicand}.
     *
     * @param   multiplicand    the value to multiply this by
     *
     * @return  {@code this * multiplicand}
     */
    @SideEffectFree
    R product(R multiplicand);
    
    /**
     * Divides this by {@code divisor}, with the caveat that the quotient is a whole number
     * (an implementer of {@link AlgebraicRing}).
     * That is, the quotient is the highest value such divisor {@code divisor * quotient <= this}.
     * The second value is the remainder, which is {@code this - (divisor * quotient)}.
     *
     * @param divisor   the value to divide this by
     *
     * @return  a {@link RemainderPair} with the quotient being {@link RemainderPair#value()}
     *          and the remainder being {@link RemainderPair#remainder()}
     *
     * @throws  ArithmeticException if dividing by {@code 0}
     */
    @SideEffectFree
    RemainderPair<? extends I, ? extends R, R, ?> quotientZWithRemainder(R divisor);
    
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
    I quotientZ(R divisor);
    
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
    R remainder(R divisor);
    
    /**
     * Does this times this.
     *
     * @return {@code this * this}, or this²
     */
    @SideEffectFree
    R squared();
    
    /**
     * Performs {@code this ^ exponent} (with {@code ^} being exponentiation, not logical xor);
     * that is, multiplication of this by itself the indicated number of times.
     * Support for negative inputs will depend on the implementation.
     *
     * @param   exponent    the exponent to use
     *
     * @return  {@code this ^ exponent}
     */
    @SideEffectFree
    R raised(int exponent);
    
    /**
     * Performs {@code this ^ exponent} (with {@code ^} being exponentiation, not logical xor);
     * that is, multiplication of this by itself the indicated number of times.
     *
     * @param   exponent    the exponent to use
     *
     * @return  {@code this ^ exponent}
     */
    @SideEffectFree
    R raised(AlgebraInteger exponent);
    
//    /**
//     * Find the square root of this value.
//     *
//     * @return  the square root
//     */
//    @SideEffectFree
//    R sqRoot();
//
//    /**
//     * Finds the root of this to the given index.
//     *
//     * @param   index   the index of the root ({@code 2} for square, {@code 3} for cube, etc)
//     *
//     * @return  the {@code index}th root of this.
//     *
//     * @throws  ArithmeticException if this is negative and {@code index} is even
//     */
//    @SideEffectFree
//    R root(int index);
//
//    /**
//     * Finds the root of this to the given index.
//     *
//     * @param   index   the index of the root ({@code 2} for square, {@code 3} for cube, etc)
//     *
//     * @return  the {@code index}th root of this.
//     *
//     * @throws  ArithmeticException if this is negative and {@code index} is even
//     */
//    @SideEffectFree
//    R root(AlgebraInteger index);
    
    /**
     * True division of this by divisor, with no remainder.
     *
     * @param divisor the value to divide this by
     *
     * @return  the quotient of {@code divisor} and this
     *
     * @throws ArithmeticException  if dividing by 0
     */
    @SideEffectFree
    R quotient(R divisor);
    
//    /**
//     * This to the power of {@code power}. Remember that a root function is simply this function,
//     * but with an inverted power.
//     *
//     * @param   power   the real power to use
//     *
//     * @return  {@code this ^ power}
//     *
//     * @throws  ArithmeticException if {@code} power has an even reciprocal and this is negative
//     */
//    @SideEffectFree
//    R power(R power);
//
//    /**
//     * Performs an exp operation, such as {@link Math#exp}.
//     *
//     * @return  {@code e ^ this}, with {@code e} being Euler's number {@link Math#E}
//     */
//    @SideEffectFree
//    R exp();
//
//    /**
//     * Returns the logarithm of this with the given base.
//     *
//     * @param   base    the base of the returned logarithm
//     *
//     * @return  {@code log_base(this)}
//     */
//    @SideEffectFree
//    R logBase(AlgebraNumber base);
//
//    /**
//     * Returns the logarithm of this with the natural base {@link Math#E}
//     *
//     * @return  {@code ln(this)}
//     */
//    @SideEffectFree
//    R logNatural();
}
