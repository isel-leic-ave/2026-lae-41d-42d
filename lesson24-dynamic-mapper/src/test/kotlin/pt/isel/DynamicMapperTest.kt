package pt.isel

import kotlin.test.Test
import kotlin.test.assertEquals

class PersonDto(
    val name: String,
    val country: String,
    @Match("bornYear") val born: Int,
)


class DynamicMapperTest {
    @Test
    fun `Test mapping PersonDto to Person`() {
        val mapper: Mapper<PersonDto, Person> = loadDynamicMapper(PersonDto::class, Person::class)
        val dto = PersonDto("Maria", "Portugal", 2001)
        val person = mapper.mapFrom(dto)
        assertEquals("Maria", person.name)
        assertEquals("Portugal", person.country)
        assertEquals(2001, person.bornYear)
    }
}
