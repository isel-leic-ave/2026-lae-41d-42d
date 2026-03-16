package pt.isel

import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.declaredMemberProperties

fun Any.mapTo(dest: KClass<*>): Any {
    // 1st create instance of dest
    val target = dest.createInstance()

    // 2nd for each property with the same name and type
    // copy the value from source (this) to target
    this::class
        .declaredMemberProperties
        .forEach { srcProp ->
            // For each srcProp look for a matching prop in dest
            dest
                .declaredMemberProperties
                .filter { prop -> prop is KMutableProperty<*> }
                .map { prop -> prop as KMutableProperty<*> }
                .firstOrNull {
                    it.name == srcProp.name && it.returnType == srcProp.returnType
                }.also { destProp ->
                    val srcValue = srcProp.call(this)
                    destProp?.setter?.call(target, srcValue)
                }
        }
    // 3rd return the created instance
    return target
}
