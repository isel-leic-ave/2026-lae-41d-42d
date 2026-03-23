package pt.isel

import kotlin.test.Test
import kotlin.test.assertEquals

class PersonDto(
    val name: String,
    val country: String,
    @Match("bornYear") val born: Int,
)

class ArtistDto(
    val name: String,
    val kind: String,
)

class NaiveMapperTest {
    @Test
    fun `Test mapping PersonDto to Person`() {
        val dto = PersonDto("Maria", "Portugal", 2001)
        val person = dto.mapTo(Person::class) as Person
        assertEquals("Maria", person.name)
        assertEquals("Portugal", person.country)
        assertEquals(2001, person.bornYear)
    }

    @Test
    fun `Test mapping to an immutable Artist`() {
        val dto = ArtistDto("Muse", "Band")
        val artist = dto.mapTo(Artist::class) as Artist
        assertEquals("Muse", artist.name)
        assertEquals("Band", artist.kind)
    }
}
