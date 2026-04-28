package pt.isel

interface Mapper<T, R> {
    fun mapFrom(src: T): R

    fun mapFromList(src: List<T>): List<R> = src.map { this.mapFrom(it) }
}
