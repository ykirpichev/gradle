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

package org.gradle.model.internal.registry;

import org.gradle.api.Action;
import org.gradle.model.internal.core.ModelInitializer;

import java.util.Collection;
import java.util.List;

public class InitializerRuleBinder extends RuleBinder {
    private final ModelInitializer action;
    private final String type;
    private final ModelBinding subjectBinding;

    public InitializerRuleBinder(final String type, ModelInitializer action, BindingPredicate subjectReference, List<BindingPredicate> inputReferences, Collection<RuleBinder> binders) {
        super(subjectReference, inputReferences, action.getDescriptor(), binders);
        this.type = type;
        this.action = action;
        this.subjectBinding = binding(subjectReference, true, new Action<ModelBinding>() {
            @Override
            public void execute(ModelBinding modelBinding) {
                ModelNodeInternal node = modelBinding.getNode();
                BindingPredicate predicate = modelBinding.getPredicate();
                if (predicate.getState() != null && node.isAtLeast(predicate.getState())) {
                    throw new IllegalStateException(String.format("Cannot add %s rule %s for model element '%s' at state %s as this element is already at state %s.",
                        type,
                        modelBinding.referrer,
                        node.getPath(),
                        predicate.getState().previous(),
                        node.getState()
                    ));
                }
                maybeFire();
            }
        });
    }

    public ModelInitializer getAction() {
        return action;
    }

    public String getType() {
        return type;
    }

    @Override
    public ModelBinding getSubjectBinding() {
        return subjectBinding;
    }

    @Override
    public boolean isBound() {
        return subjectBinding != null && subjectBinding.isBound() && super.isBound();
    }
}
