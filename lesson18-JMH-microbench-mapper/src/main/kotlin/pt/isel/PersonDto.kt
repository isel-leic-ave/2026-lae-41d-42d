package pt.isel

class PersonDto(
    val name: String,
    @Match(name = "country") val from: String,
    val bornYear: Int,
)
