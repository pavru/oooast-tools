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

package net.pototskiy.apps.lomout.api.entity.writer

import net.pototskiy.apps.lomout.api.CSV_SHEET_NAME
import net.pototskiy.apps.lomout.api.callable.AttributeWriter
import net.pototskiy.apps.lomout.api.LomoutContext
import net.pototskiy.apps.lomout.api.document.Document
import net.pototskiy.apps.lomout.api.entity.writer
import net.pototskiy.apps.lomout.api.source.nested.NestedAttributeSheet
import net.pototskiy.apps.lomout.api.source.nested.NestedAttributeWorkbook
import net.pototskiy.apps.lomout.api.source.workbook.Cell

/**
 * Default writer for [Document] attribute
 *
 * @property quotes Char? The name-value pair quote, optional. This is parameter
 * @property delimiter Char The pair list delimiter, default:','. This is parameter
 * @property valueQuote Char? The value quote, optional. This is parameter
 * @property valueDelimiter Char The delimiter between name and value, default:','. This is parameter
 * @property serializeNull True — all attribute including null will be written to cell
 * @property escape The escape char like in CSV format. This is parameter.
 * @property valueEscape The escape char for value like in CSV format. This is parameter.
 */
open class DocumentAttributeStringWriter : AttributeWriter<Document?>() {
    var quotes: Char? = null
    var delimiter: Char = ','
    var escape: Char? = '\\'
    var valueQuote: Char? = null
    var valueDelimiter: Char = '='
    var valueEscape: Char? = '\\'
    @Suppress("MemberVisibilityCanBePrivate")
    var serializeNull: Boolean = true

    override operator fun invoke(value: Document?, cell: Cell, context: LomoutContext) {
        val workbook = NestedAttributeWorkbook(
            quotes,
            delimiter,
            escape,
            valueQuote,
            valueDelimiter,
            valueEscape,
            "attributeWriter"
        )
        val sheet = workbook[CSV_SHEET_NAME] as NestedAttributeSheet
        val rows = arrayOf(sheet[0], sheet[1])
        var column = 0
        value?.documentMetadata?.attributes?.values?.forEach {
            val attrValue = value.getAttribute(it.name)
            if (attrValue != null || serializeNull) {
                rows[0].insertCell(column).setCellValue(it.name)
                @Suppress("UNCHECKED_CAST")
                (it.writer as AttributeWriter<Any?>)(attrValue, rows[1].insertCell(column))
                column++
            }
        }
        cell.setCellValue(workbook.string)
    }
}
