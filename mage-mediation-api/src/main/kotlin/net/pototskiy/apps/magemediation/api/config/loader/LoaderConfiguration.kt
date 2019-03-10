package net.pototskiy.apps.magemediation.api.config.loader

import net.pototskiy.apps.magemediation.api.config.ConfigBuildHelper
import net.pototskiy.apps.magemediation.api.config.ConfigDsl
import net.pototskiy.apps.magemediation.api.config.ConfigException
import net.pototskiy.apps.magemediation.api.entity.EntityTypeCollection

data class LoaderConfiguration(
    val files: SourceFileCollection,
    val entities: EntityTypeCollection,
    val loads: LoadCollection
) {
    @ConfigDsl
    class Builder(private val helper: ConfigBuildHelper) {
        private var files: SourceFileCollection? = null
        private var entities: EntityTypeCollection? = null
        private var loads = mutableListOf<Load>()

        @Suppress("unused")
        fun Builder.files(block: SourceFileCollection.Builder.() -> Unit) {
            files = SourceFileCollection.Builder(helper).apply(block).build()
        }

        @Suppress("unused")
        fun Builder.entities(block: EntityTypeCollection.Builder.() -> Unit) {
            this.entities = EntityTypeCollection.Builder(helper).apply(block).build()
        }

        @Suppress("unused")
        fun Builder.loadEntity(entityType: String, block: Load.Builder.() -> Unit) {
            val entity = helper.typeManager.getEntityType(entityType)
                ?: throw ConfigException("Define entity<$entityType> before load configuration")
            loads.add(Load.Builder(helper, entity).apply(block).build())
        }

        fun build(): LoaderConfiguration {
            val files = this.files ?: throw ConfigException("Files is not defined in loader section")
            return LoaderConfiguration(
                files,
                entities ?: throw ConfigException("At least one entity must be defined"),
                LoadCollection(loads)
            )
        }
    }
}
