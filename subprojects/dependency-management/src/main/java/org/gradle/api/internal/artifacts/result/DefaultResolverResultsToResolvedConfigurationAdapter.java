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
package org.gradle.api.internal.artifacts.result;

import org.gradle.api.artifacts.*;
import org.gradle.api.internal.artifacts.ResolveContext;
import org.gradle.api.internal.artifacts.ResolverResults;
import org.gradle.api.internal.artifacts.ResolverResultsBuilder;
import org.gradle.api.internal.artifacts.configurations.ConfigurationInternal;
import org.gradle.api.internal.artifacts.ivyservice.CacheLockingManager;
import org.gradle.api.internal.artifacts.ivyservice.DefaultLenientConfiguration;
import org.gradle.api.internal.artifacts.ivyservice.DefaultResolvedConfiguration;
import org.gradle.api.internal.artifacts.ivyservice.ShortcircuitEmptyConfigsArtifactDependencyResolver;
import org.gradle.api.internal.artifacts.ivyservice.resolveengine.oldresult.ResolvedArtifactResults;
import org.gradle.api.internal.artifacts.ivyservice.resolveengine.oldresult.ResolvedGraphResults;
import org.gradle.api.internal.artifacts.ivyservice.resolveengine.oldresult.TransientConfigurationResults;
import org.gradle.api.internal.artifacts.ivyservice.resolveengine.oldresult.TransientConfigurationResultsLoader;
import org.gradle.api.specs.Spec;
import org.gradle.internal.Factory;

import java.io.File;
import java.util.Set;

public class DefaultResolverResultsToResolvedConfigurationAdapter implements ResolverResultsToResolvedConfigurationAdapter {
    private final CacheLockingManager cacheLockingManager;

    public DefaultResolverResultsToResolvedConfigurationAdapter(CacheLockingManager cacheLockingManager) {
        this.cacheLockingManager = cacheLockingManager;
    }

    @Override
    public ResolvedConfiguration toResolvedConfiguration(ResolverResults results, ConfigurationInternal configuration) {
        if (results.hasError()) {
            return new BrokenResolvedConfiguration(results.getFailure(), configuration);
        }
        if (configuration.getDependencies().isEmpty()) {
            return new ShortcircuitEmptyConfigsArtifactDependencyResolver.EmptyResolvedConfiguration();
        }

        ResolverResultsBuilder resultsBuilder = (ResolverResultsBuilder) results;

        ResolvedGraphResults graphResults = resultsBuilder.getGraphResults();
        ResolvedArtifactResults artifactResults = resultsBuilder.getResolvedArtifactsBuilder().resolve();
        Factory<TransientConfigurationResults> transientConfigurationResultsFactory =
            new TransientConfigurationResultsLoader(resultsBuilder.getTransientConfigurationResultsBuilder(), graphResults, artifactResults);

        DefaultLenientConfiguration result = new DefaultLenientConfiguration(
            configuration, cacheLockingManager, graphResults, artifactResults, transientConfigurationResultsFactory);
        return new DefaultResolvedConfiguration(result);
    }

    private static class BrokenResolvedConfiguration implements ResolvedConfiguration {
        private final Throwable e;
        private final ResolveContext resolveContext;

        public BrokenResolvedConfiguration(Throwable e, ResolveContext resolveContext) {
            this.e = e;
            this.resolveContext = resolveContext;
        }

        public boolean hasError() {
            return true;
        }

        public LenientConfiguration getLenientConfiguration() {
            throw ResolveException.wrap(e, resolveContext);
        }

        public void rethrowFailure() throws ResolveException {
            throw ResolveException.wrap(e, resolveContext);
        }

        public Set<File> getFiles(Spec<? super Dependency> dependencySpec) throws ResolveException {
            throw ResolveException.wrap(e, resolveContext);
        }

        public Set<ResolvedDependency> getFirstLevelModuleDependencies() throws ResolveException {
            throw ResolveException.wrap(e, resolveContext);
        }

        public Set<ResolvedDependency> getFirstLevelModuleDependencies(Spec<? super Dependency> dependencySpec) throws ResolveException {
            throw ResolveException.wrap(e, resolveContext);
        }

        public Set<ResolvedArtifact> getResolvedArtifacts() throws ResolveException {
            throw ResolveException.wrap(e, resolveContext);
        }
    }
}
