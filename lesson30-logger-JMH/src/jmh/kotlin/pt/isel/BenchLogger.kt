package pt.isel

import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.BenchmarkMode
import org.openjdk.jmh.annotations.Mode
import org.openjdk.jmh.annotations.OutputTimeUnit
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.infra.Blackhole
import java.util.concurrent.TimeUnit

/**
 * ./gradlew jmhJar
 *  java -jar lesson30-logger-JMH/build/libs/lesson30-logger-JMH-jmh.jar -i 4 -wi 4 -f 1 -r 2 -w 2
 */
@BenchmarkMode(Mode.AverageTime) // Measure execution time per operation
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
open class BenchLogger {
    private val rect = Rectangle(7, 9)
    private val out = StringBuilder()

    @Benchmark
    fun benchRectangleLogBaseline(bh: Blackhole) {
        out.clear()
        rect.logTo(out)
        bh.consume(out)
    }

    @Benchmark
    fun benchRectangleLogReflect(bh: Blackhole) {
        out.clear()
        out.log(rect)
        bh.consume(out)
    }
}

fun Rectangle.logTo(out: Appendable) {
    out.appendLine("Type: Rectangle")
    out.appendLine("- area: $area")
    out.appendLine("- height: $height")
    out.appendLine("- width: $width")
}
