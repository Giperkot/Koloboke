/* with char|byte|short|int|long key */
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

package com.koloboke.collect.research.hash;

import com.koloboke.collect.impl.Primitives;
import com.koloboke.collect.impl.UnsafeConstants;
import com.koloboke.function.CharConsumer;

import java.util.Arrays;


public class ByteStatesLHashCharSet implements UnsafeConstants {
    public static final int FREE = 0, FULL = 1;

    public int size = 0;
    public byte[] states;
    public char[] set;

    public ByteStatesLHashCharSet(int capacity) {
        if ((capacity & (capacity - 1)) != 0) {
            throw new IllegalArgumentException();
        }
        states = new byte[capacity];
        set = new char[capacity];
    }

    public void clear() {
        if (size != 0) {
            size = 0;
            Arrays.fill(states, (byte) FREE);
        }
    }

    public int indexBinaryStateSimpleIndexing(char key) {
        byte[] states = this.states;
        char[] keys = set;
        int capacityMask = keys.length - 1;
        int index = Primitives.hashCode(key) & capacityMask;
        if (states[index] > 0) {
            if (keys[index] == key)
                return index;
            while (true) {
                index = (index + 1) & capacityMask;
                if (states[index] > 0) {
                    if (keys[index] == key)
                        return index;
                } else {
                    return -1;
                }
            }
        } else {
            return -1;
        }
    }

    public int indexBinaryStateUnsafeIndexing(char key) {
        byte[] states = this.states;
        char[] keys = set;
        long capacityMask = (long) (keys.length - 1);
        long index = ((long) Primitives.hashCode(key)) & capacityMask;
        if ((int) U.getByte(states, BYTE_BASE + index) > 0) {
            long offset = index << CHAR_SCALE_SHIFT;
            if (U.getChar(keys, CHAR_BASE + offset) == key)
                return (int) index;
            long capacityOffsetMask = capacityMask << CHAR_SCALE_SHIFT;
            while (true) {
                index = (index + 1L) & capacityMask;
                offset = (offset + CHAR_SCALE) & capacityOffsetMask;
                if ((int) U.getByte(states, BYTE_BASE + index) > 0) {
                    if (U.getChar(keys, CHAR_BASE + offset) == key)
                        return (int) index;
                } else {
                    return -1;
                }
            }
        } else {
            return -1;
        }
    }

    public boolean addBinaryStateSimpleIndexing(char key) {
        byte[] states = this.states;
        char[] keys = set;
        int capacityMask = keys.length - 1;
        int index = Primitives.hashCode(key) & capacityMask;
        keyAbsent:
        if (states[index] != FREE) {
            if (keys[index] == key)
                return false;
            while (true) {
                index = (index + 1) & capacityMask;
                if (states[index] == FREE) {
                    break keyAbsent;
                } else if (keys[index] == key) {
                    return false;
                }
            }
        }
        states[index] = FULL;
        keys[index] = key;
        size++;
        return true;
    }

    public void forEachBinaryState(CharConsumer action) {
        byte[] states = this.states;
        char[] keys = set;
        for (int i = states.length - 1; i >= 0; i--) {
            if (states[i] != FREE)
                action.accept(keys[i]);
        }
    }
}
