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

package net.pototskiy.apps.lomout.api.script.printer

import net.pototskiy.apps.lomout.api.AppConfigException
import net.pototskiy.apps.lomout.api.script.ScriptBuildHelper
import net.pototskiy.apps.lomout.api.document.Document
import net.pototskiy.apps.lomout.api.document.DocumentMetadata
import net.pototskiy.apps.lomout.api.document.Documents
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode

@Execution(ExecutionMode.CONCURRENT)
@Suppress("TooManyFunctions")
internal class PrinterConfigurationBuilderTest {
    @Suppress("unused")
    class Entity1 : Document() {
        var attr1: String = ""
        var attr2: String = ""
        var attr3: String = ""

        companion object : DocumentMetadata(Entity1::class)
    }

    @Suppress("unused")
    class Entity2 : Document() {
        var attr1: String = ""
        var attr2: String = ""
        var attr3: String = ""

        companion object : DocumentMetadata(Entity2::class)
    }

    @Test
    internal fun correctConfigurationTest() {
        val config = createCorrectConfiguration()
        assertThat(config).isNotNull
        assertThat(config.files).hasSize(2)
        assertThat(config.lines).hasSize(2)
        @Suppress("RedundantWith")
        with(config.lines[0]) {
            assertThat(inputEntities).hasSize(1)
            assertThat(outputFieldSets).isNotNull
            assertThat(outputFieldSets.file.file.id).isEqualTo("id1")
            assertThat(outputFieldSets.printHead).isEqualTo(true)
            assertThat(outputFieldSets.fieldSets).hasSize(2)
            assertThat(pipeline).isNotNull
            assertThat(config.lines.contains(this)).isEqualTo(true)
            assertThat(config.lines.indexOf(this)).isEqualTo(0)
            assertThat(config.lines.lastIndexOf(this)).isEqualTo(0)
            Unit
        }
    }

    @Test
    internal fun tooManyInputsTest() {
        assertThatThrownBy {
            createConfigurationTooManyInput()
        }.isInstanceOf(AppConfigException::class.java)
            .hasMessageContaining("One and only one input entity is allowed for printer line")
    }

    @Test
    internal fun noInputsTest() {
        assertThatThrownBy {
            createConfigurationNoInputs()
        }.isInstanceOf(AppConfigException::class.java)
            .hasMessageContaining("One and only one input entity is allowed for printer line.")
    }

    @Test
    internal fun noOutputsTest() {
        assertThatThrownBy {
            createConfigurationNoOutputs()
        }.isInstanceOf(AppConfigException::class.java)
            .hasMessageContaining("Output entity must be defined.")
    }

    @Test
    internal fun noPipelineTest() {
        assertThatThrownBy {
            createConfigurationNoPipeline()
        }.isInstanceOf(AppConfigException::class.java)
            .hasMessageContaining("Production line must have start pipeline.")
    }

    @Test
    internal fun noOutputFileTest() {
        assertThatThrownBy {
            createConfigurationNoOutputFile()
        }.isInstanceOf(AppConfigException::class.java)
            .hasMessageContaining("Output file must be defined")
    }

    @Test
    internal fun noFieldSetTest() {
        assertThatThrownBy {
            createConfigurationNoFieldSet()
        }.isInstanceOf(AppConfigException::class.java)
            .hasMessageContaining("Field sets must be defined")
    }

    @Test
    internal fun sheetWithRegexTest() {
        assertThatThrownBy {
            createConfigurationWithSheetRegex()
        }.isInstanceOf(AppConfigException::class.java)
            .hasMessageContaining("Sheet name, not regex must be used in output")
    }

    private fun createCorrectConfiguration(): PrinterConfiguration =
        PrinterConfiguration.Builder(ScriptBuildHelper()).apply {
            files {
                file("id1") { path("file1") }
                file("id2") { path("file2") }
            }
            print<Entity1> {
                input {
                    entity(Entity1::class)
                }
                output {
                    file { file("id1"); sheet("test") }
                    printHead = true
                    outputFields {
                        main("main") {
                            field("attr1") { column(0) }
                            field("attr2") { column(1) }
                        }
                        extra("extra") {
                            field("attr3") { column(0) }
                        }
                    }
                }
                pipeline {
                    classifier { if (it.entities[0].updateTime == Documents.timestamp) it.match() else it.mismatch() }
                    assembler { it.first() as Entity1 }
                }
            }
            print<Entity2> {
                input {
                    entity(Entity2::class)
                }
                output {
                    file { file("id2"); sheet("test") }
                    printHead = false
                    outputFields {
                        main("main") {
                            field("attr3")
                        }
                    }
                }
                pipeline {
                    classifier { if (it.entities[0].updateTime == Documents.timestamp) it.match() else it.mismatch() }
                    assembler { it.first() as Entity2 }
                }
            }
        }.build()

    private fun createConfigurationTooManyInput(): PrinterConfiguration =
        PrinterConfiguration.Builder(ScriptBuildHelper()).apply {
            files {
                file("id1") { path("file1") }
                file("id2") { path("file2") }
            }
            print<Entity1> {
                input {
                    entity(Entity1::class)
                    entity(Entity2::class)
                }
                output {
                    file { file("id1"); sheet("test") }
                    printHead = true
                    outputFields {
                        main("main") {
                            field("attr1") { column(0) }
                            field("attr2") { column(1) }
                        }
                        extra("extra") {
                            field("attr3") { column(0) }
                        }
                    }
                }
                pipeline {
                    classifier { it.match() }
                    assembler { it.first() as Entity1 }
                }
            }
            print<Entity2> {
                input {
                    entity(Entity2::class)
                }
                output {
                    file { file("id2"); sheet("test") }
                    printHead = false
                    outputFields {
                        main("main") {
                            field("attr3")
                        }
                    }
                }
                pipeline {
                    classifier { it.match() }
                    assembler { it.first() as Entity2 }
                }
            }
        }.build()

    private fun createConfigurationNoInputs(): PrinterConfiguration =
        PrinterConfiguration.Builder(ScriptBuildHelper()).apply {
            files {
                file("id1") { path("file1") }
                file("id2") { path("file2") }
            }
            print<Entity1> {
                pipeline {
                    classifier { it.match() }
                    assembler { it.first() as Entity1 }
                }
            }
            print<Entity2> {
                input {
                    entity(Entity2::class)
                }
                output {
                    file { file("id2"); sheet("test") }
                    printHead = false
                    outputFields {
                        main("main") {
                            field("attr3")
                        }
                    }
                }
                pipeline {
                    classifier { it.match() }
                    assembler { it.first() as Entity2 }
                }
            }
        }.build()

    private fun createConfigurationNoOutputs(): PrinterConfiguration =
        PrinterConfiguration.Builder(ScriptBuildHelper()).apply {
            files {
                file("id1") { path("file1") }
                file("id2") { path("file2") }
            }
            print<Entity1> {
                input {
                    entity(Entity1::class)
                }
                pipeline {
                    classifier { it.match() }
                    assembler { null }
                }
            }
            print<Entity2> {
                input {
                    entity(Entity2::class)
                }
                output {
                    file { file("id2"); sheet("test") }
                    printHead = false
                    outputFields {
                        main("main") {
                            field("attr3")
                        }
                    }
                }
                pipeline {
                    classifier { it.match() }
                    assembler { null }
                }
            }
        }.build()

    private fun createConfigurationNoPipeline(): PrinterConfiguration =
        PrinterConfiguration.Builder(ScriptBuildHelper()).apply {
            files {
                file("id1") { path("file1") }
                file("id2") { path("file2") }
            }
            print<Entity1> {
                input {
                    entity(Entity1::class)
                }
                output {
                    file { file("id1"); sheet("test") }
                    printHead = true
                    outputFields {
                        main("main") {
                            field("attr1") { column(0) }
                            field("attr2") { column(1) }
                        }
                        extra("extra") {
                            field("attr3") { column(0) }
                        }
                    }
                }
            }
        }.build()

    private fun createConfigurationNoOutputFile(): PrinterConfiguration =
        PrinterConfiguration.Builder(ScriptBuildHelper()).apply {
            files {
                file("id1") { path("file1") }
                file("id2") { path("file2") }
            }
            print<Entity1> {
                input {
                    entity(Entity1::class)
                }
                output {
                    printHead = true
                    outputFields {
                        main("main") {
                            field("attr1") { column(0) }
                            field("attr2") { column(1) }
                        }
                        extra("extra") {
                            field("attr3") { column(0) }
                        }
                    }
                }
                pipeline {
                    classifier { it.match() }
                    assembler { null }
                }
            }
            print<Entity2> {
                input {
                    entity(Entity2::class)
                }
                output {
                    file { file("id2"); sheet("test") }
                    printHead = false
                    outputFields {
                        main("main") {
                            field("attr3")
                        }
                    }
                }
                pipeline {
                    classifier { it.match() }
                    assembler { null }
                }
            }
        }.build()

    private fun createConfigurationNoFieldSet(): PrinterConfiguration =
        PrinterConfiguration.Builder(ScriptBuildHelper()).apply {
            files {
                file("id1") { path("file1") }
                file("id2") { path("file2") }
            }
            print<Entity1> {
                input {
                    entity(Entity1::class)
                }
                output {
                    file { file("id1"); sheet("test") }
                    printHead = true
                }
                pipeline {
                    classifier { it.match() }
                    assembler { null }
                }
            }
            print<Entity2> {
                input {
                    entity(Entity2::class)
                }
                output {
                    file { file("id2"); sheet("test") }
                    printHead = false
                    outputFields {
                        main("main") {
                            field("attr3")
                        }
                    }
                }
                pipeline {
                    classifier { it.match() }
                    assembler { null }
                }
            }
        }.build()

    private fun createConfigurationWithSheetRegex(): PrinterConfiguration =
        PrinterConfiguration.Builder(ScriptBuildHelper()).apply {
            files {
                file("id1") { path("file1") }
                file("id2") { path("file2") }
            }
            print<Entity1> {
                input {
                    entity(Entity1::class)
                }
                output {
                    file { file("id1"); sheet(Regex(".*")) }
                    printHead = true
                    outputFields {
                        main("main") {
                            field("attr1") { column(0) }
                            field("attr2") { column(1) }
                        }
                        extra("extra") {
                            field("attr3") { column(0) }
                        }
                    }
                }
                pipeline {
                    classifier { it.match() }
                    assembler { null }
                }
            }
            print<Entity2> {
                input {
                    entity(Entity2::class)
                }
                output {
                    file { file("id2"); sheet("test") }
                    printHead = false
                    outputFields {
                        main("main") {
                            field("attr3")
                        }
                    }
                }
                pipeline {
                    classifier { it.match() }
                    assembler { null }
                }
            }
        }.build()
}
