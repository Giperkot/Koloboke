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

package com.koloboke.jpsg.collect.bulk;

import com.koloboke.jpsg.collect.*;


public abstract class BulkMethod implements Method {

    protected BulkMethodGenerator gen;
    protected MethodContext cxt;

    public EntryType entryType() {
        return EntryType.SIMPLE;
    }

    public boolean withInternalVersion() {
        return false;
    }

    public String argsBeforeCollection() {
        return "";
    }

    public String collectionArgName() {
        return cxt.isMapView() ? "m" : "c";
    }

    public String name() {
        return MethodGenerator.defaultMethodName(cxt, this);
    }

    @Override
    public final void init(MethodGenerator g, MethodContext c) {
        this.gen = (BulkMethodGenerator) g;
        this.cxt = c;
    }

    @Override
    public Class<? extends MethodGenerator> generatorBase() {
        return BulkMethodGenerator.class;
    }

    public void beginning() {
    }

    public void rightBeforeLoop() {
    }

    final void escapeIfEmpty() {
        gen.lines(
                "if (isEmpty())",
                "    return;"
        );
    }

    public abstract void loopBody();

    public void end() {
    }
}
