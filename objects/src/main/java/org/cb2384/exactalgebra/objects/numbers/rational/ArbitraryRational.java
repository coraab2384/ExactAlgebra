package org.cb2384.exactalgebra.objects.numbers.rational;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigInteger;

import org.checkerframework.dataflow.qual.*;

public class ArbitraryRational
        extends AbstractRational
        implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 0x5E6EF3718CB341E4L;
    
    private final BigInteger numerator;
    
    private final BigInteger denominator;
    
    @SideEffectFree
    ArbitraryRational(
            BigInteger numerator,
            BigInteger denominator
    ) {
        assert denominator.signum() == 1;
        this.numerator = numerator;
        this.denominator = denominator;
    }
    
    @SideEffectFree
    public static ArbitraryRational fromBigIntegers(
            BigInteger numerator,
            BigInteger denominator
    ) {
        switch (denominator.signum()) {
            case 0:
                throw new ArithmeticException(DIV_0_EXC_MSG);
            
            case -1:
                numerator = numerator.negate();
                denominator = denominator.negate();
        }
        
        BigInteger gcf = numerator.gcd(denominator);
        numerator = numerator.divide(gcf);
        denominator = denominator.divide(gcf);
        
        return new ArbitraryRational(numerator, denominator);
    }
    
    @SideEffectFree
    public static ArbitraryRational fromPrims(
            long numerator,
            long denominator
    ) {
        return fromBigIntegers(BigInteger.valueOf(numerator), BigInteger.valueOf(denominator));
    }
    
    @Override
    @Pure
    public BigInteger numeratorBI() {
        return numerator;
    }
    
    @Override
    @Pure
    public BigInteger denominatorBI() {
        return denominator;
    }
}
