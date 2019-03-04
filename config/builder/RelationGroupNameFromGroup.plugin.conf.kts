class RelationGroupNameFromGroup : AttributeBuilderPlugin<StringType>() {
    override fun build(entity: DbEntity): StringType? {
        return entity.readAttribute(relationGroupCodeAttr)?.let { code ->
            DbEntity.getByAttribute(groupEType, groupCodeAttr, code).firstOrNull()?.let { group ->
                return group.readAttribute(groupNameAttr) as? StringValue
            }
        }
    }

    companion object {
        private const val relationEntityType = "onec-group-relation"
        private const val groupEntityType = "onec-group"
        private val groupEType by lazy {
            EntityTypeManager.getEntityType(groupEntityType)
                ?: throw PluginException("Entity type<$groupEntityType> is not defined")
        }
        private val relationGroupCodeAttr by lazy {
            EntityAttributeManager.getAttributeOrNull(AttributeName(relationEntityType, "group_code"))
                ?: throw PluginException("Attribute<$relationEntityType:group_code> is not defined")
        }
        private val groupCodeAttr by lazy {
            EntityAttributeManager.getAttributeOrNull(AttributeName(groupEntityType, "group_code"))
                ?: throw PluginException("Attribute<$groupEntityType:group_code> is not defined")
        }
        private val groupNameAttr by lazy {
            EntityAttributeManager.getAttributeOrNull(AttributeName(groupEntityType, "group_name"))
                ?: throw PluginException("Attribute<$groupEntityType:group_name> is not defined")
        }
    }
}