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

package org.gradle.api.internal.rules;

import org.gradle.api.Action;
import org.gradle.internal.BiAction;
import org.gradle.model.ModelMap;
import org.gradle.model.collection.internal.PolymorphicModelMapProjection;
import org.gradle.model.internal.core.*;
import org.gradle.model.internal.core.rule.describe.ModelRuleDescriptor;
import org.gradle.model.internal.type.ModelType;

import java.util.List;

public class ModelMapCreators {

    public static <T, C extends ModelMap<T>> ModelCreator specialized(ModelPath path,
                                                                      Class<T> typeClass,
                                                                      Class<C> containerClass,
                                                                      final Class<? extends C> viewClass,
                                                                      NodeInitializerRegistry nodeInitializerRegistry,
                                                                      ModelRuleDescriptor descriptor) {
        final ModelType<C> containerType = ModelType.of(containerClass);
        final ModelType<T> modelType = ModelType.of(typeClass);
        return ModelCreators.of(
            path,
            new Action<MutableModelNode>() {
                @Override
                public void execute(MutableModelNode modelNode) {
                    modelNode.setPrivateData(containerType, null);
                }
            })
            .action(ModelActionRole.DefineProjections, ModelReference.of(NodeInitializerRegistry.class), new BiAction<MutableModelNode, List<ModelView<?>>>() {
                @Override
                public void execute(MutableModelNode node, List<ModelView<?>> modelViews) {
                    NodeInitializerRegistry nodeInitializerRegistry = (NodeInitializerRegistry) modelViews.get(0).getInstance();
                    final ChildNodeInitializerStrategy<T> childFactory = NodeBackedModelMap.createUsingRegistry(modelType, nodeInitializerRegistry);
                    node.addProjection(new SpecializedModelMapProjection<C, T>(containerType, modelType, viewClass, childFactory));
                    node.addProjection(PolymorphicModelMapProjection.of(modelType, childFactory));
                }
            })
            .descriptor(descriptor)
            .build();
    }
}
