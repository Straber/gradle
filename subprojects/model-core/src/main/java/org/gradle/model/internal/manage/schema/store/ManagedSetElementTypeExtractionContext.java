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

package org.gradle.model.internal.manage.schema.store;

import org.gradle.model.collection.ManagedSet;
import org.gradle.model.internal.core.ModelType;

public class ManagedSetElementTypeExtractionContext extends AbstractModelSchemaExtractionContext {

    ManagedSetElementTypeExtractionContext(ModelType<? extends ManagedSet<?>> setType, ModelSchemaExtractionContext parent) {
        super(setType, setType.getTypeVariables().get(0), parent);
    }

    @Override
    protected String getWrappingExceptionMessage() {
        return String.format("type parameter of %s has to be a valid managed type", ManagedSet.class.getName());
    }
}