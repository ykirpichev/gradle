/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.model.internal.manage.schema;

import com.google.common.collect.ImmutableList;
import org.gradle.model.internal.core.DefaultInstanceFactoryRegistry;
import org.gradle.model.internal.core.InstanceFactoryRegistry;
import org.gradle.model.internal.core.NodeInitializer;
import org.gradle.model.internal.core.NodeInitializerRegistry;
import org.gradle.model.internal.manage.schema.extract.*;

import java.util.Collections;
import java.util.List;

public class DefaultNodeInitializerRegistry implements NodeInitializerRegistry {
    private final List<NodeInitializerExtractionStrategy> strategies;

    public DefaultNodeInitializerRegistry() {
        this(new DefaultInstanceFactoryRegistry(), Collections.<NodeInitializerExtractionStrategy>emptyList());
    }

    public DefaultNodeInitializerRegistry(InstanceFactoryRegistry instanceFactoryRegistry, List<NodeInitializerExtractionStrategy> strategies) {
        this.strategies = ImmutableList.<NodeInitializerExtractionStrategy>builder()
            .addAll(strategies)
            .add(new FactoryBasedNodeInitializerExtractionStrategy(instanceFactoryRegistry))
            .add(new ModelSetNodeInitializerExtractionStrategy())
            .add(new ManagedSetNodeInitializerExtractionStrategy())
            .add(new ModelMapNodeInitializerExtractionStrategy())
            .add(new ScalarCollectionNodeInitializerExtractionStrategy())
            .add(new ManagedImplStructNodeInitializerExtractionStrategy())
            .build();
    }

    @Override
    public <T> NodeInitializer getNodeInitializer(ManagedImplModelSchema<T> schema, ModelSchemaStore schemaStore) {
        for (NodeInitializerExtractionStrategy extractor : strategies) {
            NodeInitializer nodeInitializer = extractor.extractNodeInitializer(schema, schemaStore, this);
            if (nodeInitializer != null) {
                return nodeInitializer;
            }
        }
        throw new IllegalStateException("Cannot extract node initializer from schema " + schema);
    }
}
