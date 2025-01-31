import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.FileInputStream
import java.util.Properties
import kotlin.apply

plugins {
	// Kotlin
	kotlin("jvm") version "2.1.0"
	kotlin("plugin.serialization") version "2.1.0"
	id("org.jetbrains.dokka") version "2.0.0"

	// Publishing
	`maven-publish`
}

val nxProp = Properties().apply {
	load(FileInputStream(rootProject.file("local/nx.properties")))
}

group = "net.ririfa"
version = "1.5.0"

repositories {
	mavenCentral()
}

dependencies {
	dokkaHtmlPlugin("org.jetbrains.dokka:kotlin-as-java-plugin:1.9.20")

	implementation("org.reflections:reflections:0.10.2")
	implementation("org.jetbrains.kotlin:kotlin-reflect:2.1.0")
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

tasks.dokkaHtml.configure {
	outputDirectory.set(layout.buildDirectory.dir("dokka"))
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
				url.set("https://github.com/ririf4/Beacon")
				licenses {
					license {
						name.set("MIT")
						url.set("https://opensource.org/license/mit")
					}
				}
				developers {
					developer {
						id.set("ririf4")
						name.set("RiriFa")
						email.set("main@ririfa.net")
					}
				}
				scm {
					connection.set("scm:git:git://github.com/ririf4/Beacon.git")
					developerConnection.set("scm:git:ssh://github.com/ririf4/Beacon.git")
					url.set("https://github.com/ririf4/Beacon")
				}
				dependencies
			}
		}
	}
	repositories {
		maven {
			val releasesRepoUrl = uri("https://repo.ririfa.net/maven2-rel/")
			val snapshotsRepoUrl = uri("https://repo.ririfa.net/maven2-snap/")
			url = if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl

			credentials {
				username = nxProp.getProperty("nxUN")
				password = nxProp.getProperty("nxPW")
			}
		}
	}
}