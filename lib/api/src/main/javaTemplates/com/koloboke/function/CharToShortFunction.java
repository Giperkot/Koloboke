/* if !(char t char u) && !(byte t byte u) && !(short t short u) && !(int t int u) &&
      !(long t long u) && !(float t float u) && !(double t double u)
*/
/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.koloboke.function;

/**
 * // if !(int|long|double t int|long|double u JDK8 jdk) //
 * Represents a function that accepts //a// {@code char}-valued argument and produces //a//
 * {@code short}-valued result.  This is the {@code char}-to-{@code short} primitive
 * specialization for {@link Function}.
 *
 * @see Function
 * // elif int|long|double t int|long|double u JDK8 jdk //
 * @deprecated this interface is present for backward compatibility with the version of this library
 *             for Java 6 or 7, use {@link java.util.function.CharToShortFunction} instead.
 * // endif //
 */
/* if JDK8 jdk */@FunctionalInterface/* endif */
/* if int|long|double t int|long|double u JDK8 jdk */@Deprecated/* endif */
public interface CharToShortFunction/* if int|long|double t int|long|double u JDK8 jdk //
        extends java.util.function.CharToShortFunction// endif */ {

    /* if !(int|long|double t int|long|double u JDK8 jdk) */
    /**
     * Applies this function to the given argument.
     *
     * @param value the function argument
     * @return the function result
     */
    short applyAsShort(char value);
    /* endif */
}
