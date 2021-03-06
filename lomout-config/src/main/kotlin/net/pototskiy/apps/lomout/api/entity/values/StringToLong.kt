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

package net.pototskiy.apps.lomout.api.entity.values

import net.pototskiy.apps.lomout.api.MessageBundle.message
import java.text.NumberFormat
import java.text.ParseException
import java.text.ParsePosition
import java.util.*

/**
 * Convert String to Long according to the locale
 *
 * @receiver String
 * @param locale Locale
 * @return Long
 * @throws java.text.ParseException
 */
fun String.stringToLong(locale: Locale, groupingUsed: Boolean): Long {
    val format = NumberFormat.getIntegerInstance(locale).apply {
        isParseIntegerOnly = true
        isGroupingUsed = groupingUsed
    }
    val position = ParsePosition(0)
    val value = format.parse(this.trim(), position) as? Long
        ?: throw ParseException(message("message.error.data.string.to_long_error"), position.index)
    if (position.index != this.trim().length) {
        throw ParseException(message("message.error.data.string.extra_char_error"), position.index)
    }
    return value
}

/**
 * Convert Long to String according to the locale
 *
 * @receiver Long
 * @param locale Locale
 * @return String
 */
fun Long.longToString(locale: Locale, groupingUsed: Boolean): String {
    val format = NumberFormat.getIntegerInstance(locale).apply {
        isGroupingUsed = groupingUsed
    }
    return format.format(this)
}
