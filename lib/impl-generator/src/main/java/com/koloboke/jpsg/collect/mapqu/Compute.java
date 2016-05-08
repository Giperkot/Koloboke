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

package com.koloboke.jpsg.collect.mapqu;


public class Compute extends MapQueryUpdateMethod {

    @Override
    public BasicMapQueryUpdateOp baseOp() {
        return BasicMapQueryUpdateOp.CUSTOM_INSERT;
    }

    @Override
    public void beginning() {
        gen.requireNonNull("remappingFunction");
    }

    @Override
    public void ifPresent() {
        gen.lines(cxt.valueGenericType() + " newValue = remappingFunction." + cxt.applyValueName() +
                "(" + gen.keyAndValue() + ");");
        if (cxt.isObjectValue() || cxt.genericVersion()) {
            gen.ifBlock("newValue != null");
        }
        gen.setValue("newValue");
        gen.ret("newValue");
        if (cxt.isObjectValue() || cxt.genericVersion()) {
            gen.elseBlock();
            if (!cxt.updatable()) {
                gen.remove();
                gen.ret("null");
            } else {
                gen.unsupportedOperation(getClass().getSimpleName() +
                        " operation of updatable map doesn't support removals");
            }
            gen.blockEnd();
        }
    }

    @Override
    public void ifAbsent() {
        gen.lines(cxt.valueGenericType() + " newValue = remappingFunction." + cxt.applyValueName() +
                "(" + gen.key() + ", " + gen.defaultValue() + ");");
        if (cxt.isObjectValue() || cxt.genericVersion()) {
            gen.ifBlock("newValue != null");
        }
        gen.insert("newValue");
        gen.ret("newValue");
        if (cxt.isObjectValue() || cxt.genericVersion()) {
            gen.elseBlock();
            gen.ret("null");
            gen.blockEnd();
        }
    }

    @Override
    public String nullArgs() {
        return "remappingFunction";
    }
}
