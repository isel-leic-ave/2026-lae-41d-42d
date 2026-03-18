package pt.isel

// By default Retention saves in metadata and visible in Reflection
//
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
annotation class TagColor(val color: String = "Blue")
