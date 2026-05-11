package pt.isel

import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

fun Appendable.log(obj: Any) {
    val objClass = obj::class
    appendLine("Type: ${objClass.simpleName}")
    val properties = objClass.memberProperties
    for (property in properties) {
        property.isAccessible = true
        val propertyName = property.name
        val propertyValue = property.call(obj)
        appendLine("- $propertyName: $propertyValue")
    }
}