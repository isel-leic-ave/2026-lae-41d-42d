package pt.isel

import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.memberProperties
import kotlin.reflect.typeOf

data class PropInfo(
    val srcProp: KProperty<*>,
    val destParam: KParameter,
    val mapPropValue: (Any?) -> Any?,
)

class MapperOpt(
    srcKlass: KClass<*>,
    destKlass: KClass<*>,
) {
    // 0. get the properties of the source type
    val srcProps = srcKlass.memberProperties

    // 1. Select a constructor with a matching parameter
    // for each property from the source type
    val ctor =
        destKlass
            .constructors
            .first { ctor ->
                srcProps
                    .all { srcProp ->
                        ctor.parameters.any { destParam ->
                            match(srcProp, destParam)
                        }
                    }
            }

    // 2. For each property associate the corresponding Parameter
    val params: List<PropInfo> =
        srcProps.map { srcProp ->
            val destParam = ctor.parameters.first { match(srcProp, it) }
            PropInfo(srcProp, destParam, buildConverter(srcProp, destParam))
        }

    fun mapFrom(from: Any): Any {
        // 3. Collect the arguments to pass to the constructor
        val args: Map<KParameter, Any?> =
            params.associate { (srcProp, destParam, converter) ->
                val srcValue = srcProp.call(from)
                destParam to converter(srcValue)
            }

        // Call the constructor via Reflect that instantiates the object
        // and call the constructor
        return ctor.callBy(args)
    }
}

fun buildConverter(
    srcProp: KProperty<*>,
    destParam: KParameter,
): (Any?) -> Any? {
    if (srcProp.returnType == destParam.type) {
        return { srcValue -> srcValue }
    } else if (destParam.type.isSubtypeOf(typeOf<Iterable<*>>())) {
        val srcKlass =
            srcProp.returnType.arguments[0]
                .type
                ?.classifier as KClass<*>
        val destKlass =
            destParam.type.arguments[0]
                .type
                ?.classifier as KClass<*>
        val mapper = MapperOpt(srcKlass, destKlass)
        return { srcValue ->
            (srcValue as Iterable<Any>).map { item ->
                if (item == null) null else mapper.mapFrom(item)
            }
        }
    } else {
        val propKlass = srcProp.returnType.classifier as KClass<*>
        val destKlass = destParam.type.classifier as KClass<*>
        val mapper = MapperOpt(propKlass, destKlass)
        return { srcValue ->
            if (srcValue == null) null else mapper.mapFrom(srcValue)
        }
    }
}

fun match(
    srcProp: KProperty<*>,
    destParam: KParameter,
): Boolean {
    val match = srcProp.findAnnotation<Match>()
    val srcName = match?.name ?: srcProp.name
    return srcName == destParam.name
}
