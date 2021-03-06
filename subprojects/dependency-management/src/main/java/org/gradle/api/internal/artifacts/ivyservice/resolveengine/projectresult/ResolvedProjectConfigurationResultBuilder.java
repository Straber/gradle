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
package org.gradle.api.internal.artifacts.ivyservice.resolveengine.projectresult;

import org.gradle.api.artifacts.component.ComponentIdentifier;
import org.gradle.api.artifacts.component.ProjectComponentIdentifier;

import java.util.Collections;

public interface ResolvedProjectConfigurationResultBuilder {
    void registerRoot(ComponentIdentifier componentId);
    void addProjectComponentResult(ProjectComponentIdentifier componentId, String configurationName);
    ResolvedProjectConfigurationResults complete();

    static ResolvedProjectConfigurationResultBuilder NOOP_BUILDER = new ResolvedProjectConfigurationResultBuilder() {

        @Override
        public void registerRoot(ComponentIdentifier componentId) {
        }

        @Override
        public void addProjectComponentResult(ProjectComponentIdentifier componentId, String configurationName) {
        }

        @Override
        public ResolvedProjectConfigurationResults complete() {
            return new DefaultResolvedProjectConfigurationResults(Collections.<ResolvedProjectConfigurationResult>emptySet());
        }
    };
}
