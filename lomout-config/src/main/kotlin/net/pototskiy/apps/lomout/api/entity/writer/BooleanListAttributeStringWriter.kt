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

import net.pototskiy.apps.lomout.api.callable.AttributeWriter
import net.pototskiy.apps.lomout.api.LomoutContext
import net.pototskiy.apps.lomout.api.entity.values.CSVValueFormat
import net.pototskiy.apps.lomout.api.source.workbook.Cell
import java.io.ByteArrayOutputStream

/**
 * Default writer for List<Boolean> attribute
 *
 * * true → 1
 * * false → 0
 *
 * @property locale String The value locale, ignored. This is parameter
 * @property quotes Char? The value quote, optional. This is parameter
 * @property delimiter Char The list delimiter, default:','. This is parameter
 */
open class BooleanListAttributeStringWriter : AttributeWriter<List<Boolean>?>() {
    var locale: String? = null
    var quotes: Char? = null
    var delimiter: Char = ','

    override operator fun invoke(value: List<Boolean>?, cell: Cell, context: LomoutContext) {
        value?.let { list ->
            val listValue = ByteArrayOutputStream().use { stream ->
                stream.writer().use { writer ->
                    CSVValueFormat(delimiter, quotes, '\\').format
                        .print(writer)
                        .printRecord(list.map { if (it) "1" else "0" })
                }
                stream.toString()
            }
            cell.setCellValue(listValue)
        }
    }
}
