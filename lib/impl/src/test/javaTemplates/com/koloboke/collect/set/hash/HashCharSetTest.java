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

package com.koloboke.collect.set.hash;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import com.google.common.collect.testing.features.CollectionFeature;
import com.koloboke.collect.testing.CharSamples;
import junit.framework.*;
import com.koloboke.collect.hash.*;
import com.koloboke.collect.set.*;
import com.koloboke.collect.testing.set.HyperCharSetTestSuiteBuilder;


public class HashCharSetTest extends TestCase {

    public static Test suite() {
        HyperCharSetTestSuiteBuilder builder = new HyperCharSetTestSuiteBuilder()
                .setSamples(CharSamples.allKeys());
        builder.setFactories(Lists.transform(/* if !(float|double elem) */CharHashConfigs
                /* elif float|double elem //HashConfigs// endif */.all(),
                new Function</* if !(float|double elem) */CharHashConfig
                            /* elif float|double elem //HashConfig// endif */,
                        CharSetFactory</* if obj elem //Object, // endif */?>>() {
                    @Override
                    public CharSetFactory</* if obj elem //Object, // endif */?> apply(
                            /* if !(float|double elem) */CharHashConfig
                            /* elif float|double elem //HashConfig// endif */ config) {
                        return /* if !(float|double elem) */
                                config.apply(HashCharSets.getDefaultFactory())
                               /* elif float|double elem */
                               HashCharSets.getDefaultFactory().withHashConfig(config)/* endif */;
                    }
                }));
        /* if obj elem */
        builder.withSpecialFeatures(CollectionFeature.ALLOWS_NULL_VALUES);
        /* endif */
        return builder.create();
    }
}
