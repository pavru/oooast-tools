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

import net.pototskiy.apps.lomout.api.source.workbook.Cell
import net.pototskiy.apps.lomout.api.source.workbook.CellAddress
import net.pototskiy.apps.lomout.api.source.workbook.Row
import net.pototskiy.apps.lomout.api.source.workbook.Sheet

/**
 * Attribute workbook row
 *
 * @property backingRow Int
 * @property backingData MutableList<NestedAttributeCell>
 * @property backingSheet NestedAttributeSheet
 * @property sheet Sheet
 * @property rowNum Int
 * @constructor
 * @param backingRow Int The row number(index)
 * @param backingData MutableList<NestedAttributeCell> Row cells
 * @param backingSheet NestedAttributeSheet The row sheet
 */
class NestedAttributeRow(
    private val backingRow: Int,
    private val backingData: MutableList<NestedAttributeCell>,
    private val backingSheet: NestedAttributeSheet
) : Row {
    /**
     * Insert cell into row, by the index
     *
     * @param column Int The row index, zero based
     * @return Cell
     */
    override fun insertCell(column: Int): Cell {
        val cell = NestedAttributeCell(CellAddress(backingRow, column), "", this)
        backingData.add(column, cell)
        return cell
    }

    /**
     * Get cell by the index, return empty cell it does exist in row
     *
     * @param column Int The cell index(column)
     * @return Cell
     */
    override fun getOrEmptyCell(column: Int): Cell = get(column)

    /**
     * Row sheet
     */
    override val sheet: Sheet
        get() = backingSheet
    /**
     * Row number(index)
     */
    override val rowNum: Int
        get() = backingRow

    /**
     * Get row cells count
     *
     * @return Int
     */
    override fun countCell(): Int = backingData.count()

    /**
     * Get row cell by the index
     *
     * @param column Int The cell index(column), zero based
     * @return Cell
     */
    override operator fun get(column: Int): Cell = backingData[column]

    /**
     * Get cells iterator
     *
     * @return Iterator<Cell?>
     */
    override fun iterator(): Iterator<Cell?> = AttributeCellIterator(this)
}
