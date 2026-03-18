# **Lab Guide: Reflection and Annotations**

**Objectives:**

* Understand how to use annotations to customize the behavior of a JSON serializer.
* Implement custom formatters for specific data types using annotations.

---

## Intro

This assignment continues the work you started in the previous lab, where you
implemented a simple JSON serializer using the **Kotlin Reflection API**. 

This lab is built on top of the `memberToJson()` function. 

## Part 1: Annotations

Members can be annotated with `ToJsonPropName` to replace the member's name with
the specified JSON property name. For example, if a property is annotated with
`@ToJsonPropName("first_name")`, the JSON output will use "first_name" as the
key instead of the property's actual name, that could be "firstName".

## Part 2: Custom Formatters

Members can also be annotated with `ToJsonFormatter`, which specifies a class
implementing a function `(Any) -> String`. This function provides an alternative
JSON representation of the member's value, processing it. For example, if a
property is annotated with `@ToJsonFormatter(DateFormatter::class)`, the
`DateFormatter` class will be used to format the property's value into a JSON
string. Consider for example the next class `Student` with the following
properties:

```kotlin
data class Student(
   val name: String,
   val age: Int,
   @ToJsonFormatter(DateFormatter::class)
   val enrollmentDate: LocalDate
)
```
In this case, the `DateFormatter` class would need to implement a
function that takes an `Any` type (the value of the `enrollmentDate` property)
and returns a `String` representation of that date in the desired format,
according for example:

```kotlin
class DateFormatter : (Any) -> String {
    override fun invoke(value: Any): String {
        val date = value as LocalDate
        return date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    }
}
```
 