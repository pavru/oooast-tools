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

import net.pototskiy.apps.lomout.api.createLocale
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import java.text.ParseException

@Execution(ExecutionMode.CONCURRENT)
internal class StringToBooleanKtTest {
    @Test
    internal fun stringToBooleanTest() {
        assertThat("true".stringToBoolean("en_US".createLocale())).isEqualTo(true)
        assertThat("false".stringToBoolean("en_US".createLocale())).isEqualTo(false)
        assertThat("истина".stringToBoolean("ru_RU".createLocale())).isEqualTo(true)
        assertThat("ложь".stringToBoolean("ru_RU".createLocale())).isEqualTo(false)
        assertThatThrownBy { "true".stringToBoolean("ru_RU".createLocale()) }
            .isInstanceOf(ParseException::class.java)
            .hasMessageContaining("Value 'true' cannot be converted to boolean.")
    }
}