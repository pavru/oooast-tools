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

package net.pototskiy.apps.lomout.api.config.loader

import net.pototskiy.apps.lomout.api.config.NamedObject
import java.io.File
import java.util.*

/**
 * Source file definition
 *
 * @property id The unique file id
 * @property file The file
 * @property locale The file locale for converting, formatting operation
 * @property name The file name
 * @constructor
 */
data class SourceFileDefinition(
    val id: String,
    val file: File,
    val locale: Locale
) : NamedObject {
    override val name: String
        get() = id
}
