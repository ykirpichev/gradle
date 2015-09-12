/*
 * Copyright 2014 the original author or authors.
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

import org.gradle.model.internal.core.*;
import org.gradle.model.internal.type.ModelType;

import java.util.Collections;
import java.util.List;

public class ModelCollectionSchema<T, E> extends AbstractModelSchema<T> implements ManagedImplModelSchema<T> {

    private final ModelType<T> type;
    private final ModelType<E> elementType;
    private final ModelProjection projection;
    private final NodeInitializer nodeInitializer;

    public ModelCollectionSchema(ModelType<T> type, ModelType<E> elementType, ModelProjection projection) {
        super(type);
        this.type = type;
        this.elementType = elementType;
        this.projection = projection;
        this.nodeInitializer = new NodeInitializer() {
            @Override
            public List<? extends ModelProjection> getProjections() {
                return Collections.singletonList(ModelCollectionSchema.this.projection);
            }

            @Override
            public void execute(MutableModelNode modelNode, List<ModelView<?>> inputs) {
            }

            @Override
            public List<? extends ModelReference<?>> getInputs() {
                return Collections.emptyList();
            }
        };
    }

    public ModelType<E> getElementType() {
        return elementType;
    }

    @Override
    public NodeInitializer getNodeInitializer() {
        return nodeInitializer;
    }

    @Override
    public String toString() {
        return "collection " + getType();
    }
}
