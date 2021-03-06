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
import net.pototskiy.apps.lomout.api.entity.values.stringToDateTime
import net.pototskiy.apps.lomout.api.plus
import net.pototskiy.apps.lomout.api.source.workbook.Cell
import net.pototskiy.apps.lomout.api.source.workbook.CellType
import net.pototskiy.apps.lomout.api.suspectedLocation
import java.time.LocalDateTime

/**
 * Default reader for **List&lt;LocalDateTime&gt;** attribute
 *
 * @property locale String The value locale. This is parameter
 * @property pattern String? The value pattern, optional (use locale). This is parameter
 * @property quotes Char? The value quote, optional. This is parameter
 * @property delimiter Char The list delimiter, default:','. This is parameter
 */
open class DateTimeListAttributeReader : AttributeReader<List<LocalDateTime>?>() {
    var locale: String? = null
    var pattern: String? = null
    var quotes: Char? = null
    var delimiter: Char = ','

    override operator fun invoke(
        attribute: DocumentMetadata.Attribute,
        input: Cell,
        context: LomoutContext
    ): List<LocalDateTime>? =
        when (input.cellType) {
            CellType.STRING -> {
                input.stringValue.reader().use { reader ->
                    CSVValueFormat(delimiter, quotes, '\\').format
                        .parse(reader)
                        .records
                        .map { it.toList() }.flatten()
                        .map { data ->
                            pattern?.let { data.stringToDateTime(it) }
                                ?: data.stringToDateTime(locale?.createLocale() ?: input.locale)
                        }
                }
            }
            CellType.BLANK -> null
            else -> throw AppDataException(
                suspectedLocation(input) + attribute, message("message.error.data.datetimelist.cannot_read")
            )
        }
}
