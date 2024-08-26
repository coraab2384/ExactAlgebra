/**
 * <b>Interfaces and classes for dealing with three-valued-logic.</b>
 *
 * <p>The main class here is
 * {@link org.cb2384.corutils.ternary.Ternary}, but boolean and three-valued contexts are also provided
 * ({@link org.cb2384.corutils.ternary.ThreeValuedContext}, {@link org.cb2384.corutils.ternary.BooleanValuedContext},
 * and {@link org.cb2384.corutils.ternary.KnownDefaultBooleanContext}); these can be thought of as being to a
 * {@link org.cb2384.corutils.ternary.ThreeValued} what a {@link java.text.Format Format} class is
 * to a {@link java.lang.String}.</p>
 *
 * <p>Finally, a {@link org.cb2384.corutils.ternary.ThreeValued} variant {@link org.cb2384.corutils.ternary.Signum}
 * is provided as an improvement over using the {@code int} values {@code -1}, {@code 0}, and {@code 1} when
 * it comes to signum functions or comparisons. To complement it with comparisons, {@link
 * org.cb2384.corutils.ternary.ComparableSwitchSignum} extends {@link java.lang.Comparable} to allow for
 * obtaining {@link java.lang.Comparable#compareTo compareTo} results in this format rather than as {@code int}s.</p>
 */
package org.cb2384.exactalgebra.util.corutils.ternary;