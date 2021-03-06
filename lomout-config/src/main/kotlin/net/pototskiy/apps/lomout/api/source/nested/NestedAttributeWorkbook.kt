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

package net.pototskiy.apps.lomout.api.source.nested

import net.pototskiy.apps.lomout.api.DEFAULT_LOCALE
import net.pototskiy.apps.lomout.api.source.workbook.CellAddress
import net.pototskiy.apps.lomout.api.source.workbook.Sheet
import net.pototskiy.apps.lomout.api.source.workbook.Workbook
import net.pototskiy.apps.lomout.api.source.workbook.WorkbookType
import java.util.*

/**
 * Workbook to work with nested attribute
 *
 * @property attributeName String
 * @property parser NestedAttributeListParser
 * @property printer NestedAttributeListPrinter
 * @property cells Array<MutableList<NestedAttributeCell>>
 * @property string String
 * @property name String
 * @property type WorkbookType
 * @constructor
 * @param quote Char? The name-value pair quote, null — no quote
 * @param delimiter Char The name-value pairs delimiter
 * @param valueQuote Char? The value quote, null — no quote
 * @param valueDelimiter Char The name-value delimiter
 * @param attributeName String The owner attribute
 */
class NestedAttributeWorkbook(
    quote: Char?,
    delimiter: Char,
    escape: Char?,
    valueQuote: Char?,
    valueDelimiter: Char,
    valueEscape: Char?,
    private val attributeName: String,
    override val locale: Locale = DEFAULT_LOCALE
) : Workbook {
    private val parser = NestedAttributeListParser(quote, delimiter, escape, valueQuote, valueDelimiter, valueEscape)
    private val printer = NestedAttributeListPrinter(quote, delimiter, escape, valueQuote, valueDelimiter, valueEscape)

    /**
     * Cell of name-value pair
     *
     * The first row contains nested attribute names, the second row contains nested attribute values.
     */
    val cells = Array<MutableList<NestedAttributeCell>>(2) { mutableListOf() }

    /**
     * String that present nested attribute name-value pairs
     *
     * When string is set it is parsed to cells
     * When string is read it is printed from cells
     */
    var string: String
        set(value) {
            cells.forEach { it.clear() }
            val sheet = NestedAttributeSheet(this)
            val rows = arrayOf(
                NestedAttributeRow(0, cells[0], sheet),
                NestedAttributeRow(1, cells[1], sheet)
            )
            var column = 0
            parser.parse(value).forEach { (key, valueData) ->
                cells[0].add(column, NestedAttributeCell(CellAddress(0, column), key, rows[0]))
                cells[1].add(column, NestedAttributeCell(CellAddress(1, column), valueData, rows[1]))
                column++
            }
        }
        get() {
            return printer.print(
                cells[0].mapIndexed { c, attributeCell ->
                    attributeCell.stringValue to cells[1][c].stringValue
                }.toMap()
            )
        }

    /**
     * Insert sheet into the workbook
     *
     * @param sheet String
     * @return Sheet
     */
    override fun insertSheet(sheet: String): Sheet = NestedAttributeSheet(this)

    /**
     * Workbook name
     */
    override val name: String
        get() = "workbook_for_$attributeName"
    /**
     * Workbook type, [WorkbookType.ATTRIBUTE]
     */
    override val type: WorkbookType
        get() = WorkbookType.ATTRIBUTE

    /**
     * Get sheet by name, always return the same sheet
     *
     * @param sheet String
     * @return Sheet
     */
    override fun get(sheet: String): Sheet = NestedAttributeSheet(this)

    /**
     * Get sheet by the index, always return the same sheet
     *
     * @param sheet Int The sheet index, zero based
     * @return Sheet
     */
    override fun get(sheet: Int): Sheet = NestedAttributeSheet(this)

    /**
     * Test if workbook has a sheet with given name, it's always true
     *
     * @param sheet String The sheet name
     * @return Boolean
     */
    override fun hasSheet(sheet: String): Boolean = true

    /**
     * Get workbook sheet iterator
     *
     * @return Iterator<Sheet>
     */
    override fun iterator(): Iterator<Sheet> = NestedAttributeSheetIterator(this)

    /**
     * Close workbook
     */
    @Suppress("EmptyFunctionBlock")
    override fun close() {
        // nothing to close workbook use string
    }
}
