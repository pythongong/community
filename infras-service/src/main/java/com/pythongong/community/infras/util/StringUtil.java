/*
 * Copyright 2025 Cheng Gong
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.pythongong.community.infras.util;

/**
 * Utility class for string operations.
 * This class provides common string manipulation and validation methods
 * used throughout the framework.
 *
 * @author Cheng Gong
 */
public class StringUtil {

    /** Private constructor to prevent instantiation */
    private StringUtil() {
    }

    public static String EMPTY = "";

    /**
     * Checks if a string is null, empty, or contains only whitespace.
     *
     * @param str the string to check
     * @return true if the string is null, empty, or contains only whitespace;
     *         false otherwise
     */
    public static boolean isEmpty(String str) {
        return str == null || str.isBlank();
    }
}
