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
import net.pototskiy.apps.lomout.api.MessageBundle.message
import net.pototskiy.apps.lomout.api.UNDEFINED_ROW
import net.pototskiy.apps.lomout.api.document.Document
import net.pototskiy.apps.lomout.api.script.LomoutDsl
import net.pototskiy.apps.lomout.api.script.ScriptBuildHelper
import net.pototskiy.apps.lomout.api.script.loader.FieldSetCollection
import net.pototskiy.apps.lomout.api.script.loader.SourceData
import net.pototskiy.apps.lomout.api.script.loader.SourceDataCollection
import net.pototskiy.apps.lomout.api.script.loader.SourceSheetDefinition.SourceSheetDefinitionWithName
import net.pototskiy.apps.lomout.api.suspectedLocation
import kotlin.reflect.KClass

/**
 * Printer line output definition
 *
 * @property file SourceData The file to output printer line result
 * @property printHead Boolean The flag to print headers in first row
 * @property fieldSets FieldSetCollection The fields to print
 * @constructor
 */
data class PrinterOutput(
    val file: SourceData,
    val printHead: Boolean,
    val fieldSets: FieldSetCollection
) {
    /**
     * Printer output builder class
     *
     * @property helper ConfigBuildHelper The configuration builder helper
     * @property entityType EntityType The target entity type
     * @property printHead Boolean The flag to print or not headers
     * @property file SourceData? The output file
     * @property fieldSets FieldSetCollection? The fields to print
     * @constructor
     */
    @LomoutDsl
    class Builder<T : Document>(
        private val helper: ScriptBuildHelper,
        private val entityType: KClass<T>
    ) {
        /**
         * Print or not headers, default: true (print)
         */
        @LomoutDsl
        var printHead: Boolean = true
        private var file: SourceData? = null
        private var fieldSets: FieldSetCollection? = null

        /**
         * The output file
         *
         * ```
         * ...
         *  file { file("file id"); sheet("sheet name") }
         * ...
         * ```
         * * file — reference to file define in files block, **mandatory**
         * * sheet — sheet name to print result data, **mandatory**
         *
         * @param block The file definition
         */
        @LomoutDsl
        fun file(block: SourceData.Builder.() -> Unit) {
            this.file = SourceData.Builder(helper).apply(block).build()
            if (this.file?.sheet !is SourceSheetDefinitionWithName) {
                throw AppConfigException(suspectedLocation(), message("message.error.script.print.sheet.name.no_regex"))
            }
        }

        /**
         * Define output field set. Main set is printed for each entity, extra field set is printed only when
         * data changed.
         *
         * ```
         * ...
         *  outputFields {
         *      main("set name") {
         *          field("field name")
         *          field("field name") to attribute(...)
         *          ...
         *      }
         *      extra("set name") {
         *          field("field name") to attribute(...)
         *          field("field name")
         *          ...
         *      }
         *  }
         * ...
         * ```
         * * [main][net.pototskiy.apps.lomout.api.script.loader.FieldSetCollection.Builder.main] — main field set
         *      for output, **mandatory, only one main set is allowed**
         * * [extra][net.pototskiy.apps.lomout.api.script.loader.FieldSetCollection.Builder.extra] — extra field
         *      set, *optional, 0 or several extra set are allowed*
         *
         * @param block Output fields definition
         */
        @LomoutDsl
        fun outputFields(block: FieldSetCollection.Builder<T>.() -> Unit) {
            this.fieldSets = FieldSetCollection.Builder(
                helper,
                entityType,
                false,
                SourceDataCollection(emptyList()),
                UNDEFINED_ROW,
                false
            ).apply(block).build()
        }

        /**
         * Build printer output configuration
         *
         * @return PrinterOutput
         */
        fun build(): PrinterOutput {
            return PrinterOutput(
                file ?: throw AppConfigException(
                    suspectedLocation(),
                    message("message.error.script.print.out_file_not_defined")
                ),
                printHead,
                fieldSets ?: throw AppConfigException(
                    suspectedLocation(),
                    message("message.error.script.print.fieldset.not_defined")
                )
            )
        }
    }
}
