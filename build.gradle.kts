import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	kotlin("jvm") version "1.7.0"
	application
}

group = "c1fr1"
version = "1.0-SNAPSHOT"

repositories {
	mavenCentral()
}

dependencies {

}

application {
	mainClass.set("MainKt")
}

tasks.withType<KotlinCompile> {
	kotlinOptions.jvmTarget = "1.8"
}