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

package net.pototskiy.apps.lomout.api.callable

import net.pototskiy.apps.lomout.api.LomoutContext
import net.pototskiy.apps.lomout.api.PublicApi
import net.pototskiy.apps.lomout.api.script.pipeline.ClassifierElement

/**
 * Base class for any pipeline classifiers
 */
@PublicApi
abstract class PipelineClassifier {
    /**
     * Classifier function
     *
     * @param element ClassifierElement The element to classify
     * @return ClassifierElement
     */
    abstract operator fun invoke(
        element: ClassifierElement,
        context: LomoutContext = LomoutContext.getContext()
    ): ClassifierElement
}

/**
 * Function type for inline pipeline classifier
 */
typealias PipelineClassifierFunction = LomoutContext.(element: ClassifierElement) -> ClassifierElement
