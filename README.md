# ClassBridge

ClassBridge is a **dynamic proxy library** for Java and Android. It enables seamless interaction with **hidden**, **dynamically loaded**, or **unavailable-at-compile-time** classes using proxy mechanisms.

---

## Features

- **Dynamic Class Loading:** Loads classes at runtime using custom annotations.
- **Proxy-Based Invocation:** Enables method calls on classes that may not exist at compile time.
- **Constructor Matching:** Instantiates objects dynamically by resolving constructor parameters.
- **Field & Method Access:** Supports dynamic invocation of fields and methods, including `get_` and `set_` conventions.
- **Supports Hidden/Internal APIs:** Useful for interacting with restricted APIs (e.g., in Android frameworks).
- **Easy Integration:** Lightweight and dependency-free.

---

## Installation

### **Java Project**

Download the latest JAR from **[GitHub Releases](https://github.com/astola-studio/ClassBridge/releases)** and add it to your classpath.

### **Android Project**

Download the latest JAR from **[GitHub Releases](https://github.com/astola-studio/ClassBridge/releases)** and add it to `libs` folder and make sure your `build.gradle` has:

```gradle
dependencies {
    implementation fileTree(dir: 'libs', include: ['*.jar'])
}
```

---

## Usage

### **1. Initializing ClassBridge**

```java
import dd.astolastudio.ClassBridge;

ClassBridge bridge = ClassBridge.Get();
```

### **2. Creating a Proxy for a Static Class**

```java
package com.package;

public class Test {
    // Example of a static function
    private static void doSomething(){
        System.out.println();
    }
}

@ClassName("com.package.Test")
public interface MyInterface {
    @Static void doSomething();
}

MyInterface proxy = bridge.Static(MyInterface.class);
proxy.doSomething();
```

### **3. Instantiating an Object Dynamically**

```java
package com.package;

public class Test {
    // Example of a constructor
    private Test(String text){
        // ...
    }

    protected void run(){
        // ...
    }

}

@ClassName("com.package.Test")
public interface MyInterface {
    void run();
}

MyInterface instance = bridge.New(MyInterface.class, new Object[]{"param"});
instance.run();
```

### **4. Accessing Fields**

```java
package com.package;

public class Test {
    // Example of a field
    private String text;

    private Test(String text){
        this.text = text;
    }
}

@ClassName("com.package.Test")
public interface MyInterface {

    @Field String get_text();

    @Field void set_text(String value);

}

MyInterface instance = ...

String value = proxy.get_text();
proxy.set_text("New Value");
```

### **5. Working with Custom ClassLoader**

```java
ClassLoader customLoader = new CustomClassLoader();
ClassBridge bridge = ClassBridge.Get(customLoader);
```

---

## Annotations

ClassBridge relies on the following annotations:

### **@ClassName** (Required)

Specifies the fully qualified name of the real class that should be proxied.

```java
@ClassName("com.example.HiddenClass")
public interface HiddenAPI {}
```

Or If you are lazy like me, the following corresponds to `android.app.Application` :

```java
@ClassName("android.app.")
public interface Application {}
```

And so does:

```java
@ClassName("android.app.*")
public interface Application {}
```

### **@Static** (Optional)

Marks a method as a static method when calling through a proxy.

```java
@Static
void staticMethod();
```

### **@Field** (Depends)

Used to identify that a method is a field (set/get) method when calling through a proxy.

```java
@Field Object get_field();

@Field void set_field(Object value);
```

---

## Code Examples
There are some code examples you can find [Here](src/test/).

---

## Supported Platforms

| Platform | Minimum Version |
| -------- | --------------- |
| Java     | 7+              |
| Android  | API 1+         |

---

## License

ClassBridge is licensed under the [MIT License](LICENSE).

---

## Contributing

Contributions are welcome! To contribute:

1. Fork the repository.
2. Create a new branch (`feature-branch`).
3. Commit your changes.
4. Open a Pull Request.

---

## Resources

- [GitHub Repository](https://github.com/astola-studio/ClassBridge)
- [Releases](https://github.com/astola-studio/ClassBridge/releases)
- [Issue Tracker](https://github.com/astola-studio/ClassBridge/issues)

For any questions or feature requests, open an issue or contact me at `astolastudio@gmail.com`.
