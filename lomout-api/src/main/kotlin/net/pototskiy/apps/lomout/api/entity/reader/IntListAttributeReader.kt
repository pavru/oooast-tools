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
import net.pototskiy.apps.lomout.api.MessageBundle.message
import net.pototskiy.apps.lomout.api.badPlace
import net.pototskiy.apps.lomout.api.createLocale
import net.pototskiy.apps.lomout.api.document.DocumentMetadata
import net.pototskiy.apps.lomout.api.entity.values.stringToLong
import net.pototskiy.apps.lomout.api.plugable.AttributeReader
import net.pototskiy.apps.lomout.api.plus
import net.pototskiy.apps.lomout.api.source.workbook.Cell
import net.pototskiy.apps.lomout.api.source.workbook.CellType
import org.apache.commons.csv.CSVFormat
import java.text.ParseException

/**
 * Default reader for **List&lt;Int&gt;** attribute
 *
 * @property locale The value locale. This is parameter
 * @property quote The value quote, optional. This is parameter
 * @property delimiter The list delimiter, default:','. This is parameter
 * @property groupingUsed Indicate that number uses digit grouping. This is parameter
 */
@Suppress("MemberVisibilityCanBePrivate")
open class IntListAttributeReader : AttributeReader<List<Int>?>() {
    var locale: String = DEFAULT_LOCALE_STR
    var quote: Char? = null
    var groupingUsed: Boolean = false
    var delimiter: Char = ','

    override fun read(attribute: DocumentMetadata.Attribute, input: Cell): List<Int>? = when (input.cellType) {
        CellType.STRING -> {
            input.stringValue.reader().use { reader ->
                try {
                    CSVFormat.RFC4180
                        .withQuote(quote)
                        .withDelimiter(delimiter)
                        .withRecordSeparator("")
                        .parse(reader)
                        .records
                        .map { it.toList() }.flatten()
                        .map {
                            it.stringToLong(locale.createLocale(), groupingUsed).toInt()
                        }
                } catch (e: ParseException) {
                    throw AppDataException(badPlace(attribute) + input, e.message, e)
                }
            }
        }
        CellType.BLANK -> null
        CellType.LONG -> listOf(input.longValue.toInt())
        else -> throw AppDataException(
            badPlace(input) + attribute, message("message.error.data.int_list.reading_not_supported")
        )
    }
}
