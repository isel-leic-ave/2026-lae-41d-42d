package pt.isel

import kotlin.test.Test

class TestBenchMapper {
    private val dto = PersonDto("Ze Manel", "Portugal")

    @Test
    fun testReflectMapperPerson() {
        dto.mapTo(Person::class) as Person
    }

//    @Test
//    fun testReflectMapperOptPerson() {
//        val mapper = MapperOpt(PersonDto::class, Person::class)
//        mapper.mapFrom(dto)
//    }
}
