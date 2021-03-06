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

package net.pototskiy.apps.lomout.api.source.workbook

/**
 * Workbook sheet interface
 *
 * @property name String
 * @property workbook Workbook
 */
interface Sheet : Iterable<Row> {
    /**
     * The sheet name
     */
    val name: String
    /**
     * Sheet workbook
     */
    val workbook: Workbook

    /**
     * Get row by the index
     *
     * @param row Int The row index, zero based
     * @return Row?
     */
    operator fun get(row: Int): Row?

    /**
     * Insert row into sheet by the index
     *
     * @param row Int The row index, zero based
     * @return Row
     */
    fun insertRow(row: Int): Row
}
