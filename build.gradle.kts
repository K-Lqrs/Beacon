plugins {
    kotlin("jvm") version "2.0.0"
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

version = "1.0.0"
group = "net.rk4z"


repositories {
    mavenCentral()
}

dependencies {
    implementation("org.reflections:reflections:0.10.2")
    implementation("org.jetbrains.kotlin:kotlin-reflect:2.0.0")
}

java {
    withSourcesJar()
    withJavadocJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifact(tasks["shadowJar"])


            pom {
                name.set(project.name)
                description.set("A simple event API for Java/kotlin")
                url.set("https://github.com/KT-Ruxy/Beacon")
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://github.com/KT-Ruxy/Beacon/blob/main/LICENSE")
                        distribution.set("repo")
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
                    url.set("https://github.com/KT-Ruxy/Beacon")
                }
            }
        }
    }
    repositories {
        mavenLocal()
    }
}

tasks {
    withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
        archiveClassifier.set("")
    }
}
