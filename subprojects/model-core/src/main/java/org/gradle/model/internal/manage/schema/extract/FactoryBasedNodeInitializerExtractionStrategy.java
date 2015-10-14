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

import com.google.common.collect.ImmutableSet;
import org.gradle.model.internal.core.*;
import org.gradle.model.internal.manage.schema.ModelSchema;
import org.gradle.model.internal.type.ModelType;

public class FactoryBasedNodeInitializerExtractionStrategy<T> implements NodeInitializerExtractionStrategy {
    private final InstanceFactory<T> instanceFactory;

    public FactoryBasedNodeInitializerExtractionStrategy(InstanceFactory<T> instanceFactory) {
        this.instanceFactory = instanceFactory;
    }

    @Override
    public <S> NodeInitializer extractNodeInitializer(ModelSchema<S> schema) {
        ModelType<S> type = schema.getType();
        ModelType<? extends T> typeAsBaseType = instanceFactory.getBaseType().asSubclass(type);
        if (typeAsBaseType == null) {
            return null;
        }
        return getNodeInitializer(typeAsBaseType);
    }

    private <S extends T> FactoryBasedNodeInitializer<T, S> getNodeInitializer(ModelType<S> type) {
        return new FactoryBasedNodeInitializer<T, S>(instanceFactory, type);
    }

    @Override
    public Iterable<ModelType<?>> supportedTypes() {
        return ImmutableSet.<ModelType<?>>copyOf(instanceFactory.getSupportedTypes());
    }
}
