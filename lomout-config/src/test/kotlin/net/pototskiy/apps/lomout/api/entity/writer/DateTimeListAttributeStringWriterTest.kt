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

import net.pototskiy.apps.lomout.api.DEFAULT_LOCALE
import net.pototskiy.apps.lomout.api.callable.AttributeWriter
import net.pototskiy.apps.lomout.api.LomoutContext
import net.pototskiy.apps.lomout.api.callable.Writer
import net.pototskiy.apps.lomout.api.callable.WriterBuilder
import net.pototskiy.apps.lomout.api.callable.createWriter
import net.pototskiy.apps.lomout.api.document.Document
import net.pototskiy.apps.lomout.api.document.DocumentMetadata
import net.pototskiy.apps.lomout.api.document.SupportAttributeType
import net.pototskiy.apps.lomout.api.entity.values.datetimeToString
import net.pototskiy.apps.lomout.api.entity.writer
import net.pototskiy.apps.lomout.api.simpleTestContext
import net.pototskiy.apps.lomout.api.source.workbook.Cell
import net.pototskiy.apps.lomout.api.source.workbook.CellType
import net.pototskiy.apps.lomout.api.source.workbook.Workbook
import net.pototskiy.apps.lomout.api.source.workbook.WorkbookFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import java.io.File
import java.nio.file.Path
import java.time.LocalDateTime

@Suppress("MagicNumber")
@Execution(ExecutionMode.CONCURRENT)
internal class DateTimeListAttributeStringWriterTest {
    @Suppress("unused")
    internal class TestType : Document() {
        @Writer(Attr1Writer::class)
        var attr1: List<LocalDateTime> = emptyList()
        @Writer(Attr2Writer::class)
        var attr2: List<LocalDateTime> = emptyList()
        var attr3: List<LocalDateTime> = emptyList()

        companion object : DocumentMetadata(TestType::class)

        class Attr1Writer : WriterBuilder {
            override fun build(): AttributeWriter<out Any?> = createWriter<DateTimeListAttributeStringWriter> {
                delimiter = ','
                quotes = null
            }
        }

        class Attr2Writer : WriterBuilder {
            override fun build(): AttributeWriter<out Any?> = createWriter<DateTimeListAttributeStringWriter> {
                delimiter = ','
                quotes = '\''
                pattern = "d.M.uu H:m"
            }
        }
    }

    private lateinit var file: File
    private lateinit var workbook: Workbook
    private lateinit var cell: Cell
    @TempDir
    lateinit var tempDir: Path

    @BeforeEach
    internal fun setUp() {
        LomoutContext.setContext(simpleTestContext)
        @Suppress("GraziInspection")
        file = tempDir.resolve("attributes.xls").toFile()
        workbook = WorkbookFactory.create(file.toURI().toURL(), DEFAULT_LOCALE, false)
        cell = workbook.insertSheet("test").insertRow(0).insertCell(0)
    }

    @AfterEach
    internal fun tearDown() {
        workbook.close()
        file.delete()
    }

    @Test
    internal fun simpleWriteUnquotedTest() {
        val attr = TestType.attributes.getValue("attr1")
        val now1 = LocalDateTime.now()
        val now2 = now1.plusDays(2)
        val value = listOf(now1, now2)
        assertThat(cell.cellType).isEqualTo(CellType.BLANK)
        @Suppress("UNCHECKED_CAST")
        (attr.writer as AttributeWriter<List<LocalDateTime>>)(value, cell)
        assertThat(cell.cellType).isEqualTo(CellType.STRING)
        assertThat(cell.stringValue).isEqualTo(
            now1.datetimeToString(DEFAULT_LOCALE).replace(",", """\,""") + "," +
                    now2.datetimeToString(DEFAULT_LOCALE).replace(",", """\,""")
        )
    }

    @Test
    internal fun simpleWriteQuotedTest() {
        val attr = TestType.attributes.getValue("attr2")
        val now1 = LocalDateTime.now()
        val now2 = now1.plusDays(2)
        val value = listOf(now1, now2)
        assertThat(cell.cellType).isEqualTo(CellType.BLANK)
        @Suppress("UNCHECKED_CAST")
        (attr.writer as AttributeWriter<List<LocalDateTime>>)(value, cell)
        assertThat(cell.cellType).isEqualTo(CellType.STRING)
        assertThat(cell.stringValue).isEqualTo(
            "${now1.datetimeToString("d.M.uu H:m")},${now2.datetimeToString("d.M.uu H:m")}"
        )
    }

    @Test
    internal fun writeNullValueTest() {
        val attr = TestType.attributes.getValue("attr3")
        assertThat(cell.cellType).isEqualTo(CellType.BLANK)
        @Suppress("UNCHECKED_CAST")
        (attr.writer as AttributeWriter<List<LocalDateTime>?>)(null, cell)
        assertThat(cell.cellType).isEqualTo(CellType.BLANK)
    }

    @Test
    internal fun defaultWriterTest() {
        val writer = defaultWriters[SupportAttributeType.datetimeListType]
        assertThat(writer).isNotNull
        assertThat(writer).isInstanceOf(DateTimeListAttributeStringWriter::class.java)
        writer as DateTimeListAttributeStringWriter
        assertThat(writer.delimiter).isEqualTo(',')
        assertThat(writer.quotes).isNull()
        assertThat(writer.pattern).isEqualTo("d.M.uu H:m")
    }
}
