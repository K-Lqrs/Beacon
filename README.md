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
    implementation 'net.rk4z:beacon:[Version]'
}
```

Gradle(Kotlin)
```kotlin
repositories {
    mavenCentral()
}

dependencies {
    implementation("net.rk4z:beacon:[Version]")
}
```

Maven
```xml
<repositories>
    <repository>
        <id>central</id>
        <url>https://repo.maven.apache.org/maven2</url>
    </repository>
</repositories>

<dependency>
    <groupId>net.rk4z</groupId>
    <artifactId>beacon</artifactId>
    <version>[Version]</version>
</dependency>
```

Hint: You can find the latest version of the library on the [MavenCentral](https://central.sonatype.com/artifact/net.rk4z/beacon/versions).