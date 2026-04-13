package pt.isel

import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.memberProperties
import kotlin.reflect.typeOf

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
    val params: Map<KProperty<*>, KParameter> =
        srcProps.associateWith { srcProp ->
            ctor.parameters.first { match(srcProp, it) }
        }

    fun mapFrom(from: Any): Any {
        // 3. Collect the arguments to pass to the constructor
        val args: Map<KParameter, Any?> =
            params.entries.associate { (srcProp, destParam) ->
                destParam to convert(from, srcProp, destParam)
            }

        // Call the constructor via Reflect that instantiates the object
        // and call the constructor
        return ctor.callBy(args)
    }
}

fun convert(
    src: Any,
    srcProp: KProperty<*>,
    destParam: KParameter,
): Any? {
    val srcValue = srcProp.call(src)
    return if (srcValue == null || (srcProp.returnType == destParam.type)) {
        srcValue
    } else if (srcValue is Iterable<*> && destParam.type.isSubtypeOf(typeOf<Iterable<*>>())) {
        val srcKlass =
            srcProp.returnType.arguments[0]
                .type
                ?.classifier as KClass<*>
        val destKlass =
            destParam.type.arguments[0]
                .type
                ?.classifier as KClass<*>
        val mapper = MapperOpt(srcKlass, destKlass)
        srcValue.map { item ->
            if(item == null) null else mapper.mapFrom(item)
        }
    } else {
        val destKlass = destParam.type.classifier as KClass<*>
        MapperOpt(srcValue::class, destKlass).mapFrom(srcValue)
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
