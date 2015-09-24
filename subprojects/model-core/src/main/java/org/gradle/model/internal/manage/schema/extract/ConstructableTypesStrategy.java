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

package org.gradle.model.internal.manage.schema.extract;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import net.jcip.annotations.ThreadSafe;
import org.gradle.model.internal.manage.schema.ConstructableTypeSchema;
import org.gradle.model.internal.manage.schema.ModelSchemaStore;
import org.gradle.model.internal.type.ModelType;

@ThreadSafe
public class ConstructableTypesStrategy implements ModelSchemaExtractionStrategy {
    ConstructableTypesRegistry constructableTypesRegistry;

    public ConstructableTypesStrategy(ConstructableTypesRegistry constructableTypesRegistry) {
        this.constructableTypesRegistry = constructableTypesRegistry;
    }

    public <T> void extract(final ModelSchemaExtractionContext<T> extractionContext, ModelSchemaStore store) {
        ModelType<T> type = extractionContext.getType();
        Optional<ModelType<?>> modelTypeOptional = Iterables.tryFind(constructableTypesRegistry.supportedTypes(), new Predicate<ModelType<?>>() {
            @Override
            public boolean apply(ModelType<?> input) {
                if (input.getRawClass() == extractionContext.getType().getRawClass()) {
                    return true;
                }
                return false;
            }
        });
        if (modelTypeOptional.isPresent()) {
            extractionContext.found(new ConstructableTypeSchema<T>(type));
        }
    }
}
