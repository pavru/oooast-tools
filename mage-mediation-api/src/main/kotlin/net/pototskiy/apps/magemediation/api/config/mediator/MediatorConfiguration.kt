package net.pototskiy.apps.magemediation.api.config.mediator

import net.pototskiy.apps.magemediation.api.config.ConfigBuildHelper
import net.pototskiy.apps.magemediation.api.config.ConfigDsl

data class MediatorConfiguration(
    val lines: ProductionLineCollection
) {
    @ConfigDsl
    class Builder(private val helper: ConfigBuildHelper) {
        private var lines = mutableListOf<ProductionLine>()

        @Suppress("unused")
        fun Builder.unionProductionLine(block: ProductionLine.Builder.() -> Unit) {
            lines.add(ProductionLine.Builder(helper, ProductionLine.LineType.UNION).apply(block).build())
        }

        @Suppress("unused")
        fun Builder.crossProductionLine(block: ProductionLine.Builder.() -> Unit) {
            lines.add(ProductionLine.Builder(helper, ProductionLine.LineType.CROSS).apply(block).build())
        }

        fun build(): MediatorConfiguration {
            return MediatorConfiguration(ProductionLineCollection(lines))
        }
    }
}
