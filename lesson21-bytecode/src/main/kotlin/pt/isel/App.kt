package pt.isel

import java.io.File
import java.lang.classfile.ClassFile
import java.lang.classfile.ClassFile.ACC_PUBLIC
import java.lang.classfile.ClassFile.ACC_STATIC
import java.lang.constant.ClassDesc
import java.lang.constant.ConstantDescs.CD_float
import java.lang.constant.ConstantDescs.CD_int
import java.lang.constant.MethodTypeDesc
import kotlin.reflect.KFunction
import kotlin.reflect.full.functions

fun main() {
    println(calculateBalance(7, 0.03f, 3, 1.25f))
    val bar = builBar()
    println(bar.call(7, 0.03f, 3, 1.25f))
}

private val resourcePath =
    Unit::class.java
        .getResource("/")
        ?.toURI()
        ?.path

fun calculateBalance(
    balance: Int,
    interest: Float,
    income: Int,
    expense: Float,
): Float = balance - balance * interest + income - expense

fun builBar(): KFunction<*> {
    val className = "pt.isel.MyBar"
    val bytes =
        ClassFile.of().build(ClassDesc.of(className)) { clb ->
            clb
                .withFlags(ACC_PUBLIC)
                .withMethod(
                    "bar",
                    MethodTypeDesc.of(CD_float, CD_int, CD_float, CD_int, CD_float),
                    ACC_PUBLIC or ACC_STATIC,
                ) { mb ->
                    mb.withCode { cob ->
                        cob
                            .iload(0)
                            .i2f()
                            .iload(0)
                            .i2f()
                            .fload(1)
                            .fmul()
                            .fsub()
                            .iload(2)
                            .i2f()
                            .fadd()
                            .fload(3)
                            .fsub()
                            .freturn()
                    }
                }
        }
    File(resourcePath, className.replace('.', '/') + ".class")
        .also { it.parentFile.mkdirs() }
        .writeBytes(bytes)

    return Unit::class.java.classLoader
        .loadClass(className)
        .kotlin
        .functions
        .first { it.name == "bar" }
}

class RetryPolicy(
    private val maxRetries: Int,
) {
    fun canRetry(attempt: Int): Boolean = attempt < maxRetries
}
