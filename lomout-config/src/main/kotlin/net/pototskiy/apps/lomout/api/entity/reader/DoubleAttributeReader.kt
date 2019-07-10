/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package net.pototskiy.apps.lomout.api.entity.reader

import net.pototskiy.apps.lomout.api.AppDataException
import net.pototskiy.apps.lomout.api.DEFAULT_LOCALE_STR
import net.pototskiy.apps.lomout.api.createLocale
import net.pototskiy.apps.lomout.api.document.DocumentMetadata
import net.pototskiy.apps.lomout.api.plugable.AttributeReader
import net.pototskiy.apps.lomout.api.plus
import net.pototskiy.apps.lomout.api.source.workbook.Cell

/**
 * Default reader for [Double] attribute
 *
 * @property locale The value locale: default: system locale. This is parameter
 * @property groupingUsed Indicate that number uses digit grouping. This is parameter
 */
@Suppress("MemberVisibilityCanBePrivate")
open class DoubleAttributeReader : AttributeReader<Double?>() {
    var locale: String = DEFAULT_LOCALE_STR
    var groupingUsed: Boolean = false

    override fun read(attribute: DocumentMetadata.Attribute, input: Cell): Double? {
        try {
            return input.readDouble(locale.createLocale(), groupingUsed)?.let { it }
        } catch (e: AppDataException) {
            throw AppDataException(e.place + attribute, e.message, e)
        }
    }
}