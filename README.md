# Beacon

Beacon is a simple, lightweight, and easy-to-use library for creating and calling the event in java and kotlin.

## Installation
You need to add the MavenCentral repository to your build file:

Gradle(Groovy)
```gradle
repositories {
    mavenCentral()
}

dependencies {
    implementation 'net.rk4z:beacon:1.0.3'
}
```

Gradle(Kotlin)
```kotlin
repositories {
    mavenCentral()
}

dependencies {
    implementation("net.rk4z:beacon:1.0.3")
}
```

Maven
```xml
<dependency>
    <groupId>net.rk4z</groupId>
    <artifactId>beacon</artifactId>
    <version>1.0.3</version>
</dependency>

<repositories>
    <repository>
        <id>central</id>
        <url>https://repo.maven.apache.org/maven2</url>
    </repository>
</repositories>