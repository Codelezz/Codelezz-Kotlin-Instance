plugins {
    java
    idea
    maven
    kotlin("jvm") version "1.4.20"
}

group = "com.codelezz.instances"
version = "0.0.1"

java.sourceCompatibility = JavaVersion.VERSION_1_8

idea {
    module {
        isDownloadJavadoc = true
    }
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("com.google.code.gson:gson:2.8.6")


    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.4.21")
}
