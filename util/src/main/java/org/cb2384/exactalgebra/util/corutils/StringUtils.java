package org.cb2384.exactalgebra.util.corutils;

import java.lang.reflect.Type;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.checkerframework.checker.nullness.qual.*;
import org.checkerframework.dataflow.qual.*;

/**
 * Some utilities for dealing with Strings.
 * There are methods for determining if something starts with a vowel,
 * (for English-language a-an distinction), as well as one that prepends the appropriate indefinite article.
 * There is also a utility that is similar to {@link Class#getName}, {@link #getIdealName(Class)}
 * which is essentially {@link Class#getSimpleName} unless it is empty or just "{@code []}",
 * in which case returns {@link Class#getName()}.
 * There is a version for generic {@link Type}s as well.
 *
 * @author  Corinne Buxton
 */
public final class StringUtils {
    
    /**
     * Static constant for easy access. I like them okay
     */
    public static final char INTERROBANG = 0x203D;
    
    /**
     * This should never be called
     *
     * @throws  IllegalAccessException    always
     */
    private StringUtils() throws IllegalAccessException {
        throw new IllegalAccessException("This should never be called" + INTERROBANG);
    }
    
    /**
     * Default vowels. Y is not included because these are for the context of the beginning of a word,
     * and Y is essentially always a consonant when it begins a word.
     */
    public static final String DEFAULT_VOWELS = "AaEeIiOoUu";
    
    /**
     * Checks if the given {@link CharSequence} starts with a vowel,
     * according to {@link #DEFAULT_VOWELS}.
     *
     * @param   toComeAfter the {@link String} or other {@link CharSequence} to check
     *
     * @return  {@code true} if it starts with a vowel, otherwise {@code false}
     */
    @Pure
    public static boolean startsWithVowel(
            @NonNull CharSequence toComeAfter
    ) {
        return toComeAfter.toString().startsWith(DEFAULT_VOWELS);
    }
    
    /**
     * Prepends either "a" or "an" to a String of the given {@link CharSequence}.
     * If it does not start with a space, then one is added after the article.
     * If the second boolean is true, another space is added in front of the article as well.
     *
     * @param   toPrepend   a {@link CharSequence} representing the {@link String} that will have
     *                      the article prepended to it, which will then be returned.
     *
     * @param   addSpaceBefore  if {@code true}, a space is added in front of the article
     *
     * @return  a string consisting of possibly a space, then "a" or "an", then possibly a space,
     *          and finally the first argument, itself as a {@link String}.
     */
    @SideEffectFree
    public static @NonNull String aOrAnPrepend(
            @NonNull CharSequence toPrepend,
            boolean addSpaceBefore
    ) {
        String inString = toPrepend.toString();
        if (inString.isEmpty()) {
            return addSpaceBefore ? " a" : "a";
        }
        
        String trimString = inString.trim();
        if (trimString.codePointAt(0) == inString.codePointAt(0)) {
            inString = " " + inString;
        }
        final String result = startsWithVowel(trimString) ?
                "an" + inString :
                "a" + inString;
        
        return addSpaceBefore ? " " + result : result;
    }
    
    /**
     * Turns a camelCase {@link CharSequence} into a CONSTANT_CASE String, with an underscore added in front
     * of every capital or every group of capitals.
     *
     * @param   camelCase a string to make into CONSTANT_CASE
     *
     * @return  a String representing the same characters all in uppercase, and an underscore ('_')
     *          in front of all that were capitalized in {@code camelCase}.
     */
    @SideEffectFree
    public static @NonNull String to_CONSTANT_CASE(
            @NonNull CharSequence camelCase
    ) {
        Matcher matcher = Pattern.compile("(\\p{Lu}|\\p{Lt})(?<=\\p{Ll}\1|(\\p{Lu}|\\p{Ll})\1(?=\\p{Ll}))")
                .matcher(camelCase);
        StringBuilder result = new StringBuilder();
        while (!matcher.hitEnd()) {
            if (matcher.find()) {
                result.append( camelCase.subSequence(matcher.start(), matcher.end()) );
            } else {
                break;
            }
        }
        return result.toString();
    }
    
    /**
     * Turns a CONSTANT_CASE {@link CharSequence} into a camelCase String, with no underscores.
     *
     * @param   CONSTANT_CASE a string to make into camelCase
     *
     * @return  a String representing the same characters, save the underscore '_', in lower case,
     *          save the first after each underscore, which is still in uppercase.
     */
    @SideEffectFree
    public static @NonNull String toCamelCase(
            @NonNull CharSequence CONSTANT_CASE
    ) {
        Matcher matcher = Pattern.compile("_")
                .matcher(CONSTANT_CASE);
        StringBuilder result = new StringBuilder();
        while (!matcher.hitEnd()) {
            if (matcher.find()) {
                String substring = CONSTANT_CASE.subSequence(matcher.start(), matcher.end()).toString();
                int secondIndex = (substring.codePointCount(0, 2) == 1) ? 2 : 1;
                result.append(substring, 0, secondIndex)
                        .append( substring.substring(secondIndex).toLowerCase() );
            } else {
                break;
            }
        }
        return result.toString();
    }
    
    /**
     * Turns a CONSTANT_CASE {@link CharSequence} into a lower case String, with no underscores.
     *
     * @param   CONSTANT_CASE a string to make lowercase and remove underscores from
     *
     * @return  a String representing the same characters, save the underscore '_', in lower case.
     */
    @SideEffectFree
    public static @NonNull String toLowerCase(
            @NonNull CharSequence CONSTANT_CASE
    ) {
        return CONSTANT_CASE.toString().replace("_", "").toLowerCase();
    }
    
    /**
     * Checks if the given CharSequence is composed only of digits, with possibly '+' or '-'
     * as the first character. The checking is done using {@link Character#isDigit}.
     *
     * @param toTest the sequence to test
     *
     * @return  true if all code units/points are digits, save the first which can be '+' or '-'
     */
    @SideEffectFree
    public static boolean isDigitString(
            @NonNull CharSequence toTest
    ) {
        char first = toTest.charAt(0);
        if (Character.isHighSurrogate(first)) {
            return toTest.codePoints().allMatch(Character::isDigit);
        }
        if ((first == '-') || (first == '+') || Character.isDigit(first)) {
            return toTest.subSequence(1, toTest.length())
                    .codePoints()
                    .allMatch(Character::isDigit);
        }
        return false;
    }
    
    /**
     * Gets the name of this {@link Type}. If the given type is just a {@link Class}, calls
     * {@link #getIdealName(Class)} for that class.
     * Otherwise, just uses {@link Type#getTypeName()}
     * This is intended for verbosity of error messages.
     *
     * @param   type    the type variable to get a name for
     *
     * @return  a name for this {@link Type}
     */
    @SideEffectFree
    public static @NonNull String getIdealName(
            @NonNull Type type
    ) {
        if (type instanceof Class<?>) {
            return getIdealName((Class<?>) type);
        }
        return type.getTypeName();
    }
    
    /**
     * Gets a nice name for the given {@link Class} object. Prefers {@link Class#getSimpleName()} unless it returns
     * an empty string or just "{@code []}", in which case it returns {@link Class#getName()}
     * This is intended for verbosity of error messages.
     *
     * @param   clazz   the Class object to get a name for
     *
     * @return  a name for the given {@link Class}, either the simple or the normal name.
     */
    @SideEffectFree
    public static @NonNull String getIdealName(
            @NonNull Class<?> clazz
    ) {
        String name = clazz.getSimpleName();
        if (name.isEmpty()) {
            name = clazz.getName();
        } else if (name.equals("[]")) {
            final Class<?> componentClass = clazz.getComponentType();
            return getIdealName(componentClass) + "[]";
        }
        return name;
    }
}
