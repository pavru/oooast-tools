package net.pototskiy.apps.magemediation.api.entity.writer

import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream
import net.pototskiy.apps.magemediation.api.entity.StringListType
import net.pototskiy.apps.magemediation.api.entity.StringListValue
import net.pototskiy.apps.magemediation.api.plugable.AttributeWriterPlugin
import net.pototskiy.apps.magemediation.api.source.workbook.Cell
import org.apache.commons.csv.CSVFormat

open class StringListAttributeStringWriter : AttributeWriterPlugin<StringListType>() {
    var quote: Char? = null
    var delimiter: Char = ','

    override fun write(
        value: StringListType?,
        cell: Cell
    ) {
        (value as? StringListValue)?.let { list ->
            val listValue = ByteOutputStream().use { stream ->
                stream.writer().use { writer ->
                    CSVFormat.RFC4180
                        .withQuote(quote)
                        .withDelimiter(delimiter)
                        .withRecordSeparator("")
                        .print(writer)
                        .printRecord(list.map { it.value })
                }
                stream.toString()
            }
            cell.setCellValue(listValue)
        }
    }
}
