package org.cb2384.exactalgebra.objects.exceptions;

import java.lang.reflect.Type;

import org.cb2384.exactalgebra.util.corutils.StringUtils;

import org.checkerframework.dataflow.qual.*;

/**
 * Thrown to indicate that the requested return value does not fit within the return type
 */
public class DisallowedNarrowingException
        extends ArithmeticException {
    /**
     * Creates a new DisallowedNarrowingException
     * @param sourceType the Type of the source
     * @param targetType the Type of the target
     */
    @SideEffectFree
    public DisallowedNarrowingException(
            Type sourceType,
            Type targetType
    ) {
        super( getMsg(sourceType, targetType) );
    }
    
    /**
     * Returns an error message for a NullElementException
     */
    @SideEffectFree
    private static String getMsg(
            Type sourceType,
            Type targetType
    ) {
        String sourceName = StringUtils.getIdealName(sourceType);
        String targetName = StringUtils.getIdealName(targetType);
        
        return "This " + sourceName + " cannot be represented within the bounds of"
                + StringUtils.aOrAnPrepend(targetName, true);
    }
    
    /**
     * Gets the simple name of a Type if it is a class,
     * or else the normal name if the simple name is empty,
     * or else the type name if it is not a class
     */
    @SideEffectFree
    private static String getTypeName(
            Type type
    ) {
        String name;
        if (type instanceof Class<?> typeClass) {
            name = typeClass.getSimpleName();
            if (name.isEmpty()) {
                name = typeClass.getName();
            }
        } else {
            name = type.getTypeName();
        }
        return name;
    }
}
