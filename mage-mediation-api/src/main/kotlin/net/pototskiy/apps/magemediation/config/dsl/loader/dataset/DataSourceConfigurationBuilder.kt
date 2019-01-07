package net.pototskiy.apps.magemediation.config.dsl.loader.dataset

import net.pototskiy.apps.magemediation.config.ConfigException
import net.pototskiy.apps.magemediation.config.dataset.EmptyRowAction
import net.pototskiy.apps.magemediation.config.dsl.ConfigDsl
import net.pototskiy.apps.magemediation.config.newOne.loader.dataset.DataSourceConfiguration

@ConfigDsl
class DataSourceConfigurationBuilder {
    private var fileID: String? = null
    private var sheet: String? = null
    private var emptyRowAction: EmptyRowAction? = null

    @Suppress("unused")
    fun DataSourceConfigurationBuilder.file(id: String): DataSourceConfigurationBuilder =
        this.apply { fileID = id }

    @Suppress("unused")
    fun DataSourceConfigurationBuilder.sheet(sheet: String): DataSourceConfigurationBuilder =
        this.apply { this.sheet = sheet }

    @Suppress("unused")
    fun DataSourceConfigurationBuilder.emptyRowAction(action: EmptyRowAction): DataSourceConfigurationBuilder =
        this.apply { emptyRowAction = action }

    fun build(): DataSourceConfiguration {
        val id = fileID
            ?: throw ConfigException("File id is not configured for sheet<$sheet> with empty row action<$emptyRowAction>")
        val sheet = this.sheet ?: ".*"
        val action = emptyRowAction ?: EmptyRowAction.STOP
        return DataSourceConfiguration(id, sheet, action)
    }
}
