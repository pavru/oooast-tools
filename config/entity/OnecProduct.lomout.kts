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

import OnecGroupToLong_plugin_lomout.OnecGroupToLong

class OnecProduct : Document() {
    @Key
    var sku: String = ""
    var weight: Double = 0.0
    @Reader(OnecGroupToLong::class)
    var group_code: Long = 0L
    var group_name: String? = null
    var catalog_sku: String? = null
    var russian_name: String? = null
    var english_name: String? = null
    var manufacturer: String? = null
    var country_of_manufacture: String? = null
    var machine_vendor: String? = null
    var machine: String? = null


    companion object : DocumentMetadata(OnecProduct::class)
}
