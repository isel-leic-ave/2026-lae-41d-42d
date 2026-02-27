package pt.isel

class Rectangle(private val height: Int, var width: Int) {
    // val area
    //     get() = height * width
    val area = height * width

    override fun toString() : String{
        return "$width, $height"
    }
}

fun printWidth(r: Rectangle) {
    println(r.width)
}