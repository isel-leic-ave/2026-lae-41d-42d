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
    val country: StateDto,
    @Match("tracks") val songs: List<SongDto>,
)

class StateDto(
    val name: String,
    val idiom: String,
)

class SongDto(
    val title: String,
    val year: Int,
)

class MapperOptTest {
    @Test
    fun `Test mapping PersonDto to Person`() {
        val mapper = MapperOpt(PersonDto::class, Person::class)
        val dto = PersonDto("Maria", "Portugal", 2001)
        val person = mapper.mapFrom(dto) as Person
        assertEquals("Maria", person.name)
        assertEquals("Portugal", person.country)
        assertEquals(2001, person.bornYear)
    }

    @Test
    fun `Test mapping to an immutable Artist`() {
        val songs =
            listOf(
                SongDto("Starlight", 2006),
                SongDto("Uprising", 2009),
                SongDto("Madness", 2012),
            ).sortedBy { it.title }
        val dto =
            ArtistDto(
                "Muse",
                "Band",
                StateDto("UK", "en-UK"),
                songs,
            )
        val mapper = MapperOpt(ArtistDto::class, Artist::class)
        val artist = mapper.mapFrom(dto) as Artist
        assertEquals("Muse", artist.name)
        assertEquals("Band", artist.kind)
        assertEquals("UK", artist.country.name)
        assertEquals("en-UK", artist.country.idiom)
        artist
            .tracks
            .sortedBy { it.title }
            .forEachIndexed { index, track ->
                assertEquals(songs[index].title, track.title)
                assertEquals(songs[index].year, track.year)
            }
    }
}
