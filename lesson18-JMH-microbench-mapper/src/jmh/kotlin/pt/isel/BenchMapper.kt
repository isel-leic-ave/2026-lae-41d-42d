package pt.isel

import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.BenchmarkMode
import org.openjdk.jmh.annotations.Mode
import org.openjdk.jmh.annotations.OutputTimeUnit
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.State
import java.util.concurrent.TimeUnit

/**
 * ./gradlew jmhJar
     * java -jar lesson18-JMH-microbench-mapper/build/libs/lesson18-JMH-microbench-mapper-jmh.jar -i 4 -wi 4 -f 1 -r 2 -w 2
 */
@BenchmarkMode(Mode.AverageTime) // Measure execution time per operation
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
open class BenchMapper {
    private val dto = PersonDto("Ze Manel", "Portugal")
    private val mapper = MapperOpt(PersonDto::class, Person::class)

    @Benchmark
    fun baselineMapperPerson(): Person = dto.toPerson()

    @Benchmark
    fun reflectMapperPerson(): Person = dto.mapTo(Person::class) as Person

    @Benchmark
    fun reflectMapperOptPerson(): Person = mapper.mapFrom(dto) as Person
}

/*
 * toPerson(dto) baseline
 */
fun PersonDto.toPerson(): Person =
    Person(
        name,
        from,
    )
