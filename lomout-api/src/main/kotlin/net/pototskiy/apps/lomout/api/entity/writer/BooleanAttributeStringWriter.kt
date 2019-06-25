package net.pototskiy.apps.lomout.api.entity.writer

import net.pototskiy.apps.lomout.api.DEFAULT_LOCALE_STR
import net.pototskiy.apps.lomout.api.entity.type.BOOLEAN
import net.pototskiy.apps.lomout.api.plugable.AttributeWriterPlugin
import net.pototskiy.apps.lomout.api.source.workbook.Cell

/**
 * Default writer for [BOOLEAN] attribute
 *
 * * true → 1
 * * false → 0
 *
 * @property locale String The value local, ignored
 */
open class BooleanAttributeStringWriter : AttributeWriterPlugin<BOOLEAN>() {
    var locale: String = DEFAULT_LOCALE_STR

    override fun write(
        value: BOOLEAN?,
        cell: Cell
    ) {
        value?.let { cell.setCellValue(if (it.value) "1" else "0") }
    }
}
