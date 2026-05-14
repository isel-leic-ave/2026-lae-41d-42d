package pt.isel

import kotlin.system.measureNanoTime
import kotlin.system.measureTimeMillis

fun main() {
    println(benchLog6())
}

/**
 * e.g. log(Rectangle) took 221 ms
 */
fun benchLog1() {
    measureTimeMillis {
        System.out.log(Rectangle(7, 9))
    }.also {
        println("log(Rectangle) took $it ms")
    }
}

/**
 * Avoid unrelated operations in the measurement.
 * Take the Rectangle instantiation out of the measure.
 * e.g. log(Rectangle) took 221 ms
 */
fun benchLog2() {
    val rect = Rectangle(7, 9)
    measureTimeMillis {
        System.out.log(rect)
    }.also {
        println("log(Rectangle) took $it ms")
    }
}

/**
 * Discard first execution that includes JIT compiler overhead and
 * misses optimizations.
 * e.g. log(Rectangle) took 0 ms
 */
fun benchLog3(iters: Int = 10) {
    val rect = Rectangle(7, 9)
    repeat(iters) {
        measureTimeMillis {
            System.out.log(rect)
        }.also {
            println("log(Rectangle) took $it ms")
        }
    }
}

/**
 * Increase precision using nanoTime instead of timeInMillis.
 * e.g. log(Rectangle) took 87 us
 */
fun benchLog4(iters: Int = 10) {
    val rect = Rectangle(7, 9)
    repeat(iters) {
        measureNanoTime {
            System.out.log(rect)
        }.also {
            println("log(Rectangle) took ${it / 1000} us")
        }
    }
}

/**
 * Avoid IO. Used an auxiliary buffer in memory instead.
 * e.g. log(Rectangle) took 20 us
 */
fun benchLog5(iters: Int = 10): String {
    val rect = Rectangle(7, 9)
    val out = StringBuilder()
    repeat(iters) {
        out.clear()
        measureNanoTime {
            out.log(rect)
        }.also {
            println("log(Rectangle) took ${it / 1000} us")
        }
    }
    return out.toString()
}

/**
 * Avoid the overhead of the System call to nanoTime.
 * e.g. log(Rectangle) took 266 ns
 */
fun benchLog6(iters: Int = 10): String {
    val rect = Rectangle(7, 9)
    val out = StringBuilder()
    val opCount = 1_000_000
    repeat(iters) {
        measureNanoTime {
            repeat(opCount) {
                out.clear()
                out.log(rect)
            }
        }.also {
            println("log(Rectangle) took ${it / opCount} ns")
        }
    }
    return out.toString()
}
