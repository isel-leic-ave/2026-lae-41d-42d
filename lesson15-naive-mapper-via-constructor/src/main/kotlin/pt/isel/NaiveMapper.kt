package pt.isel

import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.memberProperties
import kotlin.reflect.typeOf

fun Any.mapTo(dest: KClass<*>): Any {
    // 1st get properties of source
    val srcProps = this::class.memberProperties

    // 2nd Look for a constructor in dest
    // with a parameter for each property in source (this)
    // Assume the remaining parameters are optional.
    val destCtor: KFunction<Any> =
        dest
            .constructors
            .first { destCtor ->
                srcProps.all { srcProp ->
                    destCtor.parameters.any { destParam ->
                        matchProp(srcProp, destParam)
                    }
                }
            }
    // 3rd Select dest constructor parameters
    val destCtorParams: Map<KParameter, KProperty<*>> =
        srcProps.associateBy { srcProp ->
            destCtor.parameters.first { matchProp(srcProp, it) }
        }

    // 4th instantiate test via destCtor call
    return destCtor.callBy(
        destCtorParams.entries.associate { (destParam, srcProp) ->
            destParam to convert(this, srcProp, destParam)
        },
    )
}

private fun convert(
    source: Any,
    srcProp: KProperty<*>,
    destParam: KParameter,
): Any? {
    val propValue = srcProp.call(source)
    return if (srcProp.returnType == destParam.type) {
        propValue
    } else if (srcProp.returnType.isSubtypeOf(typeOf<Number>()) ||
        destParam.type.isSubtypeOf(typeOf<Number>())
    ) {
        throw IllegalStateException("Not allowed mapping between primitive types")
    } else if (srcProp.returnType.isSubtypeOf(typeOf<Iterable<*>>()) &&
        destParam.type.isSubtypeOf(typeOf<Iterable<*>>())
    ) {
        val destType =
            destParam.type.arguments[0]
                .type
                ?.classifier as KClass<*>
        (propValue as Iterable<*>)
            .map { it?.mapTo(destType) }
    } else {
        propValue?.mapTo(destParam.type.classifier as KClass<*>)
    }
}

private fun matchProp(
    srcProp: KProperty<*>,
    destParam: KParameter,
): Boolean {
    val match = srcProp.findAnnotation<Match>()
    val srcName = match?.name ?: srcProp.name
    return srcName == destParam.name
}
