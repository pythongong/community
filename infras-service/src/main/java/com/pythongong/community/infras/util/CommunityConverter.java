/*
 * Copyright 2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pythongong.community.infras.util;

import lombok.NonNull;

/**
 * Functional interface for converting source class to another class.
 * 
 * <p>
 * Provides a simple conversion mechanism from source type representations
 * to their corresponding types.
 *
 * @author pythongong
 * @since 1.0
 */
@FunctionalInterface
public interface CommunityConverter<S, T> {

    /**
     * Converts a source type to its corresponding object representation.
     * 
     * @param source the source type to be converted
     * @return the converted type object
     */
    T convert(@NonNull S source);
}