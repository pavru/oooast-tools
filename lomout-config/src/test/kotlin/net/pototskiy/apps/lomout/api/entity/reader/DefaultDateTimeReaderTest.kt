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
import net.pototskiy.apps.lomout.api.LomoutContext
import net.pototskiy.apps.lomout.api.callable.AttributeReader
import net.pototskiy.apps.lomout.api.createLocale
import net.pototskiy.apps.lomout.api.document.Document
import net.pototskiy.apps.lomout.api.document.DocumentMetadata
import net.pototskiy.apps.lomout.api.document.SupportAttributeType
import net.pototskiy.apps.lomout.api.entity.values.millis
import net.pototskiy.apps.lomout.api.entity.values.toDate
import net.pototskiy.apps.lomout.api.simpleTestContext
import net.pototskiy.apps.lomout.api.source.workbook.Cell
import net.pototskiy.apps.lomout.api.source.workbook.CellType
import net.pototskiy.apps.lomout.api.source.workbook.Workbook
import net.pototskiy.apps.lomout.api.source.workbook.csv.CsvCell
import net.pototskiy.apps.lomout.api.source.workbook.csv.CsvInputWorkbook
import net.pototskiy.apps.lomout.api.source.workbook.excel.ExcelWorkbook
import org.apache.commons.csv.CSVFormat
import org.apache.poi.hssf.usermodel.HSSFCell
import org.apache.poi.hssf.usermodel.HSSFDateUtil
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.hssf.usermodel.HSSFWorkbookFactory
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlin.reflect.full.isSubtypeOf

@Suppress("MagicNumber")
@Execution(ExecutionMode.CONCURRENT)
internal class DefaultDateTimeReaderTest {
    internal class TestType : Document() {
        var attr: LocalDateTime = LocalDateTime.MIN

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
    internal fun readDoubleCellTest() {
        val expected = LocalDateTime.now()
            .withHour(7)
            .withMinute(21)
            .withSecond(0)
            .withNano(0)
        val readerEnUs = DateTimeAttributeReader().apply { locale = "en_US" }
        val readerRuRu = DateTimeAttributeReader().apply { locale = "ru_RU" }
        val readerWithPattern = DateTimeAttributeReader().apply { pattern = "d.M.yy h:m" }
        xlsTestDataCell.setCellValue(HSSFDateUtil.getExcelDate(expected.toDate()))
        assertThat(inputCell.cellType).isEqualTo(CellType.DOUBLE)
        assertThat(readerEnUs(attr, inputCell)).isEqualTo(expected)
        assertThat(readerRuRu(attr, inputCell)).isEqualTo(expected)
        assertThat(readerWithPattern(attr, inputCell)).isEqualTo(expected)
    }

    @Test
    internal fun readLongCellTest() {
        val expected = LocalDateTime.now()
            .withHour(7)
            .withMinute(21)
            .withSecond(0)
            .withNano(0)
        val readerEnUs = DateTimeAttributeReader().apply { locale = "en_US" }
        val readerRuRu = DateTimeAttributeReader().apply { locale = "ru_RU" }
        val readerWithPattern = DateTimeAttributeReader().apply { pattern = "d.M.yy h:m" }
        val cell = createCsvCell(expected.millis.toString())
        assertThat(cell.cellType).isEqualTo(CellType.LONG)
        assertThat(readerEnUs(attr, cell)).isEqualTo(expected)
        assertThat(readerRuRu(attr, cell)).isEqualTo(expected)
        assertThat(readerWithPattern(attr, cell)).isEqualTo(expected)
    }

    @Test
    internal fun readStringCellWithLocaleTest() {
        val expected = LocalDateTime.now()
            .withHour(7)
            .withMinute(21)
            .withSecond(0)
            .withNano(0)
        val readerEnUs = DateTimeAttributeReader().apply { locale = "en_US" }
        val readerRuRu = DateTimeAttributeReader().apply { locale = "ru_RU" }
        val expectedText = expected.format(
            DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).withLocale("en_US".createLocale())
        )
        xlsTestDataCell.setCellValue(
            expected.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).withLocale("en_US".createLocale()))
        )
        assertThat(inputCell.cellType).isEqualTo(CellType.STRING)
        assertThat(readerEnUs(attr, inputCell)).isEqualTo(expected)
        assertThatThrownBy { readerRuRu(attr, inputCell) }
            .isInstanceOf(AppDataException::class.java)
            .hasMessageContaining("String '$expectedText' cannot be converted to date-time with the locale 'ru_RU'.")
        xlsTestDataCell.setCellValue(
            expected.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).withLocale("ru_RU".createLocale()))
        )
        assertThat(inputCell.cellType).isEqualTo(CellType.STRING)
        assertThatThrownBy { readerEnUs(attr, inputCell) }.isInstanceOf(AppDataException::class.java)
        assertThat(readerRuRu(attr, inputCell)).isEqualTo(expected)
    }

    @Test
    internal fun readStringCellWithPatternTest() {
        val expected = LocalDateTime.now()
            .withHour(7)
            .withMinute(21)
            .withSecond(0)
            .withNano(0)
        val readerEnUs = DateTimeAttributeReader().apply { pattern = "M/d/uu a h:m" }
        val readerRuRu = DateTimeAttributeReader().apply { pattern = "d.M.uu a h:m" }
        val datetimeString = expected.format(DateTimeFormatter.ofPattern("M/d/uu a h:m"))
        xlsTestDataCell.setCellValue(expected.format(DateTimeFormatter.ofPattern("M/d/uu a h:m")))
        assertThat(inputCell.cellType).isEqualTo(CellType.STRING)
        assertThat(readerEnUs(attr, inputCell)).isEqualTo(expected)
        assertThatThrownBy { readerRuRu(attr, inputCell) }
            .isInstanceOf(AppDataException::class.java)
            .hasMessageContaining("String '$datetimeString' cannot be converted to date with the pattern 'd.M.uu a h:m'.")
        xlsTestDataCell.setCellValue(expected.format(DateTimeFormatter.ofPattern("d.M.uu a h:m")))
        assertThat(inputCell.cellType).isEqualTo(CellType.STRING)
        assertThatThrownBy { readerEnUs(attr, inputCell) }.isInstanceOf(AppDataException::class.java)
        assertThat(readerRuRu(attr, inputCell)).isEqualTo(expected)
    }

    @Test
    internal fun readFromBoolOrBlankTest() {
        xlsTestDataCell.setCellValue(true)
        val readerEnUs = DateTimeAttributeReader().apply { pattern = "M/d/uu a h:m" }
        val readerRuRu = DateTimeAttributeReader().apply { pattern = "d.M.uu a h:m" }
        assertThat(readerEnUs(attr, inputCell)).isNull()
        assertThat(readerRuRu(attr, inputCell)).isNull()
        xlsTestDataCell.setBlank()
        assertThat(readerEnUs(attr, inputCell)).isNull()
        assertThat(readerRuRu(attr, inputCell)).isNull()
    }

    @Test
    internal fun defaultDateReaderTest() {
        @Suppress("UNCHECKED_CAST")
        val reader = defaultReaders[defaultReaders.keys.find { it.isSubtypeOf(SupportAttributeType.dateTimeType) }]
        assertThat(reader).isNotNull
        assertThat(reader).isInstanceOf(AttributeReader::class.java)
        reader as DateTimeAttributeReader
        assertThat(reader.locale).isNull()
        assertThat(reader.pattern).isEqualTo("d.M.uu H:m")
    }

    private fun createCsvCell(value: String): CsvCell {
        val reader = value.byteInputStream().reader()
        CsvInputWorkbook(reader, CSVFormat.RFC4180).use {
            return it[0][0][0]!!
        }
    }
}
