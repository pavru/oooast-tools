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

@file:Import("../reader/OnecGroupToLong.plugin.lomout.kts")
@file:Import("../builder/GroupPathFromRelation.plugin.lomout.kts")
@file:Import("../builder/GroupToCategoryPath.plugin.lomout.kts")

import GroupPathFromRelation_plugin_lomout.GroupPathFromRelation
import GroupToCategoryPath_plugin_lomout.GroupToCategoryPath
import OnecGroupToLong_plugin_lomout.OnecGroupToLong
import net.pototskiy.apps.lomout.api.callable.Reader
import net.pototskiy.apps.lomout.api.document.Document
import net.pototskiy.apps.lomout.api.document.DocumentMetadata
import net.pototskiy.apps.lomout.api.document.Key
import org.bson.codecs.pojo.annotations.BsonIgnore
import org.jetbrains.kotlin.script.util.Import

class OnecGroup : Document() {
    @Key
    @Reader(OnecGroupToLong::class)
    var group_code: Long = 0L
    var group_name: String = ""
    @get: BsonIgnore
    val __path: String by lazy { pathBuilder(this)!! }
    val entity_id: Long
        get() = group_code
    @get:BsonIgnore
    val transformed_path: String by lazy { transformedPathBuilder(this)!! }

    companion object : DocumentMetadata(OnecGroup::class) {
        val transformedPathBuilder = GroupToCategoryPath()
        val pathBuilder = GroupPathFromRelation("/", "/Root Catalog/Default Category/Каталог/")
    }
}
