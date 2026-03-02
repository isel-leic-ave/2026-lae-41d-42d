fun main() {
    println("Press a or b to instantiate A or B class:")
    var c = readLine()?.lowercase()[0]
    if(c == 'a') {
        A()
    } else {
        val b = B()
        println("Press ENTER to proceed to bar!")
        readLine()
        b.bar()
    }
}
