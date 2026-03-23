package pt.isel

class Artist(
    val kind: String,
    val name: String,
    val country: Country = Country("PT", "Portuguese"),
    val tracks: List<Track> = listOf(),
)
