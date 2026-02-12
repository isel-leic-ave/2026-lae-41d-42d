# **Lab Guide: JVM Ecosystem and Interoperability**

## **Objective**

* Experiment with interoperability between Kotlin and Java on the JVM.
* Understand `.class` file generation, dependency resolution, `CLASSPATH`, and lazy class loading.
* Explore how Gradle manages dependencies and builds a runnable JVM application.

---

## **Part 1: Setup**

1. Ensure you have the following installed:

   * Java JDK (`javac`, `java`)
   * Kotlin compiler (`kotlinc`, `kotlin`)
   * Gradle (optional, for the Gradle section)

2. Create a working directory:

   ```bash
   mkdir lesson02-lab
   cd lesson02-lab
   ```

---

## **Part 2: Simple Java & Kotlin Interoperability**

### **Step 1: Create Java classes**

Create a file `Foo.java`:

```java
// Foo.java
class X { 
    public void print() { System.out.println("I am X"); }
}

class Y { 
    public void print() { System.out.println("I am Y"); }
}

interface Z { 
    void print();
}
```

Compile Java:

```bash
javac Foo.java
```

**Questions:**

1. How many `.class` files are generated?

---

### **Step 2: Create a Kotlin main file**

Create `App.kt`:

```kotlin
fun main() {
    println("Press ENTER to proceed.")
    readLine()
    X().print()    // Using Java class
}
```

Compile Kotlin, specifying classpath to include Java classes:

```bash
kotlinc -cp . App.kt
```

**Questions:**

1. What `.class` file does Kotlin generate for the top-level function?
2. Does Kotlin automatically see `X.class` in the current folder?
3. Compile again without `-cp .`. Justify the observed behavior.

---

### **Step 3: Run the Kotlin program**

```bash
kotlin -cp . AppKt
```

Expected output:

```
Press ENTER to proceed.
I am X
```

---

## **Part 3: Class files required for compile vs runtime**

### **Step 1: Test removing files**

1. Remove `Y.class`:

```bash
rm Y.class
```

2. Compile `App.kt` again:

```bash
kotlinc App.kt -cp . -d .
```

3. Run program:

```bash
kotlin -cp . AppKt
```

* **Observation:** Runs successfully.

**Question:** Why does removing `Y.class` not affect compilation or runtime?

---

### **Step 2: Trigger NoClassDefFoundError**

Create a new file `App.kt`:

```kotlin
fun main() {
    println("Press ENTER to proceed.")
    readLine()
    X().print()     // Java class
}

fun bar() {
    Y().print()     // Java class
}
```

Compile Kotlin:

```bash
kotlinc -cp . App.kt 
```

Inspect Generated Files:

```bash
ls *.class
```

You should now see:

```
AppKt.class
X.class
Y.class
Z.class
```

Run using Kotlin launcher:

```bash
kotlin -cp . AppKt
```

Press ENTER.

Expected:

```
I am X
```

## Step 3 — Delete Y

```bash
rm Y.class
```

Run Again:

```bash
kotlin -cp . AppKt
```
Questions:

1. Does the program run?
2. Why does it run even though `bar()` references `Y`?
3. Did the JVM load class `Y`?
4. When does class loading occur?

