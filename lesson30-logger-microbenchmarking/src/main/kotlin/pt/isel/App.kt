package pt.isel

import kotlin.system.measureNanoTime
import kotlin.system.measureTimeMillis

fun main() {
    benchLogRectangle6(20).also { println(it) }
}

fun benchLogRectangle1() {
    measureTimeMillis {
        System.out.log(Rectangle(7,9))
    }.also { duration ->
        println("log(Rectangle) took $duration ms")
    }
}

/**
 * Remove unrelated work from the performance measure
 * ~250 ms
 */
fun benchLogRectangle2() {
    val rect = Rectangle(7,9)
    measureTimeMillis {
        System.out.log(rect)
    }.also { duration ->
        println("log(Rectangle) took $duration ms")
    }
}

/**
 * Ignore first executions die to overhead of JitCompiler and
 * let JVM warm-up to run optimizations.
 * ~ 0 millis
 */
fun benchLogRectangle3(iters: Int = 10) {
    val rect = Rectangle(7,9)
    repeat(iters) {
        measureTimeMillis {
            System.out.log(rect)
        }.also { duration ->
            println("log(Rectangle) took $duration ms")
        }
    }
}

/**
 * Increase precision with nano time
 * ~ 60 micros
 */
fun benchLogRectangle4(iters: Int = 10) {
    val rect = Rectangle(7,9)
    repeat(iters) {
        measureNanoTime {
            System.out.log(rect)
        }.also { duration ->
            println("log(Rectangle) took ${duration/1000} micro seconds")
        }
    }
}

/**
 * Avoid IO in performance tests.
 * ~20 micros
 */
fun benchLogRectangle5(iters: Int = 10): String {
    val rect = Rectangle(7,9)
    val out = StringBuilder()
    repeat(iters) {
        measureNanoTime {
            out.clear()
            out.log(rect)
        }.also { duration ->
            println("log(Rectangle) took ${duration/1000} micro seconds")
        }
    }
    return out.toString()
}
/**
 * Eliminate the overhead of the nanoTime system call.
 * ~20 micros
 */
fun benchLogRectangle6(iters: Int = 10): String {
    val rect = Rectangle(7,9)
    val out = StringBuilder()
    val totalOps = 1_000_000
    repeat(iters) {
        measureNanoTime {
            repeat(totalOps) {
                out.clear()
                out.log(rect)
            }
        }.also { duration ->
            println("log(Rectangle) took ${duration/totalOps} nanos")
        }
    }
    return out.toString()
}
