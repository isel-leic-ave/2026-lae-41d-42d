package pt.isel

import java.io.File
import java.lang.classfile.ClassFile
import java.lang.classfile.ClassFile.ACC_FINAL
import java.lang.classfile.ClassFile.ACC_PRIVATE
import java.lang.classfile.ClassFile.ACC_PUBLIC
import java.lang.classfile.Interfaces
import java.lang.constant.ClassDesc
import java.lang.constant.ConstantDescs.CD_Object
import java.lang.constant.ConstantDescs.CD_int
import java.lang.constant.ConstantDescs.CD_void
import java.lang.constant.ConstantDescs.INIT_NAME
import java.lang.constant.ConstantDescs.MTD_void
import java.lang.constant.MethodTypeDesc
import kotlin.reflect.full.createInstance

/*
public pt.isel.Bar();
    Code:
       0: aload_0
       1: invokespecial #8                  // Method java/lang/Object."<init>":()V
       4: return

  public final int foo();
    Code:
       0: ldc           #13                 // int 67895
       2: ireturn
 */

interface IFoo {
    fun foo(): Int
}

interface Sum {
    fun add(other: Int): Int
}

class CounterBaseline(
    private val nr: Int,
) : Sum {
    override fun add(other: Int): Int = nr + other
}

fun buildBar() {
    val className = "pt.isel.Bar"
    val ifooDesc = ClassDesc.of(IFoo::class.qualifiedName)
    val bytes: ByteArray =
        ClassFile.of().build(ClassDesc.of(className)) { clb ->
            clb
                .withInterfaces(Interfaces.ofSymbols(ifooDesc).interfaces())
                .withMethod(INIT_NAME, MTD_void, ACC_PUBLIC) { mb ->
                    mb.withCode { cob ->
                        cob
                            .aload(0)
                            .invokespecial(CD_Object, INIT_NAME, MTD_void)
                            .return_()
                    }
                }.withMethod("foo", MethodTypeDesc.of(CD_int), ACC_PUBLIC or ACC_FINAL) { mb ->
                    mb.withCode { cob ->
                        cob
                            .ldc(clb.constantPool().intEntry(67895))
                            .ireturn()
                    }
                }
        }
    val resourcePath =
        Unit::class.java
            .getResource("/")
            ?.toURI()
            ?.path
    File(resourcePath, className.replace('.', '/') + ".class")
        .also { it.parentFile.mkdirs() } // Create directories if they do not exist
        .writeBytes(bytes)
}

/*
public final class pt.isel.CounterBaseline implements pt.isel.Sum {
*/
fun buildCounter() {
    val className = "pt.isel.Counter"
    val sumDesc = ClassDesc.of(Sum::class.qualifiedName)
    val bytes: ByteArray =
        ClassFile.of().build(ClassDesc.of(className)) { clb ->
            clb
                .withInterfaces(Interfaces.ofSymbols(sumDesc).interfaces())
                //  private final int nr;
                .withField("nr", CD_int, ACC_PRIVATE or ACC_FINAL)
                //   public pt.isel.CounterBaseline(int);
                //    Code:
                //       0: aload_0
                //       1: invokespecial #11                 // Method java/lang/Object."<init>":()V
                //       4: aload_0
                //       5: iload_1
                //       6: putfield      #15                 // Field nr:I
                //       9: return
                .withMethod(INIT_NAME, MethodTypeDesc.of(CD_void, CD_int), ACC_PUBLIC) { mb ->
                    mb.withCode { cob ->
                        cob
                            .aload(0)
                            .invokespecial(CD_Object, INIT_NAME, MTD_void)
                            .aload(0)
                            .iload(1)
                            .putfield(ClassDesc.of(className), "nr", CD_int)
                            .return_()
                    }
                }
                //   public int add(int);
                //    Code:
                //       0: aload_0
                //       1: getfield      #15                 // Field nr:I
                //       4: iload_1
                //       5: iadd
                //       6: ireturn
                .withMethod("add", MethodTypeDesc.of(CD_int, CD_int), ACC_PUBLIC or ACC_FINAL) { mb ->
                    mb.withCode { cob ->
                        cob
                            .aload(0)
                            .getfield(ClassDesc.of(className), "nr", CD_int)
                            .iload(1)
                            .iadd()
                            .ireturn()
                    }
                }
        }
    val resourcePath =
        Unit::class.java
            .getResource("/")
            ?.toURI()
            ?.path
    File(resourcePath, className.replace('.', '/') + ".class")
        .also { it.parentFile.mkdirs() } // Create directories if they do not exist
        .writeBytes(bytes)
}

fun main() {
    buildBar()

    val barKlass =
        Unit::class.java.classLoader
            .loadClass("pt.isel.Bar")
            .kotlin
    val bar = barKlass.createInstance() as IFoo

    // val res = barKlass.memberFunctions.first { it.name == "foo" }.call(bar)
    // println(res)

    // Alternative AVOIDING Reflect
    println(bar.foo())

    buildCounter()
    val counterKlass =
        Unit::class.java.classLoader
            .loadClass("pt.isel.Counter")
            .kotlin
    val counter = counterKlass.constructors.first().call(23) as Sum
    println(counter.add(11))

}
