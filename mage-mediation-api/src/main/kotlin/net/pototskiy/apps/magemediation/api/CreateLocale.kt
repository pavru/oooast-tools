package net.pototskiy.apps.magemediation.api

import java.util.*

@Suppress("TooGenericExceptionCaught")
fun String.createLocale(): Locale {
    val (l, c) = this.split("_")
    return try {
        Locale(l, c)
    } catch (e: Exception) {
        Locale.getDefault()
    }
}
