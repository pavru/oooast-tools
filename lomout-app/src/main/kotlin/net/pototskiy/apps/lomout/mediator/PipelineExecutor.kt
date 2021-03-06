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

package net.pototskiy.apps.lomout.mediator

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import net.pototskiy.apps.lomout.MessageBundle.message
import net.pototskiy.apps.lomout.api.AppDataException
import net.pototskiy.apps.lomout.api.MEDIATOR_LOG_NAME
import net.pototskiy.apps.lomout.api.LomoutContext
import net.pototskiy.apps.lomout.api.document.Document
import net.pototskiy.apps.lomout.api.errorMessageFromException
import net.pototskiy.apps.lomout.api.script.mediator.Assembler
import net.pototskiy.apps.lomout.api.script.mediator.InputEntityCollection
import net.pototskiy.apps.lomout.api.script.mediator.Pipeline
import net.pototskiy.apps.lomout.api.script.pipeline.ClassifierElement
import net.pototskiy.apps.lomout.api.suspectedLocation
import org.apache.logging.log4j.LogManager
import kotlin.reflect.KClass

class PipelineExecutor(
    private val context: LomoutContext,
    private val pipeline: Pipeline<*>,
    private val inputEntities: InputEntityCollection,
    private val targetEntity: KClass<out Document>
) {
    private val logger = LogManager.getLogger(MEDIATOR_LOG_NAME)
    private val jobs = mutableListOf<Job>()

    @Suppress("ComplexMethod", "TooGenericExceptionCaught")
    suspend fun execute(inputData: Channel<ClassifierElement>): ReceiveChannel<Document> =
        GlobalScope.produce(LomoutContext.getContext().asCoroutineContext()) {
            val matchedData: Channel<ClassifierElement> = Channel()
            val nextMatchedPipe = pipeline.pipelines.find {
                it.isApplicablePipeline(Pipeline.CLASS.MATCHED)
            }?.let { PipelineExecutor(context, it, inputEntities, targetEntity) }
            jobs.add(launch { nextMatchedPipe?.execute(matchedData)?.consumeEach { send(it) } })

            val unMatchedData: Channel<ClassifierElement> = Channel()
            val nextUnMatchedPipe = pipeline.pipelines.find {
                it.isApplicablePipeline(Pipeline.CLASS.UNMATCHED)
            }?.let { PipelineExecutor(context, it, inputEntities, targetEntity) }
            jobs.add(launch { nextUnMatchedPipe?.execute(unMatchedData)?.consumeEach { send(it) } })

            inputData.consumeEach { data ->
                try {
                    when (val element = pipeline.classifier(context, data)) {
                        is ClassifierElement.Matched -> {
                            if (nextMatchedPipe != null) {
                                matchedData.send(element)
                            } else {
                                val assembler = pipeline.assembler!!
                                try {
                                    sendElement(assembler, element)
                                } catch (e: Exception) {
                                    AppDataException(
                                        suspectedLocation(targetEntity),
                                        message("message.error.mediator.cannot_assemble_entity"),
                                        e
                                    ).errorMessageFromException(logger)
                                }
                            }
                        }
                        is ClassifierElement.Skipped -> {
                            // just drop element
                        }
                        else -> if (nextUnMatchedPipe != null) {
                            unMatchedData.send(element)
                        }
                    }
                } catch (e: Exception) {
                    AppDataException(
                        suspectedLocation(data.entities[0].documentMetadata.klass),
                        message("message.error.mediator.cannot_classify_element"),
                        e
                    ).errorMessageFromException(logger)
                }
            }
            matchedData.close()
            unMatchedData.close()

            @Suppress("SpreadOperator")
            joinAll(*jobs.toTypedArray())
        }

    private suspend fun ProducerScope<Document>.sendElement(
        assembler: Assembler<out Document>,
        element: ClassifierElement
    ) {
        assembler(context, element.entities)?.let { entity ->
            if (entity::class != targetEntity) {
                AppDataException(
                    suspectedLocation(targetEntity),
                    message(
                        "message.error.mediator.wrong_entity_type",
                        entity::class.qualifiedName,
                        targetEntity.qualifiedName
                    )
                ).errorMessageFromException(logger)
            } else {
                send(entity)
            }
        }
    }
}
