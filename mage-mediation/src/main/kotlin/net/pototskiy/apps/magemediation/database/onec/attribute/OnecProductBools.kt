package net.pototskiy.apps.magemediation.database.onec.attribute

import net.pototskiy.apps.magemediation.database.TypedAttributeEntity
import net.pototskiy.apps.magemediation.database.TypedAttributeEntityClass
import net.pototskiy.apps.magemediation.database.TypedAttributeTable
import net.pototskiy.apps.magemediation.database.onec.OnecProduct
import net.pototskiy.apps.magemediation.database.onec.OnecProducts
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.sql.ReferenceOption

object OnecProductBools : TypedAttributeTable<Boolean>("onec_product_bool") {
    override val owner = reference("product", OnecProducts, onDelete = ReferenceOption.CASCADE)
    override val value = bool("value")
}

class OnecProductBool(id: EntityID<Int>) : TypedAttributeEntity<Boolean>(id) {
    companion object : TypedAttributeEntityClass<Boolean, OnecProductBool>(OnecProductBools)

    override var owner: IntEntity by OnecProduct referencedOn OnecProductBools.owner
    override var index by OnecProductBools.index
    override var code by OnecProductBools.code
    override var value by OnecProductBools.value
}
