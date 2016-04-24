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

package net.openhft.jpsg.collect.algo.hash;

import net.openhft.jpsg.collect.mapqu.BasicMapQueryUpdateOp;
import net.openhft.jpsg.collect.mapqu.MapQueryUpdateMethod;


public final class Index extends MapQueryUpdateMethod {

    @Override
    public BasicMapQueryUpdateOp baseOp() {
        return BasicMapQueryUpdateOp.GET;
    }

    @Override
    public void ifPresent() {
        gen.ret(((HashMapQueryUpdateMethodGenerator) gen).indexF());
    }

    @Override
    public void ifAbsent() {
        gen.ret("-1");
    }

    @Override
    public boolean inline() {
        return true;
    }
}
