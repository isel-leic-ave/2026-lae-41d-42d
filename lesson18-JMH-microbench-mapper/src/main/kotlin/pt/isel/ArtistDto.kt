package pt.isel

class ArtistDto(
    val name: String,
    val kind: String,
    @Match("country") val state: StateDto,
    @Match("tracks") val songs: List<SongDto>,
)
