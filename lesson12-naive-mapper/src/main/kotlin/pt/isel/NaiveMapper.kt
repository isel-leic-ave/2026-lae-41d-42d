package pt.isel

import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.memberProperties

fun Any.mapTo(dest: KClass<*>): Any {
    // 1. Create an instance of dest
    val target = dest.createInstance()

    // For each property in source (i.e. this)
    // look for a matching property in dest,
    // i.e. same NAME and same TYPE
    this::class
        .memberProperties
        .forEach { srcProp ->
            dest
                .memberProperties
                .filter { it is KMutableProperty<*> }
                .map { it as KMutableProperty<*> }
                .firstOrNull { it.name == srcProp.name && it.returnType == srcProp.returnType }
                ?.also { destProp ->
                    val srcValue = srcProp.call(this)
                    destProp.setter.call(target, srcValue)
                }
        }
    // 3. return dest instance
    return target
}
