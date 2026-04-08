package pt.isel

/**
 * Mapper from PersonDto to Person
 */
fun PersonDto.toPerson(): Person =
    Person(
        name = name,
        country = from,
    )
