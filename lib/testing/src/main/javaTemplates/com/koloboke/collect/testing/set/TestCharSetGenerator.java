/* with char|byte|short|int|long|float|double|obj elem */
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

package com.koloboke.collect.testing.set;

import com.google.common.collect.testing.SampleElements;
import com.google.common.collect.testing.TestSetGenerator;
import com.koloboke.collect.set.CharSetFactory;
import com.koloboke.collect.testing.Mutability;
import com.koloboke.function.Consumer;

import java.util.List;
import java.util.Set;


public class TestCharSetGenerator/*<>*/ implements TestSetGenerator<Character> {

    private final Mutability mutability;
    private final CharSetFactory</* if obj elem //E, // endif */?> factory;
    private final SampleElements<? extends Character> elems;

    public TestCharSetGenerator(Mutability mutability,
            CharSetFactory</* if obj elem //E, // endif */?> factory,
            SampleElements<? extends Character> elems) {
        this.mutability = mutability;
        this.factory = factory;
        this.elems = elems;
    }

    @Override
    public Set<Character> create(final Object... elements) {
        Consumer<com.koloboke.function./*f*/CharConsumer/*<>*/> supplier = set -> {
            for (Object e : elements) {
                set.accept((Character) e);
            }
        };
        switch (mutability) {
            case IMMUTABLE: return factory.newImmutableSet(supplier);
            case UPDATABLE: return factory.newUpdatableSet(supplier);
            case MUTABLE: return factory.newMutableSet(supplier);
            default: throw new AssertionError();
        }
    }

    @Override
    public SampleElements<Character> samples() {
        // noinspection unchecked
        return (SampleElements) elems;
    }

    @Override
    public Character[] createArray(int length) {
        return (Character[]) new /* raw */Character[length];
    }

    @Override
    public Iterable<Character> order(List<Character> insertionOrder) {
        return insertionOrder;
    }
}
