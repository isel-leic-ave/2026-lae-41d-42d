package pt.isel

import kotlin.test.Test

class LoggerTest {
    @Test
    fun testLoggerRectangle() {
        val rect = Rectangle(3, 4)
        println(rect)
        System.out.log(rect)
    }

    @Test
    fun testLoggerPerson() {
        val p = Person("Maria", 2001, "Portugal")
        println(p)
        System.out.log(p)
    }
}
