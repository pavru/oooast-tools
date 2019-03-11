package net.pototskiy.apps.magemediation.mediator

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.pototskiy.apps.magemediation.api.MEDIATOR_LOG_NAME
import net.pototskiy.apps.magemediation.api.config.mediator.PipelineData
import net.pototskiy.apps.magemediation.api.config.mediator.PipelineDataCollection
import net.pototskiy.apps.magemediation.api.config.mediator.ProductionLine
import net.pototskiy.apps.magemediation.api.database.DbEntity
import net.pototskiy.apps.magemediation.api.database.DbEntityTable
import net.pototskiy.apps.magemediation.api.entity.EntityTypeManager
import net.pototskiy.apps.magemediation.database.BooleanConst
import net.pototskiy.apps.magemediation.database.PipelineSets
import net.pototskiy.apps.magemediation.database.StringConst
import net.pototskiy.apps.magemediation.loader.EntityUpdater
import org.apache.commons.collections4.map.LRUMap
import org.apache.logging.log4j.LogManager
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ColumnSet
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.alias
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

class CrossProductionLineExecutor(val entityTypeManager: EntityTypeManager) {

    private val pipelineDataCache = LRUMap<Int, PipelineData>(maxCacheSize, initialCacheSize)
    private val logger = LogManager.getLogger(MEDIATOR_LOG_NAME)
    private val jobs = mutableListOf<Job>()

    @ExperimentalCoroutinesApi
    @ObsoleteCoroutinesApi
    @Suppress("TooGenericExceptionCaught", "SpreadOperator")
    fun executeLine(line: ProductionLine) {
        try {
            runBlocking {
                @Suppress("UNCHECKED_CAST")
                val targetEntityType = line.outputEntity
                val entityUpdater = EntityUpdater(targetEntityType)
                val pipeline = PipelineExecutor(
                    entityTypeManager,
                    line.pipeline,
                    line.inputEntities,
                    line.outputEntity,
                    pipelineDataCache
                )
                createTopPipelineSet(line)
                val (from, where, columns) = mainQuery(line)
                val inputChannel: Channel<PipelineDataCollection> = Channel()
                jobs.add(launch {
                    pipeline.execute(inputChannel).consumeEach {
                        entityUpdater.update(it)
                    }
                })
                rowSequence(from, where, columns) { row ->
                    createPipelineData(columns, row, line)
                }.forEach { inputChannel.send(PipelineDataCollection(it)) }
                inputChannel.close()
                joinAll(*jobs.toTypedArray())
            }
        } catch (e: Exception) {
            processException(e)
        }
    }

    private fun createPipelineData(
        columns: MutableList<Column<EntityID<Int>>>,
        row: ResultRow,
        line: ProductionLine
    ): List<PipelineData> {
        return columns.map { column ->
            val id = row[column]
            val pipelineData = pipelineDataCache[id.value]
            if (pipelineData == null) {
                val entity = transaction { DbEntity.findById(id) }
                    ?: throw MediationException("Matched entity<id:${id.value}> can not be found")
                entity.readAttributes()
                PipelineData(
                    entityTypeManager,
                    entity,
                    line.inputEntities.find {
                        it.entity.name == entity.eType.name
                    }!!
                ).also { pipelineDataCache[id.value] = it }
            } else {
                pipelineData
            }
        }
    }

    private fun createTopPipelineSet(line: ProductionLine) = transaction {
        line.inputEntities.forEach {
            val alias = DbEntityTable.alias("entity_${it.entity.name}")
            var where = Op.build { alias[DbEntityTable.entityType] eq it.entity }
            it.filter?.let { filter -> where = where.and(filter.where(alias)) }
            PipelineSets.insert(
                alias
                    .slice(
                        StringConst(line.pipeline.pipelineID),
                        alias[DbEntityTable.id],
                        BooleanConst(false)
                    ).select(where)
            )
        }
    }

    private fun mainQuery(line: ProductionLine): Triple<ColumnSet, Op<Boolean>, MutableList<Column<EntityID<Int>>>> {
        val startTable = DbEntityTable.alias("start_table")
        var from: ColumnSet = startTable
        var where = Op.build {
            startTable[DbEntityTable.entityType] eq line.inputEntities.first().entity
        }
        line.inputEntities.first().filter?.let { where = where.and(it.where(startTable)) }
        val columns = mutableListOf(startTable[DbEntityTable.id])
        line.inputEntities.drop(1).forEachIndexed { i, entity ->
            val alias = DbEntityTable.alias("source_entity_$i")
            from = from.crossJoin(alias)
            where = where.and(Op.build { alias[DbEntityTable.entityType] eq entity.entity })
            entity.filter?.let { where = where.and(it.where(alias)) }
            columns.add(alias[DbEntityTable.id])
        }
        return Triple(from, where, columns)
    }

    private fun processException(e: Exception) {
        logger.error("{}", e.message)
        logger.trace("Caused by:", e)
    }

    companion object {
        private const val maxCacheSize = 1000
        private const val initialCacheSize = 300
    }
}
