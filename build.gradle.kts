import cl.franciscosolis.sonatypecentralupload.SonatypeCentralUploadTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.FileInputStream
import java.util.*

plugins {
    kotlin("jvm") version "2.0.20"
    kotlin("plugin.serialization") version "2.0.20"
    `maven-publish`
    id("cl.franciscosolis.sonatype-central-upload") version "1.0.3"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

version = "1.4.7"
group = "net.rk4z"

val localProperties = Properties().apply {
    load(FileInputStream(rootProject.file("local.properties")))
}

repositories {
    mavenCentral()
}

dependencies {

    implementation("org.reflections:reflections:0.10.2")
    implementation("org.jetbrains.kotlin:kotlin-reflect:2.0.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.1")
}

java {
    withSourcesJar()
    withJavadocJar()

    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

tasks.withType<JavaCompile> {
    options.release.set(17)
}

tasks.named<Jar>("jar") {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    archiveClassifier.set("")
}

publishing {
    publications {
        //maven
        create<MavenPublication>("maven") {

            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()

            from(components["java"])

            pom {
                name.set("Beacon")
                description.set("A simple event API for Kotlin/Java")
                url.set("https://github.com/KT-Ruxy/Beacon")
                licenses {
                    license {
                        name.set("MIT")
                        url.set("https://opensource.org/license/mit")
                    }
                }
                developers {
                    developer {
                        id.set("ruxy")
                        name.set("Ruxy")
                        email.set("main@rk4z.net")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/KT-Ruxy/Beacon.git")
                    developerConnection.set("scm:git:ssh://github.com/KT-Ruxy/Beacon.git")
                    url.set("https://github.com/KT-Ruxy/Beacon")
                }
                dependencies
            }
        }
    }
    repositories {
        mavenLocal()
    }
}

tasks.named<SonatypeCentralUploadTask>("sonatypeCentralUpload") {
    dependsOn("clean", "jar", "sourcesJar", "javadocJar", "generatePomFileForMavenPublication")

    username = localProperties.getProperty("cu")
    password = localProperties.getProperty("cp")

    archives = files(
        tasks.named("jar"),
        tasks.named("sourcesJar"),
        tasks.named("javadocJar"),
    )

    pom = file(
        tasks.named("generatePomFileForMavenPublication").get().outputs.files.single()
    )

    signingKey = localProperties.getProperty("signing.key")
    signingKeyPassphrase = localProperties.getProperty("signing.passphrase")
}
