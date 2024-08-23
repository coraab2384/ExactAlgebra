/**
 * <b>Three static utility classes which can have wide utility:</b>
 *
 * <p>{@link org.cb2384.corutils.StringUtils StringUtils} has some utilities
 * regarding {@link java.lang.String String}s.</p>
 *
 * <p>{@link org.cb2384.corutils.Arrayz Arrayz} has some additions to {@link java.util.Arrays Arrays}, a function
 * similar to {@link java.lang.System#arraycopy(Object, int, Object, int, int)
 * System.arrayCopy} that also maps in between, though is therefore not as fast, as well
 * as some more forms of {@link java.lang.reflect.Array#newInstance(Class, int)
 * Array.newInstance(Class, int)}. </p>
 *
 * <p>{@link org.cb2384.corutils.NullnessUtils NullnessUtils} holds functions useful for dealing with
 * {@code null} pointers. Two have been added in later versions of java (in {@link java.util.Objects Objects},
 * and the versions here are marked as deprecated,
 * though not for removal), and some others can be replicated with {@link java.util.Optional Optional}, though
 * these do not require the creation of an actual {@link java.util.Optional Optional} object. Finally, there
 * is a suite of functions that checks common container types to see if they contain nulls.</p>
 */
package org.cb2384.exactalgebra.util.corutils;