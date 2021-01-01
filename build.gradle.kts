plugins {
    java
    idea
    maven
    `maven-publish`
    kotlin("jvm") version "1.4.21"
}

group = "com.codelezz.instances"
version = "0.0.1"

java.sourceCompatibility = JavaVersion.VERSION_1_8

idea {
    module {
        isDownloadJavadoc = true
    }
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/codelezz/codelezz-kotlin-instance")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
    publications {
        create<MavenPublication>("gpr") {
            artifactId = "codelezz-kotlin-instance"
            from(components["java"])
        }
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
