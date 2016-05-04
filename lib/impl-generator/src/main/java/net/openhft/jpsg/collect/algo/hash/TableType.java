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

import net.openhft.jpsg.PrimitiveType;
import net.openhft.jpsg.function.UnaryOperator;

import static net.openhft.jpsg.PrimitiveType.CHAR;
import static net.openhft.jpsg.PrimitiveType.INT;
import static net.openhft.jpsg.PrimitiveType.LONG;


enum TableType implements UnaryOperator<PrimitiveType> {
    INSTANCE;

    @Override
    public PrimitiveType apply(PrimitiveType primitiveType) {
        switch (primitiveType) {
            case BYTE: return CHAR;
            case CHAR: case SHORT: return INT;
            default: return LONG;
        }
    }
}
