class OnecGroupToLong : LongAttributeReader() {
    override fun read(attribute: Attribute<out LongType>, input: Cell): LongType? {
        return LongValue(input.asString().drop(1).stringToLong(locale.createLocale()))
    }
}
