package org.cb2384.exactalgebra.text;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.SequencedCollection;
import java.util.Set;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.cb2384.exactalgebra.text.parse.Command.ReservedNames;
import org.cb2384.exactalgebra.util.corutils.ternary.ComparableSwitchSignum;

import org.checkerframework.dataflow.qual.*;

public sealed interface Identifier<I extends Enum<I> & Identifier<I, S>, S extends CharSequence>
        extends ComparableSwitchSignum<I>
        permits Utils, OpNames, ReservedNames, ReservedSymbols {
    
    Set<CharSequence> ALL_IDENTIFIERS = getAll()
            .map(Identifier::identifiers)
            .flatMap(Set::stream)
            .collect(Collectors.toUnmodifiableSet());
    
    @Pure
    default boolean reserves(
            char toTest
    ) {
        return reserves(Character.toString(toTest));
    }
    
    @Pure
    default boolean enumReserves(
            char toTest
    ) {
        return enumReserves(Character.toString(toTest));
    }
    
    @Pure
    default boolean reserves(
            int toTest
    ) {
        return reserves(Character.toString(toTest));
    }
    
    @Pure
    default boolean enumReserves(
            int toTest
    ) {
        return enumReserves(Character.toString(toTest));
    }
    
    @Pure
    default boolean reserves(
            CharSequence toTest
    ) {
        return asMatcher(toTest).matches();
    }
    
    @Pure
    default boolean enumReserves(
            CharSequence toTest
    ) {
        return enumPattern().matcher(toTest).matches();
    }
    
    @Pure
    Set<S> identifiers();
    
    @Pure
    Set<S> enumIdentifiers();
    
    @SideEffectFree
    default Set<S> enumIdentifiers(
            I other
    ) {
        return (Set<S>) identifiersFor(Set.of(this, other));
    }
    
    @SideEffectFree
    default Set<S> enumIdentifiers(
            I... others
    ) {
        return (Set<S>) identifiersFor(EnumSet.of((I) this, others));
    }
    
    @SideEffectFree
    default Set<S> enumIdentifiers(
            Set<I> others
    ) {
        EnumSet<I> set = EnumSet.copyOf(others);
        set.add((I) this);
        return (Set<S>) identifiersFor(set);
    }
    
    @Pure
    Pattern asPattern();
    
    @SideEffectFree
    Pattern enumPattern();
    
    @SideEffectFree
    default Pattern groupedEnumPattern(
            I other
    ) {
        Stream<? extends Identifier<?, ?>> keys = equals(other)
                ? Stream.of(other)
                : Stream.of(this, other);
        return groupedPattern(keys);
    }
    
    @SideEffectFree
    default Pattern groupedEnumPattern(
            I... others
    ) {
        return groupedEnumPattern(Arrays.asList(others));
    }
    
    @SideEffectFree
    default Pattern groupedEnumPattern(
            SequencedCollection<I> others
    ) {
        Stream<? extends Identifier<?, ?>> keys = others.contains(this)
                ? others.stream()
                : Stream.concat(Stream.of(this), others.stream());
        return groupedPattern(keys);
    }
    
    @SideEffectFree
    default Pattern groupedEnumPattern(
            EnumSet<I> others
    ) {
        EnumSet<I> set = EnumSet.copyOf(others);
        set.add((I) this);
        return groupedPattern(set.stream());
    }
    
    @SideEffectFree
    private static Pattern groupedPattern(
            Stream<? extends Identifier<?, ?>> keys
    ) {
        StringBuilder patternBuilder = new StringBuilder();
        keys.map(Identifier::asPattern)
                .map(Pattern::pattern)
                .forEachOrdered(s -> patternBuilder.append("(").append(s).append(")"));
        
        return Pattern.compile(patternBuilder.toString());
    }
    
    @SideEffectFree
    default Matcher asMatcher(
            CharSequence toTest
    ) {
        return asPattern().matcher(toTest);
    }
    
    @SideEffectFree
    static Set<? extends CharSequence> identifiersFor(
            Collection<? extends Identifier<?, ?>> toName
    ) {
        return toName.stream()
                .map(Identifier::identifiers)
                .flatMap(Set::stream)
                .collect(Collectors.toUnmodifiableSet());
    }
    
    @SideEffectFree
    static Pattern groupedPatternCompiler(
            SequencedCollection<? extends Identifier<?, ?>> keys
    ) {
        return groupedPattern(keys.stream());
    }
    
    @SideEffectFree
    static Pattern patternOrCompiler(
            Set<? extends CharSequence> keys
    ) {
        Iterator<? extends CharSequence> keyIterator = keys.iterator();
        if (keys.size() == 1) {
            return Pattern.compile(keyIterator.next().toString().toLowerCase());
        }
        StringBuilder patternBuilder = new StringBuilder(keyIterator.next());
        while (keyIterator.hasNext()) {
            String next = keyIterator.next().toString().toLowerCase();
            patternBuilder.append("|").append(next);
        }
        return Pattern.compile(patternBuilder.toString());
    }
    
    @SideEffectFree
    static Identifier<?, ?> firstMatchingIdentifier(
            String arg,
            Supplier<? extends RuntimeException> ifCannotFind
    ) {
        return getAll().filter(i -> i.asMatcher(arg).matches())
                .findFirst()
                .orElseThrow(ifCannotFind);
    }
    
    @SideEffectFree
    private static Stream<Identifier<?, ?>> getAll() {
        return Arrays.stream(Identifier.class.getPermittedSubclasses())
                .flatMap(c -> enumSetAllOf(c).stream());
    }
    
    @SideEffectFree
    private static <I extends Enum<I> & Identifier<I, ?>> Set<I> enumSetAllOf(
            Class<?> attemptingClass
    ) {
        assert attemptingClass.isEnum() && Identifier.class.isAssignableFrom(attemptingClass);
        return EnumSet.allOf((Class<I>) attemptingClass);
    }
}
