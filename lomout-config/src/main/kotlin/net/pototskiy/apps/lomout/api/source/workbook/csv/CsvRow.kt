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

package net.pototskiy.apps.lomout.api.source.workbook.csv

import net.pototskiy.apps.lomout.api.source.workbook.Cell
import net.pototskiy.apps.lomout.api.source.workbook.CellAddress
import net.pototskiy.apps.lomout.api.source.workbook.Row
import net.pototskiy.apps.lomout.api.source.workbook.Sheet
import org.apache.commons.csv.CSVRecord

/**
 * CSV workbook row
 *
 * @property backingRow Int
 * @property backingSheet CsvSheet
 * @property cells MutableList<CsvCell?>
 * @property sheet Sheet
 * @property rowNum Int
 * @constructor
 */
class CsvRow(
    private val backingRow: Int,
    data: CSVRecord?,
    private val backingSheet: CsvSheet
) : Row {
    private val cells: MutableList<CsvCell?> = mutableListOf()

    init {
        data?.forEachIndexed { c, value ->
            cells.add(CsvCell(CellAddress(backingRow, c), value, this))
        }
    }

    /**
     * Get cell by the index or create empty cell
     *
     * @param column The column index
     * @return The cell from row or empty cell
     */
    override fun getOrEmptyCell(column: Int): Cell = get(column)
        ?: CsvCell(
            CellAddress(
                backingRow,
                column
            ), "", this
        )

    /**
     * Row sheet
     */
    override val sheet: Sheet
        get() = backingSheet
    /**
     * Row number (index)
     */
    override val rowNum: Int
        get() = backingRow

    /**
     * Get cell by the index or null
     *
     * @param column The column index
     * @return The row cell or null if it does not exist
     */
    override fun get(column: Int): CsvCell? =
        if (column < cells.size) {
            cells[column]
        } else {
            null
        }

    /**
     * Insert cell to row by the index
     *
     * @param column Int The cell index, zero based
     * @return Cell The inserted cell
     */
    override fun insertCell(column: Int): Cell {
        checkThatItIsCsvOutputWorkbook(backingSheet.workbook as CsvWorkbook)
        val newCell = CsvCell(CellAddress(backingRow, column), "", this)
        cells.add(column, newCell)
        cells.forEachIndexed { c, cell ->
            cell?.address?.column = c
        }
        return newCell
    }

    /**
     * Get number of cell in row
     *
     * @return Int
     */
    override fun countCell(): Int = cells.size

    /**
     * Get cell iterator
     *
     * @return Iterator<CsvCell?>
     */
    override fun iterator(): Iterator<CsvCell?> =
        CsvCellIterator(this)
}
