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
import net.pototskiy.apps.lomout.api.callable.Reader
import net.pototskiy.apps.lomout.api.callable.ReaderBuilder
import net.pototskiy.apps.lomout.api.callable.createReader
import net.pototskiy.apps.lomout.api.document.Document
import net.pototskiy.apps.lomout.api.document.DocumentMetadata
import net.pototskiy.apps.lomout.api.document.SupportAttributeType
import net.pototskiy.apps.lomout.api.document.SupportAttributeType.initIntListValue
import net.pototskiy.apps.lomout.api.document.toAttribute
import net.pototskiy.apps.lomout.api.entity.reader
import net.pototskiy.apps.lomout.api.simpleTestContext
import net.pototskiy.apps.lomout.api.source.workbook.Cell
import net.pototskiy.apps.lomout.api.source.workbook.CellType
import net.pototskiy.apps.lomout.api.source.workbook.Workbook
import net.pototskiy.apps.lomout.api.source.workbook.excel.ExcelWorkbook
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
internal class DefaultIntListReaderTest {
    internal class TestType : Document() {
        var attr: List<Int> = initIntListValue

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
        val reader = LongListAttributeReader().apply { locale = "en_US" }
        xlsTestDataCell.setCellValue(1.1)
        assertThat(inputCell.cellType).isEqualTo(CellType.DOUBLE)
        assertThatThrownBy { reader(attr, inputCell) }.isInstanceOf(AppDataException::class.java)
    }

    @Test
    internal fun readStringEnUsCellTest() {
        val readerEnUs = IntListAttributeReader().apply { locale = "en_US" }
        xlsTestDataCell.setCellValue("11, 22,33")
        assertThat(inputCell.cellType).isEqualTo(CellType.STRING)
        assertThat(readerEnUs(attr, inputCell))
            .hasSize(3)
            .containsExactlyElementsOf(listOf(11, 22, 33))
        xlsTestDataCell.setCellValue("11, 22,A")
        assertThat(inputCell.cellType).isEqualTo(CellType.STRING)
        assertThatThrownBy { readerEnUs(attr, inputCell) }.isInstanceOf(AppDataException::class.java)
    }

    @Test
    internal fun defaultIntListReader() {
        @Suppress("UNCHECKED_CAST")
        val reader = defaultReaders[SupportAttributeType.intListType]
        assertThat(reader).isNotNull
        assertThat(reader).isInstanceOf(AttributeReader::class.java)
        reader as IntListAttributeReader
        assertThat(reader.locale).isEqualTo(DEFAULT_LOCALE_STR)
        assertThat(reader.delimiter).isEqualTo(',')
        assertThat(reader.quotes).isNull()
    }

    class TestDocParameters : Document() {
        @Reader(TestReaderBuilder::class)
        var attr: List<Int> = initIntListValue

        companion object : DocumentMetadata(TestDocParameters::class)

        class TestReaderBuilder : ReaderBuilder {
            override fun build(): AttributeReader<out Any?> {
                return createReader<IntListAttributeReader> {
                    locale = "en_RU"
                }
            }
        }
    }

    @Test
    internal fun setParametersTest() {
        assertThat(TestDocParameters::attr.toAttribute().reader).isInstanceOf(IntListAttributeReader::class.java)
    }
}
