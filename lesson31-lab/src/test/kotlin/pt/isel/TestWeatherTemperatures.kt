package pt.isel

import kotlin.test.Test
import kotlin.test.assertEquals

/*
 * Located on testes resources
 */
private const val weatherPath = "q-Lisbon_format-csv_date-2020-05-08_enddate-2020-06-11.csv"

class TestWeatherTemperatures {
    fun loadNaiveCsv(): List<Weather> =
        ClassLoader
            .getSystemResource(weatherPath)
            .openStream()
            .reader()
            .readLines() // List<String>
            .eagerFilter { !it.startsWith('#') } // Filter comments
            .drop(1) // Skip line: Not available
            .filterIndexed { index, _ -> index % 2 != 0 } // Filter hourly info
            .eagerMap { it.fromCsvToWeather() } // List<Weather>

    private val weatherData = loadNaiveCsv()

    @Test
    fun `check data`() {
        weatherData
            .forEach { println(it) }
    }

    @Test
    fun `count distinct descriptions in rainy days map filter Custom`() {
        val size =
            weatherData
                .eagerMap { it.weatherDesc }
                .eagerFilter { it.lowercase().contains("rain") }
                .distinct()
                .count()
        assertEquals(5, size)
    }

    @Test
    fun `count distinct descriptions in rainy days map filter Lazy`() {
        val size =
            weatherData
                .asSequence()
                .map { it.weatherDesc }
                .filter { it.lowercase().contains("rain") }
                .distinct()
                .count()
        assertEquals(5, size)
    }

    @Test
    fun `count distinct descriptions in rainy days map filter Lazy Custom`() {
        val size =
            weatherData
                .asSequence()
                .suspMap { it.weatherDesc }
                .suspFilter { it.lowercase().contains("rain") }
                .distinct()
                .count()
        assertEquals(5, size)
    }
}
