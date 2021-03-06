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

package net.pototskiy.apps.lomout.api.script

import net.pototskiy.apps.lomout.api.AppConfigException
import net.pototskiy.apps.lomout.api.CONFIG_LOG_NAME
import net.pototskiy.apps.lomout.api.MessageBundle.message
import net.pototskiy.apps.lomout.api.script.loader.SourceFileDefinition
import net.pototskiy.apps.lomout.api.suspectedLocation
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.util.*

/**
 * Helper for configuration building
 *
 * @property scopeStack The scope stack
 * @property definedSourceFiles The registrar for defined source fields
 * @constructor
 */
open class ScriptBuildHelper {
    /**
     * Configuration logger.
     */
    val logger: Logger = LogManager.getLogger(CONFIG_LOG_NAME)

    private val scopeStack = Stack<String>().apply { push("root") }
    /**
     * Push scope to stack
     *
     * @param name The scope name
     * @return String
     */
    fun pushScope(name: String) = scopeStack.push(name)!!

    /**
     * Pop scope from a stack
     *
     * @return String
     */
    fun popScope(): String = scopeStack.pop()

    /**
     * Get current scope
     *
     * @return String
     */
    fun currentScope(): String = scopeStack.peek()

    val definedSourceFiles = ConfigObjectRegistrar<SourceFileDefinition>()

    init {
        scopeStack.clear()
        scopeStack.push("root")
        definedSourceFiles.clear()
    }

    /**
     * Config object registrar
     *
     * @param T The registrar object type
     * @property register MutableMap<String, MutableList<T>>
     */
    inner class ConfigObjectRegistrar<T : NamedObject> {
        private var register = mutableMapOf<String, MutableList<T>>()

        /**
         * Clear registered objects
         */
        fun clear() = register.clear()

        /**
         * Register object
         *
         * @param entity T
         */
        fun register(entity: T) {
            val scope = currentScope()
            if (register[scope]?.any { it.name == entity.name } == true)
                throw AppConfigException(suspectedLocation(), message("message.error.script.scope.exists", scope))
            register.getOrPut(scope) { mutableListOf() }.add(entity)
        }

        /**
         * Find registered object by name.
         *
         * @param name The object name
         * @param globalSearch true — search in all scopes, false — search only in current scope
         * @return T?
         */
        fun findRegistered(name: String, globalSearch: Boolean = false): T? {
            val obj: T? = scopeStack.reversed().mapNotNull { scope ->
                register.getOrPut(scope) { mutableListOf() }.find { it.name == name }
            }.firstOrNull()
            return obj
                ?: (if (globalSearch) register.map { it.value }.flatten().find { it.name == name } else null)
        }
    }
}
