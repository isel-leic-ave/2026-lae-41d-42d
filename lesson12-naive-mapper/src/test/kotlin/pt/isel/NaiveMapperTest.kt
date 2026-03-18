package pt.isel

import kotlin.test.Test
import kotlin.test.assertEquals

class PersonDto(
    val name: String,
    val country: String,
    @Match(name = "bornYear") val born: Int,
)

class NaiveMapperTest {
    @Test
    fun `Test mapping PersonDto to Person`() {
        val dto = PersonDto("Maria", "Portugal", 2001)
        val actual = dto.mapTo(Person::class) as Person

        // Only copy properties with the same name and type
        assertEquals(dto.name, actual.name)
        assertEquals(dto.country, actual.country)
        assertEquals(2001, actual.bornYear)
    }
}
