@file:Suppress("unused")

package net.pototskiy.apps.lomout.api.database

import net.pototskiy.apps.lomout.api.entity.EntityTypeManager
import net.pototskiy.apps.lomout.api.entity.entityType
import org.jetbrains.exposed.dao.IntIdTable

/**
 * Exposed entity DB table
 */
object DbEntityTable : IntIdTable("entity") {
    /**
     * Entity type manager
     */
    lateinit var entityTypeManager: EntityTypeManager
    /**
     * Entity type
     */
    val entityType = entityType("entity_type", this).index()
    /**
     * Change flag
     */
    val touchedInLoading = bool("touched_in_loading").index()
    /**
     * Previous entity status
     */
    val previousStatus = entityStatus("previous_status").nullable().index()
    /**
     * Current entity status
     */
    val currentStatus = entityStatus("current_status").index()
    /**
     * Timestamp of creating
     */
    val created = datetime("created").index()
    /**
     * Timestamp of updating
     */
    val updated = datetime("updated").index()
    /**
     * Timestamp of removing
     */
    val removed = datetime("removed").nullable().index()
    /**
     * Absent (in a source) days
     */
    val absentDays = integer("absent_days").index()
}

val EntityTab = DbEntityTable
val EntityIdCol = DbEntityTable.id
val EntityTypeCol = DbEntityTable.entityType
val EntityTouchedCol = DbEntityTable.touchedInLoading
val EntityPStatusCol = DbEntityTable.previousStatus
val EntityCStatusCol = DbEntityTable.currentStatus
val EntityCreatedCol = DbEntityTable.created
val EntityUpdatedCol = DbEntityTable.updated
val EntityRemovedCol = DbEntityTable.removed
val EntityAbsentCol = DbEntityTable.absentDays
