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
import net.pototskiy.apps.lomout.api.MessageBundle.message
import net.pototskiy.apps.lomout.api.callable.AttributeReader
import net.pototskiy.apps.lomout.api.LomoutContext
import net.pototskiy.apps.lomout.api.createLocale
import net.pototskiy.apps.lomout.api.document.DocumentMetadata
import net.pototskiy.apps.lomout.api.entity.values.CSVValueFormat
import net.pototskiy.apps.lomout.api.entity.values.DEFAULT_DOUBLE_SCALE
import net.pototskiy.apps.lomout.api.entity.values.stringToDouble
import net.pototskiy.apps.lomout.api.plus
import net.pototskiy.apps.lomout.api.source.workbook.Cell
import net.pototskiy.apps.lomout.api.source.workbook.CellType
import net.pototskiy.apps.lomout.api.suspectedLocation
import java.text.ParseException

/**
 * Default reader for **List&lt;Double&gt;** attribute
 *
 * @property locale The value locale, default: system locale. This is parameter
 * @property quotes The value quote, optional. This is parameter
 * @property delimiter The list delimiter, default:','. This is parameter
 * @property groupingUsed Indicate that number uses digit grouping. This is parameter
 */
@Suppress("MemberVisibilityCanBePrivate")
open class DoubleListAttributeReader : AttributeReader<List<Double>?>() {
    var locale: String? = null
    var quotes: Char? = null
    var delimiter: Char = ','
    var groupingUsed: Boolean = false
    var scale: Int = DEFAULT_DOUBLE_SCALE

    override operator fun invoke(
        attribute: DocumentMetadata.Attribute,
        input: Cell,
        context: LomoutContext
    ): List<Double>? =
        when (input.cellType) {
            CellType.STRING -> {
                input.stringValue.reader().use { reader ->
                    try {
                        CSVValueFormat(delimiter, quotes, '\\').format
                            .parse(reader)
                            .records
                            .map { it.toList() }.flatten()
                            .map { it.stringToDouble(locale?.createLocale() ?: input.locale, groupingUsed, scale) }
                    } catch (e: ParseException) {
                        throw AppDataException(suspectedLocation(attribute) + input, e.message, e)
                    }
                }
            }
            CellType.BLANK -> null
            else -> throw AppDataException(
                suspectedLocation(input) + attribute, message("message.error.data.double.cannot_read")
            )
        }
}
