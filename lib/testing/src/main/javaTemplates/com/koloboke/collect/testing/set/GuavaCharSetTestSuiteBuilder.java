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

import com.google.common.collect.testing.*;
import com.koloboke.collect.testing.testers.CharCollectionCursorTester;
import com.koloboke.collect.testing.testers.CharCollectionForEachTester;

import java.util.List;


public class GuavaCharSetTestSuiteBuilder/*<>*/ extends SetTestSuiteBuilder<Character> {

    @Override
    protected List<Class<? extends AbstractTester>> getTesters() {
        List<Class<? extends AbstractTester>> testers = super.getTesters();
        testers.clear();
        testers.add(CharCollectionForEachTester.class);
        testers.add(CharCollectionCursorTester.class);
        return testers;
    }

    @Override
    public SetTestSuiteBuilder<Character> usingGenerator(
            TestCollectionGenerator<Character> subjectGenerator) {
        return super.usingGenerator(subjectGenerator);
    }
}
