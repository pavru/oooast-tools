package net.pototskiy.apps.lomout.mediator

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import net.pototskiy.apps.lomout.api.config.mediator.InputEntityCollection
import net.pototskiy.apps.lomout.api.config.mediator.Pipeline
import net.pototskiy.apps.lomout.api.config.mediator.PipelineData
import net.pototskiy.apps.lomout.api.config.mediator.PipelineDataCollection
import net.pototskiy.apps.lomout.api.config.pipeline.ClassifierElement
import net.pototskiy.apps.lomout.api.entity.AnyTypeAttribute
import net.pototskiy.apps.lomout.api.entity.EntityType
import net.pototskiy.apps.lomout.api.entity.EntityTypeManager
import net.pototskiy.apps.lomout.api.entity.Type
import org.apache.commons.collections4.map.LRUMap

class PipelineExecutor(
    private val entityTypeManager: EntityTypeManager,
    private val pipeline: Pipeline,
    private val inputEntities: InputEntityCollection,
    private val targetEntity: EntityType,
    private val entityCache: LRUMap<Int, PipelineData>
) {

    private val jobs = mutableListOf<Job>()

    suspend fun execute(inputData: Channel<ClassifierElement>): ReceiveChannel<Map<AnyTypeAttribute, Type?>> =
        GlobalScope.produce {
            val matchedData: Channel<ClassifierElement> = Channel()
            val nextMatchedPipe = pipeline.pipelines.find {
                it.isApplicablePipeline(Pipeline.CLASS.MATCHED)
            }?.let { PipelineExecutor(entityTypeManager, it, inputEntities, targetEntity, entityCache) }
            jobs.add(launch { nextMatchedPipe?.execute(matchedData)?.consumeEach { send(it) } })

            val unMatchedData: Channel<ClassifierElement> = Channel()
            val nextUnMatchedPipe = pipeline.pipelines.find {
                it.isApplicablePipeline(Pipeline.CLASS.UNMATCHED)
            }?.let { PipelineExecutor(entityTypeManager, it, inputEntities, targetEntity, entityCache) }
            jobs.add(launch { nextUnMatchedPipe?.execute(unMatchedData)?.consumeEach { send(it) } })

            inputData.consumeEach { data ->
                when (val element = pipeline.classifier.classify(data)) {
                    is ClassifierElement.Matched -> {
                        if (nextMatchedPipe != null) {
                            matchedData.send(element)
                        } else {
                            val assembler = pipeline.assembler!!
                            send(
                                assembler.assemble(
                                    targetEntity,
                                    PipelineDataCollection(element.entities)
                                )
                            )
                        }
                    }
                    is ClassifierElement.Skipped -> {
                        // just drop element
                    }
                    else -> if (nextUnMatchedPipe != null) {
                        unMatchedData.send(element)
                    }
                }
            }
            matchedData.close()
            unMatchedData.close()

            @Suppress("SpreadOperator")
            joinAll(*jobs.toTypedArray())
        }
}
