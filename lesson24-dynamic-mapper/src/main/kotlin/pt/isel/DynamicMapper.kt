package pt.isel

import java.io.File
import java.lang.classfile.ClassBuilder
import java.lang.classfile.ClassFile
import java.lang.classfile.ClassFile.ACC_PUBLIC
import java.lang.classfile.CodeBuilder
import java.lang.classfile.Interfaces
import java.lang.constant.ClassDesc
import java.lang.constant.ConstantDescs.CD_Object
import java.lang.constant.ConstantDescs.CD_boolean
import java.lang.constant.ConstantDescs.CD_char
import java.lang.constant.ConstantDescs.CD_double
import java.lang.constant.ConstantDescs.CD_float
import java.lang.constant.ConstantDescs.CD_int
import java.lang.constant.ConstantDescs.CD_long
import java.lang.constant.ConstantDescs.CD_short
import java.lang.constant.ConstantDescs.CD_void
import java.lang.constant.ConstantDescs.INIT_NAME
import java.lang.constant.ConstantDescs.MTD_void
import java.lang.constant.MethodTypeDesc
import java.net.URLClassLoader
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaGetter

private val root =
    Unit::class.java
        .getResource("/")
        ?.toURI()
        ?.path
        ?: "${System.getProperty("user.dir")}/"
private val loader = URLClassLoader(arrayOf(File(root).toURI().toURL()))

/**
 * Cache of dynamically generated mappers keyed by the domain class.
 * Prevents repeated code generation and loading.
 */
private val mappers = mutableMapOf<Pair<Class<*>, Class<*>>, Mapper<*, *>>()

/**
 * Loads a dynamic mapper instance for the given domain classes using their Java `Class` representations.
 * If not already cached, the mapper class is generated, loaded, and instantiated.
 * Keeping the mapper cache keyed by Java `Class` objects instead of Kotlin `KClass` is preferred,
 * since dynamic mappers may internally require auxiliary mappers and invoke this method with
 * the property types' corresponding `Class` references.
 * Otherwise, obtaining the Java `Class` from a `KClass` would introduce the overhead of
 * calling `kotlin/jvm/JvmClassMappingKt.getJavaClass`.
 */
fun <T : Any, R : Any> loadDynamicMapper(
    srcType: Class<T>,
    destType: Class<R>,
) = mappers.getOrPut(srcType to destType) {
    buildMapper(srcType.kotlin, destType.kotlin)
        .createInstance() as Mapper<*, *>
} as Mapper<T, R>

/**
 * Loads or creates a dynamic mapper instance for the given domain class.
 * Delegates to the Java version of `loadDynamicMapper`.
 */
fun <T : Any, R : Any> loadDynamicMapper(
    srcType: KClass<T>,
    destType: KClass<R>,
) = loadDynamicMapper(srcType.java, destType.java)

/**
 * Generates the class file for a mapper based on the structure of the given domain classes.
 * Uses code generation techniques (e.g., Class-File API) to build the repository implementation at runtime.
 *
 * @param src the Kotlin class of the source domain type.
 * @param dest the Kotlin class of the destination domain type.
 * @return the runtime-generated class implementing the repository logic.
 */
private fun <T : Any, R : Any> buildMapper(
    src: KClass<T>,
    dest: KClass<R>,
): KClass<out Any> {
    // 0. get the properties of the source type
    val srcProps = src.memberProperties

    // 1. Select a constructor with a matching parameter
    // for each property from the source type
    val ctor =
        dest
            .constructors
            .first {
                it
                    .parameters
                    .filter { param -> !param.isOptional }
                    .all { param -> srcProps.any { prop -> match(prop, param) } }
            }

    // 2. For each constructor parameter associate the corresponding property
    val params: Map<KProperty<*>, KParameter> =
        ctor
            .parameters
            .mapNotNull { destParam ->
                srcProps
                    .firstOrNull { srcProp -> match(srcProp, destParam) }
                    ?.let { it to destParam }
            }.toMap()

    /*
    public class pt.isel.PersonDto2Person implements pt.isel.Mapper<pt.isel.PersonDto, pt.isel.Person> {
     */
    val className = "pt.isel." + src.simpleName + "2" + dest.simpleName
    val mapper = ClassDesc.of(Mapper::class.qualifiedName)
    val mapFromDesc =
        MethodTypeDesc.of(
            ClassDesc.of(dest.qualifiedName),
            ClassDesc.of(src.qualifiedName),
        )
    val bytes: ByteArray =
        ClassFile.of().build(ClassDesc.of(className)) { clb ->
            clb
                .withInterfaces(Interfaces.ofSymbols(mapper).interfaces())
                //   public pt.isel.PersonDto2Person();
                //    Code:
                //         0: aload_0
                //         1: invokespecial #1    // Method java/lang/Object."<init>":()V
                //         4: return
                .withMethod(INIT_NAME, MTD_void, ACC_PUBLIC) { mb ->
                    mb.withCode { cob ->
                        cob
                            .aload(0)
                            .invokespecial(CD_Object, INIT_NAME, MTD_void)
                            .return_()
                    }
                }
                //   public pt.isel.Person mapFrom(pt.isel.PersonDto);
                //    Code:
                //         0: new           #7                  // class pt/isel/Person
                //         3: dup
                //         4: aload_1
                //         5: invokevirtual #9                  // Method pt/isel/PersonDto.getName:()Ljava/lang/String;
                //         8: aload_1
                //         9: invokevirtual #15                 // Method pt/isel/PersonDto.getCountry:()Ljava/lang/String;
                //        12: aload_1
                //        13: invokevirtual #18                 // Method pt/isel/PersonDto.getBorn:()I
                //        16: invokespecial #22                 // Method pt/isel/Person."<init>":(Ljava/lang/String;Ljava/lang/String;I)V
                //        19: areturn
                //
                .withMethod("mapFrom", mapFromDesc, ACC_PUBLIC) { mb ->
                    mb.withCode { cob ->
                        cob
                            .new_(ClassDesc.of(dest.qualifiedName))
                            .dup()
                        params.forEach { (srcProp, destParam) ->
                            if (srcProp.returnType.toKClass().isPrimitiveOrString()) {
                                cob
                                    .aload(1)
                                    .invokevirtual(
                                        ClassDesc.of(src.qualifiedName),
                                        srcProp.javaGetter?.name,
                                        MethodTypeDesc.of(srcProp.returnType.descriptor()),
                                    )
                            } else {
                                cob.mapComplexProp(clb, src, srcProp, destParam)
                            }
                        }
                        cob.invokespecial(
                            dest.descriptor(),
                            INIT_NAME,
                            MethodTypeDesc.of(CD_void, params.map { (_, destParam) -> destParam.type.descriptor() }),
                        )
                        cob.areturn()
                    }
                }
                //   public java.lang.Object mapFrom(java.lang.Object);
                //    Code:
                //         0: aload_0
                //         1: aload_1
                //         2: checkcast     #10                 // class pt/isel/PersonDto
                //         5: invokevirtual #25                 // Method mapFrom:(Lpt/isel/PersonDto;)Lpt/isel/Person;
                //         8: areturn
                .withMethod("mapFrom", MethodTypeDesc.of(CD_Object, CD_Object), ACC_PUBLIC) { mb ->
                    mb.withCode { cob ->
                        cob
                            .aload(0)
                            .aload(1)
                            .checkcast(src.descriptor())
                            .invokevirtual(ClassDesc.of(className), "mapFrom", mapFromDesc)
                            .areturn()
                    }
                }
        }

    File(root, className.replace('.', '/') + ".class")
        .also { it.parentFile.mkdirs() } // Create directories if they do not exist
        .writeBytes(bytes)
    return loader
        .loadClass(className)
        .kotlin
}

/* loadDynamicMapper(StateDto.class, Country.class).mapFrom(src.getCountry()):

 ldc           #18           // class pt/isel/StateDto
 ldc           #20           // class pt/isel/Country
 invokestatic  #22           // Method pt/isel/DynamicMapperKt.loadDynamicMapper:(Ljava/lang/Class;Ljava/lang/Class;)Lpt/isel/Mapper;
 aload_1
 invokevirtual #28           // Method pt/isel/ArtistDto.getCountry:()Lpt/isel/StateDto;
 invokeinterface #32,  2     // InterfaceMethod pt/isel/Mapper.mapFrom:(Ljava/lang/Object;)Ljava/lang/Object;
 checkcast     #20           // class pt/isel/Country
 */
private fun CodeBuilder.mapComplexProp(
    clb: ClassBuilder,
    src: KClass<*>,
    srcProp: KProperty<*>,
    destParam: KParameter,
) {
    val srcType =
        if (srcProp.returnType.classifier != List::class) {
            srcProp.returnType.descriptor()
        } else {
            srcProp.returnType.arguments[0]
                .type
                ?.descriptor()
        }
    val destType =
        if (destParam.type.classifier != List::class) {
            destParam.type.descriptor()
        } else {
            destParam.type.arguments[0]
                .type
                ?.descriptor()
        }

    ldc(clb.constantPool().classEntry(srcType))
    ldc(clb.constantPool().classEntry(destType))
    invokestatic(
        ClassDesc.of("pt.isel.DynamicMapperKt"),
        "loadDynamicMapper",
        MethodTypeDesc.of(
            Mapper::class.descriptor(),
            Class::class.descriptor(),
            Class::class.descriptor(),
        ),
    )
    aload(1)
    invokevirtual(
        src.descriptor(),
        srcProp.javaGetter?.name,
        MethodTypeDesc.of(srcProp.returnType.descriptor()),
    )
    if (srcProp.returnType.classifier != List::class) {
        invokeinterface(
            Mapper::class.descriptor(),
            "mapFrom",
            MethodTypeDesc.of(CD_Object, CD_Object),
        )
        checkcast(destParam.type.descriptor())
    } else {
        invokeinterface(
            Mapper::class.descriptor(),
            "mapFromList",
            MethodTypeDesc.of(List::class.descriptor(), List::class.descriptor()),
        )
    }
}

fun KClass<*>.isPrimitiveOrString() = this.java.isPrimitive || this == String::class

fun KType.toKClass() =
    if (classifier == List::class) {
        arguments[0]
            .type
            ?.classifier as KClass<*>
    } else {
        classifier as KClass<*>
    }

private fun match(
    srcProp: KProperty<*>,
    destParam: KParameter,
): Boolean {
    val match = srcProp.findAnnotation<Match>()
    val srcName = match?.name ?: srcProp.name
    return srcName == destParam.name
}

fun KClass<*>.descriptor(): ClassDesc =
    if (this.java.isPrimitive) {
        val desc =
            when (this) {
                Char::class -> {
                    CD_char
                }

                Short::class -> {
                    CD_short
                }

                Int::class -> {
                    CD_int
                }

                Long::class -> {
                    CD_long
                }

                Float::class -> {
                    CD_float
                }

                Double::class -> {
                    CD_double
                }

                Boolean::class -> {
                    CD_boolean
                }

                else -> {
                    throw IllegalStateException("No primitive type for ${this.qualifiedName}!")
                }
            }
        desc
    } else {
        ClassDesc.of(this.java.name)
    }

fun KType.descriptor(): ClassDesc {
    val klass = this.classifier as KClass<*>
    return klass.descriptor()
}
