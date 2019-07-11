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

package net.pototskiy.apps.lomout.api

/**
 * Application related exception
 */
@Generated
open class AppException : Exception {
    /**
     * Exception suspectedLocation
     */
    val suspectedLocation: SuspectedLocation

    /**
     *
     * @constructor
     */
    @Suppress("unused")
    constructor(suspectedLocation: SuspectedLocation = suspectedLocation()) : super() {
        this.suspectedLocation = suspectedLocation
    }

    /**
     *
     * @param message String?
     * @constructor
     */
    @Suppress("unused")
    constructor(suspectedLocation: SuspectedLocation = suspectedLocation(), message: String?) : super(message) {
        this.suspectedLocation = suspectedLocation
    }

    /**
     *
     * @param message The message
     * @param cause The cause
     * @constructor
     */
    @Suppress("unused")
    constructor(suspectedLocation: SuspectedLocation = suspectedLocation(), message: String?, cause: Throwable?) : super(
        message,
        cause
    ) {
        this.suspectedLocation = suspectedLocation
    }
}

@JvmName("throwable_cause_list")
fun Throwable.causesList(block: (msg: String) -> Unit) {
    causesList(this, block)
}

private fun causesList(throwable: Throwable, block: (msg: String) -> Unit) {
    val exception = throwable.cause
    if (exception != null) {
        val message = MessageBundle.message("message.exception.cause_of_upper", exception.message)
        when (exception) {
            is AppDataException -> block(message + " " + exception.suspectedLocation.placeInfo() + ".")
            else -> block(message)
        }
        causesList(exception, block)
    }
}

/**
 * All configuration problems
 */
@Generated
open class AppConfigException : AppException {
    /**
     *
     * @constructor
     */
    @Suppress("unused")
    constructor(place: SuspectedLocation) : super(place)

    /**
     *
     * @param message String?
     * @constructor
     */
    @Suppress("unused")
    constructor(place: SuspectedLocation, message: String?) : super(place, message)

    /**
     *
     * @param message The message
     * @param cause The cause
     * @constructor
     */
    @Suppress("unused")
    constructor(place: SuspectedLocation, message: String?, cause: Throwable?) : super(place, message, cause)
}

/**
 * Configuration problems related to entity type
 */
@Generated
open class AppDataException : AppException {
    /**
     *
     * @constructor
     */
    @Suppress("unused")
    constructor(place: SuspectedLocation) : super(place)

    /**
     *
     * @param message String?
     * @constructor
     */
    @Suppress("unused")
    constructor(place: SuspectedLocation, message: String?) : super(place, message)

    /**
     *
     * @param message The message
     * @param cause The cause
     * @constructor
     */
    @Suppress("unused")
    constructor(place: SuspectedLocation, message: String?, cause: Throwable?) : super(place, message, cause)
}
