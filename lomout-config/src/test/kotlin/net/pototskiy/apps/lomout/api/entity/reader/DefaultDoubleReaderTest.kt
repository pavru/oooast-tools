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
import net.pototskiy.apps.lomout.api.callable.AttributeReader
import net.pototskiy.apps.lomout.api.LomoutContext
import net.pototskiy.apps.lomout.api.document.Document
import net.pototskiy.apps.lomout.api.document.DocumentMetadata
import net.pototskiy.apps.lomout.api.document.SupportAttributeType
import net.pototskiy.apps.lomout.api.simpleTestContext
import net.pototskiy.apps.lomout.api.source.workbook.Cell
import net.pototskiy.apps.lomout.api.source.workbook.CellType
import net.pototskiy.apps.lomout.api.source.workbook.Workbook
import net.pototskiy.apps.lomout.api.source.workbook.csv.CsvCell
import net.pototskiy.apps.lomout.api.source.workbook.csv.CsvInputWorkbook
import net.pototskiy.apps.lomout.api.source.workbook.excel.ExcelWorkbook
import org.apache.commons.csv.CSVFormat
import org.apache.poi.hssf.usermodel.HSSFCell
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.hssf.usermodel.HSSFWorkbookFactory
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode

@Suppress("MagicNumber")
@Execution(ExecutionMode.CONCURRENT)
internal class DefaultDoubleReaderTest {
    internal class TestType : Document() {
        var attr: Double = 0.0

        companion object : DocumentMetadata(TestType::class)
    }

    private lateinit var xlsWorkbook: HSSFWorkbook
    private lateinit var workbook: Workbook
    private var attr = TestType.attributes.getValue("attr")
    private lateinit var xlsTestDataCell: HSSFCell
    private lateinit var inputCell: Cell

    @BeforeEach
    internal fun setUp() {
        LomoutContext.setContext(simpleTestContext)
        xlsWorkbook = HSSFWorkbookFactory.createWorkbook()
        val xlsSheet = xlsWorkbook.createSheet("test-data")
        xlsSheet.isActive = true
        xlsTestDataCell = xlsSheet.createRow(0).createCell(0)
        workbook = ExcelWorkbook(xlsWorkbook)
        inputCell = workbook["test-data"][0]!![0]!!
    }

    @AfterEach
    internal fun tearDown() {
        workbook.close()
    }

    @Test
    internal fun readBlankCellTest() {
        val reader = DoubleAttributeReader().apply { locale = "en_US" }
        xlsTestDataCell.setBlank()
        assertThat(reader(attr, inputCell)).isNull()
    }

    @Test
    internal fun readDoubleCellTest() {
        val reader = DoubleAttributeReader().apply { locale = "en_US" }
        xlsTestDataCell.setCellValue(1.1)
        assertThat(inputCell.cellType).isEqualTo(CellType.DOUBLE)
        assertThat(reader(attr, inputCell)).isEqualTo(1.1)
    }

    @Test
    internal fun lm57ReadDoubleScaleTest() {
        val reader = DoubleAttributeReader().apply { locale = "en_US"; scale = 3 }
        xlsTestDataCell.setCellValue(1.1)
        assertThat(inputCell.cellType).isEqualTo(CellType.DOUBLE)
        assertThat(reader(attr, inputCell)).isEqualTo(1.1)
        xlsTestDataCell.setCellValue(1.1234)
        assertThat(reader(attr, inputCell)).isEqualTo(1.123)
        xlsTestDataCell.setCellValue(1.1236)
        assertThat(reader(attr, inputCell)).isEqualTo(1.124)
        xlsTestDataCell.setCellValue("1.1234")
        assertThat(reader(attr, inputCell)).isEqualTo(1.123)
        xlsTestDataCell.setCellValue("1.1236")
        assertThat(reader(attr, inputCell)).isEqualTo(1.124)
    }

    @Test
    internal fun readLongCellTest() {
        val reader = DoubleAttributeReader().apply { locale = "en_US" }
        val cell = createCsvCell("11")
        assertThat(cell.cellType).isEqualTo(CellType.LONG)
        assertThat(reader(attr, cell)).isEqualTo(11.0)
    }

    @Test
    internal fun readBooleanCellTest() {
        val reader = DoubleAttributeReader().apply { locale = "en_US" }
        xlsTestDataCell.setCellValue(true)
        assertThat(inputCell.cellType).isEqualTo(CellType.BOOL)
        assertThat(reader(attr, inputCell)).isEqualTo(1.0)
        xlsTestDataCell.setCellValue(false)
        assertThat(inputCell.cellType).isEqualTo(CellType.BOOL)
        assertThat(reader(attr, inputCell)).isEqualTo(0.0)
    }

    @Test
    internal fun readStringEnUsCellTest() {
        val readerEnUs = DoubleAttributeReader().apply { locale = "en_US" }
        val readerRuRu = DoubleAttributeReader().apply { locale = "ru_RU" }
        xlsTestDataCell.setCellValue("1.1")
        assertThat(inputCell.cellType).isEqualTo(CellType.STRING)
        assertThat(readerEnUs(attr, inputCell)).isEqualTo(1.1)
        assertThatThrownBy { readerRuRu(attr, inputCell) }.isInstanceOf(AppDataException::class.java)
    }

    @Test
    internal fun readWithWorkbookLocaleTest() {
        xlsTestDataCell.setCellValue("3.3")
        val reader = DoubleAttributeReader().apply {
            locale = null
        }
        assertThat(reader(attr, inputCell)).isEqualTo(3.3)
    }

    @Test
    internal fun defaultDoubleReaderTest() {
        @Suppress("UNCHECKED_CAST")
        val reader = defaultReaders[SupportAttributeType.doubleType]
        assertThat(reader).isNotNull
        assertThat(reader).isInstanceOf(AttributeReader::class.java)
        reader as DoubleAttributeReader
        assertThat(reader.locale).isEqualTo(DEFAULT_LOCALE_STR)
    }

    private fun createCsvCell(value: String): CsvCell {
        val reader = value.byteInputStream().reader()
        CsvInputWorkbook(reader, CSVFormat.RFC4180).use {
            return it[0][0][0]!!
        }
    }
}
